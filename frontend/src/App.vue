<template>
  <div class="flex h-screen bg-[#F5F7FA] dark:bg-gray-950 font-sans text-gray-900 dark:text-gray-50">

    <!-- ── Sidebar ── -->
    <AppSidebar v-if="!isMobile" />

    <!-- ── Main ── -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">

      <!-- Header -->
      <AppHeader @logout="logout" />

      <!-- Content -->
      <main class="flex-1 overflow-hidden relative">
        <Transition :name="'slide-' + slideDir">
          <div :key="viewMode" class="absolute inset-0 overflow-auto p-3 custom-scrollbar flex flex-col gap-4"
               :class="isMobile ? 'pb-44' : 'pb-4'">
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
        <svg width="18" height="18" viewBox="0 0 17 17" fill="none">
          <path d="M8.5 3v11M3 8.5h11" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
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
      <Transition name="my-bookings">
      <div v-if="showMyBookings && isMobile" class="fixed inset-0 z-50 flex flex-col justify-end">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="showMyBookings = false"></div>
        <div class="relative flex flex-col bg-white dark:bg-gray-900 rounded-t-3xl shadow-2xl overflow-hidden" style="max-height:92dvh;min-height:60dvh">

          <!-- 헤더 -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 dark:border-gray-800 flex-shrink-0">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
                <svg width="16" height="16" viewBox="0 0 15 15" fill="none">
                  <circle cx="7.5" cy="5" r="3" stroke="white" stroke-width="1.5"/>
                  <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <h3 class="text-[15px] font-black text-gray-800 dark:text-gray-100 leading-tight">내 예약</h3>
                <p class="text-[11px] text-gray-400 dark:text-gray-500 leading-tight">{{ myBookings.length }}건</p>
              </div>
            </div>
            <button @click="showMyBookings = false"
                    class="w-8 h-8 flex items-center justify-center rounded-lg text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-all">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>

          <!-- 내용 -->
          <div class="flex-1 overflow-y-auto px-4 py-4 flex flex-col gap-5 custom-scrollbar">

            <!-- 빈 상태 -->
            <div v-if="myBookings.length === 0"
                 class="flex flex-col items-center justify-center py-16 gap-3">
              <div class="w-14 h-14 rounded-2xl bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
                <svg width="22" height="22" viewBox="0 0 18 18" fill="none">
                  <rect x="1.5" y="3.5" width="15" height="13" rx="2" stroke="#94a3b8" stroke-width="1.6"/>
                  <path d="M5.5 1.5v4M12.5 1.5v4M1.5 8h15" stroke="#94a3b8" stroke-width="1.6" stroke-linecap="round"/>
                </svg>
              </div>
              <p class="text-[14px] font-semibold text-gray-400">예약이 없습니다</p>
            </div>

            <template v-else>
              <!-- 섹션별 그룹 렌더링 -->
              <template v-for="section in [
                { label: '오늘 회의',   list: myBookingsToday,    fmt: b => `${dayjs(b.startTime).format('HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}` },
                { label: '이번 주 회의', list: myBookingsThisWeek, fmt: b => `${dayjs(b.startTime).format('MM/DD (dd) HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}` },
                { label: '이후 일정',   list: myBookingsUpcoming, fmt: b => `${dayjs(b.startTime).format('MM/DD (dd) HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}` },
              ]" :key="section.label">
                <div v-if="section.list.length > 0">
                  <p class="text-[11px] font-black uppercase tracking-widest text-gray-400 dark:text-gray-500 px-1 mb-2">{{ section.label }}</p>
                  <div class="flex flex-col gap-2">
                    <div v-for="b in section.list" :key="b.id"
                         class="bg-white dark:bg-gray-800 rounded-2xl border border-slate-200 dark:border-gray-700 overflow-hidden flex hover:shadow-md transition-all">
                      <div class="w-1.5 flex-shrink-0" :style="{background: getRoomColor(b.roomId)}"></div>
                      <div class="flex-1 px-4 py-3">
                        <div class="flex items-start justify-between gap-2">
                          <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-1.5 mb-1.5 flex-wrap">
                              <span class="text-[10px] font-bold px-2 py-0.5 rounded-full text-white flex-shrink-0"
                                    :style="{background: getRoomColor(b.roomId)}">{{ getRoomName(b.roomId) }}</span>
                              <span :class="b.userId === currentUser.id ? 'bg-blue-100 text-indigo-600' : 'bg-amber-100 text-amber-600'"
                                    class="text-[10px] font-bold px-2 py-0.5 rounded-full flex-shrink-0">
                                {{ b.userId === currentUser.id ? '예약자' : '참석자' }}
                              </span>
                            </div>
                            <p class="text-[13px] font-bold text-gray-800 dark:text-gray-100 truncate">{{ b.title }}</p>
                            <p class="text-[12px] font-semibold text-slate-500 dark:text-gray-400 mt-0.5 tabular-nums">{{ section.fmt(b) }}</p>
                            <p class="flex items-center gap-1 text-[11px] text-gray-400 dark:text-gray-500 mt-1">
                              <svg width="10" height="10" viewBox="0 0 15 15" fill="none" class="flex-shrink-0">
                                <circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/>
                                <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                              </svg>
                              {{ b.organizer }}
                            </p>
                            <p v-if="b.attendeeIds?.length || b.externalAttendeeNames?.length" class="flex items-center gap-1 text-[11px] text-gray-400 mt-0.5">
                              <svg width="11" height="10" viewBox="0 0 18 14" fill="none" class="flex-shrink-0">
                                <circle cx="6" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
                                <path d="M0.5 13c0-3 2.5-5.2 5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                                <circle cx="12" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
                                <path d="M17.5 13c0-3-2.5-5.2-5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                              </svg>
                              {{ resolveAttendees(b.attendeeIds, b.externalAttendeeNames) }}
                            </p>
                          </div>
                          <div v-if="canEditOrCancel(b)" class="flex gap-1.5 flex-shrink-0 pt-0.5">
                            <button @click="openEditModal(b); showMyBookings = false"
                                    class="w-7 h-7 flex items-center justify-center rounded-lg bg-indigo-50 text-blue-600 hover:bg-blue-100 transition-all">
                              <svg width="13" height="13" viewBox="0 0 14 14" fill="none"><path d="M9.5 2.5l2 2L4 12H2v-2L9.5 2.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/></svg>
                            </button>
                            <button @click="confirmCancel(b); showMyBookings = false"
                                    class="w-7 h-7 flex items-center justify-center rounded-lg bg-red-50 text-red-400 hover:bg-red-100 transition-all">
                              <svg width="13" height="13" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v4M8.5 6.5v4M3.5 4l.5 8h6l.5-8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </template>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── 내 예약 패널 (데스크톱 우측 슬라이드) ── -->
    <Transition name="my-bookings-panel">
      <div v-if="showMyBookings && !isMobile"
           class="fixed right-0 top-0 bottom-0 z-50 w-[380px] flex flex-col bg-white dark:bg-gray-900 border-l border-gray-200 dark:border-gray-700 shadow-2xl overflow-hidden">
        <div class="fixed inset-0 z-[-1]" @click="showMyBookings = false"></div>

          <!-- 헤더 -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 dark:border-gray-800 flex-shrink-0">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
                <svg width="16" height="16" viewBox="0 0 15 15" fill="none">
                  <circle cx="7.5" cy="5" r="3" stroke="white" stroke-width="1.5"/>
                  <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <h3 class="text-[15px] font-black text-gray-800 dark:text-gray-100 leading-tight">내 예약</h3>
                <p class="text-[11px] text-gray-400 dark:text-gray-500 leading-tight">{{ myBookings.length }}건</p>
              </div>
            </div>
            <button @click="showMyBookings = false"
                    class="w-8 h-8 flex items-center justify-center rounded-lg text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-all">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>

          <!-- 내용 -->
          <div class="flex-1 overflow-y-auto px-4 py-4 flex flex-col gap-5 custom-scrollbar">
            <!-- 빈 상태 -->
            <div v-if="myBookings.length === 0"
                 class="flex flex-col items-center justify-center py-16 gap-3">
              <div class="w-14 h-14 rounded-2xl bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
                <svg width="22" height="22" viewBox="0 0 18 18" fill="none">
                  <rect x="1.5" y="3.5" width="15" height="13" rx="2" stroke="#94a3b8" stroke-width="1.6"/>
                  <path d="M5.5 1.5v4M12.5 1.5v4M1.5 8h15" stroke="#94a3b8" stroke-width="1.6" stroke-linecap="round"/>
                </svg>
              </div>
              <p class="text-[14px] font-semibold text-gray-400">예약이 없습니다</p>
            </div>
            <template v-else>
              <template v-for="section in [
                { label: '오늘 회의',   list: myBookingsToday,    fmt: b => `${dayjs(b.startTime).format('HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}` },
                { label: '이번 주 회의', list: myBookingsThisWeek, fmt: b => `${dayjs(b.startTime).format('MM/DD (dd) HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}` },
                { label: '이후 일정',   list: myBookingsUpcoming, fmt: b => `${dayjs(b.startTime).format('MM/DD (dd) HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}` },
              ]" :key="section.label">
                <div v-if="section.list.length > 0">
                  <p class="text-[11px] font-black uppercase tracking-widest text-gray-400 dark:text-gray-500 px-1 mb-2">{{ section.label }}</p>
                  <div class="flex flex-col gap-2">
                    <div v-for="b in section.list" :key="b.id"
                         class="bg-white dark:bg-gray-800 rounded-2xl border border-slate-200 dark:border-gray-700 overflow-hidden flex hover:shadow-md transition-all">
                      <div class="w-1.5 flex-shrink-0" :style="{background: getRoomColor(b.roomId)}"></div>
                      <div class="flex-1 px-4 py-3">
                        <div class="flex items-start justify-between gap-2">
                          <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-1.5 mb-1.5 flex-wrap">
                              <span class="text-[10px] font-bold px-2 py-0.5 rounded-full text-white flex-shrink-0"
                                    :style="{background: getRoomColor(b.roomId)}">{{ getRoomName(b.roomId) }}</span>
                              <span :class="b.userId === currentUser.id ? 'bg-blue-100 text-indigo-600' : 'bg-amber-100 text-amber-600'"
                                    class="text-[10px] font-bold px-2 py-0.5 rounded-full flex-shrink-0">
                                {{ b.userId === currentUser.id ? '예약자' : '참석자' }}
                              </span>
                            </div>
                            <p class="text-[13px] font-bold text-gray-800 dark:text-gray-100 truncate">{{ b.title }}</p>
                            <p class="text-[12px] font-semibold text-slate-500 dark:text-gray-400 mt-0.5 tabular-nums">{{ section.fmt(b) }}</p>
                            <p class="flex items-center gap-1 text-[11px] text-gray-400 dark:text-gray-500 mt-1">
                              <svg width="10" height="10" viewBox="0 0 15 15" fill="none" class="flex-shrink-0">
                                <circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/>
                                <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                              </svg>
                              {{ b.organizer }}
                            </p>
                            <p v-if="b.attendeeIds?.length || b.externalAttendeeNames?.length" class="flex items-center gap-1 text-[11px] text-gray-400 mt-0.5">
                              <svg width="11" height="10" viewBox="0 0 18 14" fill="none" class="flex-shrink-0">
                                <circle cx="6" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
                                <path d="M0.5 13c0-3 2.5-5.2 5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                                <circle cx="12" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
                                <path d="M17.5 13c0-3-2.5-5.2-5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                              </svg>
                              {{ resolveAttendees(b.attendeeIds, b.externalAttendeeNames) }}
                            </p>
                          </div>
                          <div v-if="canEditOrCancel(b)" class="flex gap-1.5 flex-shrink-0 pt-0.5">
                            <button @click="openEditModal(b); showMyBookings = false"
                                    class="w-7 h-7 flex items-center justify-center rounded-lg bg-indigo-50 text-blue-600 hover:bg-blue-100 transition-all">
                              <svg width="13" height="13" viewBox="0 0 14 14" fill="none"><path d="M9.5 2.5l2 2L4 12H2v-2L9.5 2.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/></svg>
                            </button>
                            <button @click="confirmCancel(b); showMyBookings = false"
                                    class="w-7 h-7 flex items-center justify-center rounded-lg bg-red-50 text-red-400 hover:bg-red-100 transition-all">
                              <svg width="13" height="13" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v4M8.5 6.5v4M3.5 4l.5 8h6l.5-8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </template>
          </div>
      </div>
    </Transition>

    <!-- ── 토스트 알림 ── -->
    <ToastContainer />

  </div>
