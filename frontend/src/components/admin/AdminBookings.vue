<template>
  <div>
    <AdminBookingsToolbar
      :all-rooms="allRooms"
      :booking-filter="bookingFilter"
      :booking-sort="bookingSort"
      :sort-options="sortOptions"
      :count="filteredBookings.length"
      :dayjs="dayjs"
      @setSort="setBookingSort"
    />

    <AdminBookingsTable
      v-if="!isMobile"
      :bookings="filteredBookings"
      :get-room-name="getRoomName"
      :get-room-color="getRoomColor"
      :dayjs="dayjs"
      @edit="openEdit"
      @cancel="cancelBooking"
    />

    <AdminBookingsCards
      v-else
      :bookings="filteredBookings"
      :get-room-name="getRoomName"
      :get-room-color="getRoomColor"
      :dayjs="dayjs"
      @edit="openEdit"
      @cancel="cancelBooking"
    />

    <AdminBookingEditModal
      :show="editModal.show"
      :form="editModal.form"
      :error="editModal.error"
      :is-submitting="isSubmitting"
      :all-rooms="allRooms"
      @close="editModal.show = false"
      @submit="submitEdit"
    />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useAdmin } from '../../composables/useAdmin';
import { showAdminToast } from '../../composables/admin/useAdminToast';
import AdminBookingsToolbar from './bookings/AdminBookingsToolbar.vue';
import AdminBookingsTable from './bookings/AdminBookingsTable.vue';
import AdminBookingsCards from './bookings/AdminBookingsCards.vue';
import AdminBookingEditModal from './bookings/modals/AdminBookingEditModal.vue';

const {
  allRooms, filteredBookings, bookingFilter, bookingSort,
  cancelBooking, updateBooking, setBookingSort,
  getRoomName, getRoomColor, dayjs,
} = useAdmin();

const props = defineProps({ isMobile: Boolean });

const sortOptions = [
  { key: 'startTime', label: '날짜' },
  { key: 'organizer', label: '예약자' },
  { key: 'title',     label: '제목' },
];

const isSubmitting = ref(false);
const editModal = reactive({
  show: false, error: '',
  form: { id: null, roomId: null, title: '', date: '', startClock: '', endClock: '', description: '', attendeeIdsStr: '', organizer: '' },
});

const openEdit = (b) => {
  editModal.form = {
    id: b.id, roomId: b.roomId, title: b.title,
    date:        dayjs(b.startTime).format('YYYY-MM-DD'),
    startClock:  dayjs(b.startTime).format('HH:mm'),
    endClock:    dayjs(b.endTime).format('HH:mm'),
    description:    b.description    || '',
    attendeeIdsStr: (b.attendeeIds || []).join(','),
    organizer:      b.organizer      || '',
  };
  editModal.show = true;
};

const submitEdit = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;
  editModal.error = '';
  try {
    const f = editModal.form;
    await updateBooking(f.id, {
      roomId: Number(f.roomId), title: f.title,
      startTime: `${f.date}T${f.startClock}`,
      endTime: `${f.date}T${f.endClock}`,
      description: f.description,
      attendeeIds: f.attendeeIdsStr.split(',').map(s => s.trim()).filter(Boolean).map(Number).filter(n => !isNaN(n)),
      organizer: f.organizer,
    });
    editModal.show = false;
    showAdminToast('예약이 수정되었습니다.');
  } catch (e) {
    editModal.error = e.response?.data || e.message || '수정 중 오류가 발생했습니다.';
  } finally {
    isSubmitting.value = false;
  }
};
</script>
