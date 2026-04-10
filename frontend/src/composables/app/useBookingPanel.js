import { ref, reactive, watch } from 'vue';
import api from '../../api';
import { isMobile, viewMode } from './useCalendar';
import { fetchBookings, fetchMyBookings } from './useBookingData';

// ── 예약 패널 ─────────────────────────────────────────────────
export const showModal   = ref(false);
export const panelWidth  = ref(400);
export const resizing    = ref(false);
export const modalKey    = ref(0);
export const modalInit   = ref(null);
export const editBooking = ref(null);

let _resizeStartX = 0, _resizeStartW = 0;
export const startResize  = (e) => { resizing.value = true; _resizeStartX = e.clientX; _resizeStartW = panelWidth.value; };
export const onResizeMove = (e) => {
  if (!resizing.value) return;
  panelWidth.value = Math.max(300, Math.min(Math.floor(window.innerWidth / 2), _resizeStartW + (_resizeStartX - e.clientX)));
};
export const onResizeEnd  = () => { resizing.value = false; };

export const closeModal    = () => { showModal.value = false; modalInit.value = null; editBooking.value = null; };
export const openEditModal = (b) => { editBooking.value = b; modalInit.value = null; showModal.value = true; modalKey.value++; };
export const openQuickModal = (roomId, h, date) => {
  modalInit.value = {
    roomId,
    startTime: `${String(h).padStart(2, '0')}:00`,
    date: date.format('YYYY-MM-DD'),
  };
  showModal.value ? modalKey.value++ : (showModal.value = true);
};

// ── 예약 취소 ─────────────────────────────────────────────────
export const cancelTarget    = ref(null);
export const cancelError     = ref('');
export const isCancelling    = ref(false);
export const confirmCancel   = (b) => { cancelTarget.value = b; };
export const doCancel        = async () => {
  if (isCancelling.value) return;
  cancelError.value = '';
  isCancelling.value = true;
  try {
    await api.patch(`/bookings/${cancelTarget.value.id}`);
    fetchBookings();
    fetchMyBookings();
    cancelTarget.value = null;
  } catch (e) {
    const msg = e.response?.data;
    cancelError.value = typeof msg === 'string' ? msg : '취소에 실패했습니다.';
  } finally {
    isCancelling.value = false;
  }
};

// ── 내 예약 패널 표시 ──────────────────────────────────────────
export const showMyBookings = ref(false);
export const openMyBookings = () => { fetchMyBookings(); showMyBookings.value = true; };

// ── 툴팁 / 상세 팝오버 ────────────────────────────────────────
export const tooltip      = reactive({ show: false, booking: null, style: {}, pinned: false });
export const detailTarget = ref(null);
export const popoverStyle = ref({});

// 뷰 전환 시 툴팁 초기화
watch(viewMode, () => { tooltip.show = false; tooltip.pinned = false; tooltip.booking = null; });

// 팝업을 카드 오른쪽(공간 없으면 왼쪽)에 배치하는 공통 함수
const calcPopupStyle = (rect, popupW = 296, popupH = 320) => {
  const gap = 8;
  const vw  = window.innerWidth;
  const vh  = window.innerHeight;

  // 좌우: 오른쪽 공간이 충분하면 오른쪽, 아니면 왼쪽
  const left = (rect.right + gap + popupW <= vw)
    ? rect.right + gap
    : rect.left - popupW - gap;

  // 상하: 카드 상단 기준, 아래로 넘치면 위로 끌어올림
  const top = Math.min(rect.top, vh - popupH - gap);

  return { top: Math.max(gap, top) + 'px', left: Math.max(gap, left) + 'px' };
};

export const showTooltip = (b, event) => {
  if (tooltip.pinned || isMobile.value) return;
  const rect = event.currentTarget.getBoundingClientRect();
  tooltip.booking = b;
  tooltip.style   = calcPopupStyle(rect);
  tooltip.show    = true;
  tooltip.pinned  = false;
};

export const pinTooltip = (b, event) => {
  if (tooltip.pinned && tooltip.booking?.id === b.id) { tooltip.show = tooltip.pinned = false; return; }
  tooltip.booking = b; tooltip.pinned = true; tooltip.show = true;
  if (isMobile.value) {
    tooltip.style = { top: '50%', left: '50%', transform: 'translate(-50%, -50%)', width: 'calc(100vw - 48px)', maxWidth: '360px' };
  } else {
    const rect = event.currentTarget.getBoundingClientRect();
    tooltip.style = calcPopupStyle(rect);
  }
};

export const openDetail = (b, event) => {
  detailTarget.value = b;
  if (event) {
    const rect = event.currentTarget.getBoundingClientRect();
    popoverStyle.value = calcPopupStyle(rect);
  }
};
