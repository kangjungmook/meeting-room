<template>
  <div
    ref="monthRootRef"
    :class="isMobile ? 'flex min-h-0 w-full min-w-0 flex-1 flex-col gap-3' : 'min-w-0'"
  >
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
      class="shrink-0"
      :month-cells="monthCells"
      :selected-day="selectedDay"
      :get-bookings-for-date="getBookingsForDate"
      :get-room-color="getRoomColor"
      :go-to-day="goToDay"
      @selectDay="selectDay"
    />

    <!-- ── 일별 / 월간 일정 목록 ── -->
    <AgendaList
      :class="isMobile ? 'min-h-0 min-w-0 flex-1' : ''"
      :title="selectedDay ? selectedDay.format('MM월 DD일 (ddd)') + ' 일정' : targetDate.format('YYYY년 MM월') + ' 일정'"
      :empty-text="selectedDay ? '이 날 예약이 없습니다' : '이번 달 예약이 없습니다'"
      :loading="isLoadingBookings"
      :show-date="!selectedDay"
      :scrollable="isMobile"
      :collapsible="false"
      :popup-on-click="true"
      :groups="monthGroups"
    >
      <template #extra>
        <button v-if="selectedDay" @click="selectDay(selectedDay)"
                class="flex items-center gap-1 text-[11px] font-semibold text-blue-500 hover:text-blue-700 transition-colors">
          <svg width="10" height="10" viewBox="0 0 12 12" fill="none">
            <path d="M9 3L3 9M3 3l6 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          전체 보기
        </button>
      </template>
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
import { ref, computed, nextTick } from 'vue';
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
  goToDay, openNewBooking,
} = useApp();

const selectedDay = ref(null);

const monthRootRef = ref(null);
const getScroller = () => monthRootRef.value?.closest('.custom-scrollbar');

const selectDay = async (date) => {
  const scroller = getScroller();
  const keepTop = scroller ? scroller.scrollTop : null;

  if (selectedDay.value && date && date.isSame(selectedDay.value, 'day')) {
    selectedDay.value = null;
  } else {
    selectedDay.value = date;
  }

  // 선택/해제로 아래 리스트 높이가 바뀌어도 스크롤 점프 방지
  if (scroller && keepTop != null) {
    await nextTick();
    scroller.scrollTop = keepTop;
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
