<template>
  <div class="flex flex-col h-full overflow-y-auto overflow-x-hidden custom-scrollbar">

    <!-- Header -->
    <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-gray-700 flex-shrink-0">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-indigo-500 flex items-center justify-center flex-shrink-0">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="2" y="3" width="12" height="11" rx="2" stroke="white" stroke-width="1.5"/>
            <path d="M5 1v3M11 1v3M2 7h12" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div>
          <h3 class="text-[15px] font-black text-slate-800 dark:text-gray-100 leading-tight">{{ editBooking ? '예약 수정' : '새 예약' }}</h3>
          <p class="text-[11px] text-slate-400 dark:text-gray-500 leading-tight">{{ editBooking ? '예약 정보를 수정합니다' : '회의실을 예약합니다' }}</p>
        </div>
      </div>
      <button @click="$emit('close')"
        class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 dark:text-gray-500 hover:bg-slate-100 dark:hover:bg-gray-700 hover:text-slate-600 dark:hover:text-gray-300 transition-all">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <!-- Form -->
    <form @submit.prevent="submitBooking" class="flex-1 px-5 py-4 flex flex-col gap-4" style="touch-action: pan-y;">

      <!-- 예약자 배지 -->
      <div class="flex items-center gap-3 px-4 py-3 bg-indigo-50 dark:bg-indigo-900/30 rounded-xl">
        <div class="w-8 h-8 rounded-full bg-indigo-500 flex items-center justify-center flex-shrink-0">
          <svg width="13" height="13" viewBox="0 0 15 15" fill="none">
            <circle cx="7.5" cy="5" r="3" stroke="white" stroke-width="1.5"/>
            <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-[13px] font-black text-indigo-800 dark:text-indigo-300 leading-tight truncate">{{ userName }}</p>
          <p class="text-[11px] text-indigo-400 dark:text-indigo-500 leading-tight">{{ employeeId }}</p>
        </div>
        <span class="text-[10px] font-bold text-indigo-500 dark:text-indigo-400 bg-indigo-100 dark:bg-indigo-900/50 px-2 py-0.5 rounded-full">예약자</span>
      </div>

      <!-- 회의 제목 -->
      <div class="flex flex-col gap-1.5">
        <label class="text-[12px] font-black text-slate-500 dark:text-gray-400 uppercase tracking-wider">회의 제목 <span class="text-red-400">*</span></label>
        <input v-model="form.title" type="text" placeholder="예) 주간 회의" required
          class="w-full bg-white dark:bg-gray-800 border-2 border-slate-200 dark:border-gray-600 rounded-xl px-4 py-2.5 text-[14px] font-bold text-slate-800 dark:text-gray-100 outline-none focus:border-indigo-400 dark:focus:border-indigo-500 transition-all placeholder:text-slate-300 dark:placeholder:text-gray-600 placeholder:font-normal" />
      </div>

      <!-- 회의실 커스텀 드롭다운 -->
      <div class="flex flex-col gap-1.5 relative">
        <label class="text-[12px] font-black text-slate-500 dark:text-gray-400 uppercase tracking-wider">회의실 <span class="text-red-400">*</span></label>
        <button type="button" @click="roomOpen = !roomOpen" @blur="closeRoomDelayed"
          class="w-full bg-white dark:bg-gray-800 border-2 rounded-xl px-4 py-2.5 text-[14px] text-left flex items-center justify-between outline-none transition-all"
          :class="roomOpen ? 'border-indigo-400' : 'border-slate-200 dark:border-gray-600'">
          <span :class="form.roomId ? 'text-slate-800 dark:text-gray-100 font-bold' : 'text-slate-300 dark:text-gray-600 font-normal'">
            {{ form.roomId ? rooms.find(r => r.id === form.roomId)?.name : '회의실을 선택해주세요' }}
          </span>
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="transition-transform flex-shrink-0" :class="roomOpen ? 'rotate-180' : ''">
            <path d="M2 5l5 5 5-5" stroke="#94a3b8" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div v-if="roomOpen"
          class="absolute top-full left-0 right-0 mt-1 bg-white dark:bg-gray-800 border border-slate-200 dark:border-gray-700 rounded-xl shadow-xl overflow-hidden z-20">
          <button v-for="r in rooms" :key="r.id" type="button"
            @mousedown.prevent="selectRoom(r)"
            class="w-full px-4 py-3 text-left text-[14px] font-semibold transition-colors flex items-center justify-between"
            :class="form.roomId === r.id
              ? 'bg-indigo-500 text-white'
              : 'text-slate-700 dark:text-gray-200 hover:bg-slate-50 dark:hover:bg-gray-700'">
            <span>{{ r.name }}</span>
            <span class="text-[11px]" :class="form.roomId === r.id ? 'text-indigo-200' : 'text-slate-400 dark:text-gray-500'">최대 {{ r.capacity }}인</span>
          </button>
        </div>
      </div>

      <!-- 날짜 -->
      <div class="flex flex-col gap-1.5 overflow-hidden">
        <label class="text-[12px] font-black text-slate-500 dark:text-gray-400 uppercase tracking-wider">날짜 <span class="text-red-400">*</span></label>
        <DatePicker
          v-model="form.startDate"
          :min="editBooking ? '' : now.format('YYYY-MM-DD')"
          placeholder="날짜 선택"
          @update:modelValue="clearSelection"
        />
      </div>

      <!-- 반복 -->
      <div class="flex flex-col gap-2">
        <label class="text-[12px] font-black text-slate-500 dark:text-gray-400 uppercase tracking-wider">반복</label>

        <!-- 타입 선택 -->
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-1.5">
          <button v-for="opt in recurTypes" :key="opt.val" type="button"
            @click="recur.type = opt.val"
            class="py-2 rounded-xl text-[12.5px] font-bold border-2 transition-all"
            :class="recur.type === opt.val
              ? 'border-indigo-400 bg-indigo-500 text-white'
              : 'border-slate-200 dark:border-gray-600 bg-white dark:bg-gray-800 text-slate-500 dark:text-gray-400 hover:border-indigo-300 hover:text-indigo-500'">
            {{ opt.label }}
          </button>
        </div>

        <!-- 반복 설정 + 날짜 목록 -->
        <div v-if="recur.type !== 'none'" class="flex flex-col gap-2 px-3 py-3 bg-indigo-50 dark:bg-indigo-900/20 rounded-xl border border-indigo-100 dark:border-indigo-800/30">

          <!-- 종료일 -->
          <div class="flex flex-col gap-2 lg:flex-row lg:items-center">
            <span class="text-[12px] text-slate-500 dark:text-gray-400 font-semibold whitespace-nowrap">종료일</span>
            <div class="flex-1 min-w-0 w-full">
              <DatePicker v-model="recur.endDate" :min="form.startDate" placeholder="종료일 선택" />
            </div>
            <span v-if="generatedDates.length" class="text-[11px] text-indigo-400 font-bold whitespace-nowrap self-end lg:self-auto">총 {{ selectedDates.length }}회</span>
          </div>

          <!-- 날짜 목록 -->
          <div v-if="generatedDates.length > 0" class="flex flex-col gap-1">
            <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between mb-0.5">
              <span class="text-[11px] font-bold text-indigo-500 dark:text-indigo-400">예약될 날짜</span>
              <button type="button" @click="toggleAllDates"
                class="text-[11px] font-semibold text-indigo-400 hover:text-indigo-600 transition-colors self-start sm:self-auto">
                {{ selectedDates.length === generatedDates.length ? '전체 해제' : '전체 선택' }}
              </button>
            </div>
            <div class="flex flex-col gap-0.5 min-h-0 max-h-[180px] overflow-y-auto custom-scrollbar">
              <label v-for="d in generatedDates" :key="d.date"
                class="flex items-center gap-2.5 px-2.5 py-1.5 rounded-lg cursor-pointer transition-colors"
                :class="d.checked ? 'bg-white dark:bg-gray-800' : 'bg-slate-100/50 dark:bg-gray-700/30'">
                <input type="checkbox" v-model="d.checked" class="accent-indigo-500 w-3.5 h-3.5 flex-shrink-0" />
                <span class="text-[12.5px] font-bold flex-1"
                  :class="d.checked ? 'text-slate-700 dark:text-gray-200' : 'text-slate-300 dark:text-gray-600 line-through'">
                  {{ d.label }}
                </span>
                <span class="text-[11px] font-semibold"
                  :class="d.checked ? 'text-indigo-400' : 'text-slate-300 dark:text-gray-600'">
                  {{ d.dow }}
                </span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 시간 선택 -->
      <div class="flex flex-col gap-2">
        <!-- 헤더: 현재 시각 -->
        <div class="flex items-center gap-2">
          <label class="text-[12px] font-black text-slate-500 dark:text-gray-400 uppercase tracking-wider">시간 선택 <span class="text-red-400">*</span></label>
          <span class="text-[12px] font-semibold text-slate-500 dark:text-gray-400 bg-slate-100 dark:bg-gray-700 px-2 py-0.5 rounded-lg tabular-nums">현재 {{ liveTime }}</span>
        </div>

        <!-- 드롭다운 오버레이 (모바일 닫기용) -->
        <div v-if="startOpen || endOpen"
          class="fixed inset-0 z-20"
          @click="startOpen = false; endOpen = false"
          @touchstart.passive="startOpen = false; endOpen = false">
        </div>

        <!-- 시작/종료 드롭다운 -->
        <div class="flex items-end gap-2">

          <!-- 시작 드롭다운 -->
          <div class="flex-1 relative">
            <p class="text-[10px] font-bold text-slate-400 dark:text-gray-500 mb-1 pl-0.5">시작</p>
            <div class="flex border-2 rounded-xl overflow-hidden transition-all"
              :class="startOpen || selectedStart ? 'border-indigo-400' : 'border-slate-200 dark:border-gray-600'">
              <input type="time" :value="selectedStart || ''" min="06:00" max="20:30"
                @change="e => { onStartInput(e.target.value); e.target.value = selectedStart || ''; }"
                class="flex-1 bg-white dark:bg-gray-800 px-3 py-2.5 text-[14px] font-bold outline-none min-w-0"
                :class="selectedStart ? 'text-indigo-700 dark:text-indigo-400' : 'text-slate-400 dark:text-gray-600'" />
              <button type="button"
                @click="openStart"
                @blur="closeStartDelayed"
                class="px-2.5 bg-white dark:bg-gray-800 border-l border-slate-200 dark:border-gray-600 flex items-center hover:bg-slate-50 dark:hover:bg-gray-700 transition-colors flex-shrink-0">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none" :class="startOpen ? 'rotate-180' : ''" class="transition-transform">
                  <path d="M2 4l4 4 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
            <div v-if="startOpen" ref="startListRef"
              class="absolute z-30 top-full left-0 right-0 mt-1 bg-white dark:bg-gray-800 border border-slate-200 dark:border-gray-700 rounded-xl shadow-xl overflow-y-auto custom-scrollbar"
              style="max-height:220px">
              <button v-for="s in startSlots" :key="s.val" type="button"
                @mousedown.prevent="pickStart(s)"
                @touchend.prevent="pickStart(s)"
                class="w-full flex items-center justify-between px-4 py-2.5 text-[13px] transition-colors"
                :class="s.val === selectedStart
                  ? 'bg-indigo-500 text-white font-black'
                  : s.past || s.taken
                    ? 'text-slate-300 dark:text-gray-600 bg-slate-50/80 dark:bg-gray-800/80 cursor-not-allowed'
                    : 'text-slate-700 dark:text-gray-200 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 font-semibold cursor-pointer'">
                <span>{{ s.label }}</span>
                <span v-if="s.taken && !s.past" class="text-[10px] font-bold text-orange-400">예약됨</span>
              </button>
            </div>
          </div>

          <!-- 화살표 + 소요시간 -->
          <div class="flex flex-col items-center justify-end pb-3 flex-shrink-0 px-1 gap-0.5">
            <svg width="16" height="8" viewBox="0 0 18 8" fill="none">
              <path d="M0 4h16M11 1l4 3-4 3" stroke="#818cf8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span v-if="durationLabel" class="text-[10px] font-bold text-indigo-400 whitespace-nowrap tabular-nums">{{ durationLabel }}</span>
          </div>

          <!-- 종료 드롭다운 -->
          <div class="flex-1 relative">
            <p class="text-[10px] font-bold text-slate-400 dark:text-gray-500 mb-1 pl-0.5">종료</p>
            <div class="flex border-2 rounded-xl overflow-hidden transition-all"
              :class="endOpen || selectedEnd ? 'border-indigo-400' : selectedStart ? 'border-slate-200 dark:border-gray-600' : 'border-slate-100 dark:border-gray-700'">
              <input type="time" :value="selectedEnd || ''" min="06:30" max="21:00"
                :disabled="!selectedStart"
                @change="e => onEndInput(e.target.value)"
                class="flex-1 bg-white dark:bg-gray-800 px-3 py-2.5 text-[14px] font-bold outline-none min-w-0 disabled:bg-slate-50 dark:disabled:bg-gray-800/50 disabled:cursor-not-allowed"
                :class="selectedEnd ? 'text-indigo-700 dark:text-indigo-400' : selectedStart ? 'text-slate-400 dark:text-gray-600' : 'text-slate-300 dark:text-gray-700'" />
              <button type="button"
                @click="openEnd"
                @blur="closeEndDelayed"
                :disabled="!selectedStart"
                class="px-2.5 bg-white dark:bg-gray-800 border-l border-slate-200 dark:border-gray-600 flex items-center hover:bg-slate-50 dark:hover:bg-gray-700 transition-colors flex-shrink-0 disabled:opacity-40 disabled:cursor-not-allowed">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none" :class="endOpen ? 'rotate-180' : ''" class="transition-transform">
                  <path d="M2 4l4 4 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
            <div v-if="endOpen" ref="endListRef"
              class="absolute z-30 top-full left-0 right-0 mt-1 bg-white dark:bg-gray-800 border border-slate-200 dark:border-gray-700 rounded-xl shadow-xl overflow-y-auto custom-scrollbar"
              style="max-height:220px">
              <button v-for="s in endSlots" :key="s.val" type="button"
                @mousedown.prevent="pickEnd(s)"
                @touchend.prevent="pickEnd(s)"
                class="w-full flex items-center justify-between px-4 py-2.5 text-[13px] font-semibold transition-colors cursor-pointer"
                :class="s.val === selectedEnd
                  ? 'bg-indigo-500 text-white font-black'
                  : 'text-slate-700 dark:text-gray-200 hover:bg-indigo-50 dark:hover:bg-indigo-900/30'">
                <span>{{ s.label }}</span>
                <div class="flex items-center gap-1.5 flex-shrink-0">
                  <span v-if="s.taken" class="w-2 h-2 rounded-full bg-orange-400" title="예약있음"></span>
                  <span class="text-[11px] opacity-50 tabular-nums">{{ durationFrom(s.val) }}</span>
                </div>
              </button>
            </div>
          </div>

        </div>

        <!-- 소요시간 빠른 선택 -->
        <div v-if="selectedStart" class="flex items-center gap-1.5 flex-wrap">
          <span class="text-[10px] font-bold text-slate-400 dark:text-gray-500 whitespace-nowrap">소요시간</span>
          <button v-for="d in quickDurations" :key="d.mins" type="button"
            :disabled="d.disabled"
            @click="setDuration(d.mins)"
            class="px-2.5 py-1 text-[11px] font-bold rounded-lg border transition-all"
            :class="activeDuration === d.mins
              ? 'border-indigo-400 bg-indigo-500 text-white'
              : d.disabled
                ? 'border-slate-100 dark:border-gray-700 bg-slate-50 dark:bg-gray-800 text-slate-300 dark:text-gray-700 cursor-not-allowed'
                : 'border-slate-200 dark:border-gray-600 bg-white dark:bg-gray-800 text-slate-600 dark:text-gray-300 hover:border-indigo-400 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-900/20 active:scale-95'">
            {{ d.label }}
          </button>
        </div>

        <!-- 예약 충돌 경고 -->
        <div v-if="hasConflict" class="flex flex-col gap-1.5 px-3 py-2.5 bg-orange-50 dark:bg-orange-900/20 border border-orange-100 dark:border-orange-800/40 rounded-xl">
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

        <!-- 동일 시간 경고 -->
        <div v-if="isSameTime" class="flex items-center gap-2 px-3 py-2.5 bg-red-50 dark:bg-red-900/20 border border-red-100 dark:border-red-800/40 rounded-xl">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="none" class="flex-shrink-0">
            <path d="M8 5v4M8 11h.01M2 8a6 6 0 1 0 12 0A6 6 0 0 0 2 8z" stroke="#ef4444" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span class="text-[12px] font-bold text-red-500">{{ timeErrorMsg }}</span>
        </div>
      </div>

      <!-- 참석자 -->
      <div class="flex flex-col gap-1.5">
        <label class="text-[12px] font-black text-slate-500 dark:text-gray-400 uppercase tracking-wider">참석자 <span class="text-slate-300 dark:text-gray-600 font-medium normal-case text-[11px]">(선택)</span></label>
        <div class="flex flex-wrap gap-1.5 p-2 bg-white dark:bg-gray-800 border-2 border-slate-200 dark:border-gray-600 rounded-xl focus-within:border-indigo-400 dark:focus-within:border-indigo-500 transition-all min-h-[44px] cursor-text"
          @click="attendeeInputRef?.focus()">
          <span v-for="a in selectedAttendees" :key="a.employeeId"
            :class="a.id != null ? 'bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300' : 'bg-amber-100 dark:bg-amber-900/40 text-amber-700 dark:text-amber-400'"
            class="flex items-center gap-1 px-2.5 py-1 rounded-full text-[12px] font-bold">
            {{ a.name }}
            <button type="button" @click.stop="removeAttendee(a)"
              :class="a.id != null ? 'hover:bg-indigo-200 dark:hover:bg-indigo-800' : 'hover:bg-amber-200 dark:hover:bg-amber-800'"
              class="w-3.5 h-3.5 flex items-center justify-center rounded-full transition-colors ml-0.5">
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
            class="flex-1 min-w-[100px] bg-transparent text-[13px] font-semibold text-slate-700 dark:text-gray-200 outline-none placeholder:text-slate-300 dark:placeholder:text-gray-600 placeholder:font-normal py-1 px-1" />
        </div>
        <p class="text-[11px] text-red-500 pl-0.5 font-semibold">⚠ 참석자는 1명씩 입력 후 목록 클릭 또는 <kbd class="px-1 py-0.5 bg-red-50 dark:bg-red-900/30 rounded text-[10px] font-bold text-red-500">Enter</kbd>로 추가하세요.</p>

        <!-- 드롭다운 -->
        <div v-if="suggestions.length || attendeeQuery.trim()"
          class="bg-white dark:bg-gray-800 border border-slate-200 dark:border-gray-700 rounded-xl shadow-lg overflow-hidden">
          <button v-for="u in suggestions" :key="u.employeeId"
            type="button" @mousedown.prevent="pickUser(u)"
            class="w-full flex items-center gap-3 px-4 py-2.5 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 transition-colors text-left">
            <div class="w-7 h-7 rounded-full bg-indigo-100 dark:bg-indigo-900/50 flex items-center justify-center flex-shrink-0">
              <svg width="11" height="11" viewBox="0 0 15 15" fill="none">
                <circle cx="7.5" cy="5" r="3" stroke="#6366f1" stroke-width="1.5"/>
                <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div>
              <p class="text-[13px] font-bold text-slate-700 dark:text-gray-200">{{ u.name }}</p>
              <p class="text-[11px] text-slate-400 dark:text-gray-500">{{ u.employeeId }}</p>
            </div>
          </button>
          <button v-if="attendeeQuery.trim()" type="button" @mousedown.prevent="addFreeText"
            class="w-full flex items-center gap-3 px-4 py-2.5 hover:bg-slate-50 dark:hover:bg-gray-700 transition-colors text-left border-t border-slate-100 dark:border-gray-700">
            <div class="w-7 h-7 rounded-full bg-slate-100 dark:bg-gray-700 flex items-center justify-center flex-shrink-0">
              <svg width="11" height="11" viewBox="0 0 12 12" fill="none">
                <path d="M6 1v10M1 6h10" stroke="#94a3b8" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </div>
            <div>
              <p class="text-[12px] font-bold text-slate-500 dark:text-gray-400">직접 추가</p>
              <p class="text-[11px] text-slate-400 dark:text-gray-500">{{ attendeeQuery.trim() }}</p>
            </div>
          </button>
        </div>
      </div>

      <!-- 상세 설명 -->
      <div class="flex flex-col gap-1.5">
        <label class="text-[12px] font-black text-slate-500 dark:text-gray-400 uppercase tracking-wider">메모 <span class="text-slate-300 dark:text-gray-600 font-medium normal-case text-[11px]">(선택)</span></label>
        <textarea v-model="form.description" rows="2" placeholder="회의 안건, 준비물 등을 입력하세요"
          class="w-full bg-white dark:bg-gray-800 border-2 border-slate-200 dark:border-gray-600 rounded-xl px-4 py-2.5 text-[13px] font-semibold text-slate-700 dark:text-gray-200 outline-none focus:border-indigo-400 dark:focus:border-indigo-500 transition-all placeholder:text-slate-300 dark:placeholder:text-gray-600 placeholder:font-normal resize-none"></textarea>
      </div>



      <!-- 에러 -->
      <div v-if="errorMsg && !hasConflict" class="flex items-center gap-2 px-3 py-2.5 bg-red-50 dark:bg-red-900/20 border border-red-100 dark:border-red-800/40 rounded-xl">
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none" class="flex-shrink-0">
          <path d="M8 5v4M8 11h.01M2 8a6 6 0 1 0 12 0A6 6 0 0 0 2 8z" stroke="#ef4444" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <span class="text-[12px] font-semibold text-red-500">{{ errorMsg }}</span>
      </div>

      <!-- 버튼 -->
      <div class="flex gap-2 pt-1 pb-2">
        <button type="button" @click="$emit('close')"
          class="px-5 py-3 rounded-xl bg-slate-100 dark:bg-gray-700 hover:bg-slate-200 dark:hover:bg-gray-600 text-slate-500 dark:text-gray-300 text-[13px] font-bold transition-all">
            취소
        </button>
        <button v-if="!editBooking" type="button" @click="resetForm"
          class="px-4 py-3 rounded-xl bg-slate-100 dark:bg-gray-700 hover:bg-slate-200 dark:hover:bg-gray-600 text-slate-400 dark:text-gray-400 text-[13px] font-bold transition-all"
          title="입력 초기화">
          <AppIcon name="refresh" :size="14" />
        </button>
        <button type="submit" :disabled="!canSubmit || isSubmitting"
          :class="[
            'flex-1 py-3 rounded-xl text-[14px] font-black transition-all',
            (!canSubmit || isSubmitting)
              ? 'bg-slate-100 dark:bg-gray-700 text-slate-400 dark:text-gray-500 cursor-not-allowed'
              : 'bg-indigo-500 hover:bg-indigo-600 text-white shadow-lg shadow-indigo-100 dark:shadow-indigo-900/30 active:scale-[0.99]'
          ]">
          {{ isSubmitting ? (editBooking ? '수정 중...' : '예약 중...') : canSubmit ? (editBooking ? '수정 완료' : '예약 확정') : !form.roomId ? '회의실을 선택해주세요' : !form.title ? '회의 제목을 입력해주세요' : isSameTime ? '시작·종료 시간이 같습니다' : selectedStart && !selectedEnd ? '종료 시간을 선택해주세요' : !selectedStart ? '시간을 선택해주세요' : (editBooking ? '수정 완료' : '예약 확정') }}
        </button>
      </div>

    </form>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, toRef, onMounted, onUnmounted } from 'vue';
