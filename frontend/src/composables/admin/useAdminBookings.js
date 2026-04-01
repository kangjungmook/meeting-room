import { reactive, computed } from 'vue';
import dayjs from 'dayjs';
import api from '../../api';
import { allBookings, fetchAll } from './useAdminState';

// ── 예약 필터/정렬 ────────────────────────────────────────────
export const bookingFilter = reactive({ keyword: '', roomId: '', date: '' });
export const bookingSort   = reactive({ key: 'startTime', dir: 'asc' });

export const filteredBookings = computed(() => {
  const filtered = allBookings.value.filter(b => {
    const kw = bookingFilter.keyword.toLowerCase();
    const matchKw   = !kw || b.title.toLowerCase().includes(kw) || b.organizer.toLowerCase().includes(kw);
    const matchRoom = !bookingFilter.roomId || b.roomId === Number(bookingFilter.roomId);
    const matchDate = !bookingFilter.date   || dayjs(b.startTime).format('YYYY-MM-DD') === bookingFilter.date;
    return matchKw && matchRoom && matchDate;
  });
  return [...filtered].sort((a, b) => {
    const d  = bookingSort.dir === 'asc' ? 1 : -1;
    const av = a[bookingSort.key] ?? '';
    const bv = b[bookingSort.key] ?? '';
    return av < bv ? -d : av > bv ? d : 0;
  });
});

export const setBookingSort = (key) => {
  if (bookingSort.key === key) bookingSort.dir = bookingSort.dir === 'asc' ? 'desc' : 'asc';
  else { bookingSort.key = key; bookingSort.dir = 'asc'; }
};

// ── 예약 액션 ─────────────────────────────────────────────────
export const cancelBooking = async (b) => {
  if (!confirm(`"${b.title}" 예약을 강제 취소할까요?`)) return;
  await api.delete(`/admin/bookings/${b.id}`);
  fetchAll();
};
export const updateBooking = async (id, form) => { await api.put(`/bookings/${id}`, form); fetchAll(); };
