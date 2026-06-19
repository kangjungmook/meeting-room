import { ref, reactive } from 'vue';
import api from '../../api';
import { fetchBookings, fetchMyBookings } from './useBookingData';
import { createSseManager } from '../../utils/sse';
import { addToast, toastDuration, queueCreatedToast, queueCancelledToast, suppressOwnCreatedUntil } from './useToast';
import { currentUser } from './useAuth';

// ── 다크 모드 (localStorage = 즉시 로드, DB = 최종 소스) ──────────────
export const darkMode = ref(
  typeof window !== 'undefined' ? localStorage.getItem('darkMode') === 'true' : false
);

// 모듈 로드 즉시 적용 (깜빡임 없음)
if (typeof window !== 'undefined') {
  document.documentElement.classList.toggle('dark', darkMode.value);
}

export const userNotifPrefs = reactive({
  receiveInvitation: true,
  receiveUpdates:    true,
  receiveReminder:   true,
  toastCreated:      false,
  toastUpdated:      false,
  toastCancelled:    false,
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
export const pushEnabled = ref(false);

let _messagingListenerSet = false;

const setupFcmMessaging = async () => {
  try {
    const { getMessagingInstance, getToken, onMessage, VAPID_KEY } = await import('../../firebase');
    const messaging = await getMessagingInstance();
    if (!messaging) return false;
    const token = await getToken(messaging, { vapidKey: VAPID_KEY });
    if (token) {
      await api.post('/users/fcm-token', { token });
      pushEnabled.value = true;
    }

    // 포그라운드(앱 열려있을 때)에도 시스템 알림 표시 — 중복 등록 방지
    if (!_messagingListenerSet) {
      _messagingListenerSet = true;
      onMessage(messaging, async (payload) => {
        const title = payload.notification?.title ?? payload.data?.title ?? '알림';
        const body  = payload.notification?.body  ?? payload.data?.body  ?? '';
        if (Notification.permission !== 'granted') return;

        const options = { body, icon: '/favicon.ico', badge: '/favicon.ico' };

        try {
          if ('serviceWorker' in navigator) {
            const reg = await navigator.serviceWorker.getRegistration();
            if (reg?.showNotification) { await reg.showNotification(title, options); return; }
          }
        } catch (_) {}

        try {
          new Notification(title, options);
        } catch (_) {
          addToast(`${title}${body ? ` - ${body}` : ''}`, 'info');
        }
      });
    }
    return true;
  } catch (e) {
    console.warn('FCM 토큰 발급 실패:', e.message);
    return false;
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
  if (Notification.permission === 'denied') {
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

export const enablePush = async () => {
  if (Notification.permission !== 'granted') return;
  const ok = await setupFcmMessaging();
  if (ok) addToast('푸시 알림을 켰습니다.', 'success');
};

export const disablePush = async () => {
  try {
    await api.delete('/users/fcm-token');
    pushEnabled.value = false;
    addToast('푸시 알림을 껐습니다.', 'info');
  } catch (e) {
    console.warn('FCM 토큰 삭제 실패:', e.message);
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
        // 본인이 예약자이거나 참석자로 포함된 예약만 토스트 대상
        const uid = currentUser.id;
        const isOrganizer = uid != null && data.userId === uid;
        const isAttendee  = uid != null && Array.isArray(data.attendeeIds) && data.attendeeIds.includes(uid);
        const isRelevant  = isOrganizer || isAttendee;

        if (type === 'CREATED' && userNotifPrefs.toastCreated && isRelevant) {
          // 본인이 방금 반복 예약을 일괄 생성한 경우 각 건마다 토스트가 뜨는 것을 억제
          if (!(isOrganizer && Date.now() < suppressOwnCreatedUntil.value)) {
            queueCreatedToast(data.organizer);
          }
        } else if (type === 'CANCELLED' && userNotifPrefs.toastCancelled && isRelevant) {
          queueCancelledToast();
        } else if (type === 'UPDATED' && userNotifPrefs.toastUpdated && isRelevant) {
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
