import { ref, computed } from 'vue';
import dayjs from 'dayjs';
import { rooms, bookings, userMap } from './useBookingData';
import { sortBy, targetDate, searchQuery } from './useCalendar';
import { liveNow } from './useRealtime';
import { isAdmin, currentUser } from './useAuth';

// ── 룸 / 예약 헬퍼 ────────────────────────────────────────────
export const getRoomColor     = (id) => rooms.value.find(r => r.id === id)?.colorCode ?? '#6366f1';
export const getRoomName      = (id) => rooms.value.find(r => r.id === id)?.name ?? '-';
// attendeeIds: Long[], externalAttendeeNames: String[] (서버 응답)
export const resolveAttendees = (attendeeIds, externalAttendeeNames = []) => {
  const internal = (attendeeIds ?? []).map(id => userMap.value[id] || `#${id}`);
  const external = externalAttendeeNames ?? [];
  const all = [...internal, ...external];
  return all.length ? all.join(', ') : '';
};

const matchesSearch = (b) => {
  const q = searchQuery.value.trim().toLowerCase();
  if (!q) return true;
  return (b.title ?? '').toLowerCase().includes(q)
    || getRoomName(b.roomId).toLowerCase().includes(q)
    || (b.organizer ?? '').toLowerCase().includes(q);
};

export const filterBookings     = (roomId, date) =>
  bookings.value.filter(b => b.roomId === roomId && dayjs(b.startTime).isSame(date, 'day') && b.status !== 'CANCELLED' && matchesSearch(b));
export const getBookingsForDate = (date) =>
  bookings.value.filter(b => dayjs(b.startTime).isSame(date, 'day') && b.status !== 'CANCELLED' && matchesSearch(b));

export const sortBookings = (list) => [...list].sort((a, b) => {
  if (sortBy.value === 'time')      return dayjs(a.startTime).diff(dayjs(b.startTime));
  if (sortBy.value === 'room')      return getRoomName(a.roomId).localeCompare(getRoomName(b.roomId));
  if (sortBy.value === 'organizer') return (a.organizer ?? '').localeCompare(b.organizer ?? '');
  return 0;
});

export const canEditOrCancel = (b) =>
  !dayjs(b.endTime).isBefore(dayjs()) && (isAdmin || b.userId === currentUser.id);
export const chipMinutes     = (s, e) => dayjs(e).diff(dayjs(s), 'minute');

// ── 오늘 현황 ─────────────────────────────────────────────────
export const todayBookingCount  = computed(() =>
  bookings.value.filter(b => b.status !== 'CANCELLED' && dayjs(b.startTime).isSame(liveNow.value, 'day')).length
);
export const isRoomInUse = (roomId) =>
  bookings.value.some(b =>
    b.roomId === roomId && b.status !== 'CANCELLED' &&
    dayjs(b.startTime).isBefore(liveNow.value) && dayjs(b.endTime).isAfter(liveNow.value)
  );
export const currentlyUsedCount = computed(() => rooms.value.filter(r => isRoomInUse(r.id)).length);

// ── 회의실 상세 ───────────────────────────────────────────────
export const selectedRoom         = ref(null);
export const selectedRoomBookings = computed(() => {
  if (!selectedRoom.value) return [];
  const today = dayjs().format('YYYY-MM-DD');
  return bookings.value
    .filter(b => b.roomId === selectedRoom.value.id && b.status !== 'CANCELLED' && dayjs(b.startTime).format('YYYY-MM-DD') === today)
    .sort((a, b) => dayjs(a.startTime).valueOf() - dayjs(b.startTime).valueOf());
});
export const selectedRoomCurrent = computed(() =>
  selectedRoomBookings.value.find(b =>
    dayjs(b.startTime).isBefore(liveNow.value) && dayjs(b.endTime).isAfter(liveNow.value)
  ) ?? null
);
export const getRoomBookingStatus = (b) => {
  if (dayjs(b.endTime).isBefore(liveNow.value))   return { label: '종료',    cls: 'text-slate-500 bg-slate-700/50'    };
  if (dayjs(b.startTime).isBefore(liveNow.value)) return { label: '진행 중', cls: 'text-emerald-400 bg-emerald-500/20' };
  return { label: '예정', cls: 'text-blue-400 bg-blue-500/20' };
};

// ── 타임라인 레이아웃 계산 ────────────────────────────────────
const _assignLanes = (list) => {
  const lanes = [];
  return list.map(b => {
    const st = dayjs(b.startTime), en = dayjs(b.endTime);
    let lane = lanes.findIndex(endT => !st.isBefore(endT));
    if (lane === -1) { lane = lanes.length; lanes.push(en); } else { lanes[lane] = en; }
    return { ...b, lane };
  });
};

export const getLayoutBookings = (roomId, date) => {
  const items = _assignLanes(filterBookings(roomId, date).slice().sort((a, b) => dayjs(a.startTime).valueOf() - dayjs(b.startTime).valueOf()));
  return { items, totalLanes: Math.max(items.reduce((m, b) => Math.max(m, b.lane + 1), 1), 1) };
};
export const getAllLayoutBookings = (date) => {
  const items = _assignLanes(getBookingsForDate(date).slice().sort((a, b) => dayjs(a.startTime).valueOf() - dayjs(b.startTime).valueOf()));
  return { items, totalLanes: Math.max(items.reduce((m, b) => Math.max(m, b.lane + 1), 1), 1) };
};

// 수평 타임라인 (주간 뷰)
export const calcPos = (b, totalLanes, color) => {
  const st = dayjs(b.startTime).hour() + dayjs(b.startTime).minute() / 60;
  const en = dayjs(b.endTime).hour()   + dayjs(b.endTime).minute()   / 60;
  return { left: `${((st - 6) / 15) * 100}%`, width: `${((en - st) / 15) * 100}%`, top: `${6 + b.lane * 70}px`, height: '64px', background: color, opacity: '0.92' };
};

// 수직 타임라인 (일간 뷰)
export const HOUR_H   = 80;
export const calcPosV = (b, totalLanes, color) => {
  const startH = dayjs(b.startTime).hour() + dayjs(b.startTime).minute() / 60;
  const endH   = dayjs(b.endTime).hour()   + dayjs(b.endTime).minute()   / 60;
  const colPct = 100 / totalLanes;
  return {
    position: 'absolute',
    top:      `${(startH - 6) * HOUR_H}px`,
    height:   `${Math.max((endH - startH) * HOUR_H - 3, 32)}px`,
    left:     `calc(${b.lane * colPct}% + 2px)`,
    width:    `calc(${colPct}% - 4px)`,
    background: color,
    opacity: '0.92',
  };
};

export const nowLineTop = computed(() =>
  `${(liveNow.value.hour() + liveNow.value.minute() / 60 - 6) * HOUR_H}px`
);

// ── 일간 뷰 스크롤 ────────────────────────────────────────────
export const dayViewRef = ref(null);
export const scrollDayView = () => {
  if (!dayViewRef.value) return;
  const today = getBookingsForDate(targetDate.value);
  let h = targetDate.value.isSame(dayjs(), 'day') ? liveNow.value.hour() + liveNow.value.minute() / 60 : 8;
  if (today.length > 0) {
    const earliest = today.reduce((a, b) => dayjs(a.startTime).isBefore(dayjs(b.startTime)) ? a : b);
    h = dayjs(earliest.startTime).hour() + dayjs(earliest.startTime).minute() / 60;
  }
  dayViewRef.value.scrollTop = Math.max(0, (h - 6) * HOUR_H - 40);
};
