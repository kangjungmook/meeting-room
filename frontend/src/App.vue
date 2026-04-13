<template>
  <div class="flex h-screen bg-[#F5F7FA] dark:bg-gray-950 font-sans text-gray-900 dark:text-gray-50">

    <!-- ── Sidebar ── -->
    <AppSidebar v-if="!isMobile" @logout="logout" />

    <!-- ── Main ── -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">

      <!-- Header -->
      <AppHeader />

      <!-- Content -->
      <main class="flex-1 overflow-hidden relative">
        <Transition :name="'slide-' + slideDir">
          <div :key="viewMode" class="absolute inset-0 overflow-auto custom-scrollbar flex flex-col gap-4"
               :class="isMobile ? 'p-2 pb-44' : 'p-3 pb-4'">
            <CalendarDay   v-if="viewMode === 'day'"   />
            <CalendarWeek  v-if="viewMode === 'week'"  />
            <CalendarMonth v-if="viewMode === 'month'" />
          </div>
        </Transition>
      </main>

    </div>

    <!-- ── 데스크톱 예약 패널 (리사이저블 사이드바) ── -->
    <div v-if="!isMobile"
         :style="{ width: showModal ? panelWidth + 'px' : '0px', minWidth: '0' }"
         class="flex-shrink-0 bg-white dark:bg-gray-900 border-l border-gray-200 dark:border-gray-700 flex flex-col shadow-xl overflow-hidden relative"
         :class="resizing ? '' : 'transition-[width] duration-[280ms] ease-[cubic-bezier(0.32,0.72,0,1)]'">
      <div v-if="showModal" class="absolute left-0 top-0 bottom-0 w-2 cursor-col-resize z-10 group" @mousedown.prevent="startResize">
        <div class="absolute left-0.5 top-1/2 -translate-y-1/2 w-0.5 h-10 rounded-full bg-slate-300 group-hover:bg-indigo-400 transition-colors"></div>
      </div>
      <BookingModal v-if="showModal" :key="modalKey" :rooms="rooms" :targetDate="targetDate" :bookings="bookings"
                    :initialData="modalInit" :editBooking="editBooking"
                    @close="closeModal" @refresh="() => { fetchBookings(); fetchMyBookings(); }" />
    </div>

    <!-- ── 모바일 바텀 시트 ── -->
    <template v-if="isMobile && showModal">
      <!-- 백드롭 -->
      <div ref="backdropRef" class="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-sm" @click="closeModal"></div>
      <!-- 시트 -->
      <div ref="sheetRef"
           class="fixed bottom-0 left-0 right-0 z-50 bg-white dark:bg-gray-900 rounded-t-3xl shadow-2xl flex flex-col overflow-x-hidden"
           style="max-height: min(90svh, 90vh); min-height: min(65svh, 65vh); touch-action: pan-y; will-change: transform;">
        <!-- 드래그 핸들 -->
        <div class="flex justify-center pt-3 pb-1 flex-shrink-0 cursor-grab active:cursor-grabbing"
             @touchstart.passive="onSheetTouchStart"
             @touchmove.passive="onSheetTouchMove"
             @touchend.passive="onSheetTouchEnd">
          <div class="w-10 h-1.5 rounded-full bg-slate-300 dark:bg-gray-600"></div>
        </div>
        <BookingModal :key="modalKey" :rooms="rooms" :targetDate="targetDate" :bookings="bookings"
                      :initialData="modalInit" :editBooking="editBooking"
                      @close="closeModal" @refresh="() => { fetchBookings(); fetchMyBookings(); }" />
      </div>
    </template>

    <!-- ── 모바일 하단 새 예약 버튼 ── -->
    <div v-if="isMobile && !showModal && !showDrawer" class="fixed bottom-6 right-6 z-40">
      <button @click="showModal = true"
              class="flex items-center gap-2 px-5 py-4 bg-blue-600 hover:bg-blue-700 text-white rounded-2xl text-[15px] font-bold shadow-xl shadow-blue-200 dark:shadow-blue-900/40 active:scale-95 transition-all">
        <AppIcon name="plus" :size="18" />
        새 예약
      </button>
    </div>

    <!-- ── Mobile Drawer ── -->
    <MobileDrawer />

    <!-- ── Tooltip & Detail Popover ── -->
    <BookingTooltip />

    <!-- ── Room Detail Modal ── -->
    <RoomDetailModal />

    <!-- ── Cancel Modal ── -->
    <CancelModal />

    <!-- ── User Settings Modal ── -->
    <UserSettingsModal v-if="showUserSettings" />

    <!-- ── 내 예약 패널 ── -->
    <MyBookingsPanel />

    <!-- ── 토스트 알림 ── -->
    <ToastContainer />

    <!-- ── 튜토리얼 (최초 1회) ── -->
    <TutorialModal />

  </div>
</template>

<script setup>
import { onMounted, onUnmounted, nextTick, watch, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useApp } from './composables/useApp';
import api from './api';
import AppIcon from './components/icons/AppIcon.vue';

