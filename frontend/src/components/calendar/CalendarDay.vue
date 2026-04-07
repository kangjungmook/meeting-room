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
          <span class="text-[11.5px] font-bold text-gray-500 dark:text-gray-500 uppercase tracking-wider">회의실</span>
        </div>

        <!-- 회의실 행 -->
        <div v-for="(room, ri) in rooms" :key="room.id"
             class="border-b border-gray-100 dark:border-gray-800 last:border-b-0 flex items-center px-3"
             :class="ri % 2 === 1 ? 'bg-gray-50 dark:bg-gray-800/50' : 'bg-white dark:bg-gray-900'"
             :style="{ height: ROW_H + 'px' }">
          <div class="flex items-center gap-2 w-full min-w-0">
            <span class="flex-shrink-0 w-1 rounded-full"
                  :style="{ background: room.colorCode, height: '36px' }">
            </span>
            <div class="min-w-0 flex-1">
              <p class="text-[13px] font-bold text-gray-800 dark:text-gray-100 truncate leading-tight">{{ room.name }}</p>
              <p class="text-[12px] text-gray-500 dark:text-gray-500 mt-0.5">{{ room.capacity }}인</p>
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
              <span class="text-[12px] font-semibold text-gray-600 dark:text-gray-500 pl-2 whitespace-nowrap select-none">
                {{ h }}:00
              </span>
            </div>
            <div class="absolute top-0 bottom-0 right-0 flex items-center pointer-events-none">
              <span class="text-[12px] font-semibold text-gray-600 dark:text-gray-500 pr-2 select-none">21:00</span>
            </div>
            <!-- 현재 시각 배지 (헤더-룸 경계선에 걸쳐 고정) -->
            <template v-if="targetDate.isSame(dayjs(), 'day')">
              <div class="absolute bottom-0 translate-y-1/2 z-30 pointer-events-none -translate-x-1/2"
                   :style="{ left: nowLinePct }">
                <div class="bg-red-500 text-white text-[10px] font-bold px-2 py-[3px] rounded-full shadow-md leading-none whitespace-nowrap tabular-nums">
                  {{ liveNow.format('HH:mm') }}
                </div>
              </div>
            </template>
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
                 :class="h % 2 === 0 ? 'border-gray-200 dark:border-gray-700' : 'border-gray-100 dark:border-gray-800'"
                 :style="{ left: ((h - 6) / HOUR_COUNT * 100) + '%' }">
            </div>

            <!-- 현재 시간 세로선 -->
            <template v-if="targetDate.isSame(dayjs(), 'day')">
              <div class="absolute top-0 bottom-0 w-px bg-red-400/80 z-10 pointer-events-none"
                   :style="{ left: nowLinePct }">
              </div>
            </template>

            <!-- 예약 블록 -->
            <template v-for="b in filterBookings(room.id, targetDate)" :key="b.id">
              <div class="absolute rounded-xl cursor-pointer overflow-hidden z-20 transition-all duration-150 hover:scale-[1.02] hover:z-20 hover:shadow-lg"
                   :style="calcGanttPos(b, room.colorCode)"
                   @mouseenter="showTooltip(b, $event)"
                   @mouseleave="!tooltip.pinned && (tooltip.show = false)"
                   @click.stop="pinTooltip(b, $event)">
                <!-- 배경 -->
                <div class="absolute inset-0"
                     :style="{ background: `linear-gradient(135deg, ${room.colorCode} 0%, ${room.colorCode}cc 100%)` }">
                </div>
                <!-- 어둡게 오버레이 (글씨 가독성 보조) -->
                <div class="absolute inset-0 bg-black/10"></div>
                <!-- 왼쪽 진한 액센트 바 -->
                <div class="absolute left-0 top-0 bottom-0 w-[3px] rounded-l-xl bg-black/20">
                </div>
                <!-- 내용 -->
                <div class="relative flex flex-col pt-1.5 pl-3 pr-2">
                  <p class="text-[12.5px] font-black text-white truncate leading-tight"
                     style="text-shadow: 0 1px 3px rgba(0,0,0,0.4)">{{ b.title }}</p>
                  <p v-if="chipMinutes(b.startTime, b.endTime) >= 30"
                     class="text-[10.5px] text-white/90 truncate mt-0.5 tabular-nums"
                     style="text-shadow: 0 1px 2px rgba(0,0,0,0.3)">
                    {{ dayjs(b.startTime).format('HH:mm') }} – {{ dayjs(b.endTime).format('HH:mm') }}
                  </p>
                  <p v-if="chipMinutes(b.startTime, b.endTime) >= 60 && b.organizer"
                     class="text-[10px] text-white/80 truncate mt-0.5"
                     style="text-shadow: 0 1px 2px rgba(0,0,0,0.3)">
                    {{ b.organizer }}
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
    <div class="flex items-center justify-between mb-3 px-1">
      <div class="flex items-center gap-2">
        <p class="text-[14px] font-bold text-gray-700 dark:text-gray-200">
          {{ targetDate.isSame(dayjs(), 'day') ? '오늘 일정' : targetDate.format('MM월 DD일') + ' 일정' }}
        </p>
        <span class="text-[11px] font-bold px-2 py-0.5 rounded-full"
              :class="getBookingsForDate(targetDate).length > 0
                ? 'bg-indigo-100 text-indigo-600 dark:bg-indigo-900/40 dark:text-indigo-400'
                : 'bg-gray-100 text-gray-400 dark:bg-gray-800'">
          {{ getBookingsForDate(targetDate).length }}건
        </span>
      </div>
      <SortBar />
    </div>
    <SkeletonBookingList v-if="isLoadingBookings && getBookingsForDate(targetDate).length === 0" :count="3" />
    <div v-else-if="getBookingsForDate(targetDate).length === 0"
         class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 px-6 py-12 text-center">
      <svg width="32" height="32" viewBox="0 0 32 32" fill="none" class="mx-auto mb-3 text-gray-200 dark:text-gray-700">
        <rect x="2" y="5" width="28" height="24" rx="4" stroke="currentColor" stroke-width="2"/>
        <path d="M10 2v6M22 2v6M2 13h28" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <p class="text-[13px] font-medium text-gray-300 dark:text-gray-600">예약된 일정이 없습니다</p>
    </div>
    <div v-else class="flex flex-col gap-2.5">
      <BookingListItem v-for="b in sortBookings(getBookingsForDate(targetDate))" :key="b.id" :booking="b" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, nextTick, watch } from 'vue';
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';
import SortBar from './SortBar.vue';
import BookingListItem from './BookingListItem.vue';
import SkeletonBookingList from './SkeletonBookingList.vue';

