<template>
  <div class="flex flex-col h-full">

    <!-- Header -->
    <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 flex-shrink-0">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-indigo-500 flex items-center justify-center flex-shrink-0">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="2" y="3" width="12" height="11" rx="2" stroke="white" stroke-width="1.5"/>
            <path d="M5 1v3M11 1v3M2 7h12" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div>
          <h3 class="text-[15px] font-black text-slate-800 leading-tight">{{ editBooking ? '예약 수정' : '새 예약' }}</h3>
          <p class="text-[11px] text-slate-400 leading-tight">{{ editBooking ? '예약 정보를 수정합니다' : '회의실을 예약합니다' }}</p>
        </div>
      </div>
      <button @click="$emit('close')"
        class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition-all">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <!-- Form -->
    <form @submit.prevent="submitBooking" class="flex-1 overflow-y-auto px-5 py-4 flex flex-col gap-4 custom-scrollbar">

      <!-- 예약자 배지 -->
      <div class="flex items-center gap-3 px-4 py-3 bg-indigo-50 rounded-xl">
        <div class="w-8 h-8 rounded-full bg-indigo-500 flex items-center justify-center flex-shrink-0">
          <svg width="13" height="13" viewBox="0 0 15 15" fill="none">
            <circle cx="7.5" cy="5" r="3" stroke="white" stroke-width="1.5"/>
            <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-[13px] font-black text-indigo-800 leading-tight truncate">{{ userName }}</p>
          <p class="text-[11px] text-indigo-400 leading-tight">{{ employeeId }}</p>
        </div>
        <span class="text-[10px] font-bold text-indigo-400 bg-indigo-100 px-2 py-0.5 rounded-full">예약자</span>
      </div>

      <!-- 회의 제목 -->
      <div class="flex flex-col gap-1.5">
        <label class="text-[12px] font-black text-slate-500 uppercase tracking-wider">회의 제목 <span class="text-red-400">*</span></label>
        <input v-model="form.title" type="text" placeholder="예) 주간 팀 미팅" required
          class="w-full bg-white border-2 border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-bold text-slate-800 outline-none focus:border-indigo-400 transition-all placeholder:text-slate-300 placeholder:font-normal" />
      </div>

      <!-- 회의실 커스텀 드롭다운 -->
      <div class="flex flex-col gap-1.5 relative">
        <label class="text-[12px] font-black text-slate-500 uppercase tracking-wider">회의실 <span class="text-red-400">*</span></label>
        <button type="button" @click="roomOpen = !roomOpen" @blur="closeRoomDelayed"
          class="w-full bg-white border-2 rounded-xl px-4 py-2.5 text-[14px] text-left flex items-center justify-between outline-none transition-all"
          :class="roomOpen ? 'border-indigo-400' : 'border-slate-200'">
          <span :class="form.roomId ? 'text-slate-800 font-bold' : 'text-slate-300 font-normal'">
            {{ form.roomId ? rooms.find(r => r.id === form.roomId)?.name : '회의실을 선택해주세요' }}
          </span>
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="transition-transform flex-shrink-0" :class="roomOpen ? 'rotate-180' : ''">
            <path d="M2 5l5 5 5-5" stroke="#94a3b8" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div v-if="roomOpen"
          class="absolute top-full left-0 right-0 mt-1 bg-white border border-slate-200 rounded-xl shadow-xl overflow-hidden z-20">
          <button v-for="r in rooms" :key="r.id" type="button"
            @mousedown.prevent="selectRoom(r)"
            class="w-full px-4 py-3 text-left text-[14px] font-semibold transition-colors flex items-center justify-between"
            :class="form.roomId === r.id
              ? 'bg-indigo-500 text-white'
              : 'text-slate-700 hover:bg-slate-50'">
            <span>{{ r.name }}</span>
            <span class="text-[11px]" :class="form.roomId === r.id ? 'text-indigo-200' : 'text-slate-400'">최대 {{ r.capacity }}인</span>
          </button>
        </div>
      </div>

      <!-- 날짜 -->
      <div class="flex flex-col gap-1.5">
        <label class="text-[12px] font-black text-slate-500 uppercase tracking-wider">날짜</label>
        <input v-model="form.startDate" type="date" required @change="clearSelection"
          :min="editBooking ? undefined : now.format('YYYY-MM-DD')"
          class="w-full bg-white border-2 border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-bold text-slate-700 outline-none focus:border-indigo-400 transition-all cursor-pointer" />
      </div>


      <!-- 시간 선택 -->
      <div class="flex flex-col gap-2">
        <!-- 헤더: 현재 시각 + 지금부터 -->
        <div class="flex items-center gap-2">
          <label class="text-[12px] font-black text-slate-500 uppercase tracking-wider">시간 선택</label>
          <span class="text-[12px] font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-lg tabular-nums">현재 {{ fmtCurrentTime }}</span>
          <button v-if="isToday" type="button" @click="jumpToNow"
            class="flex items-center gap-1 text-[10px] font-bold text-indigo-500 bg-indigo-50 hover:bg-indigo-100 px-2 py-0.5 rounded-full transition-colors">
            <svg width="9" height="9" viewBox="0 0 12 12" fill="none">
              <circle cx="6" cy="6" r="5" stroke="#6366f1" stroke-width="1.5"/>
              <path d="M6 3v3.5l2 1.5" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            지금부터
          </button>
        </div>

        <!-- 시작/종료 드롭다운 -->
        <div class="flex items-end gap-2">

          <!-- 시작 드롭다운 -->
          <div class="flex-1 relative">
            <p class="text-[10px] font-bold text-slate-400 mb-1 pl-0.5">시작</p>
            <div class="flex border-2 rounded-xl overflow-hidden transition-all"
              :class="startOpen || selectedStart ? 'border-indigo-400' : 'border-slate-200'">
              <input type="time" :value="selectedStart || ''" min="06:00" max="20:30"
                @change="e => onStartInput(e.target.value)"
                class="flex-1 bg-white px-3 py-2.5 text-[14px] font-bold outline-none min-w-0"
                :class="selectedStart ? 'text-indigo-700' : 'text-slate-400'" />
              <button type="button"
                @click="openStart"
                @blur="closeStartDelayed"
                class="px-2.5 bg-white border-l border-slate-200 flex items-center hover:bg-slate-50 transition-colors flex-shrink-0">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none" :class="startOpen ? 'rotate-180' : ''" class="transition-transform">
                  <path d="M2 4l4 4 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
            <div v-if="startOpen" ref="startListRef"
              class="absolute z-30 top-full left-0 right-0 mt-1 bg-white border border-slate-200 rounded-xl shadow-xl overflow-y-auto custom-scrollbar"
              style="max-height:220px">
              <button v-for="s in startSlots" :key="s.val" type="button"
                @mousedown.prevent="pickStart(s)"
                class="w-full flex items-center justify-between px-4 py-2.5 text-[13px] transition-colors"
                :class="s.val === selectedStart
                  ? 'bg-indigo-500 text-white font-black'
                  : s.past || s.taken
                    ? 'text-slate-300 bg-slate-50/80 cursor-not-allowed'
                    : 'text-slate-700 hover:bg-indigo-50 font-semibold cursor-pointer'">
                <span>{{ s.label }}</span>
                <span v-if="s.taken && !s.past" class="text-[10px] font-bold text-orange-400">예약됨</span>
              </button>
            </div>
          </div>

          <!-- 화살표 -->
          <div class="flex items-center pb-3 flex-shrink-0 px-1">
            <svg width="16" height="8" viewBox="0 0 18 8" fill="none">
              <path d="M0 4h16M11 1l4 3-4 3" stroke="#818cf8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>

          <!-- 종료 드롭다운 -->
          <div class="flex-1 relative">
            <p class="text-[10px] font-bold text-slate-400 mb-1 pl-0.5">종료</p>
            <div class="flex border-2 rounded-xl overflow-hidden transition-all"
              :class="endOpen || selectedEnd ? 'border-indigo-400' : selectedStart ? 'border-slate-200' : 'border-slate-100'">
              <input type="time" :value="selectedEnd || ''" min="06:30" max="21:00"
                :disabled="!selectedStart"
                @change="e => onEndInput(e.target.value)"
                class="flex-1 bg-white px-3 py-2.5 text-[14px] font-bold outline-none min-w-0 disabled:bg-slate-50 disabled:cursor-not-allowed"
                :class="selectedEnd ? 'text-indigo-700' : selectedStart ? 'text-slate-400' : 'text-slate-300'" />
              <button type="button"
                @click="openEnd"
                @blur="closeEndDelayed"
                :disabled="!selectedStart"
                class="px-2.5 bg-white border-l border-slate-200 flex items-center hover:bg-slate-50 transition-colors flex-shrink-0 disabled:opacity-40 disabled:cursor-not-allowed">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none" :class="endOpen ? 'rotate-180' : ''" class="transition-transform">
                  <path d="M2 4l4 4 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
            <div v-if="endOpen" ref="endListRef"
              class="absolute z-30 top-full left-0 right-0 mt-1 bg-white border border-slate-200 rounded-xl shadow-xl overflow-y-auto custom-scrollbar"
              style="max-height:220px">
              <button v-for="s in endSlots" :key="s.val" type="button"
                @mousedown.prevent="pickEnd(s)"
                class="w-full flex items-center justify-between px-4 py-2.5 text-[13px] font-semibold transition-colors cursor-pointer"
                :class="s.val === selectedEnd
                  ? 'bg-indigo-500 text-white font-black'
                  : 'text-slate-700 hover:bg-indigo-50'">
                <span>{{ s.label }}</span>
                <div class="flex items-center gap-1.5 flex-shrink-0">
                  <span v-if="s.taken" class="w-2 h-2 rounded-full bg-orange-400" title="예약있음"></span>
                  <span class="text-[11px] opacity-50 tabular-nums">{{ durationFrom(s.val) }}</span>
                </div>
              </button>
            </div>
          </div>

          <!-- 소요시간 뱃지 -->
          <div v-if="durationLabel" class="flex items-center pb-3 flex-shrink-0">
            <span class="text-[11px] font-black text-indigo-500 bg-indigo-50 px-2 py-1.5 rounded-lg whitespace-nowrap">{{ durationLabel }}</span>
          </div>
        </div>

        <!-- 소요시간 빠른 선택 -->
        <div v-if="selectedStart" class="flex items-center gap-1.5 flex-wrap">
          <span class="text-[10px] font-bold text-slate-400 whitespace-nowrap">소요시간</span>
          <button v-for="d in quickDurations" :key="d.mins" type="button"
            :disabled="d.disabled"
            @click="setDuration(d.mins)"
            class="px-2.5 py-1 text-[11px] font-bold rounded-lg border transition-all"
            :class="activeDuration === d.mins
              ? 'border-indigo-400 bg-indigo-500 text-white'
              : d.disabled
                ? 'border-slate-100 bg-slate-50 text-slate-300 cursor-not-allowed'
                : 'border-slate-200 bg-white text-slate-600 hover:border-indigo-400 hover:text-indigo-600 hover:bg-indigo-50 active:scale-95'">
            {{ d.label }}
          </button>
        </div>

        <!-- 예약 충돌 경고 -->
        <div v-if="hasConflict" class="flex flex-col gap-1.5 px-3 py-2.5 bg-orange-50 border border-orange-100 rounded-xl">
          <div class="flex items-center gap-1.5">
            <svg width="13" height="13" viewBox="0 0 16 16" fill="none" class="flex-shrink-0">
              <path d="M8 5v4M8 11h.01M2 8a6 6 0 1 0 12 0A6 6 0 0 0 2 8z" stroke="#f97316" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="text-[12px] font-bold text-orange-500">선택한 시간대에 이미 예약이 있습니다</span>
          </div>
          <div v-for="b in conflictingBookings" :key="b.id"
            class="flex items-center gap-2 pl-1">
            <span class="text-[11px] font-bold text-orange-400 tabular-nums whitespace-nowrap">
              {{ dayjs(b.startTime).format('HH:mm') }}–{{ dayjs(b.endTime).format('HH:mm') }}
            </span>
            <span class="text-[11px] text-orange-500 font-semibold truncate">{{ b.title }}</span>
          </div>
        </div>
      </div>

      <!-- 참석자 -->
      <div class="flex flex-col gap-1.5">
        <label class="text-[12px] font-black text-slate-500 uppercase tracking-wider">참석자 <span class="text-slate-300 font-medium normal-case text-[11px]">(선택)</span></label>
        <div class="flex flex-wrap gap-1.5 p-2 bg-white border-2 border-slate-200 rounded-xl focus-within:border-indigo-400 transition-all min-h-[44px] cursor-text"
          @click="attendeeInputRef?.focus()">
          <span v-for="a in selectedAttendees" :key="a.employeeId"
            class="flex items-center gap-1 px-2.5 py-1 bg-indigo-100 text-indigo-700 rounded-full text-[12px] font-bold">
            {{ a.name }}
            <button type="button" @click.stop="removeAttendee(a)"
              class="w-3.5 h-3.5 flex items-center justify-center rounded-full hover:bg-indigo-200 transition-colors ml-0.5">
              <svg width="7" height="7" viewBox="0 0 8 8" fill="none">
                <path d="M1 1l6 6M7 1L1 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </span>
          <input ref="attendeeInputRef"
            v-model="attendeeQuery"
            @input="onAttendeeInput"
            @keydown.enter.prevent="onEnter"
            @keydown.188.prevent="onComma"
            @keydown.escape="closeSuggestions"
            @blur="closeSuggestionsDelayed"
            type="text"
            placeholder="이름 검색 또는 직접 입력..."
            class="flex-1 min-w-[100px] bg-transparent text-[13px] font-semibold text-slate-700 outline-none placeholder:text-slate-300 placeholder:font-normal py-1 px-1" />
        </div>
        <p class="text-[11px] text-slate-400 pl-0.5">이름 입력 후 <kbd class="px-1 py-0.5 bg-slate-100 rounded text-[10px] font-bold text-slate-500">Enter</kbd>로 추가</p>

        <!-- 드롭다운 -->
        <div v-if="suggestions.length || attendeeQuery.trim()"
          class="bg-white border border-slate-200 rounded-xl shadow-lg overflow-hidden">
          <button v-for="u in suggestions" :key="u.employeeId"
            type="button" @mousedown.prevent="pickUser(u)"
            class="w-full flex items-center gap-3 px-4 py-2.5 hover:bg-indigo-50 transition-colors text-left">
            <div class="w-7 h-7 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0">
              <svg width="11" height="11" viewBox="0 0 15 15" fill="none">
                <circle cx="7.5" cy="5" r="3" stroke="#6366f1" stroke-width="1.5"/>
                <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div>
              <p class="text-[13px] font-bold text-slate-700">{{ u.name }}</p>
              <p class="text-[11px] text-slate-400">{{ u.employeeId }}</p>
            </div>
          </button>
          <button v-if="attendeeQuery.trim()" type="button" @mousedown.prevent="addFreeText"
            class="w-full flex items-center gap-3 px-4 py-2.5 hover:bg-slate-50 transition-colors text-left border-t border-slate-100">
            <div class="w-7 h-7 rounded-full bg-slate-100 flex items-center justify-center flex-shrink-0">
              <svg width="11" height="11" viewBox="0 0 12 12" fill="none">
                <path d="M6 1v10M1 6h10" stroke="#94a3b8" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </div>
            <div>
              <p class="text-[12px] font-bold text-slate-500">직접 추가</p>
              <p class="text-[11px] text-slate-400">{{ attendeeQuery.trim() }}</p>
            </div>
          </button>
        </div>
      </div>

      <!-- 상세 설명 -->
      <div class="flex flex-col gap-1.5">
        <label class="text-[12px] font-black text-slate-500 uppercase tracking-wider">메모 <span class="text-slate-300 font-medium normal-case text-[11px]">(선택)</span></label>
        <textarea v-model="form.description" rows="2" placeholder="회의 안건, 준비물 등을 입력하세요"
          class="w-full bg-white border-2 border-slate-200 rounded-xl px-4 py-2.5 text-[13px] font-semibold text-slate-700 outline-none focus:border-indigo-400 transition-all placeholder:text-slate-300 placeholder:font-normal resize-none"></textarea>
      </div>


      <!-- 에러 -->
      <div v-if="errorMsg && !hasConflict" class="flex items-center gap-2 px-3 py-2.5 bg-red-50 border border-red-100 rounded-xl">
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none" class="flex-shrink-0">
          <path d="M8 5v4M8 11h.01M2 8a6 6 0 1 0 12 0A6 6 0 0 0 2 8z" stroke="#ef4444" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <span class="text-[12px] font-semibold text-red-500">{{ errorMsg }}</span>
      </div>

      <!-- 버튼 -->
      <div class="flex gap-2 pt-1 pb-2">
        <button type="button" @click="$emit('close')"
          class="px-5 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-500 text-[13px] font-bold transition-all">
          취소
        </button>
        <button v-if="!editBooking" type="button" @click="resetForm"
          class="px-4 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-400 text-[13px] font-bold transition-all"
          title="입력 초기화">
          <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
            <path d="M2 8a6 6 0 1 0 1.5-4M2 4v4h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <button type="submit" :disabled="!canSubmit"
          :class="[
            'flex-1 py-3 rounded-xl text-[14px] font-black transition-all',
            !canSubmit
              ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
              : 'bg-indigo-500 hover:bg-indigo-600 text-white shadow-lg shadow-indigo-100 active:scale-[0.99]'
          ]">
          {{ canSubmit ? (editBooking ? '수정 완료' : '예약 확정') : !form.roomId ? '회의실을 선택해주세요' : !form.title ? '회의 제목을 입력해주세요' : selectedStart && !selectedEnd ? '종료 시간을 선택해주세요' : !selectedStart ? '시간을 선택해주세요' : (editBooking ? '수정 완료' : '예약 확정') }}
        </button>
      </div>

    </form>
  </div>
