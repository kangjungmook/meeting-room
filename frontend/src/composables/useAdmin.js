/**
 * useAdmin.js — 관리자 상태 배럴 (barrel re-export)
 *
 * 컴포넌트는 const { ... } = useAdmin() 형태로 사용한다.
 * 상태는 모듈 싱글턴이므로 여러 곳에서 useAdmin()을 호출해도 같은 인스턴스를 공유한다.
 *
 * 서브 모듈 구조:
 *   composables/admin/useAdminState.js         — 핵심 데이터 (refs) + fetchAll
 *   composables/admin/useAdminRooms.js         — 회의실 CRUD
 *   composables/admin/useAdminBookings.js      — 예약 필터/정렬 + 액션
 *   composables/admin/useAdminUsers.js         — 사용자 관리
 *   composables/admin/useAdminLogs.js          — 로그 필터/정렬/페이징 + 상수
 *   composables/admin/useAdminNotifications.js — 알림 설정 + 공지
 *   composables/admin/useAdminSSE.js           — SSE 실시간 연결
 *   composables/admin/useAdminHelpers.js       — 룸 헬퍼 + UA 파싱 + JSON 포맷
 */

import { ref, computed } from 'vue';
import dayjs from 'dayjs';
import router from '../router';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/ko';
import api from '../api';

dayjs.extend(relativeTime);
dayjs.locale('ko');
// ── 서브 모듈 ─────────────────────────────────────────────────
import {
  currentUser, allRooms, allBookings, logs, dashboard, users, onlineUsers,
  notifSetting, participationStats, fetchAll, fetchNotifSetting,
} from './admin/useAdminState';
import { createRoom, updateRoom, deleteRoom, toggleRoom } from './admin/useAdminRooms';
import {
  bookingFilter, bookingSort, filteredBookings, setBookingSort,
  cancelBooking, updateBooking,
} from './admin/useAdminBookings';
import {
  manageUserFilter, pendingCount, filteredUsers, manageTabs, adminUsers,
  approveUser, rejectUser, resetUserPassword, changeRole, deleteUser,
  clearFcmToken, createUser, setUserPassword,
} from './admin/useAdminUsers';
import {
  logFilter, logSort, logPage, logDateRange, logSearch,
  logCategories, actionLabel, categoryTabClass, logCategoryCount, logBadge, logBadgeStyle,
  filteredSortedLogs, pagedLogs, totalLogPages, setLogSort,
} from './admin/useAdminLogs';
import {
  broadcast, notifMinutes, addMinute, removeMinute,
  saveNotifSetting, saveSystemSetting, sendBroadcast,
} from './admin/useAdminNotifications';
import { connectSse, disconnectSse } from './admin/useAdminSSE';
import {
  getRoomName, getRoomColor, parseDevice, parseBrowser,
  parseDetail, formatRawJson, formatJsonHtml,
} from './admin/useAdminHelpers';