</template>

<script setup>
import { onMounted, onUnmounted, nextTick, watch, ref } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { useApp } from './composables/useApp';
import api from './api';

import BookingModal   from './components/BookingModal.vue';
import AppSidebar     from './components/layout/AppSidebar.vue';
import AppHeader      from './components/layout/AppHeader.vue';
import MobileDrawer   from './components/layout/MobileDrawer.vue';
import BookingTooltip from './components/booking/BookingTooltip.vue';
import RoomDetailModal from './components/booking/RoomDetailModal.vue';
import CancelModal       from './components/booking/CancelModal.vue';
import UserSettingsModal from './components/layout/UserSettingsModal.vue';
import ToastContainer    from './components/layout/ToastContainer.vue';
import CalendarDay    from './components/calendar/CalendarDay.vue';
import CalendarWeek   from './components/calendar/CalendarWeek.vue';
import CalendarMonth  from './components/calendar/CalendarMonth.vue';

const router = useRouter();

const {
  isMobile, slideDir, showModal, showDrawer,
  panelWidth, resizing, modalKey, modalInit, editBooking,
  startResize, onResizeMove, onResizeEnd, closeModal,
  showMyBookings, myBookings, myBookingsToday, myBookingsThisWeek, myBookingsUpcoming,
  getRoomColor, getRoomName, resolveAttendees, canEditOrCancel,
  openEditModal, confirmCancel,
  rooms, bookings, targetDate,
  currentUser, showUserSettings,
  fetchRooms, fetchBookings, fetchMyBookings, connectSse, disconnectSse,
  initFcm, scrollDayView, viewMode, userMap,
  toastDuration, userNotifPrefs, applyNotifPrefs,
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
  if (dy <= 0) return; // 위로 드래그 무시
  _swipeDeltaY = dy;

  // 시트 이동
  if (sheetRef.value)
    sheetRef.value.style.transform = `translateY(${dy}px)`;

  // 백드롭 드래그 비율만큼 페이드 (0px→0%, 120px→100% 사라짐)
  if (backdropRef.value) {
    const ratio   = Math.min(dy / CLOSE_THRESHOLD, 1);
    backdropRef.value.style.opacity = String(1 - ratio);
  }
};

