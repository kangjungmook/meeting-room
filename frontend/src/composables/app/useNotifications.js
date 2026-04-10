import { ref, reactive } from 'vue';
import api from '../../api';
import { fetchBookings, fetchMyBookings } from './useBookingData';
import { createSseManager } from '../../utils/sse';
import { addToast, toastDuration, queueCreatedToast } from './useToast';

// ── 다크 모드 (localStorage = 즉시 로드, DB = 최종 소스) ──────────────
export const darkMode = ref(
  typeof window !== 'undefined' ? localStorage.getItem('darkMode') === 'true' : false
);

// 모듈 로드 즉시 적용 (깜빡임 없음)
if (typeof window !== 'undefined') {
  document.documentElement.classList.toggle('dark', darkMode.value);
}

// ── 사용자 알림 설정 상태 ──────────────────────────────────────────
export const userNotifPrefs = reactive({
  receiveInvitation: true,
  receiveUpdates:    true,
  receiveReminder:   true,
  toastCreated:      true,
  toastUpdated:      true,
  toastCancelled:    true,
  toastDuration:     5,
  darkMode:          darkMode.value,
  reminderMinutes:   '10',
});

export const showUserSettings = ref(false);

export const toggleDarkMode = async () => {
  userNotifPrefs.darkMode = !userNotifPrefs.darkMode;
  darkMode.value = userNotifPrefs.darkMode;
  document.documentElement.classList.toggle('dark', darkMode.value);
  localStorage.setItem('darkMode', String(darkMode.value));
  // DB에 즉시 저장
  try {
    await api.put('/users/notification-preference', { ...userNotifPrefs });
  } catch (e) {
    console.error('다크 모드 저장 실패', e);
  }
};

export const saveNotifPrefs = async () => {
  userNotifPrefs.darkMode = darkMode.value;
  await api.put('/users/notification-preference', { ...userNotifPrefs });
  toastDuration.value = userNotifPrefs.toastDuration;
  addToast('설정이 저장되었습니다.', 'success');
};

// DB에서 설정 로드 후 darkMode 적용
export const applyNotifPrefs = (prefs) => {
  Object.assign(userNotifPrefs, prefs);
  toastDuration.value = prefs.toastDuration ?? 5;
  // DB가 최종 소스 — localStorage 덮어쓰기
  darkMode.value = !!prefs.darkMode;
  document.documentElement.classList.toggle('dark', darkMode.value);
  localStorage.setItem('darkMode', String(darkMode.value));
};

// ── FCM ──────────────────────────────────────────
export const notifPermission = ref(typeof Notification !== 'undefined' ? Notification.permission : 'denied');

const setupFcmMessaging = async () => {
  try {
    const { getMessagingInstance, getToken, onMessage, VAPID_KEY } = await import('../../firebase');
    const messaging = await getMessagingInstance();
    if (!messaging) return;
    const token = await getToken(messaging, { vapidKey: VAPID_KEY });
    if (token) await api.post('/users/fcm-token', { token });

    // 포그라운드(앱 열려있을 때)에도 시스템 알림 표시
    onMessage(messaging, (payload) => {
      const title = payload.notification?.title ?? payload.data?.title ?? '알림';
      const body  = payload.notification?.body  ?? payload.data?.body  ?? '';
      if (Notification.permission === 'granted') {
        new Notification(title, {
          body,
          icon: '/favicon.ico',
          badge: '/favicon.ico',
        });
      }
    });
  } catch (e) {
    console.warn('FCM 토큰 발급 실패:', e.message);
  }
};

export const initFcm = async () => {
  if (typeof Notification === 'undefined') return;
  if (Notification.permission === 'default') {
    const p = await Notification.requestPermission();
    notifPermission.value = p;
    if (p === 'granted') await setupFcmMessaging();
  } else if (Notification.permission === 'granted') {
    await setupFcmMessaging();
  }
};

export const requestNotifPermission = async () => {
  if (typeof Notification === 'undefined') return;
  if (Notification.permission === 'denied')  {
    addToast('브라우저 설정에서 알림 차단을 해제해 주세요.', 'info');
    return;
  }
  if (Notification.permission === 'granted') {
    addToast('이미 알림이 허용되어 있습니다.', 'info');
    return;
  }
  const p = await Notification.requestPermission();
  notifPermission.value = p;
  if (p === 'granted') {
    await setupFcmMessaging();
    addToast('알림이 허용되었습니다.', 'info');
  }
};

// ── SSE  구독 및 핸들링 ──────────────────────────────────────────
let _firstOpen = true;
let _lastMyBookingsFetchAt = 0;
const _sse = createSseManager({
  getUrl: () => {
    const token = localStorage.getItem('token') || '';
    return `/api/sse/subscribe?token=${token}`;
  },
  listeners: {
    booking: (e) => {
      try {
        const { type, data } = JSON.parse(e.data);
        fetchBookings();
        // "내 예약" 패널도 실시간 반영(과도한 연속 호출은 약하게 제한)
        const now = Date.now();
        if (now - _lastMyBookingsFetchAt > 1200) {
          _lastMyBookingsFetchAt = now;
          fetchMyBookings();
        }
        if (type === 'CREATED' && userNotifPrefs.toastCreated) {
          queueCreatedToast(data.organizer);
        } else if (type === 'CANCELLED' && userNotifPrefs.toastCancelled) {
          addToast('❌ 예약이 취소되었습니다.', 'info');
        } else if (type === 'UPDATED' && userNotifPrefs.toastUpdated) {
          addToast(`📝 ${data.organizer}님이 예약을 수정했습니다.`, 'info');
        }
      } catch (err) {
        console.error('SSE Parsing Error:', err);
      }
    },
  },
  onOpen: () => {
    // 재연결 시 누락된 예약 이벤트 보완
    if (_firstOpen) { _firstOpen = false; return; }
    fetchBookings();
    fetchMyBookings();
  },
});

export const connectSse = () => {
  _firstOpen = true;
  _sse.connect();
};

export const disconnectSse = () => {
  _sse.disconnect();
};
