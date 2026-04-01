<template>
  <div class="flex h-screen bg-[#F5F7FA] dark:bg-gray-950 font-sans text-gray-900 dark:text-gray-50 overflow-hidden">

    <AppSidebar v-if="!isMobile" />

    <div class="flex-1 flex flex-col overflow-hidden min-w-0">

      <!-- 헤더 -->
      <header class="h-[56px] flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-center px-4 gap-3">
        <button v-if="isMobile" @click="showDrawer = true"
                class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-500 dark:text-gray-400 transition-colors">
          <svg width="17" height="17" viewBox="0 0 18 18" fill="none">
            <path d="M2 4.5h14M2 9h14M2 13.5h14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </button>
        <button v-else @click="router.push('/main')"
                class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
          <svg width="16" height="16" viewBox="0 0 18 18" fill="none">
            <path d="M11 4L7 9l4 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <h1 class="text-[15px] font-semibold text-gray-800 dark:text-gray-100">내 예약</h1>
        <span class="text-[13px] text-gray-400 dark:text-gray-500 font-medium">{{ myBookings.length }}건</span>
      </header>

      <!-- 상단 통계 바 -->
      <div class="flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 px-5 py-3 flex items-center gap-6 overflow-x-auto">
        <div v-for="stat in stats" :key="stat.label" class="flex items-center gap-2.5 flex-shrink-0">
          <span class="w-1.5 h-7 rounded-full flex-shrink-0" :style="{ background: stat.color }"></span>
          <div>
            <p class="text-[20px] font-bold leading-none tabular-nums"
               :style="stat.active ? { color: stat.color } : {}"
               :class="!stat.active ? 'text-gray-800 dark:text-gray-100' : ''">{{ stat.count }}</p>
            <p class="text-[10.5px] text-gray-400 dark:text-gray-500 mt-0.5">{{ stat.label }}</p>
          </div>
        </div>
      </div>

      <main class="flex-1 overflow-y-auto custom-scrollbar">

        <!-- 빈 상태 -->
        <div v-if="myBookings.length === 0"
             class="flex flex-col items-center justify-center h-full gap-4">
          <div class="w-16 h-16 rounded-2xl bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
            <svg width="26" height="26" viewBox="0 0 18 18" fill="none">
              <rect x="1.5" y="3.5" width="15" height="13" rx="2" stroke="#cbd5e1" stroke-width="1.6"/>
              <path d="M5.5 1.5v4M12.5 1.5v4M1.5 8h15" stroke="#cbd5e1" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </div>
          <p class="text-[14px] font-semibold text-gray-400 dark:text-gray-500">예약이 없습니다</p>
        </div>

        <!-- 테이블 목록 -->
        <template v-for="section in sections" :key="section.label">
          <template v-if="section.list.length > 0">

            <!-- 섹션 헤더 -->
            <div class="sticky top-0 z-10 bg-[#F5F7FA] dark:bg-gray-950 px-5 py-2 flex items-center gap-2 border-b border-gray-200 dark:border-gray-700">
              <span class="w-1.5 h-1.5 rounded-full" :style="{ background: section.color }"></span>
              <p class="text-[11px] font-bold uppercase tracking-widest" :style="{ color: section.color }">{{ section.label }}</p>
              <span class="text-[10.5px] font-bold text-white px-1.5 py-0.5 rounded-full" :style="{ background: section.color }">{{ section.list.length }}</span>
            </div>

            <!-- 행 -->
            <div v-for="b in section.list" :key="b.id"
                 class="group flex items-center border-b border-gray-100 dark:border-gray-800 hover:bg-white dark:hover:bg-gray-900 transition-colors"
                 :class="section.past ? 'opacity-60' : ''">

              <!-- 컬러 바 -->
              <div class="w-1 self-stretch flex-shrink-0" :style="{ background: getRoomColor(b.roomId) }"></div>

              <!-- 회의실 (모바일에선 숨김) -->
              <div class="hidden sm:block w-[130px] flex-shrink-0 px-4 py-3.5">
                <span class="text-[10.5px] font-bold px-2 py-0.5 rounded-full text-white"
                      :style="{ background: getRoomColor(b.roomId) }">
                  {{ getRoomName(b.roomId) }}
                </span>
              </div>

              <!-- 제목 + 모바일 서브정보 -->
              <div class="flex-1 min-w-0 px-4 py-3.5">
                <div class="flex items-center gap-1.5 mb-1 sm:hidden flex-wrap">
                  <span class="text-[10px] font-bold px-1.5 py-0.5 rounded-full text-white"
                        :style="{ background: getRoomColor(b.roomId) }">
                    {{ getRoomName(b.roomId) }}
                  </span>
                </div>
                <p class="text-[13.5px] font-semibold text-gray-800 dark:text-gray-100 truncate">{{ b.title }}</p>
                <p class="text-[12px] text-gray-500 dark:text-gray-400 mt-0.5 tabular-nums">{{ section.fmt(b) }}</p>
              </div>

              <!-- 주최자 (모바일 숨김) -->
              <div class="hidden sm:flex w-[120px] flex-shrink-0 px-4 py-3.5 items-center gap-1.5">
                <svg width="10" height="10" viewBox="0 0 15 15" fill="none" class="text-gray-300 dark:text-gray-600 flex-shrink-0">
                  <circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <p class="text-[12px] text-gray-500 dark:text-gray-400 truncate">{{ b.organizer }}</p>
              </div>

              <!-- 역할 배지 -->
              <div class="w-[76px] flex-shrink-0 px-3 py-3.5">
                <span v-if="!section.past"
                      :class="b.userId === currentUser.id
                        ? 'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400'
                        : 'bg-amber-50 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400'"
                      class="text-[10.5px] font-semibold px-2 py-0.5 rounded-full whitespace-nowrap">
                  {{ b.userId === currentUser.id ? '예약자' : '참석자' }}
                </span>
                <span v-else class="text-[10.5px] font-semibold px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-500">완료</span>
              </div>

              <!-- 액션 -->
              <div class="w-[72px] flex-shrink-0 px-3 py-3.5 flex gap-1.5 opacity-0 group-hover:opacity-100 transition-opacity">
                <template v-if="!section.past && canEditOrCancel(b)">
                  <button @click="openEditModal(b); router.push('/main')"
                          class="w-7 h-7 flex items-center justify-center rounded-lg bg-blue-50 dark:bg-blue-900/30 text-blue-500 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors">
                    <svg width="12" height="12" viewBox="0 0 14 14" fill="none">
                      <path d="M9.5 2.5l2 2L4 12H2v-2L9.5 2.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
                    </svg>
                  </button>
                  <button @click="confirmCancel(b)"
                          class="w-7 h-7 flex items-center justify-center rounded-lg bg-red-50 dark:bg-red-900/30 text-red-400 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/50 transition-colors">
                    <svg width="12" height="12" viewBox="0 0 14 14" fill="none">
                      <path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v4M8.5 6.5v4M3.5 4l.5 8h6l.5-8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </template>
              </div>

            </div>

          </template>
        </template>

      </main>
    </div>

    <CancelModal />
    <MobileDrawer />
    <ToastContainer />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { useApp } from '../composables/useApp';
