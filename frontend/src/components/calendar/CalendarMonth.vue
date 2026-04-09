<template>
  <CalendarMonthGridDesktop
    v-if="!isMobile"
    :month-cells="monthCells"
    :selected-day="selectedDay"
    :get-bookings-for-date="getBookingsForDate"
    :get-room-color="getRoomColor"
    :is-expanded="isExpanded"
    :toggle-expand="toggleExpand"
    :tooltip="tooltip"
    :show-tooltip="showTooltip"
    :pin-tooltip="pinTooltip"
    @selectDay="selectDay"
  />
  <CalendarMonthGridMobile
    v-else
    :month-cells="monthCells"
    :selected-day="selectedDay"
    :get-bookings-for-date="getBookingsForDate"
    :get-room-color="getRoomColor"
    @selectDay="selectDay"
  />

  <!-- ── 일별 / 월간 일정 목록 ── -->
  <AgendaList
    :title="selectedDay ? selectedDay.format('MM월 DD일 (ddd)') + ' 일정' : targetDate.format('YYYY년 MM월') + ' 일정'"
    :empty-text="selectedDay ? '이 날 예약이 없습니다' : '이번 달 예약이 없습니다'"
    :loading="isLoadingBookings"
    :show-date="!selectedDay"
    :groups="monthGroups"
  >
    <template #extra>
      <button v-if="selectedDay" @click="selectedDay = null"
              class="flex items-center gap-1 text-[11px] font-semibold text-blue-500 hover:text-blue-700 transition-colors">
        <svg width="10" height="10" viewBox="0 0 12 12" fill="none">
          <path d="M9 3L3 9M3 3l6 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
        전체 보기
      </button>
    </template>
  </AgendaList>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';
import AgendaList from './AgendaList.vue';
import CalendarMonthGridDesktop from './month/CalendarMonthGridDesktop.vue';
import CalendarMonthGridMobile from './month/CalendarMonthGridMobile.vue';

const {
  targetDate, monthCells, bookings, isMobile,
  tooltip, showTooltip, pinTooltip,
  getRoomColor, getBookingsForDate, sortBookings,
  isExpanded, toggleExpand, isLoadingBookings,
} = useApp();

const selectedDay = ref(null);

const selectDay = (date) => {
  if (selectedDay.value && date.isSame(selectedDay.value, 'day')) {
    selectedDay.value = null;
  } else {
    selectedDay.value = date;
  }
};

// 선택된 날 → 단일 그룹 (날짜 컬럼 숨김)
// 미선택   → 이번 달 전체를 일별 그룹 (날짜 컬럼 표시)
const monthGroups = computed(() => {
  if (selectedDay.value) {
    return [{
      key:      selectedDay.value.format('YYYYMMDD'),
      bookings: sortBookings(getBookingsForDate(selectedDay.value)),
    }];
  }
  return monthCells.value
    .filter(c => c.isCurrentMonth && getBookingsForDate(c.date).length > 0)
    .map(c => ({
      key:      c.date.format('YYYYMMDD'),
      dayLabel: c.date.format('ddd'),
      dayNum:   c.date.format('D'),
      dow:      c.date.day(),
      isToday:  c.date.isSame(dayjs(), 'day'),
      bookings: sortBookings(getBookingsForDate(c.date)),
    }));
});
</script>
