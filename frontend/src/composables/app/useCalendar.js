import { ref, reactive, computed } from 'vue';
import dayjs from 'dayjs';
import 'dayjs/locale/ko';

dayjs.locale('ko');

// ── 뷰 모드 ───────────────────────────────────────────────────
export const viewMode   = ref('day');
export const targetDate = ref(dayjs());
export const modes      = [
  { name: '일', val: 'day'   },
  { name: '주', val: 'week'  },
  { name: '월', val: 'month' },
];
export const searchQuery = ref('');
export const modeOrder = ['day', 'week', 'month'];
export const slideDir  = ref('left');
export const hours     = Array.from({ length: 15 }, (_, i) => i + 6); // 6~20시

// ── UI 상태 ───────────────────────────────────────────────────
export const isMobile         = ref(typeof window !== 'undefined' ? window.innerWidth < 768 : false);
export const sidebarCollapsed = ref(false);
export const showDrawer       = ref(false);
export const showDateSheet    = ref(false);
const MOBILE_BREAKPOINT = 768;

if (typeof window !== 'undefined') {
  const updateMobile = () => {
    isMobile.value = window.innerWidth < MOBILE_BREAKPOINT;
  };
  window.addEventListener('resize', updateMobile, { passive: true });
  window.addEventListener('orientationchange', updateMobile, { passive: true });
}

// ── 캘린더 날짜 계산 ──────────────────────────────────────────
export const weekDays = computed(() => {
  // locale의 weekStart 영향 없이 항상 일요일(0) 기준
  const dow    = targetDate.value.day(); // 0=일, 6=토
  const sunday = targetDate.value.subtract(dow, 'day');
  return Array.from({ length: 7 }, (_, i) => sunday.add(i, 'day'));
});

export const dateLabel = computed(() => {
  if (isMobile.value) {
    if (viewMode.value === 'day')   return targetDate.value.format('MM/DD (dd)');
    if (viewMode.value === 'week')  return `${weekDays.value[0].format('MM/DD')}~${weekDays.value[6].format('MM/DD')}`;
    return targetDate.value.format('YYYY년 MM월');
  }
  if (viewMode.value === 'day')  return targetDate.value.format('YYYY년 MM월 DD일 (dd)');
  if (viewMode.value === 'week') return `${weekDays.value[0].format('MM월 DD일')} – ${weekDays.value[6].format('MM월 DD일')}`;
  return targetDate.value.format('YYYY년 MM월');
});

export const monthCells = computed(() => {
  const first = targetDate.value.startOf('month');
  const last  = targetDate.value.endOf('month');
  // 항상 일요일 시작 기준 (locale 무관)
  const s = first.subtract(first.day(), 'day');          // 첫날이 속한 주의 일요일
  const e = last.add(6 - last.day(), 'day');             // 마지막날이 속한 주의 토요일
  const cells = []; let cur = s;
  while (cur.isBefore(e) || cur.isSame(e, 'day')) {
    cells.push({ date: cur, isCurrentMonth: cur.month() === targetDate.value.month() });
    cur = cur.add(1, 'day');
  }
  return cells;
});

// ── 정렬 / 펼치기 ─────────────────────────────────────────────
export const expandedCells = reactive({});
export const sortBy        = ref('time');
export const sortOptions   = [
  { val: 'time', label: '시간순' },
];
export const toggleExpand = (key) => { expandedCells[key] = !expandedCells[key]; };
export const isExpanded   = (key) => !!expandedCells[key];

