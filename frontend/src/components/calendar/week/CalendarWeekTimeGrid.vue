<template>
  <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 overflow-hidden flex min-h-0 w-full min-w-0 shrink-0 flex-col rounded-xl md:rounded-2xl
              md:max-h-[calc(85vh-220px)]
              max-md:max-h-[min(53dvh,430px)] max-md:flex-none">
    <div class="flex-shrink-0 z-20 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700"
         :style="{ display: 'grid', gridTemplateColumns: '48px repeat(7, minmax(140px, 1fr))' }">
      <div class="h-14 border-r border-gray-200 dark:border-gray-700"></div>
      <button
        v-for="day in weekDays"
        :key="day.format('YYYYMMDD')"
        type="button"
        class="h-14 px-3 flex items-center justify-between gap-2 text-left border-r border-gray-100 dark:border-gray-800 last:border-r-0 transition-colors"
        :class="day.isSame(dayjs(), 'day')
          ? 'bg-blue-50/70 dark:bg-blue-900/20 hover:bg-blue-50 dark:hover:bg-blue-900/25'
          : 'hover:bg-gray-50 dark:hover:bg-gray-800/80'"
        @click="goToDay(day)">
        <div class="min-w-0 flex flex-col">
          <span class="text-[10px] font-bold uppercase tracking-[0.16em]"
                :class="day.day() === 0
                  ? 'text-red-400'
                  : day.day() === 6
                    ? 'text-blue-500'
                    : 'text-gray-400 dark:text-gray-500'">
          </span>
          <span class="text-[12px] font-semibold truncate"
                :class="day.isSame(dayjs(), 'day')
                  ? 'text-blue-700 dark:text-blue-300'
                  : 'text-gray-500 dark:text-gray-400'">
            {{ day.format('MM월') }}
          </span>
        </div>
        <span class="flex items-center justify-center font-black text-[16px] leading-none"
              :class="day.isSame(dayjs(), 'day')
                ? 'w-8 h-8 rounded-full bg-blue-600 text-white text-[14px]'
                : day.day() === 0
                  ? 'text-red-500'
                  : day.day() === 6
                    ? 'text-blue-500'
                    : 'text-gray-800 dark:text-gray-100'">
          {{ day.format('D') }}
        </span>
      </button>
    </div>

    <div ref="scrollEl" class="flex-1 overflow-y-auto overflow-x-auto custom-scrollbar">
      <div class="relative"
           :style="{
             display: 'grid',
             gridTemplateColumns: '48px repeat(7, minmax(140px, 1fr))',
             height: GRID_H + 'px',
             minWidth: 48 + 7 * 140 + 'px',
           }">
        <div class="relative border-r border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900">
          <div v-for="h in hours" :key="h"
               class="absolute right-2 text-[11px] font-semibold text-gray-400 dark:text-gray-600 tabular-nums select-none"
               :style="{ top: (h - 6) * HOUR_H + 'px', transform: 'translateY(-50%)' }">
            {{ String(h).padStart(2, '0') }}
          </div>
          <div class="absolute right-2 text-[11px] font-semibold text-gray-400 dark:text-gray-600 tabular-nums select-none"
               :style="{ top: GRID_H + 'px', transform: 'translateY(-50%)' }">
            21
          </div>
        </div>

        <div v-for="(day, di) in weekDays" :key="day.format('YYYYMMDD')"
             class="relative border-r border-gray-100 dark:border-gray-800 last:border-r-0"
             :class="day.isSame(dayjs(), 'day') ? 'bg-blue-50/20 dark:bg-blue-900/10' : ''">
          <div v-for="h in hours" :key="'line-' + di + '-' + h"
               class="absolute left-0 right-0 border-t pointer-events-none"
               :class="h % 2 === 0
                 ? 'border-gray-200 dark:border-gray-700'
                 : 'border-gray-100 dark:border-gray-800'"
               :style="{ top: (h - 6) * HOUR_H + 'px' }">
          </div>

          <div v-for="h in hours" :key="'click-' + di + '-' + h"
               class="absolute left-0 right-0 cursor-pointer hover:bg-indigo-50/30 dark:hover:bg-indigo-900/10 transition-colors"
               :style="{ top: (h - 6) * HOUR_H + 'px', height: HOUR_H + 'px' }"
               @click="openQuickModal(null, h, day)">
          </div>

          <!-- 현재 시간선 -->
          <div v-if="day.isSame(dayjs(), 'day') && liveNow.hour() >= 6 && liveNow.hour() < 21"
               class="absolute left-0 right-0 z-20 pointer-events-none"
               :style="{ top: (liveNow.hour() + liveNow.minute() / 60 - 6) * HOUR_H + 'px' }">
            <div class="relative h-px bg-red-400/70">
              <div class="absolute -left-[3px] -top-[3px] w-2 h-2 rounded-full bg-red-400/70"></div>
              <div class="absolute left-[-8px] -translate-x-full -top-2 px-1 py-0.5 rounded bg-white/90 dark:bg-gray-900/90 border border-red-300/60 text-[9px] leading-none font-semibold text-red-500 tabular-nums whitespace-nowrap shadow-sm">
                {{ liveNow.format('HH:mm') }}
              </div>
            </div>
          </div>

          <!-- 더 보기 / 접기 버튼 -->
          <div v-if="dayLayouts[di].hiddenCount > 0 || isExpanded(dayLayouts[di].key)"
               class="absolute bottom-2 left-1 right-1 z-20 flex justify-center"
               @click.stop="toggleExpand(dayLayouts[di].key)">
            <button class="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-indigo-500 dark:text-indigo-400 shadow-sm hover:bg-indigo-50 dark:hover:bg-indigo-900/20 transition-colors">
              <span v-if="!isExpanded(dayLayouts[di].key)">+{{ dayLayouts[di].hiddenCount }}개 더 보기</span>
              <span v-else>접기 ↑</span>
            </button>
          </div>

          <div v-for="b in dayLayouts[di].items" :key="b.id"
               class="absolute rounded-xl cursor-pointer z-10 transition-all duration-150 hover:shadow-lg hover:z-30 hover:brightness-95 shadow-sm"
               :style="{
                 ...calcPosV(b, dayLayouts[di].totalLanes, getRoomColor(b.roomId)),
                 background: getRoomColor(b.roomId) + '28',
                 border: `1.5px solid ${getRoomColor(b.roomId)}50`,
                 borderLeft: `4px solid ${getRoomColor(b.roomId)}`,
               }"
               @mouseenter="showTooltip(b, $event)"
               @mouseleave="!tooltip.pinned && (tooltip.show = false)"
               @click.stop="pinTooltip(b, $event)">
            <div class="flex flex-col h-full overflow-hidden"
                 :class="isCompactChip(b) ? 'px-2 py-1 justify-center gap-0' : 'pt-2 pl-2.5 pr-2 pb-1.5 gap-0.5'">
              <!-- 짧은 일정: 회의실명 + 시간만 -->
              <template v-if="isCompactChip(b)">
                <div class="flex items-center gap-1 min-w-0 text-[11px] leading-tight overflow-hidden">
                  <span class="w-1.5 h-1.5 rounded-full flex-shrink-0"
                        :style="{ background: getRoomColor(b.roomId) }"></span>
                  <span class="font-semibold text-gray-700 dark:text-gray-300 truncate min-w-0">
                    {{ getRoomName(b.roomId) }}
                  </span>
                  <span class="text-gray-500 dark:text-gray-400 tabular-nums truncate min-w-0 ml-auto flex-shrink-0">
                    {{ dayjs(b.startTime).format('HH:mm') }}
                  </span>
                </div>
              </template>
              <!-- 일반 일정 -->
              <template v-else>
                <div v-if="chipMinutes(b.startTime, b.endTime) >= 22"
                     class="flex items-center gap-1 min-w-0 overflow-hidden">
                  <span class="w-2 h-2 rounded-full flex-shrink-0"
                        :style="{ background: getRoomColor(b.roomId) }"></span>
                  <p class="text-[11px] font-semibold text-gray-600 dark:text-gray-300 truncate min-w-0">
                    {{ getRoomName(b.roomId) }}
                  </p>
                </div>
                <p class="text-[14px] font-semibold text-gray-800 dark:text-gray-100 truncate leading-tight min-w-0 w-full">
                  {{ b.title }}
                </p>
                <p v-if="chipMinutes(b.startTime, b.endTime) >= 25"
                   class="text-[12px] text-gray-600 dark:text-gray-300 truncate tabular-nums min-w-0 w-full font-medium">
                  {{ dayjs(b.startTime).format('HH:mm') }}–{{ dayjs(b.endTime).format('HH:mm') }}
                </p>
                <!-- 예약자 + 참석자 (>= 45분) -->
                <div v-if="!isMobile && chipMinutes(b.startTime, b.endTime) >= 45 && (b.organizer || weekAttendeeCount(b) > 0)"
                     class="flex items-center gap-2 min-w-0 w-full mt-auto text-[11px] overflow-hidden">
                  <span v-if="b.organizer" class="flex items-center gap-1 min-w-0 overflow-hidden">
                    <span class="font-bold text-[10px] flex-shrink-0" :style="{ color: getRoomColor(b.roomId) }">예약자</span>
                    <span class="truncate min-w-0 text-gray-700 dark:text-gray-200">{{ b.organizer }}</span>
                  </span>
                  <span v-if="weekAttendeeCount(b) > 0" class="flex items-center gap-1 flex-shrink-0 ml-auto whitespace-nowrap">
                    <span class="font-bold text-[10px]" :style="{ color: getRoomColor(b.roomId) }">참석자</span>
                    <span class="text-gray-700 dark:text-gray-200 font-semibold">{{ weekAttendeeCount(b) }}명</span>
                  </span>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useApp } from '../../../composables/useApp';
