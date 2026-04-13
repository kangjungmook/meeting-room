import { ref, reactive } from 'vue';
import dayjs from 'dayjs';
import api from '../../api';
import { parseJwt } from '../../utils/parseJwt';

// ── 관리자용 라이브 타이머 (30초마다 갱신) ────────────────────
export const adminLiveNow = ref(dayjs());
setInterval(() => { adminLiveNow.value = dayjs(); }, 30_000);

// ── 현재 사용자 (관리자 컨텍스트) ────────────────────────────
const _p = parseJwt();
export const currentUser = reactive({ name: _p.name || '', employeeId: _p.sub || '' });

// ── 핵심 데이터 ───────────────────────────────────────────────
export const allRooms    = ref([]);
export const allBookings = ref([]);
export const logs        = ref([]);
export const dashboard   = ref({
  totalRooms: 0, activeRooms: 0, todayBookings: 0,
  totalBookings: 0, totalUsers: 0, pendingUsers: 0, fcmUsers: 0, roomStats: [],
});
export const users       = ref([]);
export const onlineUsers = ref([]);

// ── 참여 통계 ─────────────────────────────────────────────────
export const participationStats = ref({
  topBookers:   [],  // [{ userId, name, count }]
  topAttendees: [],  // [{ userId, name, count }]
});

// ── 알림 설정 ─────────────────────────────────────────────────
export const notifSetting = ref({
  minutesBefore: '5,10',
  titleTemplate: '🔔 회의 {minutes}분 전',
  bodyTemplate: '{title} · {time} 시작',
  toastDuration: 5,
  enabled: true,
  enableBookingCreated: true,
  enableBookingUpdated: true,
  enableInvitation: true,
  enableReminder: true,
  allowSelfRegistration: true,
  maintenanceMode: false,
  autoApprove: false,
});

// ── 전체 데이터 로드 ──────────────────────────────────────────
export const fetchAll = async () => {
  // 관리자 페이지 진입 시 최신 JWT로 사용자 정보 갱신
  const p = parseJwt();
  if (p.sub)  currentUser.employeeId = p.sub;
  if (p.name) currentUser.name       = p.name;

  const [roomsRes, bookingsRes, logsRes, dashRes, usersRes, onlineRes, statsRes] = await Promise.all([
    api.get('/admin/rooms'),
    api.get('/admin/bookings'),
    api.get('/admin/logs'),
    api.get('/admin/dashboard'),
    api.get('/admin/users'),
    api.get('/admin/online-users').catch(() => ({ data: [] })),
    api.get('/admin/stats').catch(() => ({ data: { topBookers: [], topAttendees: [] } })),
  ]);
  allRooms.value    = roomsRes.data;
  allBookings.value = bookingsRes.data;
  logs.value        = logsRes.data;
  dashboard.value   = dashRes.data;
  users.value       = usersRes.data;
  onlineUsers.value = onlineRes.data ?? [];
  participationStats.value = {
    topBookers:   statsRes.data.topBookers   ?? [],
    topAttendees: statsRes.data.topAttendees ?? [],
  };

  // JWT에도 없으면 서버 users 목록에서 보완
  if (!currentUser.name || !currentUser.employeeId) {
    const me = usersRes.data.find(u => u.employeeId === currentUser.employeeId);
    if (me) { currentUser.name = me.name; currentUser.employeeId = me.employeeId; }
  }
};

export const fetchNotifSetting = async () => {
  const res = await api.get('/admin/notification-setting');
  notifSetting.value = {
    ...notifSetting.value, ...res.data,
    enableBookingCreated:  res.data.enableBookingCreated  ?? true,
    enableBookingUpdated:  res.data.enableBookingUpdated  ?? true,
    enableInvitation:      res.data.enableInvitation      ?? true,
    enableReminder:        res.data.enableReminder        ?? true,
    allowSelfRegistration: res.data.allowSelfRegistration ?? true,
    maintenanceMode:       res.data.maintenanceMode       ?? false,
  };
};
