<template>
  <!-- ── 월간 그리드 ── -->
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0">
    <!-- 요일 헤더 -->
    <div class="grid grid-cols-7 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
      <div v-for="(d, i) in ['일','월','화','수','목','금','토']" :key="d"
           class="text-center text-[11px] font-bold tracking-wide py-2.5"
           :class="i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-500' : 'text-gray-400'">
        {{ d }}
      </div>
    </div>
    <!-- 날짜 셀 -->
    <div class="grid grid-cols-7">
      <div v-for="(cell, i) in monthCells" :key="i"
           :class="[
             'border-r border-b border-gray-100 dark:border-gray-800 p-2 flex flex-col gap-1 cursor-pointer transition-colors min-h-[130px]',
             (i + 1) % 7 === 0 ? 'border-r-0' : '',
             cell.date.isSame(dayjs(), 'day')  ? 'bg-blue-50/60 dark:bg-blue-900/20' :
             !cell.isCurrentMonth              ? 'bg-gray-50/70 dark:bg-gray-800/50' :
             cell.date.day() === 0             ? 'bg-red-50/20 hover:bg-red-50/40 dark:bg-red-900/10 dark:hover:bg-red-900/20' :
             cell.date.day() === 6             ? 'bg-blue-50/20 hover:bg-blue-50/40 dark:bg-blue-900/10 dark:hover:bg-blue-900/20' :
             'hover:bg-gray-50/70 dark:hover:bg-gray-800/50'
           ]"
           @click="onDayClick({ date: cell.date.toDate() })">
        <div class="mb-0.5">
          <span :class="[
                  'text-[13.5px] font-bold inline-flex items-center justify-center',
                  cell.date.isSame(dayjs(), 'day')
                    ? 'w-7 h-7 rounded-full bg-blue-600 text-white text-[13px]'
                    : !cell.isCurrentMonth ? 'text-gray-300 dark:text-gray-600'
                    : cell.date.day() === 0 ? 'text-red-500'
                    : cell.date.day() === 6 ? 'text-blue-500'
                    : 'text-gray-700 dark:text-gray-200'
                ]">
            {{ cell.date.format('D') }}
          </span>
        </div>
        <!-- 예약 바 -->
        <div class="flex flex-col gap-0.5">
          <div v-for="b in (isExpanded('m-' + cell.date.format('YYYYMMDD'))
                 ? getBookingsForDate(cell.date)
                 : getBookingsForDate(cell.date).slice(0, 3))" :key="b.id"
               class="px-1.5 py-0.5 rounded-md text-[11px] truncate cursor-pointer leading-snug border-l-[2.5px]"
               :style="{ borderColor: getRoomColor(b.roomId), background: getRoomColor(b.roomId) + '15' }"
               @mouseenter="showTooltip(b, $event)"
               @mouseleave="!tooltip.pinned && (tooltip.show = false)"
               @click.stop="pinTooltip(b, $event)">
            <span class="font-bold" :style="{ color: getRoomColor(b.roomId) }">{{ dayjs(b.startTime).format('HH:mm') }}</span>
            <span class="ml-1 font-medium text-gray-600 dark:text-gray-300">{{ b.title }}</span>
          </div>
          <div v-if="getBookingsForDate(cell.date).length > 3"
               @click.stop="toggleExpand('m-' + cell.date.format('YYYYMMDD'))"
               class="px-1.5 py-0.5 rounded text-[10px] font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 transition-colors cursor-pointer text-center whitespace-nowrap leading-tight">
            <span v-if="!isExpanded('m-' + cell.date.format('YYYYMMDD'))">+{{ getBookingsForDate(cell.date).length - 3 }}개</span>
            <span v-else>접기</span>`
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- ── 월간 일정 목록 ── -->
  <div>
    <div class="flex items-center justify-between mb-3 px-1 flex-wrap gap-2">
      <div class="flex items-center gap-2">
        <p class="text-[14px] font-semibold text-gray-700 dark:text-gray-200">{{ targetDate.format('YYYY년 MM월') }} 일정</p>
        <span class="text-[12.5px] font-medium text-gray-400">{{ monthBookings.length }}건</span>
      </div>
      <SortBar />
    </div>
    <div v-if="monthBookings.length === 0"
         class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 px-6 py-10 text-center text-gray-300 dark:text-gray-600 text-[13.5px] font-medium">
      이번 달 예약이 없습니다
    </div>
    <div v-else class="grid grid-cols-1 gap-2.5">
      <BookingListItem v-for="b in sortBookings(monthBookings)" :key="b.id" :booking="b" show-date />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';
import SortBar from './SortBar.vue';
import BookingListItem from './BookingListItem.vue';

const {
  targetDate, monthCells, bookings,
  tooltip, showTooltip, pinTooltip, onDayClick,
  getRoomColor, getBookingsForDate, sortBookings,
  isExpanded, toggleExpand,
} = useApp();

const monthBookings = computed(() =>
  bookings.value.filter(b => b.status !== 'cancelled' && dayjs(b.startTime).isSame(targetDate.value, 'month'))
);
</script>
