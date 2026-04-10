<template>
  <div class="kiosk-root">
    <KioskHeader :room="room" :clock-time="clockTime" :clock-date="clockDate" @exit="exitKiosk" />

    <div class="kiosk-body">
      <KioskHeroPanel
        :is-in-use="isInUse"
        :current-booking="currentBooking"
        :next-booking="nextBooking"
        :time-until-end="timeUntilEnd"
        :time-until-next="timeUntilNext"
        :progress-pct="progressPct"
        @open-booking="showModal = true"
        @show-end-confirm="showEndConfirm = true"
      />
      <KioskSchedulePanel :today-bookings="todayBookings" :room="room" :now="now" />
    </div>

    <KioskTimeline :timeline-slots="timelineSlots" :now-line-left="nowLineLeft" />

    <KioskEndConfirmModal v-model="showEndConfirm" :booking="currentBooking" @confirm="endNow" />
    <KioskBookingModal v-model="showModal" :room="room" :now="now" :room-id="roomId" @booked="fetchBookings" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dayjs from 'dayjs';
import 'dayjs/locale/ko';
import axios from 'axios';
import { createSseManager } from '../../utils/sse';
import KioskHeader from '../../components/kiosk/KioskHeader.vue';
import KioskHeroPanel from '../../components/kiosk/KioskHeroPanel.vue';
import KioskSchedulePanel from '../../components/kiosk/KioskSchedulePanel.vue';
import KioskTimeline from '../../components/kiosk/KioskTimeline.vue';
import KioskEndConfirmModal from '../../components/kiosk/KioskEndConfirmModal.vue';
import KioskBookingModal from '../../components/kiosk/KioskBookingModal.vue';

const api = axios.create({ baseURL: '/api', headers: { 'Content-Type': 'application/json' } });

dayjs.locale('ko');

const route  = useRoute();
const router = useRouter();
const roomId = computed(() => Number(route.params.roomId));

const room         = ref(null);
const todayBookings = ref([]);
const now          = ref(dayjs());
const showEndConfirm = ref(false);
const showModal    = ref(false);

const clockTime = computed(() => now.value.format('A hh:mm:ss'));
const clockDate = computed(() => now.value.format('YYYY년 M월 D일 dddd'));

const currentBooking = computed(() =>
  todayBookings.value.find(b =>
    now.value.isAfter(dayjs(b.startTime)) && now.value.isBefore(dayjs(b.endTime))
  ) ?? null
);

const nextBooking = computed(() =>
  todayBookings.value
    .filter(b => now.value.isBefore(dayjs(b.startTime)))
    .sort((a, b) => dayjs(a.startTime).diff(dayjs(b.startTime)))[0] ?? null
);

const isInUse = computed(() => !!currentBooking.value);

