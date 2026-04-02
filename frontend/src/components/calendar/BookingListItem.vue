<template>
  <div class="bg-white dark:bg-gray-900 rounded-2xl overflow-hidden flex shadow-sm hover:shadow-md transition-all border border-gray-100 dark:border-gray-800 group">

    <!-- 좌측 컬러 액센트 바 -->
    <div class="w-[5px] flex-shrink-0" :style="{ background: getRoomColor(booking.roomId) }"></div>

    <div class="flex-1 px-4 py-3.5 flex items-center gap-3 min-w-0">

      <!-- 메인 콘텐츠 -->
      <div class="flex-1 min-w-0">

        <!-- 배지 행 -->
        <div class="flex items-center gap-1.5 mb-1.5 flex-wrap">
          <span class="text-[10.5px] font-bold px-2.5 py-0.5 rounded-full text-white leading-none"
                :style="{ background: getRoomColor(booking.roomId) }">
            {{ getRoomName(booking.roomId) }}
          </span>
          <span v-if="showDate"
                class="text-[10.5px] font-semibold px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 leading-none">
            {{ dayjs(booking.startTime).format('MM/DD (dd)') }}
          </span>
        </div>

        <!-- 제목 -->
        <p class="text-[15px] font-bold text-gray-800 dark:text-gray-100 truncate leading-snug">
          {{ booking.title }}
        </p>

        <!-- 주최자 + 참석자 -->
        <div class="flex flex-wrap items-center gap-x-2.5 gap-y-0.5 mt-1.5">
          <span class="flex items-center gap-1 text-[11.5px] text-gray-400 dark:text-gray-500">
            <svg width="10" height="10" viewBox="0 0 15 15" fill="none">
              <circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/>
              <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            {{ booking.organizer }}
          </span>
          <span v-if="booking.attendeeIds?.length || booking.externalAttendeeNames?.length"
                class="flex items-center gap-1 text-[11.5px] text-gray-400 dark:text-gray-500">
            <svg width="12" height="10" viewBox="0 0 18 14" fill="none">
              <circle cx="6" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
              <path d="M0.5 13c0-3 2.5-5.2 5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
              <circle cx="12" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
              <path d="M17.5 13c0-3-2.5-5.2-5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
            </svg>
            {{ resolveAttendees(booking.attendeeIds, booking.externalAttendeeNames) }}
          </span>
        </div>

      </div>

      <!-- 우측 시간 정보 -->
      <div class="flex-shrink-0 flex flex-col items-end gap-1 text-right">
        <p class="text-[13.5px] font-black text-gray-800 dark:text-gray-100 tabular-nums whitespace-nowrap">
          {{ dayjs(booking.startTime).format('HH:mm') }}
        </p>
        <p class="text-[11px] text-gray-400 dark:text-gray-500 tabular-nums whitespace-nowrap">
          ~ {{ dayjs(booking.endTime).format('HH:mm') }}
        </p>
        <span class="text-[10px] font-bold px-2 py-0.5 rounded-full text-white leading-none whitespace-nowrap"
              :style="{ background: getRoomColor(booking.roomId) }">
          {{ durationLabel(booking) }}
        </span>
      </div>

    </div>
  </div>
</template>

<script setup>
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';

defineProps({
  booking:  { type: Object, required: true },
  showDate: { type: Boolean, default: false },
});

const { getRoomColor, getRoomName, resolveAttendees, chipMinutes } = useApp();

const durationLabel = (b) => {
  const mins = chipMinutes(b.startTime, b.endTime);
  const h = Math.floor(mins / 60), m = mins % 60;
  return h > 0 && m > 0 ? `${h}시간 ${m}분` : h > 0 ? `${h}시간` : `${m}분`;
};
</script>
