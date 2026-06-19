<template>
  <div class="flex h-screen bg-[#F5F7FA] dark:bg-gray-950 font-sans text-gray-900 dark:text-gray-50">

    <AppSidebar v-if="!isMobile" @logout="logout" />

    <div class="flex-1 flex flex-col overflow-hidden min-w-0">

      <AppHeader />

      <!-- 세 뷰를 항상 마운트, 캐러셀로 슬라이드 (마운트/언마운트 없음 → 깜빡임 없음) -->
      <main class="flex-1 overflow-hidden relative bg-[#F5F7FA] dark:bg-gray-950"
            @touchstart.passive="onViewTouchStart"
            @touchmove="onViewTouchMove"
            @touchend.passive="onViewTouchEnd">
        <div ref="carouselRef"
             class="absolute inset-0 flex"
             style="width: 300%; will-change: transform;">
          <div v-for="mode in modeOrder" :key="mode"
               class="min-h-0 flex flex-col gap-3 md:gap-4"
               style="flex-shrink: 0; width: 33.333%; height: 100%;"
               :class="[isMobile ? 'p-2 overflow-hidden' : 'p-3 pb-4 overflow-auto custom-scrollbar']"
               :style="isMobile ? { paddingBottom: 'calc(2.5rem + env(safe-area-inset-bottom, 0px))' } : {}">
            <CalendarDay   v-if="mode === 'day'"   />
            <CalendarWeek  v-if="mode === 'week'"  />
            <CalendarMonth v-if="mode === 'month'" />
          </div>
        </div>

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
      <div v-if="showModal" class="flex-1 min-h-0 overflow-y-auto overflow-x-hidden custom-scrollbar">
        <BookingModal :key="modalKey" :rooms="rooms" :targetDate="targetDate" :bookings="bookings"
                      :initialData="modalInit" :editBooking="editBooking"
                      @close="closeModal" @refresh="() => { fetchBookings(); fetchMyBookings(); }" />
      </div>
    </div>

    <!-- ── 모바일 바텀 시트 ── -->
    <template v-if="isMobile && showModal">
      <!-- 백드롭 -->
      <div
        ref="backdropRef"
        class="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-sm"
        @click="closeModal"
        @touchmove.prevent
        @wheel.prevent
      ></div>
      <!-- 시트 -->
      <div ref="sheetRef"
           class="fixed bottom-0 left-0 right-0 z-50 bg-white dark:bg-gray-900 rounded-t-3xl shadow-2xl flex flex-col overflow-hidden"
           style="max-height: min(90svh, 90vh); min-height: min(65svh, 65vh); touch-action: pan-y; will-change: transform;">
        <!-- 드래그 핸들 -->
        <div class="flex justify-center pt-3 pb-1 flex-shrink-0 cursor-grab active:cursor-grabbing"
             @touchstart.passive="onSheetTouchStart"
             @touchmove.passive="onSheetTouchMove"
             @touchend.passive="onSheetTouchEnd">
          <div class="w-10 h-1.5 rounded-full bg-slate-300 dark:bg-gray-600"></div>
        </div>
        <div
          class="flex-1 min-h-0 overflow-y-auto overflow-x-hidden overscroll-contain custom-scrollbar pb-6"
          style="touch-action: pan-y; -webkit-overflow-scrolling: touch; padding-bottom: calc(24px + env(safe-area-inset-bottom, 0px)); padding-left: env(safe-area-inset-left, 0px); padding-right: env(safe-area-inset-right, 0px);"
        >
          <BookingModal :key="modalKey" :rooms="rooms" :targetDate="targetDate" :bookings="bookings"
                        :initialData="modalInit" :editBooking="editBooking"
                        @close="closeModal" @refresh="() => { fetchBookings(); fetchMyBookings(); }" />
        </div>
      </div>
    </template>

    <MobileDrawer />
    <MobileDateSheet />
    <BookingTooltip />
    <RoomDetailModal />
    <CancelModal />
    <UserSettingsModal v-if="showUserSettings" />
    <MyBookingsPanel />
    <ToastContainer />
    <TutorialModal />

  </div>
</template>