import dayjs from 'dayjs';
import api from '../api';
import AppIcon from './icons/AppIcon.vue';
import DatePicker from './ui/DatePicker.vue';
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
  attendeeIds: [],        // Long[] — useAttendeeSearch의 syncAttendees()가 채워줌
  externalAttendees: [],  // String[] — 외부 참석자 이름
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
  quickDurations, setDuration,
  conflictingBookings, hasConflict, isSameTime,
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

// ── 반복 예약 ────────────────────────────────────────────────
const recurTypes = [
  { val: 'none',    label: '없음' },
  { val: 'weekly',  label: '매주' },
  { val: 'monthly', label: '매월' },
  { val: 'yearly',  label: '매년' },
];
const recur = reactive({ type: 'none', endDate: '' });
const DOW_LABELS = ['일', '월', '화', '수', '목', '금', '토'];

const recurUnit = computed(() =>
  recur.type === 'weekly' ? 'week' : recur.type === 'monthly' ? 'month' : 'year'
);

// 체크박스 포함 날짜 목록
const generatedDates = ref([]);

const buildDates = () => {
  if (recur.type === 'none' || !form.startDate || !recur.endDate) { generatedDates.value = []; return; }
  const result = [];
  let cur = dayjs(form.startDate);
  const end = dayjs(recur.endDate);
  while (!cur.isAfter(end) && result.length < 52) {
    result.push({ date: cur.format('YYYY-MM-DD'), label: cur.format('YYYY년 MM월 DD일'), dow: DOW_LABELS[cur.day()] + '요일', checked: true });
    cur = cur.add(1, recurUnit.value);
  }
  generatedDates.value = result;
};