import BookingModal      from './components/BookingModal.vue';
import AppSidebar        from './components/layout/AppSidebar.vue';
import AppHeader         from './components/layout/AppHeader.vue';
import MobileDrawer      from './components/layout/MobileDrawer.vue';
import MyBookingsPanel   from './components/layout/MyBookingsPanel.vue';
import BookingTooltip    from './components/booking/BookingTooltip.vue';
import RoomDetailModal   from './components/booking/RoomDetailModal.vue';
import CancelModal       from './components/booking/CancelModal.vue';
import UserSettingsModal from './components/layout/UserSettingsModal.vue';
import ToastContainer    from './components/layout/ToastContainer.vue';
import TutorialModal     from './components/layout/TutorialModal.vue';
import CalendarDay       from './components/calendar/CalendarDay.vue';
import CalendarWeek      from './components/calendar/CalendarWeek.vue';
import CalendarMonth     from './components/calendar/CalendarMonth.vue';

const router = useRouter();

const {
  isMobile, slideDir, showModal, showDrawer,
  panelWidth, resizing, modalKey, modalInit, editBooking,
  startResize, onResizeMove, onResizeEnd, closeModal,
  rooms, bookings, targetDate,
  showUserSettings,
  fetchRooms, fetchBookings, fetchMyBookings, connectSse, disconnectSse,
  initFcm, scrollDayView, viewMode, userMap,
  applyNotifPrefs,
} = useApp();

// 모바일 모달 열릴 때 body 스크롤 잠금 (배경 캘린더 고정)
watch(() => isMobile.value && showModal.value, (locked) => {
  document.body.style.overflow = locked ? 'hidden' : '';
});

// ── 바텀시트 스와이프 다운 닫기 ────────────────────────────────
const sheetRef    = ref(null);
const backdropRef = ref(null);
let _swipeStartY  = 0;
let _swipeDeltaY  = 0;
const CLOSE_THRESHOLD = 120;

const onSheetTouchStart = (e) => {
  _swipeStartY = e.touches[0].clientY;
  _swipeDeltaY = 0;
  if (sheetRef.value)    sheetRef.value.style.transition    = 'none';
  if (backdropRef.value) backdropRef.value.style.transition = 'none';
};

const onSheetTouchMove = (e) => {
  const dy = e.touches[0].clientY - _swipeStartY;
  if (dy <= 0) return;
  _swipeDeltaY = dy;

  if (sheetRef.value)
    sheetRef.value.style.transform = `translateY(${dy}px)`;

  if (backdropRef.value) {
    const ratio = Math.min(dy / CLOSE_THRESHOLD, 1);
    backdropRef.value.style.opacity = String(1 - ratio);
  }
};

const onSheetTouchEnd = () => {
  if (!sheetRef.value) return;
  if (_swipeDeltaY > CLOSE_THRESHOLD) {
    const t = 'transform 0.25s ease, opacity 0.25s ease';
    sheetRef.value.style.transition    = t;
    sheetRef.value.style.transform     = 'translateY(100%)';
    if (backdropRef.value) {
      backdropRef.value.style.transition = 'opacity 0.25s ease';
      backdropRef.value.style.opacity    = '0';
    }
    setTimeout(() => closeModal(), 240);
  } else {
    const t = 'transform 0.3s cubic-bezier(0.32,0.72,0,1)';
    sheetRef.value.style.transition    = t;
    sheetRef.value.style.transform     = 'translateY(0)';
    if (backdropRef.value) {
      backdropRef.value.style.transition = 'opacity 0.3s ease';
      backdropRef.value.style.opacity    = '1';
    }
  }
};

const logout = async () => {
  try { await api.post('/auth/logout'); } catch {}
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  router.push('/');
};

onMounted(async () => {
  window.addEventListener('resize', () => { isMobile.value = window.innerWidth < 768; });
  window.addEventListener('mousemove', onResizeMove);
  window.addEventListener('mouseup', onResizeEnd);
  await fetchRooms();
  fetchBookings();
  connectSse();
  api.get('/users').then(res => {
    const map = {};
    res.data.forEach(u => { map[u.id] = u.name; });
    userMap.value = map;
  }).catch(() => {});
  initFcm();
  api.get('/users/notification-preference').then(res => {
    applyNotifPrefs(res.data);
  }).catch(() => {});
  if (viewMode.value === 'day') nextTick(scrollDayView);
});

onUnmounted(() => {
  disconnectSse();
  window.removeEventListener('mousemove', onResizeMove);
  window.removeEventListener('mouseup', onResizeEnd);
});
</script>

<style scoped>
/* ── 뷰 슬라이드 트랜지션 ── */
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1), opacity 0.3s ease;
}
.slide-left-enter-from  { transform: translateX(30px);  opacity: 0; }
.slide-left-leave-to    { transform: translateX(-30px); opacity: 0; }
.slide-right-enter-from { transform: translateX(-30px); opacity: 0; }
.slide-right-leave-to   { transform: translateX(30px);  opacity: 0; }

/* ── 스크롤바 ── */
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
</style>
