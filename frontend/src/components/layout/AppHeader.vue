<template>
  <!-- 데스크톱 헤더 -->
  <header v-if="!isMobile"
          class="h-[56px] flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-stretch px-4 relative">

    <!-- 왼쪽: 뷰 탭 -->
    <div class="flex items-stretch flex-shrink-0">
      <button v-for="m in modes" :key="m.val" @click="viewMode = m.val"
              class="relative px-4 flex items-center text-[13.5px] font-semibold transition-colors"
              :class="viewMode === m.val
                ? 'text-[#4F46E5] dark:text-indigo-400'
                : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'">
        {{ m.name }}
        <span v-if="viewMode === m.val"
              class="absolute bottom-0 left-0 right-0 h-[2.5px] rounded-t-full bg-[#4F46E5] dark:bg-indigo-400"></span>
      </button>
    </div>

    <!-- 가운데: 날짜 네비 (absolute 정중앙) -->
    <div class="absolute left-1/2 -translate-x-1/2 h-[56px] flex items-center gap-1">
      <button @click="moveDate(-1)"
              class="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <button @click="resetDate"
              class="px-2 text-[17px] font-bold text-gray-800 dark:text-gray-100 hover:text-[#4F46E5] dark:hover:text-indigo-400 transition-colors whitespace-nowrap select-none">
        {{ dateLabel }}
      </button>
      <button @click="moveDate(1)"
              class="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path d="M5 2l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <button @click="resetDate"
              class="ml-1 px-2.5 h-6 rounded-md border border-gray-200 dark:border-gray-700 text-[11.5px] font-semibold text-gray-500 dark:text-gray-400 hover:border-[#4F46E5] hover:text-[#4F46E5] dark:hover:text-indigo-400 transition-colors">
        오늘
      </button>
    </div>

    <!-- 오른쪽: 검색 + 유저 -->
    <div class="flex items-center gap-3 ml-auto flex-shrink-0">
      <!-- 검색바 -->
      <div class="relative">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none"
             width="13" height="13" viewBox="0 0 16 16" fill="none">
          <circle cx="6.5" cy="6.5" r="4.5" stroke="currentColor" stroke-width="1.5"/>
          <path d="M10 10l3.5 3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <input v-model="searchQuery" type="text" placeholder="예약 검색..."
               class="w-44 h-8 pl-9 pr-3 rounded-full border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-[12.5px] text-gray-700 dark:text-gray-200 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:border-[#4F46E5] focus:bg-white dark:focus:bg-gray-900 transition-all" />
      </div>

      <div class="w-px h-5 bg-gray-200 dark:bg-gray-700"></div>

      <!-- 유저 -->
      <div class="flex items-center gap-2">
        <div class="w-7 h-7 rounded-full bg-indigo-100 dark:bg-indigo-900/40 flex items-center justify-center flex-shrink-0">
          <svg width="13" height="13" viewBox="0 0 15 15" fill="none">
            <circle cx="7.5" cy="5" r="3" stroke="#4F46E5" stroke-width="1.5"/>
            <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#4F46E5" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="leading-tight">
          <p class="text-[13px] font-bold text-gray-800 dark:text-gray-100">{{ currentUser.name }}</p>
          <p class="text-[10.5px] text-gray-400 dark:text-gray-500">{{ currentUser.employeeId }}</p>
        </div>
        <button @click="logout"
                class="ml-2 px-2.5 h-7 rounded-lg bg-red-50 dark:bg-red-900/20 text-[11px] font-semibold text-red-500 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/40 transition-colors">
          로그아웃
        </button>
      </div>
    </div>

  </header>

  <!-- 모바일 헤더 -->
  <header v-else
          class="flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-100 dark:border-gray-800">
    <div class="relative flex items-center h-[52px] px-3">

      <!-- 왼쪽: 햄버거 -->
      <button @click="showDrawer = true"
              class="w-9 h-9 flex-shrink-0 flex items-center justify-center rounded-xl z-10
                     text-gray-500 dark:text-gray-400 active:bg-gray-100 dark:active:bg-gray-800 transition-colors">
        <AppIcon name="menu" :size="18" />
      </button>

      <!-- 가운데: 날짜 네비 (absolute 정중앙) -->
      <div class="absolute left-1/2 -translate-x-1/2 flex items-center gap-0.5">
        <button @click="moveDate(-1)"
                class="w-7 h-7 flex items-center justify-center rounded-lg
                       text-gray-400 dark:text-gray-500 active:bg-gray-100 dark:active:bg-gray-800 transition-colors">
          <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
            <path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>

        <button @click="resetDate"
                class="flex items-baseline gap-1 px-1 active:opacity-60 transition-opacity select-none">
          <span class="text-[15px] font-bold text-gray-900 dark:text-gray-50 leading-none whitespace-nowrap tracking-tight">
            {{ dateDisplay.main }}
          </span>
          <span v-if="dateDisplay.sub"
                class="text-[12px] font-semibold text-gray-400 dark:text-gray-500 leading-none">
            {{ dateDisplay.sub }}
          </span>
        </button>

        <button @click="moveDate(1)"
                class="w-7 h-7 flex items-center justify-center rounded-lg
                       text-gray-400 dark:text-gray-500 active:bg-gray-100 dark:active:bg-gray-800 transition-colors">
          <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
            <path d="M5 2l5 5-5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>

      </div>

      <!-- 오른쪽: 뷰 탭 세그먼트 컨트롤 (좌우 스와이프로 모드 전환) -->
      <div class="ml-auto flex-shrink-0 relative z-10 flex items-center
                  bg-gray-100 dark:bg-gray-800 rounded-xl p-[3px]"
           @touchstart.passive="onTabSwipeStart"
           @touchend.passive="onTabSwipeEnd">
        <button v-for="m in modes" :key="m.val"
                @click="viewMode = m.val"
                class="px-2.5 h-7 rounded-[9px] text-[12px] font-semibold transition-all duration-150"
                :class="viewMode === m.val
                  ? 'bg-white dark:bg-gray-600 text-gray-900 dark:text-gray-100 shadow-sm'
                  : 'text-gray-500 dark:text-gray-400'">
          {{ m.name }}
        </button>
      </div>

    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useApp } from '../../composables/useApp';