const onSheetTouchEnd = () => {
  if (!sheetRef.value) return;
  if (_swipeDeltaY > CLOSE_THRESHOLD) {
    // 임계값 초과 → 시트+백드롭 동시에 사라지고 닫기
    const t = 'transform 0.25s ease, opacity 0.25s ease';
    sheetRef.value.style.transition    = t;
    sheetRef.value.style.transform     = 'translateY(100%)';
    if (backdropRef.value) {
      backdropRef.value.style.transition = 'opacity 0.25s ease';
      backdropRef.value.style.opacity    = '0';
    }
    setTimeout(() => closeModal(), 240);
  } else {
    // 임계값 미만 → 시트+백드롭 원위치 복귀
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
  window.addEventListener('resize', () => { isMobile.value = window.innerWidth < 640; });
  window.addEventListener('mousemove', onResizeMove);
  window.addEventListener('mouseup', onResizeEnd);
  await fetchRooms();
  fetchBookings();
  connectSse();
  const { default: api } = await import('./api');
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

/* ── 내 예약 패널 (모바일 바텀시트) ── */
.my-bookings-enter-active { transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1); }
.my-bookings-leave-active { transition: all 0.2s ease-in; }
.my-bookings-enter-from   { opacity: 0; transform: translateY(100%); }
.my-bookings-leave-to     { opacity: 0; transform: translateY(100%); }

/* ── 내 예약 패널 (데스크톱 우측 슬라이드) ── */
.my-bookings-panel-enter-active { transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1), opacity 0.3s ease; }
.my-bookings-panel-leave-active { transition: transform 0.25s ease-in, opacity 0.2s ease-in; }
.my-bookings-panel-enter-from   { transform: translateX(100%); opacity: 0; }
.my-bookings-panel-leave-to     { transform: translateX(100%); opacity: 0; }

/* ── 스크롤바 ── */
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
</style>
