/**
 * useApp.js — 앱 전역 상태 배럴 (barrel re-export)
 *
 * 각 관심사별 서브 모듈에서 상태를 가져와 재내보낸다.
 * 컴포넌트는 이 파일 하나만 import하면 된다.
 *
 * 서브 모듈 구조:
 *   composables/app/useAuth.js          — 현재 사용자 / isAdmin
 *   composables/app/useRealtime.js      — 실시간 시계
 *   composables/app/useCalendar.js      — 뷰 모드 / 날짜 네비게이션 / 정렬
 *   composables/app/useBookingData.js   — rooms / bookings / myBookings / API
 *   composables/app/useBookingHelpers.js — 헬퍼 함수 / 레이아웃 계산
 *   composables/app/useBookingPanel.js  — 예약 패널 / 취소 다이얼로그 / 툴팁
 *   composables/app/useToast.js          — 토스트 시스템
 *   composables/app/useNotifications.js — 다크모드 / FCM / SSE / 사용자 알림 설정
 */

import { watch, nextTick } from 'vue';
import dayjs from 'dayjs';
import 'dayjs/locale/ko';

// ── 서브 모듈 re-export ───────────────────────────────────────
export * from './app/useAuth';
export * from './app/useRealtime';
export * from './app/useCalendar';
export * from './app/useBookingData';
export * from './app/useBookingHelpers';
export * from './app/useBookingPanel';
export * from './app/useToast';
export * from './app/useNotifications';

// ── 내부 의존성 (배럴에서 조합) ───────────────────────────────
import { viewMode, targetDate, modeOrder, slideDir } from './app/useCalendar';
import { fetchBookings } from './app/useBookingData';
import { scrollDayView } from './app/useBookingHelpers';

// ── 캘린더 네비게이션 (fetchBookings를 호출하므로 배럴에서 정의) ─
export const moveDate = (v) => {
  const unit = viewMode.value === 'month' ? 'month' : viewMode.value === 'week' ? 'week' : 'day';
  targetDate.value = targetDate.value.add(v, unit);
  fetchBookings();
};

export const resetDate = () => {
  targetDate.value = dayjs();
  fetchBookings();
};

export const onDayClick = (day) => {
  targetDate.value = dayjs(day.date);
  viewMode.value = 'day';
  fetchBookings();
};

export const goToDay = async (date) => {
  targetDate.value = dayjs(date);
  await nextTick();
  viewMode.value = 'day';
};

// ── viewMode 변경 시 자동 연동 ────────────────────────────────
watch(viewMode, (newVal, oldVal) => {
  slideDir.value = modeOrder.indexOf(newVal) > modeOrder.indexOf(oldVal) ? 'left' : 'right';
  fetchBookings();
  if (newVal === 'day') nextTick(scrollDayView);
});

// ── useApp() 편의 훅 — const { rooms } = useApp() 형태로 사용 ─
import {
  currentUser, isAdmin, refreshCurrentUser,
} from './app/useAuth';
import {
  liveNow, liveTime, liveDate,
} from './app/useRealtime';
import {
  viewMode as _viewMode, targetDate as _targetDate, modes, modeOrder as _modeOrder,
  slideDir as _slideDir, hours, isMobile, sidebarCollapsed, showDrawer,
  weekDays, dateLabel, monthCells, expandedCells, sortBy, sortOptions,
  toggleExpand, isExpanded, searchQuery,
} from './app/useCalendar';
import {
  rooms, bookings, userMap, myBookings, myBookingsToday, myBookingsThisWeek,
  myBookingsUpcoming, myBookingsPast, fetchRooms, fetchBookings as _fetchBookings, fetchMyBookings,
  isLoadingBookings, isLoadingMyBookings,
} from './app/useBookingData';
import {
  getRoomColor, getRoomName, resolveAttendees, filterBookings, getBookingsForDate,
  sortBookings, canEditOrCancel, chipMinutes, todayBookingCount, isRoomInUse,
  currentlyUsedCount, selectedRoom, selectedRoomBookings, selectedRoomCurrent,
  getRoomBookingStatus, getLayoutBookings, getAllLayoutBookings, calcPos, HOUR_H,
  calcPosV, nowLineTop, dayViewRef, scrollDayView as _scrollDayView,
} from './app/useBookingHelpers';
import {
  showModal, panelWidth, resizing, modalKey, modalInit, editBooking,
  startResize, onResizeMove, onResizeEnd, closeModal, openEditModal, openQuickModal,
  cancelTarget, cancelError, isCancelling, confirmCancel, doCancel,
  showMyBookings, openMyBookings,
  tooltip, detailTarget, popoverStyle, showTooltip, pinTooltip, openDetail,
} from './app/useBookingPanel';
import {
  toasts, toastDuration, addToast,
} from './app/useToast';
import {
  userNotifPrefs, showUserSettings, saveNotifPrefs,
  notifPermission, initFcm, requestNotifPermission, connectSse, disconnectSse,
  darkMode, toggleDarkMode, applyNotifPrefs,
} from './app/useNotifications';

export function useApp() {
  return {
    // 사용자 정보
    currentUser, isAdmin,
    // 핵심 데이터
    rooms, bookings, userMap,
    // 캘린더 네비게이션
    viewMode: _viewMode, targetDate: _targetDate, modes, modeOrder: _modeOrder,
    slideDir: _slideDir, weekDays, dateLabel, monthCells, hours,
    expandedCells, sortBy, sortOptions, toggleExpand, isExpanded, searchQuery,
    // UI 상태
    isMobile, sidebarCollapsed, showDrawer,
    // 시계
    liveNow, liveTime, liveDate,
    // 토스트
    toasts, toastDuration, addToast,
    // 예약 패널
    showModal, panelWidth, resizing, modalKey, modalInit, editBooking,
    closeModal, openEditModal, openQuickModal,
    startResize, onResizeMove, onResizeEnd,
    // 취소
    cancelTarget, cancelError, isCancelling, confirmCancel, doCancel,
    // 내 예약
    showMyBookings, myBookings,
    myBookingsToday, myBookingsThisWeek, myBookingsUpcoming, myBookingsPast,
    fetchMyBookings, openMyBookings,
    // 툴팁 / 상세
    tooltip, detailTarget, popoverStyle, showTooltip, pinTooltip, openDetail,
    // 룸 / 예약 헬퍼
    getRoomColor, getRoomName, resolveAttendees,
    filterBookings, getBookingsForDate, sortBookings,
    canEditOrCancel, chipMinutes,
    // 오늘 현황
    todayBookingCount, currentlyUsedCount, isRoomInUse,
    // 회의실 상세
    selectedRoom, selectedRoomBookings, selectedRoomCurrent, getRoomBookingStatus,
    // 레이아웃 계산
    getLayoutBookings, getAllLayoutBookings,
    calcPos, HOUR_H, calcPosV, nowLineTop,
    dayViewRef, scrollDayView: _scrollDayView,
    // 네비게이션
    moveDate, resetDate, onDayClick, goToDay,
    // FCM
    notifPermission, initFcm, requestNotifPermission,
    // SSE
    connectSse, disconnectSse,
    // fetch
    fetchRooms, fetchBookings: _fetchBookings,
    // 로딩 상태
    isLoadingBookings, isLoadingMyBookings,
    // 사용자 알림 설정
    userNotifPrefs, showUserSettings, saveNotifPrefs, applyNotifPrefs,
    // 다크 모드
    darkMode, toggleDarkMode,
  };
}