import AppIcon from '../icons/AppIcon.vue';
import api from '../../api.js';

const router = useRouter();

const {
  isMobile, modes, viewMode, targetDate, weekDays, dateLabel,
  showDrawer, showDateSheet, currentUser, searchQuery,
  moveDate, resetDate,
} = useApp();

const logout = async () => {
  try { await api.post('/auth/logout'); } catch {}
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  sessionStorage.removeItem('adminAuth');
  sessionStorage.removeItem('passwordResetRequired');
  router.push('/');
};

// 탭 스와이프로 일/주/월 전환
const modeOrder = ['day', 'week', 'month'];
let _tabSwipeX = 0;
const onTabSwipeStart = (e) => { _tabSwipeX = e.touches[0].clientX; };
const onTabSwipeEnd   = (e) => {
  const dx  = e.changedTouches[0].clientX - _tabSwipeX;
  if (Math.abs(dx) < 40) return;
  const idx = modeOrder.indexOf(viewMode.value);
  if (dx < 0 && idx < modeOrder.length - 1) viewMode.value = modeOrder[idx + 1]; // ← 다음 모드
  if (dx > 0 && idx > 0)                    viewMode.value = modeOrder[idx - 1]; // → 이전 모드
};

// 모바일 날짜 표시 (뷰 모드별 분리)
const dateDisplay = computed(() => {
  if (viewMode.value === 'week') {
    return {
      main: `${weekDays.value[0].format('M/D')} – ${weekDays.value[6].format('M/D')}`,
      sub: '',
    };
  }
  if (viewMode.value === 'month') {
    return { main: targetDate.value.format('YYYY년 M월'), sub: '' };
  }
  return {
    main: targetDate.value.format('M월 D일'),
    sub: targetDate.value.format('dd'),
  };
});

</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { scrollbar-width: none; }
</style>
