<template>
  <!-- flat=true: 외부 카드 없이 행(row)으로만 렌더 -->
  <div :class="flat
    ? 'border-b border-gray-100 dark:border-gray-800 last:border-b-0'
    : 'bg-white dark:bg-gray-900 rounded-2xl overflow-hidden shadow-sm border border-gray-100 dark:border-gray-800'">

    <!-- 메인 행 -->
    <div class="flex transition-colors"
         :class="[
           collapsible || popupOnClick
             ? 'cursor-pointer hover:bg-gray-50/60 dark:hover:bg-gray-800/30'
             : 'cursor-default',
           expanded ? 'bg-gray-50/40 dark:bg-gray-800/20' : '',
         ]"
         @click="handleRowClick">

      <!-- 좌측 컬러 액센트 바 -->
      <div class="w-[5px] flex-shrink-0" :style="{ background: getRoomColor(booking.roomId) }"></div>

      <div class="flex-1 px-4 py-3.5 max-md:px-3 max-md:py-2.5 flex items-center gap-3 max-md:gap-2 min-w-0">

        <!-- 메인 콘텐츠 -->
        <div class="flex-1 min-w-0">

          <!-- 배지 행 -->
          <div class="flex items-center gap-1.5 mb-1.5 max-md:mb-1 flex-wrap">
            <span class="text-[10.5px] max-md:text-[10px] font-bold px-2.5 max-md:px-2 py-0.5 rounded-full text-white leading-none"
                  :style="{ background: getRoomColor(booking.roomId) }">
              {{ getRoomName(booking.roomId) }}
            </span>
            <span v-if="showDate"
                  class="text-[10.5px] max-md:text-[10px] font-semibold px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 leading-none">
              {{ dayjs(booking.startTime).format('MM/DD (dd)') }}
            </span>
          </div>

          <!-- 제목 -->
          <p class="text-[15px] max-md:text-[14px] font-bold text-gray-800 dark:text-gray-100 truncate leading-snug">
            {{ booking.title }}
          </p>

          <!-- 예약자 + 참석자 -->
          <div class="flex flex-wrap items-center gap-x-3 max-md:gap-x-2 gap-y-0.5 mt-1.5 max-md:mt-1">
            <span class="flex items-center gap-1 text-[11.5px] max-md:text-[10.5px]">
              <span class="font-bold text-[10px]" :style="{ color: getRoomColor(booking.roomId) }">예약자</span>
              <span class="text-gray-600 dark:text-gray-300">{{ booking.organizer }}</span>
            </span>
            <span v-if="booking.attendeeIds?.length || booking.externalAttendeeNames?.length"
                  class="flex items-center gap-1 text-[11.5px] max-md:text-[10.5px]">
              <span class="font-bold text-[10px]" :style="{ color: getRoomColor(booking.roomId) }">참석자</span>
              <span class="text-gray-600 dark:text-gray-300">
                {{ (booking.attendeeIds?.length || 0) + (booking.externalAttendeeNames?.length || 0) }}명
              </span>
            </span>
          </div>

        </div>

        <!-- 우측 시간 정보 -->
        <div class="flex-shrink-0 flex flex-col items-end gap-1 max-md:gap-0.5 text-right">
          <p class="text-[13.5px] max-md:text-[12.5px] font-black text-gray-800 dark:text-gray-100 tabular-nums whitespace-nowrap">
            {{ dayjs(booking.startTime).format('HH:mm') }}
          </p>
          <p class="text-[11px] max-md:text-[10px] text-gray-400 dark:text-gray-500 tabular-nums whitespace-nowrap">
            ~ {{ dayjs(booking.endTime).format('HH:mm') }}
          </p>
          <span class="text-[10px] max-md:text-[9px] font-bold px-2 max-md:px-1.5 py-0.5 rounded-full text-white leading-none whitespace-nowrap"
                :style="{ background: getRoomColor(booking.roomId) }">
            {{ durationLabel(booking) }}
          </span>
        </div>

      </div>

      <!-- 펼치기 화살표 -->
      <div v-if="collapsible" class="flex items-center pr-3 flex-shrink-0">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none"
             class="text-gray-400 dark:text-gray-500 transition-transform duration-200"
             :class="expanded ? 'rotate-180' : ''">
          <path d="M3 5l4 4 4-4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>

    </div>

    <!-- 상세 패널 -->
    <Transition
      enter-active-class="transition-all duration-200 ease-out"
      leave-active-class="transition-all duration-150 ease-in"
      enter-from-class="opacity-0 -translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-1">
      <div v-if="collapsible && expanded"
           class="ml-[5px] border-t border-gray-100 dark:border-gray-700 bg-gray-50/80 dark:bg-gray-800/50 px-4 py-4">

        <!-- 날짜 + 시간 -->
        <div class="flex items-center gap-2 text-[13px] text-gray-600 dark:text-gray-300 mb-2.5">
          <svg width="13" height="13" viewBox="0 0 14 14" fill="none" class="flex-shrink-0 text-gray-400 dark:text-gray-500">
            <circle cx="7" cy="7" r="5.5" stroke="currentColor" stroke-width="1.4"/>
            <path d="M7 4.5V7l1.5 1.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span class="font-semibold tabular-nums">
            {{ dayjs(booking.startTime).format('YYYY년 MM월 DD일 (ddd)') }}
            &nbsp;{{ dayjs(booking.startTime).format('HH:mm') }} – {{ dayjs(booking.endTime).format('HH:mm') }}
          </span>
        </div>

        <!-- 설명 -->
        <div v-if="booking.description"
             class="flex items-start gap-2 text-[13px] text-gray-500 dark:text-gray-400 mb-2.5 leading-relaxed">
          <svg width="12" height="12" viewBox="0 0 14 14" fill="none" class="flex-shrink-0 mt-0.5 text-gray-400 dark:text-gray-500">
            <rect x="1" y="1" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.4"/>
            <path d="M4 5h6M4 8h4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
          </svg>
          <span>{{ booking.description }}</span>
        </div>

        <!-- 수정 / 취소 버튼 -->
        <div v-if="canEditOrCancel(booking) || canCancel(booking)"
             class="flex gap-2 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
          <button v-if="canEditOrCancel(booking)"
                  @click.stop="openEditModal(booking); expanded = false"
                  class="flex-1 py-2 text-[13px] font-bold text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-900/30 hover:bg-indigo-100 dark:hover:bg-indigo-900/50 rounded-xl transition-colors">
            수정
          </button>
          <button v-if="canCancel(booking)"
                  @click.stop="confirmCancel(booking); expanded = false"
                  class="flex-1 py-2 text-[13px] font-bold text-red-500 dark:text-red-400 bg-red-50 dark:bg-red-900/30 hover:bg-red-100 dark:hover:bg-red-900/50 rounded-xl transition-colors">
            취소
          </button>
        </div>

      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';

const props = defineProps({
  booking:  { type: Object,  required: true },
  showDate: { type: Boolean, default: false },
  flat:     { type: Boolean, default: false },
  collapsible: { type: Boolean, default: true },
  popupOnClick: { type: Boolean, default: false },
});

const { getRoomColor, getRoomName, chipMinutes,
        canEditOrCancel, canCancel, openEditModal, confirmCancel, openDetail } = useApp();

const expanded = ref(false);

const handleRowClick = (event) => {
  if (props.collapsible) {
    expanded.value = !expanded.value;
    return;
  }
  if (props.popupOnClick) {
    openDetail(props.booking);
  }
};

const durationLabel = (b) => {
  const mins = chipMinutes(b.startTime, b.endTime);
  const h = Math.floor(mins / 60), m = mins % 60;
  return h > 0 && m > 0 ? `${h}시간 ${m}분` : h > 0 ? `${h}시간` : `${m}분`;
};
</script>
