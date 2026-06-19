<template>
  <div class="flex min-h-0 w-full min-w-0 flex-1 flex-col gap-3 md:gap-4">
    <div
      data-calendar-grid
      class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 overflow-hidden flex min-h-0 w-full min-w-0 shrink-0 flex-col rounded-xl md:rounded-2xl
             md:max-h-[calc(85vh-220px)]
             max-md:h-[clamp(240px,45%,380px)] max-md:flex-none"
    >

    <!-- 헤더 -->
    <div class="flex-shrink-0 z-20 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700"
         :style="{ display: 'grid', gridTemplateColumns: dayGridColumns }">
      <div class="h-12 shrink-0 border-r border-gray-200 dark:border-gray-700 md:h-14"></div>
      <div v-for="room in rooms" :key="room.id"
           class="flex h-12 min-w-0 items-center gap-1.5 border-r border-gray-100 px-2 dark:border-gray-800 last:border-r-0 md:h-14 md:gap-2 md:px-3">
        <span class="w-2.5 h-2.5 rounded-full flex-shrink-0"
              :style="{ background: room.colorCode }"></span>
        <div class="min-w-0">
          <p class="text-[12px] font-bold text-gray-800 dark:text-gray-100 truncate leading-tight">{{ room.name }}</p>
          <p class="text-[10px] text-gray-400 dark:text-gray-500">{{ room.capacity }}인</p>
        </div>
      </div>
    </div>

    <div
      ref="dayViewRef"
      data-calendar-content
      class="min-h-0 flex-1 overflow-y-auto custom-scrollbar md:overflow-x-auto"
      :class="isMobile ? 'overflow-x-hidden' : ''"
    >
      <div
        class="relative w-full min-w-0 md:w-auto"
        :style="dayGridBodyStyle"
      >

        <!-- 시간 레이블 -->
        <div class="relative border-r border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900">
          <div v-for="h in hours" :key="h"
               class="absolute right-0.5 text-[9px] font-semibold tabular-nums text-gray-400 select-none dark:text-gray-600 md:right-2 md:text-[11px]"
               :style="{ top: (h - 6) * HOUR_H + 'px', transform: 'translateY(-50%)' }">
            {{ String(h).padStart(2, '0') }}
          </div>
          <div class="absolute right-0.5 text-[9px] font-semibold tabular-nums text-gray-400 select-none dark:text-gray-600 md:right-2 md:text-[11px]"
               :style="{ top: GRID_H + 'px', transform: 'translateY(-50%)' }">
            21
          </div>
        </div>

        <div v-for="({ room, layout }, ri) in roomLayouts" :key="room.id"
             class="relative border-r border-gray-100 dark:border-gray-800 last:border-r-0">

          <div v-for="h in hours" :key="'line-' + h"
               class="absolute left-0 right-0 border-t pointer-events-none"
               :class="h % 2 === 0
                 ? 'border-gray-200 dark:border-gray-700'
                 : 'border-gray-100 dark:border-gray-800'"
               :style="{ top: (h - 6) * HOUR_H + 'px' }">
          </div>

          <!-- 현재 시간선 -->
          <div v-if="nowLineVisible"
               class="absolute left-0 right-0 z-20 pointer-events-none"
               :style="{ top: nowLineTop }">
            <div class="relative h-px bg-red-400/70">
              <div class="absolute -left-[3px] -top-[3px] w-2 h-2 rounded-full bg-red-400/70"></div>
              <div v-if="ri === 0 && !isMobile"
                   class="absolute left-[-8px] -translate-x-full -top-2 px-1 py-0.5 rounded bg-white/90 dark:bg-gray-900/90 border border-red-300/60 text-[9px] leading-none font-semibold text-red-500 tabular-nums whitespace-nowrap shadow-sm">
                {{ liveNow.format('HH:mm') }}
              </div>
            </div>
          </div>

          <!-- 빈 시간 클릭 -->
          <div v-for="h in hours" :key="'click-' + h"
               class="absolute left-0 right-0 cursor-pointer hover:bg-indigo-50/30 dark:hover:bg-indigo-900/10 transition-colors"
               :style="{ top: (h - 6) * HOUR_H + 'px', height: HOUR_H + 'px' }"
               @click="openQuickModal(room.id, h, targetDate)">
          </div>

          <!-- 예약 블록 -->
          <div v-for="b in layout.items" :key="b.id"
               class="absolute rounded-lg cursor-pointer z-10 transition-all duration-150 hover:shadow-md hover:z-30 hover:-translate-y-px"
               :style="{
                 ...calcPosV(b, layout.totalLanes, room.colorCode),
                 background: room.colorCode + '28',
                 border: `1px solid ${room.colorCode}50`,
                 borderLeft: `3px solid ${room.colorCode}`,
               }"
               @mouseenter="showTooltip(b, $event)"
               @mouseleave="!tooltip.pinned && (tooltip.show = false)"
               @click.stop="pinTooltip(b, $event)">
            <div class="flex h-full flex-col overflow-hidden pt-1.5 pl-2 pr-1.5 pb-1 max-md:px-1 max-md:pt-0.5 max-md:pb-0.5">
              <!-- ~39분 -->
              <template v-if="chipMinutes(b.startTime, b.endTime) <39">
                <div class="flex items-center gap-1.5 min-w-0 h-full">
                  <p class="truncate flex-1 text-[12px] font-bold leading-tight text-gray-900 dark:text-gray-50 max-md:text-[10px]">{{ b.title }}</p>
                  <p class="flex-shrink-0 tabular-nums text-[10px] text-gray-500 dark:text-gray-400 whitespace-nowrap max-md:text-[9px]">
                    {{ dayjs(b.startTime).format('HH:mm') }}
                  </p>
                </div>
              </template>
              <!-- 39~51분 -->
              <template v-else-if="chipMinutes(b.startTime, b.endTime) < 52">
                <p class="truncate min-w-0 w-full text-[14px] font-bold leading-tight text-gray-900 dark:text-gray-50 max-md:text-[11px]">{{ b.title }}</p>
                <div class="flex items-center gap-1.5 min-w-0 w-full mt-0.5 text-[11px] overflow-hidden max-md:gap-1 max-md:text-[9px] max-md:mt-0">
                  <span class="tabular-nums whitespace-nowrap flex-shrink-0 font-semibold text-[12px] max-md:text-[10px]" :style="{ color: room.colorCode }">
                    {{ dayjs(b.startTime).format('HH:mm') }}–{{ dayjs(b.endTime).format('HH:mm') }}
                  </span>
                  <span v-if="!isMobile && b.organizer" class="flex items-center gap-1 min-w-0 overflow-hidden ml-auto">
                    <span class="font-bold text-[10px] flex-shrink-0 whitespace-nowrap max-md:text-[8px]" :style="{ color: room.colorCode }">예약자</span>
                    <span class="truncate min-w-0 text-gray-700 dark:text-gray-200">{{ b.organizer }}</span>
                  </span>
                  <span v-if="!isMobile && attendeeCount(b) > 0" class="flex items-center gap-1 flex-shrink-0 whitespace-nowrap">
                    <span class="font-bold text-[10px] max-md:text-[8px]" :style="{ color: room.colorCode }">참석자</span>
                    <span class="text-gray-700 dark:text-gray-200 font-semibold">{{ attendeeCount(b) }}명</span>
                  </span>
                </div>
              </template>
              <!-- 52분+ -->
              <template v-else>
                <p class="truncate min-w-0 w-full text-[14px] font-bold leading-tight text-gray-900 dark:text-gray-50 max-md:text-[11px]">{{ b.title }}</p>
                <p class="tabular-nums text-[12px] font-semibold mt-0.5 max-md:text-[10px] max-md:mt-0" :style="{ color: room.colorCode }">
                  {{ dayjs(b.startTime).format('HH:mm') }}–{{ dayjs(b.endTime).format('HH:mm') }}
                </p>
                <div v-if="!isMobile && (b.organizer || attendeeCount(b) > 0)"
                     class="flex items-center gap-2 min-w-0 w-full mt-auto pt-0.5 text-[11px] overflow-hidden max-md:gap-1 max-md:pt-0 max-md:text-[9px]">
                  <span v-if="b.organizer" class="flex items-center gap-1 min-w-0 overflow-hidden">
                    <span class="font-bold text-[10px] flex-shrink-0 max-md:text-[8px]" :style="{ color: room.colorCode }">예약자</span>
                    <span class="truncate min-w-0 text-gray-700 dark:text-gray-200">{{ b.organizer }}</span>
                  </span>
                  <span v-if="attendeeCount(b) > 0" class="flex items-center gap-1 flex-shrink-0 ml-auto whitespace-nowrap">
                    <span class="font-bold text-[10px] max-md:text-[8px]" :style="{ color: room.colorCode }">참석자</span>
                    <span class="text-gray-700 dark:text-gray-200 font-semibold">{{ attendeeCount(b) }}명</span>
                  </span>
                </div>
              </template>
            </div>
          </div>
        </div>

      </div>
    </div>
    </div>

    <!-- 일정 목록 -->
    <AgendaList
      :class="isMobile ? 'min-h-0 min-w-0 flex-1' : 'min-w-0 shrink-0'"
      :title="targetDate.isSame(dayjs(), 'day') ? '오늘 일정' : targetDate.format('MM월 DD일') + ' 일정'"
      empty-text="예약된 일정이 없습니다"
      :loading="isLoadingBookings"
      :show-date="false"
      :scrollable="isMobile"
      :collapsible="false"
      :popup-on-click="true"
      :groups="dayGroups"
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
import { computed, onMounted, nextTick } from 'vue';
import { useApp } from '../../../composables/useApp';
import dayjs from 'dayjs';
import AgendaList from '../AgendaList.vue';