<script setup>
import { onMounted, onUnmounted, nextTick, watch, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useApp } from './composables/useApp';
import api from './api';
import BookingModal      from './components/BookingModal.vue';
import AppSidebar        from './components/layout/AppSidebar.vue';
import AppHeader         from './components/layout/AppHeader.vue';
import MobileDrawer      from './components/layout/MobileDrawer.vue';
import MobileDateSheet   from './components/layout/MobileDateSheet.vue';
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

let _resizeHandler = null;
let _swipeTimer    = null;


const {
  isMobile, slideDir, showModal, showDrawer,
  panelWidth, resizing, modalKey, modalInit, editBooking,
  startResize, onResizeMove, onResizeEnd, closeModal,
  rooms, bookings, targetDate,
  showUserSettings,
  fetchRooms, fetchBookings, fetchMyBookings, connectSse, disconnectSse,
  initFcm, scrollDayView, viewMode, modeOrder, userMap,
  applyNotifPrefs, moveDate,
} = useApp();

// 모바일 모달 열릴 때 배경 스크롤 완전 잠금(iOS 포함)
let _lockedScrollY = 0;
watch(() => isMobile.value && showModal.value, (locked) => {
  const body = document.body;
  const html = document.documentElement;
  if (locked) {
    _lockedScrollY = window.scrollY || 0;
    html.style.overflow = 'hidden';
    body.style.overflow = 'hidden';
    body.style.position = 'fixed';
    body.style.top = `-${_lockedScrollY}px`;
    body.style.left = '0';
    body.style.right = '0';
    body.style.width = '100%';
  } else {
    html.style.overflow = '';
    body.style.overflow = '';
    body.style.position = '';
    body.style.top = '';
    body.style.left = '';
    body.style.right = '';
    body.style.width = '';
    window.scrollTo(0, _lockedScrollY);
  }
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
    _swipeTimer = setTimeout(() => closeModal(), 240);
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

// ── 뷰 캐러셀 (탭 클릭 시 일/주/월 슬라이드) ─────────────────
const carouselRef = ref(null);
let _swipeArrow   = null;
let _viewSwipeStartX   = 0;
let _viewSwipeStartY   = 0;
let _viewSwipeScrollEl = null;
let _viewSwiping       = false;
let _viewScrolling     = false;
let _viewSwipeTime     = 0;
let _viewSwipeEnabled  = false;

// 캘린더·헤더 외 영역 스와이프 → 뷰 모드(일/주/월) 전환
let _modeSwipeStartX  = 0;
let _modeSwipeEnabled = false;
const MODE_SWIPE_THRESHOLD = 50;

const VIEW_COMMIT_RATIO = 0.18;
const VIEW_FLICK_VEL    = 0.4;
const VIEW_FLICK_MIN_X  = 30;

const _slotW   = () => carouselRef.value?.parentElement?.clientWidth ?? window.innerWidth;
const _targetX = (mode) => -modeOrder.indexOf(mode) * _slotW();

const _currentSlot = () => {
  if (!carouselRef.value) return null;
  return carouselRef.value.children[modeOrder.indexOf(viewMode.value)] ?? null;
};

const _currentContent = () => {
  const slot = _currentSlot();
  return slot?.querySelector('[data-calendar-content]') ?? null;
};

const _animateCarousel = (x, ms, cb) => {
  const el = carouselRef.value;
  if (!el) { cb?.(); return; }
  el.style.transition = `transform ${ms}ms cubic-bezier(0.32,0.72,0,1)`;
  el.style.transform  = `translateX(${x}px)`;
  el.addEventListener('transitionend', () => { el.style.transition = ''; cb?.(); }, { once: true });
};

watch(viewMode, (newVal) => {
  _animateCarousel(_targetX(newVal), 300);
});

const _isInCalendarGrid = (el) => {
  while (el && el !== document.body) {
    if (el.dataset?.calendarGrid !== undefined) return true;
    el = el.parentElement;
  }
  return false;
};

const _findHorizontalScrollEl = (el) => {
  while (el && el !== document.body) {
    const ov = window.getComputedStyle(el).overflowX;
    if ((ov === 'auto' || ov === 'scroll') && el.scrollWidth > el.clientWidth) return el;
    el = el.parentElement;
  }
  return null;
};

const _removeSwipeArrow = () => {
  if (_swipeArrow) {
    _swipeArrow.parentElement?.removeChild(_swipeArrow);
    _swipeArrow = null;
  }
};

const onViewTouchStart = (e) => {
  _viewSwipeEnabled = false;
  _modeSwipeEnabled = false;
  if (!isMobile.value || showModal.value) return;

  if (!_isInCalendarGrid(e.target)) {
    // 캘린더 외 영역 → 모드 전환 스와이프 대기
    _modeSwipeEnabled = true;
    _modeSwipeStartX  = e.touches[0].clientX;
    return;
  }
  _viewSwipeEnabled = true;
  _removeSwipeArrow();
  _viewSwipeStartX   = e.touches[0].clientX;
  _viewSwipeStartY   = e.touches[0].clientY;
  _viewSwipeScrollEl = _findHorizontalScrollEl(e.target);
  _viewSwiping       = false;
  _viewScrolling     = false;
  _viewSwipeTime     = Date.now();
  if (carouselRef.value) {
    const cur = new DOMMatrix(window.getComputedStyle(carouselRef.value).transform).m41;
    carouselRef.value.style.transition = 'none';
    carouselRef.value.style.transform  = `translateX(${cur}px)`;
  }
  const content = _currentContent();
  if (content) {
    content.style.transition = 'none';
    content.style.opacity    = '';
    content.style.transform  = '';
  }
};

const onViewTouchMove = (e) => {
  if (!isMobile.value || showModal.value || _viewScrolling || !_viewSwipeEnabled) return;
  const dx = e.touches[0].clientX - _viewSwipeStartX;
  const dy = e.touches[0].clientY - _viewSwipeStartY;

  if (!_viewSwiping) {
    if (Math.abs(dx) < 8 && Math.abs(dy) < 8) return;
    if (Math.abs(dy) >= Math.abs(dx)) { _viewScrolling = true; return; }

    if (_viewSwipeScrollEl) {
      const s = _viewSwipeScrollEl;
      const atRight = s.scrollLeft + s.clientWidth >= s.scrollWidth - 1;
      const atLeft  = s.scrollLeft <= 0;
      if (dx < 0 && !atRight) { _viewScrolling = true; return; }
      if (dx > 0 && !atLeft)  { _viewScrolling = true; return; }
    }
    _viewSwiping = true;

    // 카드 내부 배경(비어 드러나는 쪽)에 화살표 삽입
    const content = _currentContent();
    const grid    = content?.parentElement;
    if (grid && !_swipeArrow) {
      const goingNext = dx < 0; // 왼쪽 스와이프 = 다음 날짜, 오른쪽 빈 공간
      _swipeArrow = document.createElement('div');
      Object.assign(_swipeArrow.style, {
        position: 'absolute',
        top: '0', bottom: '0',
        [goingNext ? 'right' : 'left']: '0',
        width: '72px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        pointerEvents: 'none',
        zIndex: '0',
        opacity: '0',
      });
      const arrowPath = goingNext
        ? '<path d="M5 2l5 5-5 5" stroke="#6366f1" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>'
        : '<path d="M9 2L4 7l5 5" stroke="#6366f1" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>';
      _swipeArrow.innerHTML = `<svg width="22" height="22" viewBox="0 0 14 14" fill="none">${arrowPath}</svg>`;
      if (getComputedStyle(grid).position === 'static') grid.style.position = 'relative';
      grid.appendChild(_swipeArrow);
    }
  }

  e.preventDefault();

  // 화살표 투명도: 드래그 거리에 따라 서서히 표시
  if (_swipeArrow) _swipeArrow.style.opacity = String(Math.min(Math.abs(dx) / 50, 1));

  const content = _currentContent();
  if (content) {
    content.style.transition = 'none';
    content.style.transform  = `translateX(${dx}px)`;
    // 주간 뷰: overflow 변경 없음 (overflow:visible 로 바꾸면 scrollLeft가 0으로
    // 초기화되어 일요일로 튀는 문제 발생 → 드래그 미리보기는 단순 이동만 사용)
  }
};

const onViewTouchEnd = (e) => {
  if (!isMobile.value || showModal.value) return;

  // 캘린더 외 영역 스와이프 → 일/주/월 모드 전환
  if (_modeSwipeEnabled) {
    _modeSwipeEnabled = false;
    const dx  = e.changedTouches[0].clientX - _modeSwipeStartX;
    if (Math.abs(dx) >= MODE_SWIPE_THRESHOLD) {
      const idx = modeOrder.indexOf(viewMode.value);
      if (dx < 0 && idx < modeOrder.length - 1) viewMode.value = modeOrder[idx + 1];
      if (dx > 0 && idx > 0)                    viewMode.value = modeOrder[idx - 1];
    }
    return;
  }

  if (!_viewSwipeEnabled) return;
  _removeSwipeArrow();
  if (!_viewSwiping) { _viewSwiping = false; _viewScrolling = false; return; }
  _viewSwiping   = false;
  _viewScrolling = false;

  const dx      = e.changedTouches[0].clientX - _viewSwipeStartX;
  const elapsed = Date.now() - _viewSwipeTime;
  const vel     = Math.abs(dx) / Math.max(elapsed, 1);
  const isFlick = vel >= VIEW_FLICK_VEL && Math.abs(dx) >= VIEW_FLICK_MIN_X;
  const commit  = _slotW() * VIEW_COMMIT_RATIO;

  const content = _currentContent();
  if (!content) return;

  // 임계값 미달 → 제자리로 복귀
  if (!isFlick && Math.abs(dx) < commit) {
    content.style.transition = 'transform 180ms cubic-bezier(0.32,0.72,0,1)';
    content.style.transform  = 'translateX(0)';
    content.addEventListener('transitionend', () => {
      content.style.transition = '';
      content.style.transform  = '';
    }, { once: true });
    return;
  }

  const direction = dx < 0 ? 1 : -1;
  const isWeek    = viewMode.value === 'week';

  // 주간 뷰: overflow 변경 없이 페이드로 전환
  // (overflow:visible 로 바꾸면 scrollLeft가 리셋되어 일요일로 튀는 문제 발생)
  if (isWeek) {
    content.style.transition = 'none';
    content.style.transform  = '';
    content.style.opacity    = '0';
    moveDate(direction);
    content.scrollLeft = 0; // 안 보이는 동안 일요일로 리셋 → 페이드 후 깜빡임 없음
    void content.offsetWidth;
    content.style.transition = 'opacity 160ms ease';
    content.style.opacity    = '1';
    content.addEventListener('transitionend', () => {
      content.style.transition = '';
      content.style.opacity    = '';
    }, { once: true });
    return;
  }

  // 일/월 뷰: 슬라이드 전환
  const w    = content.clientWidth;
  const outX = dx < 0 ? -w : w;
  const EASE = 'transform 160ms cubic-bezier(0.25,0.46,0.45,0.94)';

  moveDate(direction);
  content.style.transition = 'none';
  content.style.transform  = `translateX(${-outX}px)`;
  void content.offsetWidth;
  content.style.transition = EASE;
  content.style.transform  = 'translateX(0)';
  content.addEventListener('transitionend', () => {
    content.style.transition = '';
    content.style.transform  = '';
  }, { once: true });
};

const logout = async () => {
  try { await api.post('/auth/logout'); } catch {}
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  sessionStorage.removeItem('adminAuth');
  sessionStorage.removeItem('passwordResetRequired');
  router.push('/');
};

onMounted(async () => {
  _resizeHandler = () => { isMobile.value = window.innerWidth < 768; };
  window.addEventListener('resize', _resizeHandler);
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
  // 초기 캐러셀 위치 설정 (기본 'day'=0이면 translateX(0)이므로 대부분 불필요하지만 안전하게)
  nextTick(() => {
    if (carouselRef.value) {
      carouselRef.value.style.transform = `translateX(${_targetX(viewMode.value)}px)`;
    }
  });
});

onUnmounted(() => {
  disconnectSse();
  if (_resizeHandler) window.removeEventListener('resize', _resizeHandler);
  window.removeEventListener('mousemove', onResizeMove);
  window.removeEventListener('mouseup', onResizeEnd);
  if (_swipeTimer) clearTimeout(_swipeTimer);
});
</script>

<style scoped>
/* ── 스크롤바 ── */
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }

</style>
