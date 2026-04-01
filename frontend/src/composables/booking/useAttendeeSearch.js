/**
 * useAttendeeSearch.js — 참석자 자동완성 + 선택 관리
 *
 * @param {Ref}    form        - form reactive object (form.attendeeIds: Long[] 로 동기화)
 * @param {Number} currentUserId - 현재 사용자 DB id (본인 제외)
 * @param {Object} editBooking  - 수정 중인 예약 (기존 참석자 pre-fill용)
 */
import { ref } from 'vue';
import { nextTick } from 'vue';
import api from '../../api';

export function useAttendeeSearch(form, currentUserId, editBooking) {
  const allUsers          = ref([]);
  const selectedAttendees = ref([]);
  const attendeeQuery     = ref('');
  const suggestions       = ref([]);
  const attendeeInputRef  = ref(null);

  // ── 초기 사용자 목록 로드 ─────────────────────────────────────
  const loadUsers = async () => {
    try {
      const res = await api.get('/users');
      // 본인 제외
      allUsers.value = res.data.filter(u => u.id !== currentUserId);

      // 수정 모드: attendeeIds(Long[]) 기반으로 기존 참석자 pre-fill
      if (editBooking?.attendeeIds?.length) {
        editBooking.attendeeIds.forEach(id => {
          const user = allUsers.value.find(u => u.id === id);
          if (user) selectedAttendees.value.push(user);
        });
      }
      // 수정 모드: 외부 참석자 pre-fill
      if (editBooking?.externalAttendeeNames?.length) {
        editBooking.externalAttendeeNames.forEach(name => {
          selectedAttendees.value.push({ name, id: null, employeeId: `guest_${Date.now()}_${Math.random()}` });
        });
      }
    } catch (e) {
      console.error('[attendee] failed to load users:', e.response?.status, e.message);
    }
  };

  // ── 참석자 동기화 (DB 유저 → attendeeIds, 외부인 → externalAttendees) ─
  const syncAttendees = () => {
    form.attendeeIds = selectedAttendees.value
      .filter(a => a.id != null)
      .map(a => a.id);
    form.externalAttendees = selectedAttendees.value
      .filter(a => a.id == null)
      .map(a => a.name);
  };

  // ── 자동완성 필터링 ───────────────────────────────────────────
  const onAttendeeInput = () => {
    const q = attendeeQuery.value.trim();
    if (!q) { suggestions.value = []; return; }
    const alreadyIds = selectedAttendees.value.map(a => a.id);
    suggestions.value = allUsers.value.filter(u =>
      !alreadyIds.includes(u.id) &&
      (u.name.includes(q) || u.employeeId.includes(q))
    ).slice(0, 6);
  };

  // ── 사용자 선택 ───────────────────────────────────────────────
  const pickUser = (u) => {
    selectedAttendees.value.push(u);
    attendeeQuery.value = '';
    suggestions.value   = [];
    syncAttendees();
    nextTick(() => attendeeInputRef.value?.focus());
  };

  // ── 시스템 외부 인원 직접 추가 (표시만, attendeeIds에 미포함) ─
  const addFreeText = () => {
    const name = attendeeQuery.value.trim();
    if (!name) return;
    const already = selectedAttendees.value.some(a => a.name === name);
    if (!already) {
      // id 없는 게스트 — UI 표시용, syncAttendees에서 제외됨
      selectedAttendees.value.push({ name, id: null, employeeId: `guest_${Date.now()}` });
      syncAttendees();
    }
    attendeeQuery.value = '';
    suggestions.value   = [];
    nextTick(() => attendeeInputRef.value?.focus());
  };

  // Enter: 매칭 있으면 첫 번째 선택, 없으면 직접 추가
  const onEnter = () => {
    if (suggestions.value.length) pickUser(suggestions.value[0]);
    else addFreeText();
  };

  const onComma = () => { addFreeText(); };

  const removeAttendee = (u) => {
    selectedAttendees.value = selectedAttendees.value.filter(a => a.employeeId !== u.employeeId);
    syncAttendees();
  };

  const closeSuggestions        = () => { suggestions.value = []; };
  const closeSuggestionsDelayed = () => { setTimeout(closeSuggestions, 150); };

  return {
    allUsers, selectedAttendees, attendeeQuery, suggestions, attendeeInputRef,
    loadUsers, syncAttendees, onAttendeeInput,
    pickUser, addFreeText, onEnter, onComma, removeAttendee,
    closeSuggestions, closeSuggestionsDelayed,
  };
}