const {
  rooms, targetDate, hours,
  chipMinutes,
  tooltip, showTooltip, pinTooltip, openQuickModal,
  getBookingsForDate, sortBookings,
  liveNow, isLoadingBookings,
  getLayoutBookings, calcPosV, HOUR_H,
  nowLineTop, dayViewRef, scrollDayView,
  isMobile, openNewBooking,
} = useApp();

const GRID_H = computed(() => 15 * HOUR_H.value); // 6:00 ~ 21:00

/** 모바일: 화면 너비에 맞게 열 분배 / 데스크톱: 최소 140px 유지 */
const dayGridColumns = computed(() => {
  const n = rooms.value.length;
  if (n === 0) return '40px 1fr';
  return isMobile.value
    ? `36px repeat(${n}, minmax(0, 1fr))`
    : `48px repeat(${n}, minmax(140px, 1fr))`;
});

const dayGridBodyStyle = computed(() => {
  const n = rooms.value.length;
  const base = {
    display: 'grid',
    gridTemplateColumns: dayGridColumns.value,
    height: `${GRID_H.value}px`,
  };
  if (isMobile.value) {
    return { ...base, width: '100%', minWidth: '0', maxWidth: '100%' };
  }
  return {
    ...base,
    minWidth: `${48 + Math.max(n, 1) * 140}px`,
  };
});

function attendeeCount(b) {
  return (b.attendeeIds?.length || 0) + (b.externalAttendeeNames?.length || 0);
}

// 회의실별 레이아웃 (lane 배정 포함)
const roomLayouts = computed(() =>
  rooms.value.map(room => ({
    room,
    layout: getLayoutBookings(room.id, targetDate.value),
  }))
);

const nowLineVisible = computed(() =>
  targetDate.value.isSame(dayjs(), 'day') &&
  liveNow.value.hour() >= 6 &&
  liveNow.value.hour() < 21
);

const dayGroups = computed(() => [{
  key: targetDate.value.format('YYYYMMDD'),
  bookings: sortBookings(getBookingsForDate(targetDate.value)),
}]);

onMounted(async () => {
  await nextTick();
  scrollDayView();
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; height: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
</style>
