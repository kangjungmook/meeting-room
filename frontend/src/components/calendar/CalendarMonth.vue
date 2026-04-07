<template>
  <!-- ── 월간 그리드 ── -->
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0">
    <!-- 요일 헤더 -->
    <div class="grid grid-cols-7 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
      <div v-for="(d, i) in ['일','월','화','수','목','금','토']" :key="d"
           class="text-center font-bold tracking-wide py-2.5"
           :class="[isMobile ? 'text-[10px]' : 'text-[11px]', i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-500' : 'text-gray-400']">
        {{ d }}
      </div>
    </div>

    <!-- 날짜 셀 -->
    <div class="grid grid-cols-7">
      <div v-for="(cell, i) in monthCells" :key="i"
           :class="[
             'border-r border-b border-gray-100 dark:border-gray-800 flex flex-col cursor-pointer transition-colors',
             isMobile ? 'p-1 min-h-[60px]' : 'p-2 min-h-[110px]',
             (i + 1) % 7 === 0 ? 'border-r-0' : '',
             cell.date.isSame(dayjs(), 'day')  ? 'bg-blue-50/60 dark:bg-blue-900/20' :
             !cell.isCurrentMonth              ? 'bg-gray-50/60 dark:bg-gray-800/40' :
             cell.date.day() === 0             ? 'bg-red-50/20 hover:bg-red-50/40' :
             cell.date.day() === 6             ? 'bg-blue-50/20 hover:bg-blue-50/40' :
             'hover:bg-gray-50/60 dark:hover:bg-gray-800/30'
           ]"
           @click="onDayClick({ date: cell.date.toDate() })">

        <!-- 날짜 숫자 -->
        <div :class="isMobile ? 'mb-1' : 'mb-1.5'">
          <span :class="[
                  'font-bold inline-flex items-center justify-center',
                  isMobile ? 'text-[12px]' : 'text-[13.5px]',
                  cell.date.isSame(dayjs(), 'day')
                    ? (isMobile ? 'w-5 h-5 text-[11px]' : 'w-7 h-7 text-[13px]') + ' rounded-full bg-blue-600 text-white'
                    : !cell.isCurrentMonth ? 'text-gray-300 dark:text-gray-600'
                    : cell.date.day() === 0 ? 'text-red-500'
                    : cell.date.day() === 6 ? 'text-blue-500'
                    : 'text-gray-700 dark:text-gray-200'
                ]">
            {{ cell.date.format('D') }}
          </span>
        </div>

        <!-- 모바일: 컬러 dot + 카운트 -->
        <template v-if="isMobile">
          <div v-if="getBookingsForDate(cell.date).length > 0"
               class="flex flex-col items-start gap-0.5">
            <!-- dot 최대 3개 -->
            <div class="flex gap-0.5 flex-wrap">
              <span v-for="b in getBookingsForDate(cell.date).slice(0, 3)" :key="b.id"
                    class="w-1.5 h-1.5 rounded-full flex-shrink-0"
                    :style="{ background: getRoomColor(b.roomId) }">
              </span>
            </div>
            <!-- 4개 이상이면 카운트 뱃지 -->
            <span v-if="getBookingsForDate(cell.date).length > 3"
                  class="text-[8px] font-bold text-blue-500 leading-none">
              +{{ getBookingsForDate(cell.date).length - 3 }}
            </span>
          </div>
        </template>

        <!-- 데스크탑: 텍스트 바 -->
        <template v-else>
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
                 class="px-1.5 py-0.5 rounded text-[10px] font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 transition-colors cursor-pointer text-center leading-tight">
              <span v-if="!isExpanded('m-' + cell.date.format('YYYYMMDD'))">+{{ getBookingsForDate(cell.date).length - 3 }}개</span>
              <span v-else>접기</span>
            </div>
          </div>
        </template>

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
    <SkeletonBookingList v-if="isLoadingBookings && monthBookings.length === 0" :count="5" />
    <div v-else-if="monthBookings.length === 0"
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
import SkeletonBookingList from './SkeletonBookingList.vue';

const {
  targetDate, monthCells, bookings, isMobile,
  tooltip, showTooltip, pinTooltip, onDayClick,
  getRoomColor, getBookingsForDate, sortBookings,
  isExpanded, toggleExpand, isLoadingBookings,
} = useApp();

const monthBookings = computed(() =>
  monthCells.value
    .filter(c => c.isCurrentMonth)
    .flatMap(c => getBookingsForDate(c.date))
    .filter((b, i, arr) => arr.findIndex(x => x.id === b.id) === i)
);
</script>
