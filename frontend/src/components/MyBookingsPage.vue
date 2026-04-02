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
        <button @click="refresh" :disabled="refreshing"
                class="ml-auto w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors disabled:opacity-40">
          <svg :class="refreshing ? 'animate-spin' : ''" width="15" height="15" viewBox="0 0 18 18" fill="none">
            <path d="M15.5 9A6.5 6.5 0 1 1 9 2.5c2 0 3.8.9 5 2.3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            <path d="M14 2l.5 3-3-.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
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

      <!-- 상세 모달 -->
      <transition name="modal">
        <div v-if="detailBooking"
             class="fixed inset-0 z-50 flex items-center justify-center p-4"
             @click.self="detailBooking = null">

          <!-- 오버레이 -->
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="detailBooking = null"></div>

          <!-- 모달 본체 -->
          <div class="relative w-full max-w-[420px] bg-white dark:bg-gray-900 rounded-2xl shadow-2xl flex flex-col overflow-hidden max-h-[85vh]">

            <!-- 상단 컬러 바 + 제목 -->
            <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100 dark:border-gray-800 flex-shrink-0">
              <div class="flex items-center gap-2.5 min-w-0">
                <span class="w-3 h-3 rounded-full flex-shrink-0" :style="{ background: getRoomColor(detailBooking.roomId) }"></span>
                <p class="text-[15px] font-bold text-gray-800 dark:text-gray-100 truncate">{{ detailBooking.title }}</p>
              </div>
              <button @click="detailBooking = null"
                      class="w-8 h-8 flex items-center justify-center rounded-lg text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors flex-shrink-0 ml-2">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M2 2l10 10M12 2L2 12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
              </button>
            </div>

            <!-- 내용 -->
            <div class="overflow-y-auto px-5 py-5 flex flex-col gap-5 custom-scrollbar">

              <!-- 회의실 + 시간 -->
              <div class="grid grid-cols-2 gap-3">
                <div class="flex items-center gap-3 px-4 py-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                  <svg width="14" height="14" viewBox="0 0 16 16" fill="none" class="text-gray-400 flex-shrink-0">
                    <rect x="1" y="2" width="14" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M5 1v3M11 1v3M1 7h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <div class="min-w-0">
                    <p class="text-[10px] text-gray-400 dark:text-gray-500">회의실</p>
                    <p class="text-[12.5px] font-bold text-gray-800 dark:text-gray-100 truncate">{{ getRoomName(detailBooking.roomId) }}</p>
                  </div>
                </div>
                <div class="flex items-center gap-3 px-4 py-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                  <svg width="14" height="14" viewBox="0 0 16 16" fill="none" class="text-gray-400 flex-shrink-0">
                    <circle cx="8" cy="8" r="6.5" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M8 4.5v4l2.5 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <div class="min-w-0">
                    <p class="text-[10px] text-gray-400 dark:text-gray-500">시간</p>
                    <p class="text-[12.5px] font-bold text-gray-800 dark:text-gray-100 tabular-nums">
                      {{ dayjs(detailBooking.startTime).format('HH:mm') }} – {{ dayjs(detailBooking.endTime).format('HH:mm') }}
                    </p>
                  </div>
                </div>
              </div>

              <!-- 날짜 -->
              <div class="flex items-center gap-2 px-4 py-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                <svg width="13" height="13" viewBox="0 0 16 16" fill="none" class="text-gray-400 flex-shrink-0">
                  <rect x="1" y="2" width="14" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M5 1v3M11 1v3M1 7h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <p class="text-[12.5px] font-semibold text-gray-700 dark:text-gray-300 tabular-nums">
                  {{ dayjs(detailBooking.startTime).format('YYYY년 MM월 DD일 (dd)') }}
                </p>
              </div>

              <!-- 주최자 -->
              <div>
                <p class="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider mb-2">주최자</p>
                <div class="flex items-center gap-2.5 px-4 py-3 bg-blue-50 dark:bg-blue-900/20 rounded-xl">
                  <div class="w-7 h-7 rounded-full bg-blue-200 dark:bg-blue-800 flex items-center justify-center flex-shrink-0">
                    <svg width="11" height="11" viewBox="0 0 15 15" fill="none">
                      <circle cx="7.5" cy="5" r="3" stroke="#3b82f6" stroke-width="1.5"/>
                      <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#3b82f6" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <p class="text-[13px] font-semibold text-blue-700 dark:text-blue-300">{{ detailBooking.organizer }}</p>
                </div>
              </div>

              <!-- 참석자 -->
              <div v-if="detailBooking.attendeeIds?.length || detailBooking.externalAttendeeNames?.length">
                <p class="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider mb-2">
                  참석자 <span class="text-gray-300 dark:text-gray-600 font-normal normal-case">{{ (detailBooking.attendeeIds?.length || 0) + (detailBooking.externalAttendeeNames?.length || 0) }}명</span>
                </p>
                <div class="flex flex-col gap-1.5">
                  <div v-for="id in (detailBooking.attendeeIds || [])" :key="id"
                       class="flex items-center gap-2.5 px-4 py-2.5 bg-gray-50 dark:bg-gray-800 rounded-xl">
                    <div class="w-6 h-6 rounded-full bg-indigo-100 dark:bg-indigo-900/40 flex items-center justify-center flex-shrink-0">
                      <svg width="10" height="10" viewBox="0 0 15 15" fill="none">
                        <circle cx="7.5" cy="5" r="3" stroke="#6366f1" stroke-width="1.5"/>
                        <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                    </div>
                    <p class="text-[12.5px] font-medium text-gray-700 dark:text-gray-300">{{ resolveAttendees([id]) }}</p>
                  </div>
                  <div v-for="name in (detailBooking.externalAttendeeNames || [])" :key="name"
                       class="flex items-center gap-2.5 px-4 py-2.5 bg-gray-50 dark:bg-gray-800 rounded-xl">
                    <div class="w-6 h-6 rounded-full bg-amber-100 dark:bg-amber-900/40 flex items-center justify-center flex-shrink-0">
                      <svg width="10" height="10" viewBox="0 0 15 15" fill="none">
                        <circle cx="7.5" cy="5" r="3" stroke="#f59e0b" stroke-width="1.5"/>
                        <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#f59e0b" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                    </div>
                    <p class="text-[12.5px] font-medium text-gray-700 dark:text-gray-300">{{ name }}</p>
                    <span class="ml-auto text-[10px] text-amber-500 font-semibold bg-amber-50 dark:bg-amber-900/20 px-1.5 py-0.5 rounded-full">미가입</span>
                  </div>
                </div>
              </div>

              <!-- 메모 -->
              <div v-if="detailBooking.description">
                <p class="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider mb-2">메모</p>
                <div class="px-4 py-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                  <p class="text-[13px] text-gray-600 dark:text-gray-300 leading-relaxed whitespace-pre-wrap">{{ detailBooking.description }}</p>
                </div>
              </div>

            </div>

            <!-- 하단 액션 -->
            <div v-if="canEditOrCancel(detailBooking)" class="px-5 py-4 border-t border-gray-100 dark:border-gray-800 flex gap-2 flex-shrink-0">
              <button @click="openEditModal(detailBooking); detailBooking = null; router.push('/main')"
                      class="flex-1 py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[13px] font-bold transition-colors">
                수정
              </button>
              <button @click="confirmCancel(detailBooking); detailBooking = null"
                      class="flex-1 py-2.5 rounded-xl bg-red-50 dark:bg-red-900/20 hover:bg-red-100 text-red-500 text-[13px] font-bold transition-colors">
                예약 취소
              </button>
            </div>

          </div>
        </div>
      </transition>

      <main class="flex-1 overflow-y-auto custom-scrollbar" style="padding-bottom: calc(8rem + env(safe-area-inset-bottom, 0px))">

        <!-- 로딩 스켈레톤 -->
        <div v-if="isLoadingMyBookings && myBookings.length === 0" class="px-4 py-4">
          <SkeletonBookingList :count="4" />
        </div>

        <!-- 빈 상태 -->
        <div v-else-if="myBookings.length === 0"
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
              <span class="text-[10px] text-gray-400 dark:text-gray-600 font-medium ml-1">{{ section.dateRange }}</span>
            </div>

            <!-- 행 -->
            <div v-for="b in section.list" :key="b.id"
                 class="group flex items-center border-b border-gray-100 dark:border-gray-800 hover:bg-white dark:hover:bg-gray-900 transition-colors cursor-pointer"
                 :class="[section.past ? 'opacity-60' : '', detailBooking?.id === b.id ? 'bg-indigo-50/50 dark:bg-indigo-900/10' : '']"
                 @click="detailBooking = detailBooking?.id === b.id ? null : b">

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
                  <button @click.stop="openEditModal(b); router.push('/main')"
                          class="w-7 h-7 flex items-center justify-center rounded-lg bg-blue-50 dark:bg-blue-900/30 text-blue-500 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors">
                    <svg width="12" height="12" viewBox="0 0 14 14" fill="none">
                      <path d="M9.5 2.5l2 2L4 12H2v-2L9.5 2.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
                    </svg>
                  </button>
                  <button @click.stop="confirmCancel(b)"
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
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { useApp } from '../composables/useApp';
import AppSidebar from './layout/AppSidebar.vue';
import CancelModal from './booking/CancelModal.vue';
import MobileDrawer from './layout/MobileDrawer.vue';
import ToastContainer from './layout/ToastContainer.vue';
import SkeletonBookingList from './calendar/SkeletonBookingList.vue';
import api from '../api';