watch(() => [recur.type, recur.endDate, form.startDate], buildDates, { immediate: true });

const selectedDates = computed(() => generatedDates.value.filter(d => d.checked).map(d => d.date));

const toggleAllDates = () => {
  const allChecked = generatedDates.value.every(d => d.checked);
  generatedDates.value.forEach(d => { d.checked = !allChecked; });
};

const generateDates = () => {
  if (recur.type === 'none') return [form.startDate];
  return selectedDates.value.length ? selectedDates.value : [form.startDate];
};

// ── 시간 오류 메시지 ─────────────────────────────────────────
const timeErrorMsg = computed(() => {
  if (!selectedStart.value || !selectedEnd.value) return '';
  if (selectedStart.value === selectedEnd.value) return '시작 시간과 종료 시간이 같을 수 없습니다';
  if (selectedEnd.value < selectedStart.value)   return '종료 시간은 시작 시간보다 이후여야 합니다';
  return '';
});

// ── 제출 가능 여부 ────────────────────────────────────────────
const isPastDate = computed(() => !editBooking && dayjs(form.startDate).isBefore(now, 'day'));
const canSubmit  = computed(() =>
  !!form.roomId && !!selectedStart.value && !!selectedEnd.value &&
  !!form.title && !isPastDate.value && !hasConflict.value && !isSameTime.value
);