// ── 배럴 전용 computed (여러 모듈 데이터를 조합) ───────────────
const todayBookings = computed(() =>
  allBookings.value.filter(b => dayjs(b.startTime).isSame(dayjs(), 'day'))
);
const activeBookings = computed(() =>
  allBookings.value.filter(b => dayjs().isAfter(dayjs(b.startTime)) && dayjs().isBefore(dayjs(b.endTime)))
);
const recentActivity = computed(() => [...logs.value].slice(0, 5));
const stats = computed(() => [
  {
    label: '전체 회의실', value: dashboard.value.totalRooms, sub: `활성 ${dashboard.value.activeRooms}개`,
    bgColor: 'bg-indigo-50', tab: 'rooms',
    icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="1" y="4" width="16" height="12" rx="2" stroke="#6366f1" stroke-width="1.6"/><path d="M5 4V2M13 4V2" stroke="#6366f1" stroke-width="1.6" stroke-linecap="round"/></svg>',
  },
  {
    label: '오늘 예약', value: dashboard.value.todayBookings, sub: `총 ${dashboard.value.totalBookings}건`,
    bgColor: 'bg-emerald-50', tab: 'bookings',
    icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="1" y="3" width="16" height="13" rx="2" stroke="#10b981" stroke-width="1.6"/><path d="M1 7h16M6 1v3M12 1v3" stroke="#10b981" stroke-width="1.6" stroke-linecap="round"/></svg>',
  },
  {
    label: '승인 대기', value: pendingCount.value, sub: '회원가입 승인 필요',
    bgColor: 'bg-amber-50', tab: 'user-manage',
    icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="4" stroke="#f59e0b" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#f59e0b" stroke-width="1.6" stroke-linecap="round"/></svg>',
  },
  {
    label: '전체 회원', value: dashboard.value.totalUsers ?? 0, sub: `알림 설정 ${dashboard.value.fcmUsers ?? 0}명`,
    bgColor: 'bg-violet-50', tab: 'user-manage',
    icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="7" cy="5" r="3" stroke="#7c3aed" stroke-width="1.6"/><path d="M1 16c0-3.3 2.7-6 6-6" stroke="#7c3aed" stroke-width="1.6" stroke-linecap="round"/><circle cx="13" cy="7" r="2.5" stroke="#7c3aed" stroke-width="1.4"/><path d="M10 16c0-2.2 1.3-4 3-4s3 1.8 3 4" stroke="#7c3aed" stroke-width="1.4" stroke-linecap="round"/></svg>',
  },
  {
    label: '관리 로그', value: logs.value.length, sub: '전체 이력',
    bgColor: 'bg-slate-50', tab: 'logs',
    icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M3 5h12M3 9h8M3 13h5" stroke="#94a3b8" stroke-width="1.6" stroke-linecap="round"/></svg>',
  },
]);

// ── 네비게이션 탭 ─────────────────────────────────────────────
const activeTab = ref('dashboard');

// ── 초기화 (리셋) API ─────────────────────────────────────────
const resetBookings    = async () => { await api.delete('/admin/bookings/reset');           fetchAll(); };
const resetLogs        = async () => { await api.delete('/admin/logs/reset');               fetchAll(); };
const resetUsers       = async () => { await api.delete('/admin/users/reset');              fetchAll(); };
const resetNotifSetting = async () => { await api.delete('/admin/notification-setting/reset'); fetchNotifSetting(); };
const doFullReset = async () => {
  await api.delete('/admin/full-reset');
  // 세션/토큰 전부 제거 후 로그인 페이지로 이동
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  sessionStorage.removeItem('adminAuth');
  router.push('/');
};

// ── Export ──
export function useAdmin() {
  return {
    // 핵심 데이터
    allRooms, allBookings, logs, dashboard, users, notifSetting, broadcast, currentUser, onlineUsers,
    participationStats,
    // 네비게이션
    activeTab, manageUserFilter,
    // computed
    pendingCount, filteredUsers, manageTabs, adminUsers,
    todayBookings, activeBookings, recentActivity, stats,
    filteredBookings, bookingFilter, bookingSort,
    logFilter, logSort, logPage, logDateRange, logSearch,
    filteredSortedLogs, pagedLogs, totalLogPages,
    logCategories, actionLabel, categoryTabClass, logCategoryCount, logBadge, logBadgeStyle,
    notifMinutes,
    // methods
    fetchAll, fetchNotifSetting, connectSse, disconnectSse,
    createRoom, updateRoom, deleteRoom, toggleRoom,
    cancelBooking, updateBooking,
    approveUser, rejectUser, resetUserPassword, changeRole, deleteUser, clearFcmToken, createUser, setUserPassword,
    saveNotifSetting, sendBroadcast, saveSystemSetting,
    resetBookings, resetLogs, resetUsers, resetNotifSetting, doFullReset,
    addMinute, removeMinute,
    setBookingSort, setLogSort,
    getRoomName, getRoomColor, parseDevice, parseBrowser,
    parseDetail, formatRawJson, formatJsonHtml,
    dayjs,
  };
}
