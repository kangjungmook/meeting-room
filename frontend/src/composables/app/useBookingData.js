import { ref, computed } from 'vue';
import dayjs from 'dayjs';
import api from '../../api';
import { viewMode, targetDate } from './useCalendar';

// ── 핵심 데이터 ───────────────────────────────────────────────
export const rooms    = ref([]);
export const bookings = ref([]);
export const userMap  = ref({}); // userId(Long) → name (참석자 표시용)

// ── 내 예약 ───────────────────────────────────────────────────
export const myBookings         = ref([]);
export const myBookingsToday    = computed(() => myBookings.value.filter(b => dayjs(b.startTime).isSame(dayjs(), 'day')));
export const myBookingsThisWeek = computed(() => myBookings.value.filter(b => {
  const d = dayjs(b.startTime);
  return !d.isSame(dayjs(), 'day') && d.isSame(dayjs(), 'week');
}));
export const myBookingsUpcoming = computed(() => myBookings.value.filter(b => dayjs(b.startTime).isAfter(dayjs().endOf('week'))));

// ── API ───────────────────────────────────────────────────────
export const fetchRooms = async () => {
  try {
    const res = await api.get('/rooms');
    rooms.value = res.data
      .filter(r => r.isActive)
      .map(r => ({ id: r.id, name: r.name, capacity: r.capacity, colorCode: r.colorCode }));
  } catch (e) { console.error(e); }
};

let _fetchController = null;

export const fetchBookings = async () => {
  if (_fetchController) _fetchController.abort();
  _fetchController = new AbortController();

  const unit  = viewMode.value === 'month' ? 'month' : viewMode.value === 'week' ? 'week' : 'day';
  const start = targetDate.value.startOf(unit).format('YYYY-MM-DD');
  const end   = targetDate.value.endOf(unit).format('YYYY-MM-DD');
  try {
    const res = await api.get(`/bookings?startDate=${start}&endDate=${end}`, {
      signal: _fetchController.signal,
    });
    bookings.value = res.data;
  } catch (e) {
    if (e.code === 'ERR_CANCELED') return;
    console.error(e);
  }
};

export const fetchMyBookings = async () => {
  try {
    const res = await api.get('/bookings/my');
    myBookings.value = res.data;
  } catch (e) { console.error(e); }
};
