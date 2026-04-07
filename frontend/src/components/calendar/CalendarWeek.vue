<template>
  <!-- ── 주간 그리드 ── -->
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0">
    <!-- 모바일: 가로 스크롤 래퍼 -->
    <div :class="isMobile ? 'overflow-x-auto' : ''">
      <div :style="isMobile ? 'min-width: 620px' : ''">

        <!-- 요일 헤더 -->
        <div class="flex border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
          <div class="w-28 flex-shrink-0 border-r border-gray-200 dark:border-gray-700 sticky left-0 z-20 bg-gray-50 dark:bg-gray-800"></div>
          <div v-for="day in weekDays" :key="day.format()"
               class="flex-1 flex flex-col items-center gap-1 py-2.5 border-r border-gray-100 dark:border-gray-800 last:border-r-0 cursor-pointer select-none"
               :class="day.day() === 0 ? 'bg-red-50/50 dark:bg-red-900/10' : day.day() === 6 ? 'bg-blue-50/50 dark:bg-blue-900/10' : ''"
               @click.stop="goToDay(day)">
            <span class="text-[10px] font-bold uppercase tracking-wider"
                  :style="day.day() === 0 ? { color: '#f87171' } : day.day() === 6 ? { color: '#60a5fa' } : { color: '#94a3b8' }">
              {{ day.format('ddd') }}
            </span>
            <span :class="[
                    isMobile ? 'text-[13px] w-7 h-7' : 'text-[20px] w-9 h-9',
                    'font-bold flex items-center justify-center rounded-full transition-all',
                    day.isSame(dayjs(), 'day') ? 'bg-blue-600 text-white' :
                    day.day() === 0 ? 'text-red-500 hover:bg-red-50' :
                    day.day() === 6 ? 'text-blue-500 hover:bg-blue-50' : 'text-gray-800 dark:text-gray-100 hover:bg-gray-100 dark:hover:bg-gray-700'
                  ]">
              {{ day.format('D') }}
            </span>
          </div>
        </div>

        <!-- 회의실 × 요일 셀 -->
        <div v-for="room in rooms" :key="room.id"
             class="flex border-b border-gray-100 dark:border-gray-800 last:border-b-0">
          <div class="w-28 px-2.5 flex-shrink-0 flex items-center gap-2 border-r border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 sticky left-0 z-10"
               :style="{ minHeight: isMobile ? '90px' : '130px' }">
            <span class="w-1.5 h-8 rounded-full flex-shrink-0" :style="{ background: room.colorCode }"></span>
            <div class="min-w-0">
              <p class="text-[13px] font-bold text-gray-800 dark:text-gray-100 leading-tight truncate">{{ room.name }}</p>
              <p class="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">{{ room.capacity }}인</p>
            </div>
          </div>
          <div v-for="day in weekDays" :key="day.format()"
               :class="[
                 'flex-1 border-r border-gray-100 dark:border-gray-800 last:border-r-0 p-2 flex flex-col gap-1.5 transition-colors',
                 day.isSame(dayjs(), 'day') ? 'bg-blue-50/50 dark:bg-blue-900/10' : 'hover:bg-gray-50 dark:hover:bg-gray-800/50'
               ]"
               @click="openQuickModal(room.id, 9, day)">

            <!-- 예약 칩 (데스크탑: 상세 / 모바일: 간소화) -->
            <template v-if="!isMobile">
              <div v-for="b in (isExpanded(room.id + '-' + day.format('YYYYMMDD'))
                     ? filterBookings(room.id, day)
                     : filterBookings(room.id, day).slice(0, 1))" :key="b.id"
                   @mouseenter="showTooltip(b, $event)"
                   @mouseleave="!tooltip.pinned && (tooltip.show = false)"
                   @click.stop="pinTooltip(b, $event)"
                   class="rounded-xl px-3 py-2 cursor-pointer transition-all hover:brightness-95 border-l-[3px]"
                   :style="{ borderLeftColor: room.colorCode, background: room.colorCode + '18' }">
                <p class="text-[12px] font-black text-gray-800 dark:text-gray-100 truncate leading-tight">{{ b.title }}</p>
                <p class="text-[11px] font-semibold mt-0.5" :style="{ color: room.colorCode }">
                  {{ dayjs(b.startTime).format('HH:mm') }} – {{ dayjs(b.endTime).format('HH:mm') }}
                </p>
                <p v-if="b.organizer" class="text-[10px] text-gray-400 dark:text-gray-500 mt-0.5 truncate">{{ b.organizer }}</p>
              </div>
              <div v-if="filterBookings(room.id, day).length > 1"
                   @click.stop="toggleExpand(room.id + '-' + day.format('YYYYMMDD'))"
                   class="px-2 py-1 rounded-lg font-bold text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20 hover:bg-blue-100 transition-colors cursor-pointer text-center text-[11px]">
                <span v-if="!isExpanded(room.id + '-' + day.format('YYYYMMDD'))">+{{ filterBookings(room.id, day).length - 1 }}개</span>
                <span v-else>접기 ↑</span>
              </div>
            </template>

            <!-- 모바일 간소화 칩 -->
            <template v-else>
              <div v-for="b in filterBookings(room.id, day).slice(0, 2)" :key="b.id"
                   @click.stop="pinTooltip(b, $event)"
                   class="rounded-lg px-1.5 py-1 cursor-pointer transition-all hover:brightness-95 border-l-2"
                   :style="{ borderLeftColor: room.colorCode, background: room.colorCode + '18' }">
                <p class="text-[10px] font-bold truncate leading-tight" :style="{ color: room.colorCode }">
                  {{ dayjs(b.startTime).format('HH:mm') }}
                </p>
                <p class="text-[9px] text-gray-600 dark:text-gray-300 truncate leading-tight">{{ b.title }}</p>
              </div>
              <div v-if="filterBookings(room.id, day).length > 2"
                   @click.stop="goToDay(day)"
                   class="text-[9px] font-bold text-blue-500 text-center">
                +{{ filterBookings(room.id, day).length - 2 }}
              </div>
            </template>

            <!-- 빈 셀 -->
            <div v-if="filterBookings(room.id, day).length === 0"
                 class="flex-1 flex items-center justify-center py-1">
              <div class="w-6 h-6 rounded-full flex items-center justify-center bg-gray-100 dark:bg-gray-800 text-gray-300 dark:text-gray-600 hover:bg-blue-50 hover:text-blue-400 transition-colors">
                <svg width="9" height="9" viewBox="0 0 12 12" fill="none">
                  <path d="M6 1v10M1 6h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>

  <!-- ── 주간 일정 목록 ── -->
  <div>
    <div class="flex items-center justify-between mb-3 px-1 flex-wrap gap-2">
      <div class="flex items-center gap-2">
        <p class="text-[15px] font-bold text-gray-700 dark:text-gray-200">
          {{ weekDays[0].format('MM월 DD일') }} – {{ weekDays[6].format('MM월 DD일') }} 일정
        </p>
        <span class="text-[13px] font-semibold text-gray-400 dark:text-gray-500">{{ weekBookings.length }}건</span>
      </div>
      <SortBar />
    </div>
    <SkeletonBookingList v-if="isLoadingBookings && weekBookings.length === 0" :count="4" />
    <div v-else-if="weekBookings.length === 0"
         class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 px-6 py-10 text-center text-gray-300 dark:text-gray-600 text-[14px] font-semibold">
      이번 주 예약이 없습니다
    </div>
    <div v-else class="grid grid-cols-1 gap-3">
      <BookingListItem v-for="b in sortBookings(weekBookings)" :key="b.id" :booking="b" show-date />
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
  rooms, bookings, weekDays, isMobile,
  tooltip, showTooltip, pinTooltip, openQuickModal,
  filterBookings, getBookingsForDate, sortBookings,
  isExpanded, toggleExpand,
  goToDay, isLoadingBookings,
} = useApp();

const weekBookings = computed(() =>
  weekDays.value.flatMap(d => getBookingsForDate(d))
    .filter((b, i, arr) => arr.findIndex(x => x.id === b.id) === i)
);
</script>
