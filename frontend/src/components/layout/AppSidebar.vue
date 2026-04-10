 <template>
  <aside
    class="flex-shrink-0 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-700 flex flex-col transition-[width] duration-200 overflow-visible z-20"
    :style="sidebarCollapsed ? 'width:60px' : 'width:232px'"
  >

    <!-- ── 접힌 상태 ── -->
    <div v-if="sidebarCollapsed" class="h-full flex flex-col items-center py-4 gap-3">
      <!-- 햄버거 (펼치기) -->
      <button @click="sidebarCollapsed = false"
              class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
        <AppIcon name="menu" :size="15" />
      </button>

      <!-- 새 예약 버튼 -->
      <div class="relative group">
        <button @click="openNewBooking"
                class="w-9 h-9 flex items-center justify-center rounded-xl bg-blue-600 hover:bg-blue-700 text-white transition-colors shadow-sm">
          <AppIcon name="plus" :size="15" />
        </button>
        <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-3 px-2.5 py-1.5 bg-gray-900 dark:bg-gray-700 text-white text-[12px] font-semibold rounded-lg whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-150 z-50">
          새 예약
          <div class="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-gray-900 dark:border-r-gray-700"></div>
        </div>
      </div>

      <!-- 네비게이션 메뉴 -->
      <div class="flex flex-col items-center gap-1 mt-1">
        <div class="relative group">
          <button @click="router.push('/main')"
                  :class="['w-8 h-8 flex items-center justify-center rounded-lg transition-colors',
                    route.path === '/main'
                      ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400'
                      : 'hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500']">
            <AppIcon name="calendar" :size="15" />
          </button>
          <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-3 px-2.5 py-1.5 bg-gray-900 dark:bg-gray-700 text-white text-[12px] font-semibold rounded-lg whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-150 z-50">
            전체 예약 현황
            <div class="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-gray-900 dark:border-r-gray-700"></div>
          </div>
        </div>
        <div class="relative group">
          <button @click="router.push('/my-bookings')"
                  :class="['w-8 h-8 flex items-center justify-center rounded-lg transition-colors',
                    route.path === '/my-bookings'
                      ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400'
                      : 'hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500']">
            <AppIcon name="user" :size="15" />
          </button>
          <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-3 px-2.5 py-1.5 bg-gray-900 dark:bg-gray-700 text-white text-[12px] font-semibold rounded-lg whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-150 z-50">
            내 회의
            <div class="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-gray-900 dark:border-r-gray-700"></div>
          </div>
        </div>
        <div class="relative group">
          <button @click="router.push('/settings')"
                  :class="['w-8 h-8 flex items-center justify-center rounded-lg transition-colors',
                    route.path === '/settings'
                      ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400'
                      : 'hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500']">
            <AppIcon name="settings" :size="15" />
          </button>
          <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-3 px-2.5 py-1.5 bg-gray-900 dark:bg-gray-700 text-white text-[12px] font-semibold rounded-lg whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-150 z-50">
            설정
            <div class="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-gray-900 dark:border-r-gray-700"></div>
          </div>
        </div>
      </div>

      <div class="mt-auto"></div>

      <!-- 로그아웃 -->
      <div class="relative group">
        <button @click="logout"
                class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20 text-gray-400 dark:text-gray-500 hover:text-red-500 transition-colors">
          <svg width="15" height="15" viewBox="0 0 16 16" fill="none">
            <path d="M6 2H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            <path d="M11 11l3-3-3-3M14 8H6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-3 px-2.5 py-1.5 bg-gray-900 dark:bg-gray-700 text-white text-[12px] font-semibold rounded-lg whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-150 z-50">
          로그아웃
          <div class="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-gray-900 dark:border-r-gray-700"></div>
        </div>
      </div>
    </div>

    <!-- ── 펼친 상태 ── -->
    <div v-else class="h-full flex flex-col overflow-hidden">

      <!-- 헤더: 로고 + 접기 -->
      <div class="flex-shrink-0 flex items-center justify-between px-4 h-[56px] border-b border-gray-200 dark:border-gray-700">
        <span class="text-[13.5px] font-bold text-gray-900 dark:text-gray-50 whitespace-nowrap">회의실 예약 시스템</span>
        <button @click="sidebarCollapsed = true"
                class="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
          <AppIcon name="menu" :size="15" />
        </button>
      </div>

      <!-- 새 예약 버튼 -->
      <div class="flex-shrink-0 px-3 py-3">z
        <button @click="openNewBooking"
                class="flex items-center justify-center gap-2 w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 active:scale-[0.98] text-white text-[13px] fokzkznt-semibold transition-all shadow-sm">
          <AppIcon name="plus" :size="13" />
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
          <AppIcon name="calendar" :size="15" cls="flex-shrink-0" :class="route.path === '/main' ? 'text-blue-600' : ''" />
          전체 예약 현황
        </button>
        <button @click="router.push('/my-bookings')"
                :class="[
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-[13px] text-left w-full transition-colors',
                  route.path === '/my-bookings'
                    ? 'font-semibold text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                    : 'font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100'
                ]">
          <AppIcon name="user" :size="15" cls="flex-shrink-0" :class="route.path === '/my-bookings' ? 'text-blue-600' : ''" />
          내 회의
        </button>
        <button @click="router.push('/settings')"
                :class="[
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-[13px] text-left w-full transition-colors',
                  route.path === '/settings'
                    ? 'font-semibold text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                    : 'font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100'
                ]">
          <AppIcon name="settings" :size="15" cls="flex-shrink-0" :class="route.path === '/settings' ? 'text-blue-600' : ''" />
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
            <span class="text-[14px]">{{ darkMode ? '☀️' : '🌙' }}</span>
          </button>
        </div>

        <!-- 로그아웃 -->
        <button @click="logout"
                class="mt-1.5 flex items-center gap-2.5 w-full px-3 py-2 rounded-lg text-[13px] font-medium text-gray-400 dark:text-gray-500 hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-500 transition-colors">
          <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
            <path d="M6 2H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            <path d="M11 11l3-3-3-3M14 8H6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          로그아웃
        </button>
      </div>

    </div>
  </aside>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import AppIcon from '../icons/AppIcon.vue';
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

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  router.push('/');
};

</script>
