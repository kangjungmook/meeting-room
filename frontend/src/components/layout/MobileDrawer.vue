<template>
  <Transition name="drawer">
    <div v-if="showDrawer" class="fixed inset-0 z-50">

      <!-- 백드롭 -->
      <div ref="backdropRef"
           class="absolute inset-0 bg-gray-900/40 backdrop-blur-sm"
           @click="closeDrawer"></div>

      <!-- 바텀 시트 -->
      <div ref="sheetRef"
           class="absolute bottom-0 left-0 right-0 bg-white dark:bg-gray-900 rounded-t-2xl shadow-2xl flex flex-col overflow-hidden"
           style="max-height: min(88svh, 88vh); will-change: transform; touch-action: pan-y;">

        <!-- 드래그 핸들 -->
        <div class="flex justify-center pt-3 pb-2 flex-shrink-0 cursor-grab active:cursor-grabbing"
             @touchstart.passive="onTouchStart"
             @touchmove.passive="onTouchMove"
             @touchend.passive="onTouchEnd">
          <div class="w-10 h-1.5 rounded-full bg-gray-300 dark:bg-gray-600"></div>
        </div>

        <!-- 헤더 -->
        <div class="flex items-center justify-between px-4 pb-3 flex-shrink-0">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center flex-shrink-0 shadow-sm">
              <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
                <rect x="1" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
                <rect x="10" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
                <rect x="1" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
                <rect x="10" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
              </svg>
            </div>
            <span class="text-[13.5px] font-bold text-gray-900 dark:text-gray-50">회의실 예약</span>
          </div>
        </div>

        <!-- 내비게이션 -->
        <nav class="flex-shrink-0 px-2 flex flex-col gap-0.5">
          <button @click="router.push('/main'); closeDrawer()"
                  :class="[
                    'flex items-center gap-3 px-4 py-3.5 rounded-xl text-[14px] text-left w-full transition-colors',
                    route.path === '/main'
                      ? 'font-semibold text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                      : 'font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800'
                  ]">
            <svg :class="route.path === '/main' ? 'text-blue-600' : 'text-gray-400'" class="flex-shrink-0" width="16" height="16" viewBox="0 0 18 18" fill="none">
              <rect x="1.5" y="3.5" width="15" height="13" rx="2" stroke="currentColor" stroke-width="1.6"/>
              <path d="M5.5 1.5v4M12.5 1.5v4M1.5 8h15" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            전체 예약 현황
          </button>

          <button @click="router.push('/my-bookings'); closeDrawer()"
                  :class="[
                    'flex items-center gap-3 px-4 py-3.5 rounded-xl text-[14px] text-left w-full transition-colors',
                    route.path === '/my-bookings'
                      ? 'font-semibold text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                      : 'font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800'
                  ]">
            <svg :class="route.path === '/my-bookings' ? 'text-blue-600' : 'text-gray-400'" class="flex-shrink-0" width="16" height="16" viewBox="0 0 18 18" fill="none">
              <circle cx="9" cy="6" r="3.5" stroke="currentColor" stroke-width="1.6"/>
              <path d="M2 16c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            내 예약
          </button>

          <button @click="router.push('/settings'); closeDrawer()"
                  :class="[
                    'flex items-center gap-3 px-4 py-3.5 rounded-xl text-[14px] text-left w-full transition-colors',
                    route.path === '/settings'
                      ? 'font-semibold text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                      : 'font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800'
                  ]">
            <svg :class="route.path === '/settings' ? 'text-blue-600' : 'text-gray-400'" class="flex-shrink-0" width="16" height="16" viewBox="0 0 18 18" fill="none">
              <path d="M9 11.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M14.5 9c0-.4 0-.8-.1-1.2l1.7-1.3-1.5-2.6-2 .8a6 6 0 0 0-2.1-1.2L10 1.5H7.5l-.5 2A6 6 0 0 0 4.9 4.7l-2-.8L1.4 6.5l1.7 1.3A6 6 0 0 0 3 9c0 .4 0 .8.1 1.2L1.4 11.5l1.5 2.6 2-.8a6 6 0 0 0 2.1 1.2l.5 2H10l.5-2a6 6 0 0 0 2.1-1.2l2 .8 1.5-2.6-1.7-1.3c.1-.4.1-.8.1-1.2z" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            설정
          </button>

        </nav>

        <!-- 하단 현황 -->
        <div class="flex-shrink-0 px-3 py-3 mt-2 border-t border-gray-100 dark:border-gray-800">
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
                    class="ml-auto flex items-center justify-center w-7 h-7 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-400 dark:text-gray-500 transition-colors flex-shrink-0">
              <svg v-if="darkMode" width="15" height="15" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="4" stroke="currentColor" stroke-width="1.6"/><path d="M9 1v2M9 15v2M1 9h2M15 9h2M3.5 3.5l1.5 1.5M13 13l1.5 1.5M3.5 14.5l1.5-1.5M13 5l1.5-1.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
              <svg v-else width="15" height="15" viewBox="0 0 18 18" fill="none"><path d="M15 10.5A7 7 0 0 1 7.5 3a7 7 0 0 0 7.5 7.5z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
          </div>
        </div>

        <!-- 하단 안전 영역 여백 -->
        <div class="flex-shrink-0 pb-safe" style="padding-bottom: env(safe-area-inset-bottom)"></div>

      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useApp } from '../../composables/useApp';

