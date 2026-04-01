<template>
  <!-- ── 수평 간트 그리드 ── -->
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0">


    <div class="flex">

      <!-- ── 왼쪽: 회의실 이름 열 (스크롤 밖, 고정) ── -->
      <div class="flex-shrink-0 border-r border-gray-200 dark:border-gray-700 z-10"
           :style="{ width: ROOM_COL + 'px' }">

        <!-- 헤더 -->
        <div class="bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-4 flex items-center"
             :style="{ height: HEADER_H + 'px' }">
          <span class="text-[10.5px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider">회의실</span>
        </div>

        <!-- 회의실 행 -->
        <div v-for="(room, ri) in rooms" :key="room.id"
             class="border-b border-gray-100 dark:border-gray-800 last:border-b-0 flex items-center px-3"
             :class="ri % 2 === 1 ? 'bg-gray-50 dark:bg-gray-800/50' : 'bg-white dark:bg-gray-900'"
             :style="{ height: ROW_H + 'px' }">
          <div class="flex items-center gap-2 w-full min-w-0">
            <span class="flex-shrink-0 w-[3px] rounded-full"
                  :style="{ background: room.colorCode, height: '28px' }">
            </span>
            <div class="min-w-0 flex-1">
              <p class="text-[12px] font-semibold text-gray-800 dark:text-gray-100 truncate leading-tight">{{ room.name }}</p>
              <p class="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">{{ room.capacity }}인</p>
            </div>
          </div>
        </div>
      </div>

      <!-- ── 오른쪽: 타임 트랙 (독립 가로 스크롤) ── -->
      <div ref="scrollTrack" class="flex-1 overflow-x-auto custom-scrollbar">
        <div :style="{ minWidth: MIN_TRACK_W + 'px' }">

          <!-- 시간 헤더 -->
          <div class="relative bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700"
               :style="{ height: HEADER_H + 'px' }">
            <div v-for="h in hours" :key="h"
                 class="absolute top-0 bottom-0 flex items-center pointer-events-none"
                 :style="{ left: ((h - 6) / HOUR_COUNT * 100) + '%' }">
              <span class="text-[11px] font-medium text-gray-400 dark:text-gray-500 pl-1.5 whitespace-nowrap select-none">
                {{ String(h).padStart(2, '0') }}:00
              </span>
            </div>
            <div class="absolute top-0 bottom-0 right-0 flex items-center pointer-events-none">
              <span class="text-[11px] font-medium text-gray-400 dark:text-gray-500 pr-1.5 select-none">21:00</span>
            </div>
          </div>

          <!-- 회의실별 타임 트랙 행 -->
          <div v-for="(room, ri) in rooms" :key="room.id"
               class="relative border-b border-gray-100 dark:border-gray-800 last:border-b-0"
               :class="ri % 2 === 1 ? 'bg-gray-50/50 dark:bg-gray-800/30' : 'bg-white dark:bg-gray-900'"
               :style="{ height: ROW_H + 'px' }"
               @click="openQuickModal(room.id, 9, targetDate)">

            <!-- 시간 격자선 -->
            <div v-for="h in hours" :key="h"
                 class="absolute top-0 bottom-0 border-l pointer-events-none"
                 :class="h % 2 === 0 ? 'border-gray-100 dark:border-gray-800' : 'border-gray-50 dark:border-gray-800/50'"
                 :style="{ left: ((h - 6) / HOUR_COUNT * 100) + '%' }">
            </div>

            <!-- 현재 시간 세로선 -->
            <template v-if="targetDate.isSame(dayjs(), 'day')">
              <div class="absolute top-0 bottom-0 w-px bg-red-400/60 z-10 pointer-events-none"
                   :style="{ left: nowLinePct }">
              </div>
              <div v-if="ri === 0"
                   class="absolute z-20 pointer-events-none -translate-x-1/2"
                   :style="{ left: nowLinePct, top: '0px' }">
                <div class="flex flex-col items-center">
                  <div class="bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-md shadow-md leading-none whitespace-nowrap tabular-nums">
                    {{ liveNow.format('HH:mm') }}
                  </div>
                  <div class="w-1.5 h-1.5 rounded-full bg-red-500 mt-0.5"></div>
                </div>
              </div>
            </template>

            <!-- 예약 블록 -->
            <template v-for="b in filterBookings(room.id, targetDate)" :key="b.id">
              <div class="absolute rounded-md cursor-pointer overflow-hidden z-10 transition-all hover:shadow-md hover:brightness-105"
                   :style="calcGanttPos(b, room.colorCode)"
                   @mouseenter="showTooltip(b, $event)"
                   @mouseleave="!tooltip.pinned && (tooltip.show = false)"
                   @click.stop="pinTooltip(b, $event)">
                <div class="h-full flex flex-col justify-center px-2.5 overflow-hidden">
                  <p class="text-[12px] font-bold text-gray-900 truncate leading-tight">{{ b.title }}</p>
                  <p v-if="chipMinutes(b.startTime, b.endTime) >= 30"
                     class="text-[10.5px] text-gray-700 truncate mt-0.5">
                    {{ dayjs(b.startTime).format('HH:mm') }}–{{ dayjs(b.endTime).format('HH:mm') }}
                  </p>
                </div>
              </div>
            </template>

          </div>
        </div>
      </div>

    </div>
  </div>

  <!-- ── 일정 목록 ── -->
  <div>
    <div class="flex items-center justify-between mb-3 px-1 flex-wrap gap-2">
      <div class="flex items-center gap-2">
        <p class="text-[14px] font-semibold text-gray-700 dark:text-gray-200">
          {{ targetDate.isSame(dayjs(), 'day') ? '오늘 일정' : targetDate.format('MM월 DD일') + ' 일정' }}
        </p>
        <span class="text-[12.5px] font-medium text-gray-400">{{ getBookingsForDate(targetDate).length }}건</span>
      </div>
      <SortBar />
    </div>
    <div v-if="getBookingsForDate(targetDate).length === 0"
         class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 px-6 py-10 text-center text-gray-300 dark:text-gray-600 text-[13.5px] font-medium">
      예약된 일정이 없습니다
    </div>
    <div v-else class="grid grid-cols-1 gap-2.5">
      <BookingListItem v-for="b in sortBookings(getBookingsForDate(targetDate))" :key="b.id" :booking="b" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, nextTick } from 'vue';
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';
import SortBar from './SortBar.vue';
import BookingListItem from './BookingListItem.vue';

