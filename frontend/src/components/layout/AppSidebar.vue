<template>
  <aside
    class="flex-shrink-0 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-700 flex flex-col transition-[width] duration-200 overflow-visible z-10"
    :style="sidebarCollapsed ? 'width:60px' : 'width:232px'"
  >

    <!-- ── 접힌 상태 ── -->
    <div v-if="sidebarCollapsed" class="h-full flex flex-col items-center py-4 gap-3">
      <!-- 로고 아이콘 -->
      <div class="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0 shadow-sm">
        <svg width="17" height="17" viewBox="0 0 18 18" fill="none">
          <rect x="1" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
          <rect x="10" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
          <rect x="1" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
          <rect x="10" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
        </svg>
      </div>
      <!-- 펼치기 -->
      <button @click="sidebarCollapsed = false"
              class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path d="M4 3l5 4-5 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <!-- 내비게이션 아이콘 -->
      <div class="flex flex-col gap-1">
        <div class="relative group/tip">
          <button @click="router.push('/main')"
                  :class="['w-9 h-9 flex items-center justify-center rounded-lg transition-colors',
                    route.path === '/main'
                      ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400'
                      : 'text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800']">
            <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
              <rect x="1.5" y="3.5" width="15" height="13" rx="2" stroke="currentColor" stroke-width="1.6"/>
              <path d="M5.5 1.5v4M12.5 1.5v4M1.5 8h15" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </button>
          <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-2.5 z-50 opacity-0 group-hover/tip:opacity-100 transition-opacity duration-150">
            <div class="bg-gray-900 dark:bg-gray-700 text-white text-[12px] font-semibold px-2.5 py-1.5 rounded-lg shadow-lg whitespace-nowrap">예약 현황</div>
          </div>
        </div>
        <div class="relative group/tip">
          <button @click="router.push('/my-bookings')"
                  :class="['w-9 h-9 flex items-center justify-center rounded-lg transition-colors',
                    route.path === '/my-bookings'
                      ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400'
                      : 'text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800']">
            <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
              <circle cx="9" cy="6" r="3.5" stroke="currentColor" stroke-width="1.6"/>
              <path d="M2 16c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </button>
          <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-2.5 z-50 opacity-0 group-hover/tip:opacity-100 transition-opacity duration-150">
            <div class="bg-gray-900 dark:bg-gray-700 text-white text-[12px] font-semibold px-2.5 py-1.5 rounded-lg shadow-lg whitespace-nowrap">내 회의</div>
          </div>
        </div>
        <div class="relative group/tip">
          <button @click="router.push('/settings')"
                  :class="['w-9 h-9 flex items-center justify-center rounded-lg transition-colors',
                    route.path === '/settings'
                      ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400'
                      : 'text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800']">
            <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
              <path d="M9 11.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M14.5 9c0-.4 0-.8-.1-1.2l1.7-1.3-1.5-2.6-2 .8a6 6 0 0 0-2.1-1.2L10 1.5H7.5l-.5 2A6 6 0 0 0 4.9 4.7l-2-.8L1.4 6.5l1.7 1.3A6 6 0 0 0 3 9c0 .4 0 .8.1 1.2L1.4 11.5l1.5 2.6 2-.8a6 6 0 0 0 2.1 1.2l.5 2H10l.5-2a6 6 0 0 0 2.1-1.2l2 .8 1.5-2.6-1.7-1.3c.1-.4.1-.8.1-1.2z" stroke="currentColor" stroke-width="1.5"/>
            </svg>
          </button>
          <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-2.5 z-50 opacity-0 group-hover/tip:opacity-100 transition-opacity duration-150">
            <div class="bg-gray-900 dark:bg-gray-700 text-white text-[12px] font-semibold px-2.5 py-1.5 rounded-lg shadow-lg whitespace-nowrap">설정</div>
          </div>
        </div>
      </div>

      <!-- 새 예약 버튼 -->
      <button @click="openNewBooking"
              class="w-9 h-9 flex items-center justify-center rounded-xl bg-blue-600 hover:bg-blue-700 text-white transition-colors shadow-sm">
        <svg width="15" height="15" viewBox="0 0 16 16" fill="none">
          <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
        </svg>
      </button>

      <!-- 다크 모드 토글 (접힌 상태) -->
      <button @click="toggleDarkMode"
              class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors" :title="darkMode ? '라이트 모드' : '다크 모드'">
        <svg v-if="darkMode" width="15" height="15" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="4" stroke="currentColor" stroke-width="1.6"/><path d="M9 1v2M9 15v2M1 9h2M15 9h2M3.5 3.5l1.5 1.5M13 13l1.5 1.5M3.5 14.5l1.5-1.5M13 5l1.5-1.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
        <svg v-else width="15" height="15" viewBox="0 0 18 18" fill="none"><path d="M15 10.5A7 7 0 0 1 7.5 3a7 7 0 0 0 7.5 7.5z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <!-- 설정 -->
      <button @click="showUserSettings = true"
              class="mt-auto w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors" title="설정">
        <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
          <path d="M9 11.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" stroke="currentColor" stroke-width="1.5"/>
          <path d="M14.5 9c0-.4 0-.8-.1-1.2l1.7-1.3-1.5-2.6-2 .8a6 6 0 0 0-2.1-1.2L10 1.5H7.5l-.5 2A6 6 0 0 0 4.9 4.7l-2-.8L1.4 6.5l1.7 1.3A6 6 0 0 0 3 9c0 .4 0 .8.1 1.2L1.4 11.5l1.5 2.6 2-.8a6 6 0 0 0 2.1 1.2l.5 2H10l.5-2a6 6 0 0 0 2.1-1.2l2 .8 1.5-2.6-1.7-1.3c.1-.4.1-.8.1-1.2z" stroke="currentColor" stroke-width="1.5"/>
        </svg>
      </button>
    </div>

    <!-- ── 펼친 상태 ── -->
    <div v-else class="h-full flex flex-col overflow-hidden">

      <!-- 헤더: 로고 + 접기 -->
      <div class="flex-shrink-0 flex items-center justify-between px-4 py-4 border-b border-gray-100 dark:border-gray-800">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center flex-shrink-0 shadow-sm">
            <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
              <rect x="1" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
              <rect x="10" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
              <rect x="1" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
              <rect x="10" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
            </svg>
          </div>
          <span class="text-[13.5px] font-bold text-gray-900 dark:text-gray-50 whitespace-nowrap">회의실 예약</span>
        </div>
        <button @click="sidebarCollapsed = true"
                class="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
          <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
            <path d="M10 3L5 7l5 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>

      <!-- 새 예약 버튼 -->
      <div class="flex-shrink-0 px-3 py-3">
        <button @click="openNewBooking"
                class="flex items-center justify-center gap-2 w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 active:scale-[0.98] text-white text-[13px] font-semibold transition-all shadow-sm">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="none">
            <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="2.3" stroke-linecap="round"/>
          </svg>
          새 예약
        </button>
      </div>

      <!-- 내비게이션 -->
      <nav class="flex-1 px-2 flex flex-col gap-0.5">
        <button @click="router.push('/main')"
                :class="[
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-[13px] text-left w-full transition-colors',
                  route.path === '/main'
                    ? 'font-semibold text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                    : 'font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100'
                ]">
          <svg :class="route.path === '/main' ? 'text-blue-600' : ''" class="flex-shrink-0" width="15" height="15" viewBox="0 0 18 18" fill="none">
            <rect x="1.5" y="3.5" width="15" height="13" rx="2" stroke="currentColor" stroke-width="1.6"/>
            <path d="M5.5 1.5v4M12.5 1.5v4M1.5 8h15" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>
          전체 예약 현황
        </button>
        <button @click="router.push('/my-bookings')"
                :class="[
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-[13px] text-left w-full transition-colors',
                  route.path === '/my-bookings'
                    ? 'font-semibold text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                    : 'font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100'
                ]">
          <svg :class="route.path === '/my-bookings' ? 'text-blue-600' : ''" class="flex-shrink-0" width="15" height="15" viewBox="0 0 18 18" fill="none">
            <circle cx="9" cy="6" r="3.5" stroke="currentColor" stroke-width="1.6"/>
            <path d="M2 16c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>
          내 회의
        </button>
        <button @click="router.push('/settings')"
                :class="[
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-[13px] text-left w-full transition-colors',
                  route.path === '/settings'
                    ? 'font-semibold text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                    : 'font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100'
                ]">
          <svg :class="route.path === '/settings' ? 'text-blue-600' : ''" class="flex-shrink-0" width="15" height="15" viewBox="0 0 18 18" fill="none">
            <path d="M9 11.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M14.5 9c0-.4 0-.8-.1-1.2l1.7-1.3-1.5-2.6-2 .8a6 6 0 0 0-2.1-1.2L10 1.5H7.5l-.5 2A6 6 0 0 0 4.9 4.7l-2-.8L1.4 6.5l1.7 1.3A6 6 0 0 0 3 9c0 .4 0 .8.1 1.2L1.4 11.5l1.5 2.6 2-.8a6 6 0 0 0 2.1 1.2l.5 2H10l.5-2a6 6 0 0 0 2.1-1.2l2 .8 1.5-2.6-1.7-1.3c.1-.4.1-.8.1-1.2z" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          설정
        </button>
      </nav>

      <!-- 하단 현황 -->
      <div class="flex-shrink-0 px-3 py-3 border-t border-gray-100 dark:border-gray-800">
        <div class="grid grid-cols-2 gap-1.5 mb-1.5">
          <div class="rounded-xl bg-gray-50 dark:bg-gray-800 px-3 py-2.5">
            <p class="text-[20px] font-bold text-gray-900 dark:text-gray-50 leading-none tabular-nums">{{ todayBookingCount }}</p>
            <p class="text-[10px] text-gray-400 dark:text-gray-500 mt-1 leading-none">오늘 예약</p>
          </div>
          <div class="rounded-xl bg-gray-50 dark:bg-gray-800 px-3 py-2.5">
            <p class="text-[20px] font-bold leading-none tabular-nums"
               :class="currentlyUsedCount > 0 ? 'text-green-600' : 'text-gray-900 dark:text-gray-50'">
              {{ currentlyUsedCount }}
            </p>
            <p class="text-[10px] text-gray-400 dark:text-gray-500 mt-1 leading-none">사용 중</p>
          </div>
        </div>
        <div class="rounded-xl bg-gray-50 dark:bg-gray-800 px-3 py-2.5 flex items-center gap-3">
          <div>
            <p class="text-[21px] font-bold text-gray-900 dark:text-gray-50 leading-none tracking-tight tabular-nums">{{ liveTime }}</p>
            <p class="text-[10px] text-gray-400 dark:text-gray-500 mt-1 leading-none">{{ liveDate }}</p>
          </div>
          <button @click="toggleDarkMode"
                  class="ml-auto flex items-center justify-center w-7 h-7 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-400 dark:text-gray-500 transition-colors flex-shrink-0"
                  :title="darkMode ? '라이트 모드' : '다크 모드'">
            <svg v-if="darkMode" width="15" height="15" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="4" stroke="currentColor" stroke-width="1.6"/><path d="M9 1v2M9 15v2M1 9h2M15 9h2M3.5 3.5l1.5 1.5M13 13l1.5 1.5M3.5 14.5l1.5-1.5M13 5l1.5-1.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
            <svg v-else width="15" height="15" viewBox="0 0 18 18" fill="none"><path d="M15 10.5A7 7 0 0 1 7.5 3a7 7 0 0 0 7.5 7.5z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
        </div>
      </div>

    </div>
  </aside>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import { useApp } from '../../composables/useApp';

const router = useRouter();
const route  = useRoute();

const {
  sidebarCollapsed, showModal,
  todayBookingCount, currentlyUsedCount,
  liveTime, liveDate,
  showUserSettings,
  darkMode, toggleDarkMode,
} = useApp();

const openNewBooking = () => {
  showModal.value = true;
  if (route.path !== '/main') router.push('/main');
};
</script>
