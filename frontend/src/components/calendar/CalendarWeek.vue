<template>
  <CalendarWeekGridDesktop v-if="!isMobile" />
  <CalendarWeekGridMobile v-else />

  <!-- ── 주간 일별 일정 ── -->
  <AgendaList
    :title="`${weekDays[0].format('MM월 DD일')} – ${weekDays[6].format('MM월 DD일')} 일정`"
    empty-text="이번 주 예약이 없습니다"
    :loading="isLoadingBookings"
    show-date
    :groups="weekGroups"
  />
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
  isLoadingBookings,
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
