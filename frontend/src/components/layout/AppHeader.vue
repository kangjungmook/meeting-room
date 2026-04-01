<template>
  <!-- 데스크톱 헤더 -->
  <header v-if="!isMobile"
          class="h-[56px] flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between px-5 gap-4">

    <!-- 왼쪽: 날짜 네비게이션 -->
    <div class="flex items-center gap-1.5">
      <button @click="moveDate(-1)"
              class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
        <svg width="16" height="16" viewBox="0 0 18 18" fill="none">
          <path d="M11 4L7 9l4 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <span class="text-[15px] font-semibold text-gray-800 dark:text-gray-100 min-w-[200px] text-center select-none">{{ dateLabel }}</span>
      <button @click="moveDate(1)"
              class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
        <svg width="16" height="16" viewBox="0 0 18 18" fill="none">
          <path d="M7 4l4 5-4 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <button @click="resetDate"
              class="px-3 py-1.5 rounded-lg text-[12.5px] font-semibold text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 border border-gray-200 dark:border-gray-700 transition-colors ml-1">
        오늘
      </button>
    </div>

    <!-- 오른쪽: 뷰 탭 + 사용자 -->
    <div class="flex items-center gap-3">
      <!-- 뷰 모드 탭 -->
      <div class="flex items-center bg-gray-100 dark:bg-gray-800 rounded-lg p-0.5 gap-0.5">
        <button v-for="m in modes" :key="m.val" @click="viewMode = m.val"
                :class="[
                  'px-4 py-1.5 rounded-md text-[13px] font-semibold transition-all',
                  viewMode === m.val
                    ? 'bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-50 shadow-sm'
                    : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'
                ]">
          {{ m.name }}
        </button>
      </div>

      <!-- 사용자 정보 -->
      <div class="flex items-center gap-2">
        <div class="w-7 h-7 rounded-full bg-blue-50 flex items-center justify-center ring-1 ring-blue-100">
          <svg width="13" height="13" viewBox="0 0 15 15" fill="none">
            <circle cx="7.5" cy="5" r="3" stroke="#2563eb" stroke-width="1.5"/>
            <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#2563eb" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="leading-tight">
          <p class="text-[13px] font-semibold text-gray-800 dark:text-gray-100">{{ currentUser.name }}</p>
          <p class="text-[10.5px] text-gray-400 dark:text-gray-500">{{ currentUser.employeeId }}</p>
        </div>
        <button @click="$emit('logout')" title="로그아웃"
                class="w-7 h-7 flex items-center justify-center rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors ml-1">
          <svg width="14" height="14" viewBox="0 0 15 15" fill="none">
            <path d="M6 2H3a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h3M10 10l3-3-3-3M13 7H6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>
  </header>

  <!-- 모바일 헤더 -->
  <header v-else class="flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
    <div class="flex items-center justify-between px-4 py-2.5 gap-2">
      <button @click="showDrawer = true"
              class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400 flex-shrink-0">
        <svg width="15" height="15" viewBox="0 0 16 16" fill="none">
          <path d="M2 4h12M2 8h12M2 12h12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
      </button>
      <div class="flex bg-gray-100 dark:bg-gray-800 rounded-lg p-0.5 gap-0.5">
        <button v-for="m in modes" :key="m.val" @click="viewMode = m.val"
                :class="[
                  'px-3 py-1.5 rounded-md text-[12px] font-semibold transition-all',
                  viewMode === m.val ? 'bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-50 shadow-sm' : 'text-gray-500 dark:text-gray-400'
                ]">
          {{ m.name }}
        </button>
      </div>
      <div class="flex items-center gap-1.5 flex-shrink-0">
        <div class="w-7 h-7 rounded-full bg-blue-50 flex items-center justify-center ring-1 ring-blue-100">
          <svg width="12" height="12" viewBox="0 0 15 15" fill="none">
            <circle cx="7.5" cy="5" r="3" stroke="#2563eb" stroke-width="1.5"/>
            <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#2563eb" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="leading-tight">
          <p class="text-[12px] font-semibold text-gray-800 dark:text-gray-100">{{ currentUser.name }}</p>
        </div>
        <!-- 알림 버튼 (모바일 전용) -->
        <button @click="requestNotifPermission"
                :class="[
                  'w-7 h-7 flex items-center justify-center rounded-lg transition-colors',
                  notifPermission === 'granted' ? 'text-blue-500' : 'text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800'
                ]">
          <svg v-if="notifPermission === 'granted'" width="14" height="14" viewBox="0 0 15 15" fill="none">
            <path d="M7.5 1a4.5 4.5 0 0 1 4.5 4.5c0 2.5.5 3.5 1 4.5H2c.5-1 1-2 1-4.5A4.5 4.5 0 0 1 7.5 1z" fill="currentColor" stroke="currentColor" stroke-width="1" stroke-linejoin="round"/>
            <path d="M5.5 10a2 2 0 0 0 4 0" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
          </svg>
          <svg v-else width="14" height="14" viewBox="0 0 15 15" fill="none">
            <path d="M7.5 1a4.5 4.5 0 0 1 4.5 4.5c0 2.5.5 3.5 1 4.5H2c.5-1 1-2 1-4.5A4.5 4.5 0 0 1 7.5 1z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
            <path d="M5.5 10a2 2 0 0 0 4 0" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
          </svg>
        </button>
        <button @click="showUserSettings = true"
                class="w-7 h-7 flex items-center justify-center rounded-lg text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
          <svg width="14" height="14" viewBox="0 0 18 18" fill="none">
            <path d="M9 11.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M14.5 9c0-.4 0-.8-.1-1.2l1.7-1.3-1.5-2.6-2 .8a6 6 0 0 0-2.1-1.2L10 1.5H7.5l-.5 2A6 6 0 0 0 4.9 4.7l-2-.8L1.4 6.5l1.7 1.3A6 6 0 0 0 3 9c0 .4 0 .8.1 1.2L1.4 11.5l1.5 2.6 2-.8a6 6 0 0 0 2.1 1.2l.5 2H10l.5-2a6 6 0 0 0 2.1-1.2l2 .8 1.5-2.6-1.7-1.3c.1-.4.1-.8.1-1.2z" stroke="currentColor" stroke-width="1.5"/>
          </svg>
        </button>
        <button @click="$emit('logout')"
                class="w-7 h-7 flex items-center justify-center rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors">
          <svg width="14" height="14" viewBox="0 0 15 15" fill="none">
            <path d="M6 2H3a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h3M10 10l3-3-3-3M13 7H6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>
    <div class="flex items-center justify-center gap-1.5 px-4 pb-2.5">
      <button @click="moveDate(-1)"
              class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400">
        <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
          <path d="M11 4L7 9l4 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <span class="text-[12.5px] font-semibold text-gray-800 dark:text-gray-100 text-center whitespace-nowrap min-w-[140px]">{{ dateLabel }}</span>
      <button @click="moveDate(1)"
              class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400">
        <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
          <path d="M7 4l4 5-4 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <button @click="resetDate"
              class="px-2.5 py-1 rounded-lg border border-gray-200 dark:border-gray-700 text-[12px] font-semibold text-gray-500 dark:text-gray-400">
        오늘
      </button>
    </div>
  </header>
</template>

<script setup>
import { useApp } from '../../composables/useApp';

defineEmits(['logout']);

const {
  isMobile, modes, viewMode, dateLabel,
  showDrawer, currentUser,
  notifPermission, requestNotifPermission,
  moveDate, resetDate,
  showUserSettings,
} = useApp();
</script>
