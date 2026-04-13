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

  <!-- 모바일 헤더 (1줄) -->
  <header v-else class="flex-shrink-0 h-[48px] bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-center px-2 gap-1 overflow-hidden">

    <!-- 햄버거 -->
    <button @click="showDrawer = true"
            class="w-7 h-7 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400 flex-shrink-0">
      <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
        <path d="M2 4h12M2 8h12M2 12h12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
      </svg>
    </button>

    <!-- 뷰 탭 -->
    <div class="inline-flex bg-gray-100 dark:bg-gray-800 rounded-lg p-0.5 gap-0.5 flex-shrink-0">
      <button v-for="m in modes" :key="m.val" @click="viewMode = m.val"
              :class="[
                'px-2 py-1 rounded-md text-[11.5px] font-semibold transition-all whitespace-nowrap',
                viewMode === m.val ? 'bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-50 shadow-sm' : 'text-gray-500 dark:text-gray-400'
              ]">
        {{ m.name }}
      </button>
    </div>

    <!-- 날짜 네비 -->
    <div class="flex items-center gap-0.5 flex-1 justify-center min-w-0 overflow-hidden">
      <button @click="moveDate(-1)"
              class="w-5 h-5 flex items-center justify-center rounded text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors flex-shrink-0">
        <svg width="11" height="11" viewBox="0 0 14 14" fill="none">
          <path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <button @click="resetDate"
              class="text-[12px] font-semibold text-gray-800 dark:text-gray-100 whitespace-nowrap px-0.5 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors flex-shrink-0">
        {{ dateLabel }}
      </button>
      <button @click="moveDate(1)"
              class="w-5 h-5 flex items-center justify-center rounded text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors flex-shrink-0">
        <svg width="11" height="11" viewBox="0 0 14 14" fill="none">
          <path d="M5 2l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <button @click="resetDate"
              class="px-1.5 h-5 rounded border border-gray-200 dark:border-gray-700 text-[10.5px] font-semibold text-gray-500 dark:text-gray-400 hover:border-indigo-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors flex-shrink-0">
        오늘
      </button>
    </div>

    <!-- 우측 아이콘 -->
    <div class="flex items-center gap-0 flex-shrink-0">
      <div class="w-6 h-6 rounded-full bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center ring-1 ring-blue-100 dark:ring-blue-900/30">
        <svg width="11" height="11" viewBox="0 0 15 15" fill="none">
          <circle cx="7.5" cy="5" r="3" stroke="#2563eb" stroke-width="1.5"/>
          <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#2563eb" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </div>
      <button @click="requestNotifPermission"
              :class="[
                'w-6 h-6 flex items-center justify-center rounded-lg transition-colors',
                notifPermission === 'granted' ? 'text-blue-500' : 'text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800'
              ]">
        <svg v-if="notifPermission === 'granted'" width="13" height="13" viewBox="0 0 15 15" fill="none">
          <path d="M7.5 1a4.5 4.5 0 0 1 4.5 4.5c0 2.5.5 3.5 1 4.5H2c.5-1 1-2 1-4.5A4.5 4.5 0 0 1 7.5 1Z " fill="currentColor" stroke="currentColor" stroke-width="1" stroke-linejoin="round"/>
          <path d="M5.5 10a2 2 0 0 0 4 0" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
        </svg>
        <svg v-else width="13" height="13" viewBox="0 0 15 15" fill="none">
          <path d="M7.5 1a4.5 4.5 0 0 1 4.5 4.5c0 2.5.5 3.5 1 4.5H2c.5-1 1-2 1-4.5A4.5 4.5 0 0 1 7.5 1z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
          <path d="M5.5 10a2 2 0 0 0 4 0" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
        </svg>
      </button>
      <button @click="logout"
              class="w-6 h-6 flex items-center justify-center rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors">
        <svg width="13" height="13" viewBox="0 0 15 15" fill="none">
          <path d="M6 2H3a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h3M10 10l3-3-3-3M13 7H6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>

  </header>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useApp } from '../../composables/useApp';
import api from '../../api';

const router = useRouter();

const {
  isMobile, modes, viewMode, dateLabel,
  showDrawer, currentUser, searchQuery,
  notifPermission, requestNotifPermission,
  moveDate, resetDate, showUserSettings,
} = useApp();

const logout = async () => {
  try { await api.post('/auth/logout'); } catch {}
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  sessionStorage.removeItem('adminAuth');
  router.push('/');
};
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { scrollbar-width: none; }
</style>