</template>

<script setup>
import { reactive, ref, computed, toRef, onMounted } from 'vue';
import dayjs from 'dayjs';
import api from '../api';
import { currentUser } from '../composables/useApp';
import { useTimeSlots }      from '../composables/booking/useTimeSlots';
import { useAttendeeSearch } from '../composables/booking/useAttendeeSearch';

const props = defineProps(['rooms', 'targetDate', 'initialData', 'bookings', 'editBooking']);
const emit  = defineEmits(['close', 'refresh']);
const editBooking = props.editBooking || null;

const now = dayjs();

// ── 사용자 정보 ──────────────────────────────────────────────
const userId     = currentUser.id;
const employeeId = currentUser.employeeId;
const userName   = currentUser.name || currentUser.employeeId;

// ── 폼 상태 ──────────────────────────────────────────────────
const defaultDateStr = props.initialData?.date || props.targetDate.format('YYYY-MM-DD');
const form = reactive({
  roomId:      editBooking?.roomId || props.initialData?.roomId || '',
  startDate:   editBooking ? dayjs(editBooking.startTime).format('YYYY-MM-DD') : defaultDateStr,
  title:       editBooking?.title || '',
  attendeeIds: [], // Long[] — useAttendeeSearch의 syncAttendees()가 채워줌
  description: editBooking?.description || '',
});