const isSubmitting = ref(false);

const submitBooking = async () => {
  if (isSubmitting.value) return;
  errorMsg.value = '';
  if (!selectedStart.value || !selectedEnd.value) { errorMsg.value = '시간을 선택해주세요.'; return; }
  if (selectedStart.value === selectedEnd.value) { errorMsg.value = '시작 시간과 종료 시간이 같을 수 없습니다.'; return; }
  if (selectedEnd.value < selectedStart.value) { errorMsg.value = '종료 시간은 시작 시간보다 이후여야 합니다.'; return; }
  if (isPastDate.value) { errorMsg.value = '지나간 날짜는 예약할 수 없습니다.'; return; }

  if (dayjs(form.startDate).isSame(now, 'day')) {
    const [hh, mm] = selectedStart.value.split(':').map(Number);
    if (!dayjs(form.startDate).hour(hh).minute(mm).isAfter(now)) {
      errorMsg.value = '현재 시간 이후로만 예약할 수 있습니다.'; return;
    }
  }
  isSubmitting.value = true;
  try {
    if (editBooking) {
      const basePayload = {
        roomId:      form.roomId,
        title:       form.title,
        organizer:   userName,
        attendeeIds: form.attendeeIds.length ? form.attendeeIds : null,
        externalAttendees: form.externalAttendees.length ? form.externalAttendees : null,
        description: form.description || null,
        startTime:   `${form.startDate}T${selectedStart.value}:00`,
        endTime:     `${form.startDate}T${selectedEnd.value}:00`,
      };
      // 기존 예약 수정
      await api.put(`/bookings/${editBooking.id}`, basePayload);
      // 반복 날짜 중 현재 날짜 제외한 나머지는 새 예약으로 생성
      const extraDates = generateDates().filter(d => d !== form.startDate);
      for (const date of extraDates) {
        await api.post('/bookings', {
          ...basePayload,
          startTime: `${date}T${selectedStart.value}:00`,
          endTime:   `${date}T${selectedEnd.value}:00`,
        });
      }
    } else {
      const dates = generateDates();
      for (const date of dates) {
        await api.post('/bookings', {
          roomId:      form.roomId,
          title:       form.title,
          organizer:   userName,
          attendeeIds: form.attendeeIds.length ? form.attendeeIds : null,
          externalAttendees: form.externalAttendees.length ? form.externalAttendees : null,
          description: form.description || null,
          startTime:   `${date}T${selectedStart.value}:00`,
          endTime:     `${date}T${selectedEnd.value}:00`,
        });
      }
    }
    emit('refresh');
    emit('close');
  } catch (e) {
    const msg = e.response?.data;
    errorMsg.value = typeof msg === 'string' ? msg : '이미 예약된 시간대입니다.';
  } finally {
    isSubmitting.value = false;
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
  recur.type    = 'none';
  recur.endDate = '';
};

// ── 실시간 시계 (초 단위) ─────────────────────────────────────
const liveTime = ref(dayjs().format('HH:mm:ss'));
let _clockTimer = null;

// ── 마운트 ────────────────────────────────────────────────────
onMounted(async () => {
  if (!form.roomId && props.rooms?.length) form.roomId = props.rooms[0].id;
  await loadUsers();
  _clockTimer = setInterval(() => { liveTime.value = dayjs().format('HH:mm:ss'); }, 1000);
});

onUnmounted(() => { clearInterval(_clockTimer); });
</script>