const router = useRouter();
const route  = useRoute();

const {
  showDrawer, showModal,
  todayBookingCount, currentlyUsedCount,
  liveTime, liveDate,
  darkMode, toggleDarkMode,
} = useApp();

// ── 스와이프 다운 닫기 ─────────────────────────────────────────
const sheetRef    = ref(null);
const backdropRef = ref(null);
let _startY       = 0;
let _deltaY       = 0;
const THRESHOLD   = 100;

const closeDrawer = () => { showDrawer.value = false; };

const onTouchStart = (e) => {
  _startY = e.touches[0].clientY;
  _deltaY = 0;
  if (sheetRef.value)    sheetRef.value.style.transition    = 'none';
  if (backdropRef.value) backdropRef.value.style.transition = 'none';
};

const onTouchMove = (e) => {
  const dy = e.touches[0].clientY - _startY;
  if (dy <= 0) return;
  _deltaY = dy;
  if (sheetRef.value)
    sheetRef.value.style.transform = `translateY(${dy}px)`;
  if (backdropRef.value)
    backdropRef.value.style.opacity = String(Math.max(0, 1 - dy / THRESHOLD));
};

const onTouchEnd = () => {
  if (!sheetRef.value) return;
  if (_deltaY > THRESHOLD) {
    sheetRef.value.style.transition    = 'transform 0.25s ease';
    sheetRef.value.style.transform     = 'translateY(100%)';
    if (backdropRef.value) {
      backdropRef.value.style.transition = 'opacity 0.25s ease';
      backdropRef.value.style.opacity    = '0';
    }
    setTimeout(() => closeDrawer(), 230);
  } else {
    sheetRef.value.style.transition    = 'transform 0.3s cubic-bezier(0.32,0.72,0,1)';
    sheetRef.value.style.transform     = 'translateY(0)';
    if (backdropRef.value) {
      backdropRef.value.style.transition = 'opacity 0.3s ease';
      backdropRef.value.style.opacity    = '1';
    }
  }
};
</script>

<style scoped>
.drawer-enter-active { transition: opacity 0.3s ease; }
.drawer-leave-active { transition: opacity 0.25s ease; }
.drawer-enter-from   { opacity: 0; }
.drawer-leave-to     { opacity: 0; }

.drawer-enter-active .absolute.bottom-0,
.drawer-leave-active .absolute.bottom-0 {
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}
.drawer-enter-from .absolute.bottom-0 { transform: translateY(100%); }
.drawer-leave-to   .absolute.bottom-0 { transform: translateY(100%); }
</style>
