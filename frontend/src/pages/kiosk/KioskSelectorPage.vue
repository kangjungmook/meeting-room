<template>
  <div class="h-screen overflow-hidden flex flex-col items-center justify-center p-10 font-sans" :style="{ background: isDark ? '#0f172a' : '#f1f5f9' }">


    <!-- 로고 -->
    <div class="flex items-center gap-4 mb-16">
      <div>
        <p class="text-3xl font-black tracking-widest" :style="{ color: isDark ? 'white' : '#1e293b' }">회의실 예약 시스템</p>
        <p class="text-base mt-0.5" :style="{ color: isDark ? '#64748b' : '#94a3b8' }">키오스크 모드</p>
      </div>
    </div>

    <h1 class="text-2xl font-bold mb-10" :style="{ color: isDark ? '#cbd5e1' : '#475569' }">사용할 회의실을 선택하세요</h1>

    <div v-if="rooms.length === 0" class="text-lg" :style="{ color: isDark ? '#64748b' : '#94a3b8' }">불러오는 중...</div>

    <div class="grid grid-cols-1 gap-4 w-full max-w-lg mb-2">
      <button v-for="room in rooms" :key="room.id"
        @click="enterKiosk(room.id)"
        class="flex items-center gap-6 px-8 py-6 rounded-3xl border transition-all active:scale-95 text-left"
        :style="getRoomStatus(room.id).inUse
          ? { background: isDark ? 'rgba(69,10,10,0.5)' : '#fee2e2', borderColor: isDark ? 'rgba(153,27,27,0.5)' : '#fca5a5' }
          : { background: isDark ? '#1e293b' : '#ffffff', borderColor: isDark ? '#334155' : '#e2e8f0' }">

        <!-- 색상 도트 -->
        <span class="w-5 h-5 rounded-full flex-shrink-0" :style="{background: room.colorCode}"></span>

        <!-- 회의실 정보 -->
        <div class="flex-1">
          <p class="text-2xl font-black" :style="{ color: isDark ? 'white' : '#1e293b' }">{{ room.name }}</p>
          <p class="text-base mt-1" :style="{ color: getRoomStatus(room.id).inUse ? '#f87171' : (isDark ? '#94a3b8' : '#64748b') }">
            최대 {{ room.capacity }}인
          </p>
          <p v-if="getRoomStatus(room.id).inUse" class="text-sm mt-1 font-semibold text-red-400">
            {{ getRoomStatus(room.id).title }} · {{ getRoomStatus(room.id).until }} 까지
          </p>
          <p v-else-if="getRoomStatus(room.id).next" class="text-sm mt-1" :style="{ color: isDark ? '#64748b' : '#94a3b8' }">
            다음 예약 {{ getRoomStatus(room.id).next }}
          </p>
        </div>

        <!-- 상태 배지 -->
        <div class="flex flex-col items-end gap-2">
          <span class="flex items-center gap-2 px-3 py-1.5 rounded-xl text-sm font-bold"
            :style="getRoomStatus(room.id).inUse
              ? { background: isDark ? 'rgba(239,68,68,0.2)' : '#fecaca', color: isDark ? '#fca5a5' : '#dc2626' }
              : { background: isDark ? 'rgba(16,185,129,0.2)' : '#d1fae5', color: isDark ? '#6ee7b7' : '#059669' }">
            <span class="w-2 h-2 rounded-full animate-pulse"
              :style="{ background: getRoomStatus(room.id).inUse ? (isDark ? '#f87171' : '#ef4444') : (isDark ? '#34d399' : '#10b981') }"></span>
            {{ getRoomStatus(room.id).inUse ? '사용 중' : '사용 가능' }}
          </span>
          <AppIcon name="chevron-right" :size="18" :style="{ color: isDark ? '#475569' : '#94a3b8' }" />
        </div>
      </button>
    </div>

    <!-- 일반 로그인 링크 -->
    <div class="mt-10">
      <button @click="router.push('/')"
        class="inline-flex items-center gap-1.5 text-[13px] transition-colors"
        :style="{ color: isDark ? '#475569' : '#94a3b8' }"
        @mouseover="e => e.currentTarget.style.color = isDark ? '#94a3b8' : '#64748b'"
        @mouseleave="e => e.currentTarget.style.color = isDark ? '#475569' : '#94a3b8'">
        <AppIcon name="chevron-left" :size="13" />
        일반 로그인으로 돌아가기
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import 'dayjs/locale/ko';
import api from '../../api';
import AppIcon from '../../components/icons/AppIcon.vue';

dayjs.locale('ko');

const router = useRouter();
const rooms  = ref([]);
const isDark = ref(localStorage.getItem('theme') === 'dark');
const bookings = ref([]);
const now      = ref(dayjs());

const fetchRooms = async () => {
  try {
    const res = await api.get('/rooms');
    rooms.value = res.data.filter(r => r.isActive);
  } catch (e) { console.error(e); }
};

const fetchBookings = async () => {
  try {
    const today = now.value.format('YYYY-MM-DD');
    const res = await api.get(`/bookings?startDate=${today}&endDate=${today}`);
    bookings.value = res.data;
  } catch (e) { console.error(e); }
};

const getRoomStatus = (roomId) => {
  const roomBookings = bookings.value.filter(b => b.roomId === roomId);

  // 현재 진행 중인 예약
  const current = roomBookings.find(b =>
    now.value.isAfter(dayjs(b.startTime)) && now.value.isBefore(dayjs(b.endTime))
  );

  if (current) {
    return {
      inUse: true,
      title: current.title,
      until: dayjs(current.endTime).format('HH:mm'),
    };
  }

  // 다음 예약
  const next = roomBookings
    .filter(b => now.value.isBefore(dayjs(b.startTime)))
    .sort((a, b) => dayjs(a.startTime).diff(dayjs(b.startTime)))[0];

  return {
    inUse: false,
    next: next ? dayjs(next.startTime).format('HH:mm') : null,
  };
};

const enterKiosk = (roomId) => {
  router.push(`/kiosk/${roomId}`);
};

const handleFullscreenChange = async () => {
  if (!document.fullscreenElement) {
    try { await document.documentElement.requestFullscreen(); } catch { /* ignore */ }
  }
};

let clockTimer = null;
let refreshTimer = null;

onMounted(async () => {
  document.documentElement.style.overflow = 'hidden';
  document.body.style.overflow = 'hidden';
  if (!document.fullscreenElement) {
    try { await document.documentElement.requestFullscreen(); } catch { /* ignore */ }
  }
  document.addEventListener('fullscreenchange', handleFullscreenChange);
  await fetchRooms();
  await fetchBookings();
  clockTimer   = setInterval(() => { now.value = dayjs(); }, 1000);
  refreshTimer = setInterval(fetchBookings, 60000);
});

onUnmounted(() => {
  clearInterval(clockTimer);
  clearInterval(refreshTimer);
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
  document.documentElement.style.overflow = '';
  document.body.style.overflow = '';
});
</script>