// ── 시간 슬롯 composable ──────────────────────────────────────
const {
  slots, fmt, fmtCurrentTime,
  selectedStart, selectedEnd,
  startOpen, endOpen, startListRef, endListRef,
  isToday, takenSlots,
  startSlots, endSlots,
  openStart, openEnd,
  pickStart: _pickStart, pickEnd,
  onStartInput: _onStartInput, onEndInput: _onEndInput,
  durationFrom, durationLabel, activeDuration,
  jumpToNow, quickDurations, setDuration,
  conflictingBookings, hasConflict,
  clearSelection, closeStartDelayed, closeEndDelayed,
} = useTimeSlots(form, toRef(props, 'bookings'), editBooking);

// 시간 선택 초기값 (initialData)
if (!editBooking && props.initialData?.startTime) {
  selectedStart.value = props.initialData.startTime;
}

// ── 에러 메시지 ───────────────────────────────────────────────
const errorMsg    = ref('');
const clearError  = () => { errorMsg.value = ''; };

// errorMsg 클리어를 포함한 래퍼
const pickStart   = (s) => _pickStart(s, clearError);
const onStartInput = (hhmm) => _onStartInput(hhmm, clearError);
const onEndInput   = (hhmm) => _onEndInput(hhmm, clearError);

// ── 회의실 드롭다운 ───────────────────────────────────────────
const roomOpen = ref(false);
const selectRoom = (r) => { form.roomId = r.id; roomOpen.value = false; clearSelection(); };
const closeRoomDelayed = () => { setTimeout(() => { roomOpen.value = false; }, 150); };

