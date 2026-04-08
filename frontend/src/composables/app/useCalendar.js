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

// ── 캘린더 날짜 계산 ──────────────────────────────────────────
export const weekDays = computed(() => {
  const s = targetDate.value.startOf('week');
  return Array.from({ length: 7 }, (_, i) => s.add(i, 'day'));
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
  const s = targetDate.value.startOf('month').startOf('week');
  const e = targetDate.value.endOf('month').endOf('week');
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

