import { ref, reactive, computed, watch } from 'vue';
import dayjs from 'dayjs';
import { logs } from './useAdminState';

// ── 로그 필터/정렬 ────────────────────────────────────────────
export const logFilter    = ref('ALL');
export const logSort      = reactive({ key: 'createdAt', dir: 'desc' });
export const logPage      = ref(0);
export const logDateRange = ref('ALL');
export const logSearch    = ref('');

watch([logFilter, logDateRange, logSearch], () => { logPage.value = 0; });

// ── 로그 카테고리 상수 ────────────────────────────────────────
export const logCategories = [
  { key: 'ALL',          label: '전체',     color: 'slate',   prefixes: null },
  { key: 'ROOM',         label: '회의실',   color: 'indigo',  prefixes: ['ROOM'] },
  { key: 'BOOKING',      label: '예약',     color: 'red',     prefixes: ['BOOKING'] },
  { key: 'USER',         label: '회원',     color: 'violet',  prefixes: ['USER'] },
  { key: 'PASSWORD',     label: '비밀번호', color: 'amber',   prefixes: ['PASSWORD'] },
  { key: 'FCM',          label: 'FCM',      color: 'sky',     prefixes: ['FCM'] },
  { key: 'NOTIFICATION', label: '공지',     color: 'emerald', prefixes: ['NOTIFICATION'] },
];

export const actionLabel = {
  ROOM_CREATE: '회의실 생성',   ROOM_UPDATE: '회의실 수정',
  ROOM_DELETE: '회의실 삭제',   ROOM_TOGGLE: '상태 변경',
  BOOKING_CREATE: '예약 등록',  BOOKING_UPDATE: '예약 수정',
  BOOKING_CANCEL: '예약 취소',  BOOKING_RESET: '예약 초기화',
  BOOKING_END: '예약 종료',
  USER_APPROVE: '회원 승인',    USER_REJECT: '회원 거절',
  USER_CREATE: '회원 생성',     USER_ROLE_CHANGE: '역할 변경',
  USER_DELETE: '회원 삭제',     USER_RESET: '회원 초기화',
  USER_LOGIN:  '로그인',
  USER_LOGOUT: '로그아웃',
  PASSWORD_RESET: '비밀번호 초기화', PASSWORD_SET: '비밀번호 설정',
  FCM_CLEAR: 'FCM 초기화',
  NOTIFICATION_BROADCAST: '전체 공지',
  LOG_RESET: '로그 초기화',
  SYSTEM_RESET: '시스템 초기화',
  FULL_RESET: '전체 초기화',
};

export const categoryTabClass = {
  slate:   { active: 'bg-slate-800 text-white',   badge: 'bg-white/20' },
  indigo:  { active: 'bg-indigo-500 text-white',  badge: 'bg-white/30' },
  red:     { active: 'bg-red-500 text-white',     badge: 'bg-white/30' },
  violet:  { active: 'bg-violet-500 text-white',  badge: 'bg-white/30' },
  amber:   { active: 'bg-amber-500 text-white',   badge: 'bg-white/30' },
  sky:     { active: 'bg-sky-500 text-white',     badge: 'bg-white/30' },
  emerald: { active: 'bg-emerald-500 text-white', badge: 'bg-white/30' },
};

export const logCategoryCount = (cat) => {
  if (!cat.prefixes) return logs.value.length;
  return logs.value.filter(l => cat.prefixes.some(p => l.actionType?.startsWith(p))).length;
};

export const logBadgeStyle = (type) => {
  if (type?.startsWith('ROOM'))         return { bg: 'bg-indigo-100', text: 'text-indigo-700', dot: '#6366f1' };
  if (type?.startsWith('BOOKING'))      return { bg: 'bg-red-100',    text: 'text-red-600',    dot: '#ef4444' };
  if (type?.startsWith('USER'))         return { bg: 'bg-violet-100', text: 'text-violet-700', dot: '#7c3aed' };
  if (type?.startsWith('PASSWORD'))     return { bg: 'bg-amber-100',  text: 'text-amber-700',  dot: '#d97706' };
  if (type?.startsWith('FCM'))          return { bg: 'bg-sky-100',    text: 'text-sky-700',    dot: '#0284c7' };
  if (type?.startsWith('NOTIFICATION')) return { bg: 'bg-emerald-100',text: 'text-emerald-700',dot: '#059669' };
  return { bg: 'bg-slate-100', text: 'text-slate-600', dot: '#94a3b8' };
};

export const logBadge = (type) => { const s = logBadgeStyle(type); return `${s.bg} ${s.text}`; };

// ── 필터링된 로그 ─────────────────────────────────────────────
export const filteredSortedLogs = computed(() => {
  const cat = logCategories.find(c => c.key === logFilter.value);
  let filtered = cat?.prefixes
    ? logs.value.filter(l => cat.prefixes.some(p => l.actionType?.startsWith(p)))
    : logs.value;

  // 날짜 범위 필터
  if (logDateRange.value === 'TODAY') {
    filtered = filtered.filter(l => dayjs(l.createdAt).isSame(dayjs(), 'day'));
  } else if (logDateRange.value === '7D') {
    filtered = filtered.filter(l => dayjs(l.createdAt).isAfter(dayjs().subtract(7, 'day')));
  } else if (logDateRange.value === '30D') {
    filtered = filtered.filter(l => dayjs(l.createdAt).isAfter(dayjs().subtract(30, 'day')));
  }

  // 검색 필터
  if (logSearch.value.trim()) {
    const q = logSearch.value.toLowerCase();
    filtered = filtered.filter(l =>
      l.description?.toLowerCase().includes(q) || l.actionType?.toLowerCase().includes(q)
    );
  }

  return [...filtered].sort((a, b) => {
    const d  = logSort.dir === 'asc' ? 1 : -1;
    const av = a[logSort.key] ?? '';
    const bv = b[logSort.key] ?? '';
    return av < bv ? -d : av > bv ? d : 0;
  });
});

export const totalLogPages = computed(() => Math.max(1, Math.ceil(filteredSortedLogs.value.length / 20)));
export const pagedLogs     = computed(() => filteredSortedLogs.value.slice(logPage.value * 20, (logPage.value + 1) * 20));

export const setLogSort = (key) => {
  if (logSort.key === key) logSort.dir = logSort.dir === 'asc' ? 'desc' : 'asc';
  else { logSort.key = key; logSort.dir = 'desc'; }
};