const {
  rooms, targetDate, hours, isMobile,
  isRoomInUse, filterBookings, chipMinutes,
  tooltip, showTooltip, pinTooltip, openQuickModal,
  getBookingsForDate, sortBookings,
  liveNow,
} = useApp();

// ── 레이아웃 상수 ─────────────────────────────────────────────
const ROOM_COL    = computed(() => isMobile.value ? 96  : 156);
const ROW_H       = computed(() => isMobile.value ? 72  : 84);
const HEADER_H    = 38;
const HOUR_START  = 6;
const HOUR_COUNT  = hours.length; // 15 (06~20)
const MIN_TRACK_W = computed(() => isMobile.value ? 1000 : 1800);

// ── 현재 시간 세로선 % ─────────────────────────────────────────
const nowLinePct = computed(() => {
  const h = liveNow.value.hour() + liveNow.value.minute() / 60;
  return `${Math.max(0, Math.min(100, (h - HOUR_START) / HOUR_COUNT * 100))}%`;
});

// ── 초기 진입 시 현재 시각으로 스크롤 ─────────────────────────
const scrollTrack = ref(null);

onMounted(async () => {
  await nextTick();
  if (!scrollTrack.value) return;
  const now   = dayjs();
  const h     = now.hour() + now.minute() / 60;
  const trackW    = MIN_TRACK_W.value;
  const containerW = scrollTrack.value.clientWidth;
  // 현재 시각이 화면 중앙에 오도록 배치
  const scrollTo = (h - HOUR_START) / HOUR_COUNT * trackW - containerW / 2;
  scrollTrack.value.scrollLeft = Math.max(0, scrollTo);
});

// ── 예약 블록 위치 (헤더 레이블과 동일한 % 공식) ────────────────
const calcGanttPos = (b, color) => {
  const st = dayjs(b.startTime).hour() + dayjs(b.startTime).minute() / 60;
  const en = dayjs(b.endTime).hour()   + dayjs(b.endTime).minute()   / 60;
  return {
    top:        '10px',
    bottom:     '10px',
    left:       `${Math.max(0, (st - HOUR_START) / HOUR_COUNT * 100)}%`,
    width:      `${Math.max(0.5, (en - st) / HOUR_COUNT * 100)}%`,
    background: color,
    opacity:    '0.9',
  };
};
</script>