import dayjs from 'dayjs';

const {
  weekDays, hours,
  getAllLayoutBookings, getRoomColor, getRoomName, chipMinutes,
  calcPosV, HOUR_H, liveNow,
  tooltip, showTooltip, pinTooltip,
  openQuickModal, goToDay,
  isMobile,
  isExpanded, toggleExpand,
} = useApp();

const GRID_H = computed(() => 15 * HOUR_H.value); // 6:00 ~ 21:00

const scrollEl = ref(null);

function weekAttendeeCount(b) {
  return (b.attendeeIds?.length || 0) + (b.externalAttendeeNames?.length || 0);
}

// 날짜별 레이아웃 (펼침 상태에 따라 2레인 or 전체)
const MAX_LANES = 2;
const dayLayouts = computed(() =>
  weekDays.value.map(day => {
    const key = 'week-' + day.format('YYYYMMDD');
    const { items, totalLanes } = getAllLayoutBookings(day);
    const expanded = isExpanded(key);
    const visible  = expanded ? items : items.filter(b => b.lane < MAX_LANES);
    const hidden   = items.length - visible.length;
    return {
      items: visible,
      totalLanes: expanded ? totalLanes : Math.min(totalLanes, MAX_LANES),
      hiddenCount: hidden,
      key,
    };
  })
);

// 블록 높이 (px) — compact 판단 기준
function chipBlockHeightPx(b) {
  const st = dayjs(b.startTime);
  const en = dayjs(b.endTime);
  const startH = st.hour() + st.minute() / 60;
  const endH = en.hour() + en.minute() / 60;
  return Math.max((endH - startH) * HOUR_H.value - 3, 10);
}
function isCompactChip(b) {
  return chipBlockHeightPx(b) < 56;
}

onMounted(async () => {
  await nextTick();
  if (!scrollEl.value) return;
  const h = weekDays.value.some(d => d.isSame(dayjs(), 'day'))
    ? liveNow.value.hour() + liveNow.value.minute() / 60
    : 8;
  scrollEl.value.scrollTop = Math.max(0, (h - 6) * HOUR_H.value - 80);
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; height: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
</style>
