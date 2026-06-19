import { ref } from 'vue';

// ── 토스트 알림 상태 ──────────────────────────────────────────
export const toasts        = ref([]);
export const toastDuration = ref(5);
let _toastSeq = 0;

// ── 자기 예약 CREATED 토스트 일시 억제 플래그 (반복 예약 일괄 생성 등) ─
export const suppressOwnCreatedUntil = ref(0);
export const suppressOwnCreatedToasts = (ms = 5000) => {
  suppressOwnCreatedUntil.value = Date.now() + ms;
};

export const addToast = (message, type = 'info', durationSec) => {
  const id = ++_toastSeq;
  toasts.value.push({ id, message, type });
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id);
  }, (durationSec ?? toastDuration.value) * 1000);
};

// ── 예약 생성 토스트 배치 처리 (연속 이벤트 묶음) ──────────────
let _createdToastTimer = null;
let _createdToastBatch = null;

export const queueCreatedToast = (organizer) => {
  const name = organizer || '누군가';
  if (!_createdToastBatch || _createdToastBatch.organizer !== name) {
    if (_createdToastTimer) clearTimeout(_createdToastTimer);
    _createdToastBatch = { organizer: name, count: 0 };
  }

  _createdToastBatch.count += 1;
  if (_createdToastTimer) clearTimeout(_createdToastTimer);
  _createdToastTimer = setTimeout(() => {
    if (!_createdToastBatch) return;
    const { organizer: batchOrganizer, count } = _createdToastBatch;
    addToast(
      count > 1
        ? `${batchOrganizer}님이 예약 ${count}건을 등록했습니다.`
        : `${batchOrganizer}님이 새 예약을 등록했습니다.`,
      'success'
    );
    _createdToastBatch = null;
    _createdToastTimer = null;
  }, 800);
};

// ── 예약 취소 토스트 배치 처리 ──────────────────────────────────
let _cancelledToastTimer = null;
let _cancelledToastCount = 0;

export const queueCancelledToast = () => {
  _cancelledToastCount += 1;
  if (_cancelledToastTimer) clearTimeout(_cancelledToastTimer);
  _cancelledToastTimer = setTimeout(() => {
    const count = _cancelledToastCount;
    addToast(
      count > 1
        ? `❌ 예약 ${count}건이 취소되었습니다.`
        : '❌ 예약이 취소되었습니다.',
      'info'
    );
    _cancelledToastCount = 0;
    _cancelledToastTimer = null;
  }, 800);
};