const {
  rooms, targetDate, hours, isMobile,
  filterBookings, chipMinutes,
  tooltip, showTooltip, pinTooltip, openQuickModal,
  getBookingsForDate, sortBookings,
  liveNow, isLoadingBookings,
} = useApp();

// ── 레이아웃 상수 ─────────────────────────────────────────────
const ROOM_COL    = computed(() => isMobile.value ? 108 : 176);
const ROW_H       = computed(() => isMobile.value ? 88  : 116);
const HEADER_H    = 48;
const HOUR_START  = 6;
const HOUR_COUNT  = hours.length; // 15 (06~20)
const MIN_TRACK_W = computed(() => isMobile.value ? 1800 : 2600);

// ── 현재 시간 세로선 % ─────────────────────────────────────────
const nowLinePct = computed(() => {
  const h = liveNow.value.hour() + liveNow.value.minute() / 60;
  return `${Math.max(0, Math.min(100, (h - HOUR_START) / HOUR_COUNT * 100))}%`;
});

// ── 현재 시각으로 스크롤 ──────────────────────────────────────
const scrollTrack = ref(null);

const scrollToNow = () => {
  if (!scrollTrack.value) return;
  const h = liveNow.value.hour() + liveNow.value.minute() / 60;
  const trackW     = MIN_TRACK_W.value;
  const containerW = scrollTrack.value.clientWidth;
  const target = (h - HOUR_START) / HOUR_COUNT * trackW - containerW / 2;
  scrollTrack.value.scrollLeft = Math.max(0, target);
};

onMounted(async () => {
  await nextTick();
  scrollToNow();
});

// 1분마다 현재 시각으로 자동 스크롤 (빨간 선이 항상 화면 중앙에 유지)
const currentMinute = computed(() => liveNow.value.format('HHmm'));
watch(currentMinute, scrollToNow);

// ── 예약 블록 위치 (헤더 레이블과 동일한 % 공식) ────────────────
const calcGanttPos = (b, color) => {
  const st = dayjs(b.startTime).hour() + dayjs(b.startTime).minute() / 60;
  const en = dayjs(b.endTime).hour()   + dayjs(b.endTime).minute()   / 60;
  return {
    top:    '10px',
    bottom: '10px',
    left:   `calc(${Math.max(0, (st - HOUR_START) / HOUR_COUNT * 100)}% + 2px)`,
    width:  `calc(${Math.max(0.5, (en - st) / HOUR_COUNT * 100)}% - 4px)`,
  };
};
</script>
