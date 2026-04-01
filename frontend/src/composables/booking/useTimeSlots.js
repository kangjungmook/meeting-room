/**
 * useTimeSlots.js — 예약 시간 슬롯 선택 로직
 *
 * @param {Object} form        - reactive 폼 상태 (roomId, startDate)
 * @param {Ref}    bookings    - 예약 목록 (props.bookings)
 * @param {Object} editBooking - 수정 중인 예약 (null이면 새 예약)
 */
import { ref, computed, nextTick } from 'vue';
import dayjs from 'dayjs';

export function useTimeSlots(form, bookings, editBooking) {
  const now = dayjs();

  // ── 슬롯 목록 (06:00~21:00, 30분 단위) ──────────────────────
  const slots = Array.from({ length: 31 }, (_, i) => {
    const totalMin = 6 * 60 + i * 30;
    const h = Math.floor(totalMin / 60), m = totalMin % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
  });

  // 오전/오후 포맷
  const fmt = (hhmm) => {
    if (!hhmm) return '';
    const [h, m] = hhmm.split(':').map(Number);
    const h12 = h % 12 || 12;
    return `${h < 12 ? '오전' : '오후'} ${h12}:${String(m).padStart(2, '0')}`;
  };

  const fmtCurrentTime = computed(() => fmt(dayjs().format('HH:mm')));

  // ── 선택 상태 ─────────────────────────────────────────────────
  const selectedStart = ref(null);
  const selectedEnd   = ref(null);

  const startOpen    = ref(false);
  const endOpen      = ref(false);
  const startListRef = ref(null);
  const endListRef   = ref(null);

  // ── 초기화 ────────────────────────────────────────────────────
  if (editBooking) {
    selectedStart.value = dayjs(editBooking.startTime).format('HH:mm');
    selectedEnd.value   = dayjs(editBooking.endTime).format('HH:mm');
  }

  const clearSelection = () => { selectedStart.value = null; selectedEnd.value = null; };

  // ── 지난 슬롯 체크 ────────────────────────────────────────────
  const isToday    = computed(() => dayjs(form.startDate).isSame(now, 'day'));
  const isPastSlot = (slot) => {
    if (editBooking) return false;
    if (!isToday.value) return false;
    const [h, m] = slot.split(':').map(Number);
    return !dayjs(form.startDate).hour(h).minute(m).isAfter(now);
  };

  // ── 예약된 슬롯 ───────────────────────────────────────────────
  const takenSlots = computed(() => {
    if (!form.roomId || !form.startDate) return new Set();
    const dayBookings = (bookings.value || []).filter(b =>
      String(b.roomId) === String(form.roomId) &&
      dayjs(b.startTime).format('YYYY-MM-DD') === form.startDate &&
      (!editBooking || b.id !== editBooking.id)
    );
    return new Set(slots.filter(slot => {
      const st = dayjs(`${form.startDate}T${slot}:00`);
      const en = st.add(30, 'minute');
      return dayBookings.some(b => st.isBefore(dayjs(b.endTime)) && en.isAfter(dayjs(b.startTime)));
    }));
  });

  // ── 드롭다운 옵션 ─────────────────────────────────────────────
  const startSlots = computed(() =>
    slots.filter(s => s !== '21:00').map(s => ({
      val: s, label: fmt(s), past: isPastSlot(s), taken: takenSlots.value.has(s),
    }))
  );
  const endSlots = computed(() => {
    if (!selectedStart.value) return [];
    return slots.filter(s => s > selectedStart.value).map(s => ({
      val: s, label: fmt(s), taken: takenSlots.value.has(s),
    }));
  });

  // ── 드롭다운 열기 (자동 스크롤) ──────────────────────────────
  const openStart = async () => {
    startOpen.value = true; endOpen.value = false;
    await nextTick();
    const target = selectedStart.value || (isToday.value ? dayjs().format('HH:mm') : '09:00');
    const idx = startSlots.value.findIndex(s => s.val >= target);
    if (startListRef.value && idx > 1) startListRef.value.scrollTop = (idx - 2) * 40;
  };
  const openEnd = async () => {
    if (!selectedStart.value) return;
    endOpen.value = true; startOpen.value = false;
    await nextTick();
    if (endListRef.value && selectedEnd.value) {
      const idx = endSlots.value.findIndex(s => s.val === selectedEnd.value);
      if (idx > 1) endListRef.value.scrollTop = (idx - 2) * 40;
    }
  };

  // ── 선택 핸들러 (errorMsg는 컴포넌트에서 주입) ────────────────
  const pickStart = (s, clearError) => {
    if (s.past || s.taken) return;
    clearError?.();
    selectedStart.value = s.val;
    if (selectedEnd.value && selectedEnd.value <= s.val) selectedEnd.value = null;
    startOpen.value = false;
  };
  const pickEnd   = (s) => { selectedEnd.value = s.val; endOpen.value = false; };

  // ── 직접 입력 핸들러 ──────────────────────────────────────────
  const onStartInput = (hhmm, clearError) => {
    clearError?.();
    if (!hhmm) { selectedStart.value = null; return; }
    selectedStart.value = hhmm;
    startOpen.value = false;
    if (selectedEnd.value && selectedEnd.value <= hhmm) selectedEnd.value = null;
  };
  const onEndInput = (hhmm, clearError) => {
    clearError?.();
    if (!hhmm) { selectedEnd.value = null; return; }
    if (selectedStart.value && hhmm <= selectedStart.value) return;
    selectedEnd.value = hhmm;
    endOpen.value = false;
  };

  // ── 소요시간 표시 ─────────────────────────────────────────────
  const durationFrom = (endHhmm) => {
    if (!selectedStart.value || !endHhmm) return '';
    const [sh, sm] = selectedStart.value.split(':').map(Number);
    const [eh, em] = endHhmm.split(':').map(Number);
    const mins = (eh * 60 + em) - (sh * 60 + sm);
    if (mins <= 0) return '';
    const h = Math.floor(mins / 60), m = mins % 60;
    return h > 0 && m > 0 ? `${h}h${m}m` : h > 0 ? `${h}h` : `${m}m`;
  };

  const durationLabel = computed(() => {
    if (!selectedStart.value || !selectedEnd.value) return '';
    const [sh, sm] = selectedStart.value.split(':').map(Number);
    const [eh, em] = selectedEnd.value.split(':').map(Number);
    const mins = eh * 60 + em - sh * 60 - sm;
    if (mins <= 0) return '';
    const h = Math.floor(mins / 60), m = mins % 60;
    return h > 0 && m > 0 ? `${h}시간 ${m}분` : h > 0 ? `${h}시간` : `${m}분`;
  });

  const activeDuration = computed(() => {
    if (!selectedStart.value || !selectedEnd.value) return null;
    const [sh, sm] = selectedStart.value.split(':').map(Number);
    const [eh, em] = selectedEnd.value.split(':').map(Number);
    return (eh * 60 + em) - (sh * 60 + sm);
  });

  // ── 지금부터 ──────────────────────────────────────────────────
  const jumpToNow = () => {
    if (!isToday.value) return;
    const hhmm = dayjs().format('HH:mm');
    if (hhmm < '06:00' || hhmm >= '21:00') return;
    selectedStart.value = hhmm;
    startOpen.value = false;
    if (selectedEnd.value && selectedEnd.value <= hhmm) selectedEnd.value = null;
  };

  // ── 빠른 소요시간 선택 ────────────────────────────────────────
  const quickDurations = computed(() => {
    const opts = [
      { mins: 30,  label: '30분'    },
      { mins: 60,  label: '1시간'   },
      { mins: 90,  label: '1.5시간' },
      { mins: 120, label: '2시간'   },
      { mins: 180, label: '3시간'   },
    ];
    if (!selectedStart.value) return opts.map(o => ({ ...o, disabled: true }));
    return opts.map(o => {
      const [sh, sm] = selectedStart.value.split(':').map(Number);
      return { ...o, disabled: sh * 60 + sm + o.mins > 21 * 60 };
    });
  });

  const setDuration = (mins) => {
    if (!selectedStart.value) return;
    const [sh, sm] = selectedStart.value.split(':').map(Number);
    const endMins = sh * 60 + sm + mins;
    if (endMins > 21 * 60) return;
    selectedEnd.value = `${String(Math.floor(endMins / 60)).padStart(2, '0')}:${String(endMins % 60).padStart(2, '0')}`;
  };

  // ── 충돌 감지 ─────────────────────────────────────────────────
  const conflictingBookings = computed(() => {
    if (!selectedStart.value || !selectedEnd.value || !form.roomId) return [];
    const date = form.startDate;
    const dayBookings = (bookings.value || []).filter(b =>
      String(b.roomId) === String(form.roomId) &&
      dayjs(b.startTime).format('YYYY-MM-DD') === date &&
      b.status !== 'cancelled' &&
      (!editBooking || b.id !== editBooking.id)
    );
    const start = dayjs(`${date}T${selectedStart.value}:00`);
    const end   = dayjs(`${date}T${selectedEnd.value}:00`);
    return dayBookings.filter(b => start.isBefore(dayjs(b.endTime)) && end.isAfter(dayjs(b.startTime)));
  });

  const hasConflict = computed(() => conflictingBookings.value.length > 0);

  // ── 닫기 지연 헬퍼 ────────────────────────────────────────────
  const closeStartDelayed = () => { setTimeout(() => { startOpen.value = false; }, 150); };
  const closeEndDelayed   = () => { setTimeout(() => { endOpen.value   = false; }, 150); };

  return {
    slots, fmt, fmtCurrentTime,
    selectedStart, selectedEnd,
    startOpen, endOpen, startListRef, endListRef,
    isToday, takenSlots,
    startSlots, endSlots,
    openStart, openEnd,
    pickStart, pickEnd,
    onStartInput, onEndInput,
    durationFrom, durationLabel, activeDuration,
    jumpToNow, quickDurations, setDuration,
    conflictingBookings, hasConflict,
    clearSelection, closeStartDelayed, closeEndDelayed,
  };
}