// ── 참석자 자동완성 composable ────────────────────────────────
const {
  selectedAttendees, attendeeQuery, suggestions, attendeeInputRef,
  loadUsers, onAttendeeInput, pickUser, addFreeText, onEnter, onComma,
  removeAttendee, closeSuggestions, closeSuggestionsDelayed,
} = useAttendeeSearch(form, userId, editBooking);

// ── 제출 가능 여부 ────────────────────────────────────────────
const isPastDate = computed(() => !editBooking && dayjs(form.startDate).isBefore(now, 'day'));
const canSubmit  = computed(() =>
  !!form.roomId && !!selectedStart.value && !!selectedEnd.value &&
  !!form.title && !isPastDate.value && !hasConflict.value
);

const submitBooking = async () => {
  errorMsg.value = '';
  if (!selectedStart.value || !selectedEnd.value) { errorMsg.value = '시간을 선택해주세요.'; return; }
  if (isPastDate.value) { errorMsg.value = '과거 날짜에는 예약할 수 없습니다.'; return; }
  if (dayjs(form.startDate).isSame(now, 'day')) {
    const [hh, mm] = selectedStart.value.split(':').map(Number);
    if (!dayjs(form.startDate).hour(hh).minute(mm).isAfter(now)) {
      errorMsg.value = '현재 시간 이후로만 예약할 수 있습니다.'; return;
    }
  }
  try {
    const payload = {
      roomId:      form.roomId,
      title:       form.title,
      organizer:   userName,
      attendeeIds: form.attendeeIds.length ? form.attendeeIds : null,
      description: form.description || null,
      startTime:   `${form.startDate}T${selectedStart.value}:00`,
      endTime:     `${form.startDate}T${selectedEnd.value}:00`,
    };
    if (editBooking) await api.put(`/bookings/${editBooking.id}`, payload);
    else             await api.post('/bookings', payload);
    emit('refresh');
    emit('close');
  } catch (e) {
    const msg = e.response?.data;
    errorMsg.value = typeof msg === 'string' ? msg : '이미 예약된 시간대입니다.';
  }
};

// ── 폼 초기화 ─────────────────────────────────────────────────
const resetForm = () => {
  form.roomId      = props.initialData?.roomId || '';
  form.startDate   = defaultDateStr;
  form.title       = '';
  form.attendeeIds = [];
  form.description = '';
  selectedStart.value     = props.initialData?.startTime || null;
  selectedEnd.value       = null;
  selectedAttendees.value = [];
  attendeeQuery.value     = '';
  suggestions.value       = [];
  errorMsg.value          = '';
};

// ── 마운트 ────────────────────────────────────────────────────
onMounted(async () => {
  if (!form.roomId && props.rooms?.length) form.roomId = props.rooms[0].id;
  await loadUsers();
});
</script>