const timeUntilEnd = computed(() => {
  if (!currentBooking.value) return '00:00';
  const diff = dayjs(currentBooking.value.endTime).diff(now.value, 'second');
  if (diff <= 0) return '00:00';
  const h = Math.floor(diff / 3600);
  const m = Math.floor((diff % 3600) / 60);
  const s = diff % 60;
  if (h > 0) return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
  return `${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
});

const timeUntilNext = computed(() => {
  if (!nextBooking.value) return '';
  const diff = dayjs(nextBooking.value.startTime).diff(now.value, 'minute');
  if (diff < 60) return `${diff}분 후`;
  return `${Math.floor(diff / 60)}시간 ${diff % 60}분 후`;
});

const progressPct = computed(() => {
  if (!currentBooking.value) return 0;
  const total   = dayjs(currentBooking.value.endTime).diff(dayjs(currentBooking.value.startTime), 'second');
  const elapsed = now.value.diff(dayjs(currentBooking.value.startTime), 'second');
  return Math.min(100, Math.max(0, (elapsed / total) * 100));
});

const getState = (booking) => {
  if (now.value.isAfter(dayjs(booking.endTime)))   return 'done';
  if (now.value.isAfter(dayjs(booking.startTime))) return 'ongoing';
  return 'upcoming';
};

const timelineSlots = computed(() => {
  const START = 6, TOTAL = 15 * 60;
  return todayBookings.value.map(booking => {
    const start    = dayjs(booking.startTime);
    const end      = dayjs(booking.endTime);
    const startMin = Math.max(0, start.hour() * 60 + start.minute() - START * 60);
    const endMin   = Math.min(TOTAL, end.hour() * 60 + end.minute() - START * 60);
    return {
      left:  `${((startMin / TOTAL) * 100).toFixed(2)}%`,
      width: `${(((endMin - startMin) / TOTAL) * 100).toFixed(2)}%`,
      state: getState(booking),
    };
  });
});

const nowLineLeft = computed(() => {
  const START = 6, TOTAL = 15 * 60;
  const min = now.value.hour() * 60 + now.value.minute() - START * 60;
  return `${Math.max(0, Math.min(100, (min / TOTAL) * 100)).toFixed(2)}%`;
});

const fetchRoom = async () => {
  try {
    const res = await api.get('/rooms');
    room.value = res.data.find(r => r.id === roomId.value) ?? null;
  } catch (e) { console.error(e); }
};

const fetchBookings = async () => {
  try {
    const today = now.value.format('YYYY-MM-DD');
    const res = await api.get(`/bookings?startDate=${today}&endDate=${today}`);
    todayBookings.value = res.data
      .filter(b => b.roomId === roomId.value)
      .sort((a, b) => dayjs(a.startTime).diff(dayjs(b.startTime)));
  } catch (e) { console.error(e); }
};

const endNow = async () => {
  try {
    await api.patch(`/bookings/${currentBooking.value.id}/end-kiosk`, null, {
      params: { roomId: roomId.value },
    });
    showEndConfirm.value = false;
    fetchBookings();
  } catch (e) { console.error(e); }
};

const exitKiosk = async () => {
  if (document.fullscreenElement) await document.exitFullscreen();
  router.push('/kiosk');
};

const _sse = createSseManager({
  getUrl: () => '/api/sse/subscribe',
  listeners: { booking: () => fetchBookings() },
});

const handleBeforeUnload = (e) => { e.preventDefault(); e.returnValue = ''; };
const handleKeydown = (e) => {
  if (
    e.key === 'F5' || (e.ctrlKey && e.key === 'r') ||
    (e.ctrlKey && e.key === 'w') || (e.ctrlKey && e.key === 'F4') ||
    (e.altKey && e.key === 'F4') || (e.altKey && e.key === 'ArrowLeft')
  ) { e.preventDefault(); e.stopPropagation(); return false; }
};
const handleContextMenu = (e) => e.preventDefault();

let clockTimer = null;
let popstateHandler = null;

onMounted(async () => {
  localStorage.setItem('lastKioskRoom', roomId.value);
  document.documentElement.style.overflow = 'hidden';
  document.body.style.overflow = 'hidden';

  await fetchRoom();
  await fetchBookings();

  clockTimer = setInterval(() => { now.value = dayjs(); }, 1000);
  _sse.connect();

  const enterFullscreenOnce = async () => {
    if (!document.fullscreenElement) {
      try { await document.documentElement.requestFullscreen(); } catch { /* ignore */ }
    }
    window.removeEventListener('pointerdown', enterFullscreenOnce);
  };
  window.addEventListener('pointerdown', enterFullscreenOnce);

  window.addEventListener('beforeunload', handleBeforeUnload);
  window.addEventListener('keydown', handleKeydown, true);
  window.addEventListener('contextmenu', handleContextMenu);

  history.pushState({ ...history.state }, '', location.href);
  popstateHandler = () => { history.pushState({ ...history.state }, '', location.href); };
  window.addEventListener('popstate', popstateHandler);
});

onUnmounted(() => {
  clearInterval(clockTimer);
  _sse.disconnect();
  window.removeEventListener('beforeunload', handleBeforeUnload);
  window.removeEventListener('keydown', handleKeydown, true);
  window.removeEventListener('contextmenu', handleContextMenu);
  if (popstateHandler) window.removeEventListener('popstate', popstateHandler);
  document.documentElement.style.overflow = '';
  document.body.style.overflow = '';
});
</script>

<style scoped>
.kiosk-root {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  color: #0f172a;
  background:
    radial-gradient(circle at top left, rgba(59,130,246,0.08), transparent 22%),
    radial-gradient(circle at top right, rgba(16,185,129,0.08), transparent 24%),
    linear-gradient(180deg, #f8fbff 0%, #eef4f9 100%);
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, sans-serif;
}

.kiosk-body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(420px, 0.82fr);
  gap: 18px;
  padding: 18px;
  overflow: hidden;
}

@media (max-width: 1280px) {
  .kiosk-body {
    grid-template-columns: minmax(0, 1fr) minmax(360px, 0.72fr);
  }
}
</style>
