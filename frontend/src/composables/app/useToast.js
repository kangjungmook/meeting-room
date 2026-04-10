import { ref } from 'vue';

// ── 토스트 알림 상태 ──────────────────────────────────────────
export const toasts        = ref([]);
export const toastDuration = ref(5);
let _toastSeq = 0;

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
