<template>
  <div :class="isMobile ? 'flex min-h-0 w-full min-w-0 flex-1 flex-col gap-5 mt' : 'flex w-full min-w-0 flex-col gap-3 md:gap-4 min-h-0'">
    <CalendarWeekGridDesktop v-if="!isMobile" />
    <CalendarWeekGridMobile v-else />

    <!-- ── 주간 일별 일정 ── -->
    <AgendaList
      :class="isMobile ? 'min-h-0 min-w-0 flex-1' : 'min-w-0 shrink-0'"
      :title="`${weekDays[0].format('MM월 DD일')} – ${weekDays[6].format('MM월 DD일')} 일정`"
      empty-text="이번 주 예약이 없습니다"
      :loading="isLoadingBookings"
      show-date
      :scrollable="isMobile"
      :collapsible="false"
      :popup-on-click="true"
      :groups="weekGroups"
    >
      <template v-if="isMobile" #actions>
        <button @click="openNewBooking"
                class="flex items-center gap-1 px-2.5 h-6 rounded-lg bg-blue-600 hover:bg-blue-700 active:scale-95 text-white text-[11px] font-bold transition-all">
          <svg width="9" height="9" viewBox="0 0 12 12" fill="none">
            <path d="M6 1v10M1 6h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          새 예약
        </button>
      </template>
    </AgendaList>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';
import AgendaList from './AgendaList.vue';
import CalendarWeekGridDesktop from './week/CalendarWeekGridDesktop.vue';
import CalendarWeekGridMobile from './week/CalendarWeekGridMobile.vue';

const {
  weekDays, isMobile,
  getBookingsForDate, sortBookings,
  isLoadingBookings, openNewBooking,
} = useApp();

const weekGroups = computed(() =>
  weekDays.value
    .filter(d => getBookingsForDate(d).length > 0)
    .map(d => ({
      key:      d.format('YYYYMMDD'),
      dayLabel: d.format('ddd'),
      dayNum:   d.format('D'),
      dow:      d.day(),
      isToday:  d.isSame(dayjs(), 'day'),
      bookings: sortBookings(getBookingsForDate(d)),
    }))
);
</script>
