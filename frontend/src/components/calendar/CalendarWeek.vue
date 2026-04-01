<template>
  <!-- ── 주간 그리드 ── -->
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0">
    <!-- 요일 헤더 -->
    <div class="flex border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
      <div :class="isMobile ? 'w-20' : 'w-44'" class="flex-shrink-0 border-r border-gray-200 dark:border-gray-700"></div>
      <div v-for="day in weekDays" :key="day.format()"
           class="flex-1 flex flex-col items-center gap-1 py-3 border-r border-gray-100 dark:border-gray-800 last:border-r-0"
           :class="day.day() === 0 ? 'bg-red-50/50 dark:bg-red-900/10' : day.day() === 6 ? 'bg-blue-50/50 dark:bg-blue-900/10' : ''">
        <span :class="isMobile ? 'text-[9px]' : 'text-[11px]'" class="font-bold uppercase tracking-wider"
              :style="day.day() === 0 ? { color: '#f87171' } : day.day() === 6 ? { color: '#60a5fa' } : { color: '#94a3b8' }">
          {{ day.format('ddd') }}
        </span>
        <span :class="[
                isMobile ? 'text-[14px] w-7 h-7' : 'text-[20px] w-9 h-9',
                'font-bold flex items-center justify-center rounded-full',
                day.isSame(dayjs(), 'day') ? 'bg-blue-600 text-white' :
                day.day() === 0 ? 'text-red-500' :
                day.day() === 6 ? 'text-blue-500' : 'text-gray-800 dark:text-gray-100'
              ]">
          {{ day.format('D') }}
        </span>
      </div>
    </div>
    <!-- 회의실 × 요일 셀 -->
    <div v-for="room in rooms" :key="room.id"
         class="flex border-b border-gray-100 dark:border-gray-800 last:border-b-0">
      <div :class="isMobile ? 'w-20 px-2' : 'w-44 px-5'"
           class="flex-shrink-0 flex items-center gap-2 border-r border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800"
           style="min-height: 88px;">
        <span class="w-1 h-10 rounded-full flex-shrink-0" :style="{ background: room.colorCode }"></span>
        <div>
          <p :class="isMobile ? 'text-[11px]' : 'text-[15px]'" class="font-bold text-gray-800 dark:text-gray-100 leading-tight">{{ room.name }}</p>
          <p :class="isMobile ? 'text-[9px]' : 'text-xs'" class="text-gray-400 dark:text-gray-500 mt-0.5">{{ room.capacity }}인</p>
        </div>
      </div>
      <div v-for="day in weekDays" :key="day.format()"
           :class="[
             'flex-1 border-r border-gray-100 dark:border-gray-800 last:border-r-0 p-2 flex flex-col gap-1.5 transition-colors',
             day.isSame(dayjs(), 'day') ? 'bg-blue-50/50 dark:bg-blue-900/10' : 'hover:bg-gray-50 dark:hover:bg-gray-800/50'
           ]"
           @click="openQuickModal(room.id, 9, day)">
        <!-- 예약 칩 -->
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
        <!-- 더보기 -->
        <div v-if="filterBookings(room.id, day).length > 1"
             @click.stop="toggleExpand(room.id + '-' + day.format('YYYYMMDD'))"
             class="px-2 py-1 rounded-lg font-bold text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20 hover:bg-blue-100 dark:hover:bg-blue-900/30 transition-colors cursor-pointer text-center whitespace-nowrap text-[11px]">
          <span v-if="!isExpanded(room.id + '-' + day.format('YYYYMMDD'))">+{{ filterBookings(room.id, day).length - 1 }}개 더보기</span>
          <span v-else>접기 ↑</span>
        </div>
        <div v-if="filterBookings(room.id, day).length === 0"
             class="flex-1 flex items-center justify-center py-2">
          <div class="w-7 h-7 rounded-full flex items-center justify-center bg-gray-100 dark:bg-gray-800 text-gray-300 dark:text-gray-600 hover:bg-blue-50 dark:hover:bg-blue-900/20 hover:text-blue-400 transition-colors">
            <svg width="11" height="11" viewBox="0 0 12 12" fill="none">
              <path d="M6 1v10M1 6h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
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
    <div v-if="weekBookings.length === 0"
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

const {
  rooms, bookings, weekDays, isMobile,
  tooltip, showTooltip, pinTooltip, openQuickModal,
  filterBookings, sortBookings,
  isExpanded, toggleExpand,
} = useApp();

const weekBookings = computed(() =>
  bookings.value.filter(b => b.status !== 'cancelled' && weekDays.value.some(d => dayjs(b.startTime).isSame(d, 'day')))
);
</script>