const router = useRouter();

const {
  isMobile, showDrawer,
  myBookings, myBookingsToday, myBookingsThisWeek, myBookingsUpcoming,
  getRoomColor, getRoomName, resolveAttendees, canEditOrCancel,
  openEditModal, confirmCancel, currentUser,
  fetchMyBookings, fetchRooms,
  connectSse, disconnectSse, applyNotifPrefs,
  isLoadingMyBookings,
} = useApp();

const detailBooking = ref(null);
const refreshing    = ref(false);

const refresh = async () => {
  refreshing.value = true;
  await fetchMyBookings();
  refreshing.value = false;
};

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
  { label: '오늘 회의',  color: '#3b82f6', list: myBookingsToday.value,   fmt: fmtToday, past: false,
    dateRange: dayjs().format('M/D (ddd)') },
  { label: '이번 주',   color: '#8b5cf6', list: myBookingsThisWeek.value, fmt: fmtRange, past: false,
    dateRange: `${dayjs().startOf('week').format('M/D')} – ${dayjs().endOf('week').format('M/D')}` },
  { label: '이후 일정', color: '#10b981', list: myBookingsUpcoming.value, fmt: fmtRange, past: false,
    dateRange: `${dayjs().endOf('week').add(1, 'day').format('M/D')} ~` },
  { label: '지난 회의', color: '#94a3b8', list: myBookingsPast.value,     fmt: fmtRange, past: true,
    dateRange: `~ ${dayjs().subtract(1, 'day').format('M/D')}` },
]);

const stats = computed(() => [
  { label: '전체',    count: myBookings.value.length,        color: '#374151', active: false },
  { label: '오늘',    count: myBookingsToday.value.length,    color: '#3b82f6', active: myBookingsToday.value.length > 0 },
  { label: '이번 주', count: myBookingsThisWeek.value.length, color: '#8b5cf6', active: myBookingsThisWeek.value.length > 0 },
  { label: '이후',    count: myBookingsUpcoming.value.length, color: '#10b981', active: myBookingsUpcoming.value.length > 0 },
  { label: '지난',    count: myBookingsPast.value.length,     color: '#94a3b8', active: false },
]);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.25s ease; }
.slide-right-enter-from, .slide-right-leave-to       { transform: translateX(100%); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