import AppSidebar from './layout/AppSidebar.vue';
import CancelModal from './booking/CancelModal.vue';
import MobileDrawer from './layout/MobileDrawer.vue';
import ToastContainer from './layout/ToastContainer.vue';
import api from '../api';

const router = useRouter();

const {
  isMobile, showDrawer,
  myBookings, myBookingsToday, myBookingsThisWeek, myBookingsUpcoming,
  getRoomColor, getRoomName, resolveAttendees, canEditOrCancel,
  openEditModal, confirmCancel, currentUser,
  fetchMyBookings, fetchRooms,
  connectSse, disconnectSse, applyNotifPrefs,
} = useApp();

onMounted(async () => {
  await fetchRooms();
  await fetchMyBookings();
  connectSse();
  api.get('/users/notification-preference').then(res => applyNotifPrefs(res.data)).catch(() => {});
});

onUnmounted(() => { disconnectSse(); });

const myBookingsPast = computed(() =>
  myBookings.value.filter(b => dayjs(b.endTime).isBefore(dayjs()))
);

const fmtRange = b => `${dayjs(b.startTime).format('MM/DD (dd) HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}`;
const fmtToday = b => `${dayjs(b.startTime).format('HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}`;

const sections = computed(() => [
  { label: '오늘 회의',  color: '#3b82f6', list: myBookingsToday.value,   fmt: fmtToday, past: false },
  { label: '이번 주',   color: '#8b5cf6', list: myBookingsThisWeek.value, fmt: fmtRange, past: false },
  { label: '이후 일정', color: '#64748b', list: myBookingsUpcoming.value, fmt: fmtRange, past: false },
  { label: '지난 회의', color: '#94a3b8', list: myBookingsPast.value,     fmt: fmtRange, past: true  },
]);

const stats = computed(() => [
  { label: '전체',    count: myBookings.value.length,        color: '#374151', active: false },
  { label: '오늘',    count: myBookingsToday.value.length,   color: '#3b82f6', active: myBookingsToday.value.length > 0 },
  { label: '이번 주', count: myBookingsThisWeek.value.length, color: '#8b5cf6', active: myBookingsThisWeek.value.length > 0 },
  { label: '지난',    count: myBookingsPast.value.length,    color: '#94a3b8', active: false },
]);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }
</style>
