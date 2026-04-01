<template>
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex hover:shadow-md transition-all group">
    <div class="w-1.5 flex-shrink-0" :style="{ background: getRoomColor(booking.roomId) }"></div>
    <div class="flex-1 px-4 py-3.5">
      <div class="flex items-start justify-between gap-3">
        <!-- 좌측: 방 배지 + 날짜 + 제목 + 메타 -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-1.5 mb-1.5 flex-wrap">
            <span class="text-[10px] font-bold px-2 py-0.5 rounded-full text-white flex-shrink-0"
                  :style="{ background: getRoomColor(booking.roomId) }">
              {{ getRoomName(booking.roomId) }}
            </span>
            <span v-if="showDate"
                  class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 flex-shrink-0">
              {{ dayjs(booking.startTime).format('MM/DD (dd)') }}
            </span>
            <span class="text-[14px] font-bold text-gray-800 dark:text-gray-100 truncate">{{ booking.title }}</span>
          </div>
          <div class="flex flex-wrap gap-x-3 gap-y-0.5">
            <span class="flex items-center gap-1 text-[12px] text-gray-500 dark:text-gray-400">
              <svg width="11" height="11" viewBox="0 0 15 15" fill="none" class="flex-shrink-0 text-gray-400">
                <circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/>
                <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              {{ booking.organizer }}
            </span>
            <span v-if="booking.attendeeIds?.length" class="flex items-center gap-1 text-[12px] text-gray-500 dark:text-gray-400">
              <svg width="13" height="11" viewBox="0 0 18 14" fill="none" class="flex-shrink-0 text-gray-400">
                <circle cx="6" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
                <path d="M0.5 13c0-3 2.5-5.2 5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                <circle cx="12" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
                <path d="M17.5 13c0-3-2.5-5.2-5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
              </svg>
              {{ resolveAttendees(booking.attendeeIds) }}
            </span>
          </div>
          <p v-if="booking.description" class="flex items-start gap-1 text-[12px] text-gray-500 mt-1.5 leading-relaxed">
            <svg width="11" height="11" viewBox="0 0 14 14" fill="none" class="flex-shrink-0 mt-0.5 text-gray-400">
              <rect x="1" y="1" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.4"/>
              <path d="M4 5h6M4 8h4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
            {{ booking.description }}
          </p>
        </div>

        <!-- 우측: 시간 + 소요시간 -->
        <div class="text-right flex-shrink-0 flex flex-col items-end gap-1">
          <p class="text-[13px] font-black text-gray-800 dark:text-gray-100 tabular-nums whitespace-nowrap">
            {{ fmtTime(booking.startTime) }}
          </p>
          <p class="text-[11px] text-gray-400 dark:text-gray-500 tabular-nums whitespace-nowrap">
            ~ {{ dayjs(booking.endTime).format('HH:mm') }}
          </p>
          <span class="text-[10px] font-bold px-2 py-0.5 rounded-full text-white"
                :style="{ background: getRoomColor(booking.roomId) + 'cc' }">
            {{ durationLabel(booking) }}
          </span>
        </div>
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

const fmtTime = (iso) => {
  const d = dayjs(iso);
  const h = d.hour(), m = d.minute();
  const h12 = h % 12 || 12;
  return `${h < 12 ? '오전' : '오후'} ${h12}:${String(m).padStart(2, '0')}`;
};

const durationLabel = (b) => {
  const mins = chipMinutes(b.startTime, b.endTime);
  const h = Math.floor(mins / 60), m = mins % 60;
  return h > 0 && m > 0 ? `${h}시간 ${m}분` : h > 0 ? `${h}시간` : `${m}분`;
};
</script>
