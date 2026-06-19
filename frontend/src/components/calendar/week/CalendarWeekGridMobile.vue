<template>
  <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 overflow-hidden flex min-h-0 w-full min-w-0 shrink-0 flex-col rounded-xl md:rounded-2xl
              md:max-h-[calc(100vh-220px)]
              max-md:flex-none" data-calendar-grid>
    <!-- 모바일 주간은 좌우 스크롤만 허용 -->
    <div
      ref="scrollContainerRef"
      data-calendar-content
      class="overflow-x-auto overflow-y-hidden custom-scrollbar flex-1 min-h-0 bg-white dark:bg-gray-900"
      style="touch-action: pan-x; overscroll-behavior-x: contain;"
    >
      <div
        class="grid min-w-[820px]"
        style="grid-template-columns: 104px repeat(7, minmax(88px, 1fr));"
      >
        <!-- 코너 (좌측/상단 고정) -->
        <div data-room-label class="sticky left-0 top-0 z-40 h-10 md:h-14 bg-white dark:bg-gray-900 border-b border-r border-gray-200 dark:border-gray-700" />

        <!-- 요일 헤더 (주간 데스크톱과 동일 톤) -->
        <button
          v-for="day in weekDays"
          :key="day.format('YYYYMMDD')"
          type="button"
          class="sticky top-0 z-30 h-10 md:h-14 px-1.5 flex items-center justify-between gap-1 border-b border-r border-gray-100 dark:border-gray-800 last:border-r-0 text-left min-w-0 transition-colors"
          :class="day.isSame(dayjs(), 'day')
            ? 'bg-blue-50/70 dark:bg-blue-900/20'
            : 'bg-white dark:bg-gray-900 hover:bg-gray-50 dark:hover:bg-gray-800/80'"
          @click.stop="goToDay(day)"
        >
          <div class="min-w-0 flex flex-col">
            <span
              class="text-[8px] font-bold uppercase tracking-[0.12em] truncate"
              :class="day.day() === 0
                ? 'text-red-400'
                : day.day() === 6
                  ? 'text-blue-500'
                  : 'text-gray-400 dark:text-gray-500'"
            >
              {{ day.format('ddd') }}
            </span>
            <span
              class="text-[9px] font-semibold truncate"
              :class="day.isSame(dayjs(), 'day')
                ? 'text-blue-700 dark:text-blue-300'
                : 'text-gray-500 dark:text-gray-400'"
            >
              {{ day.format('MM월') }}
            </span>
          </div>
          <span
            class="flex h-6 min-w-[1.5rem] items-center justify-center font-black text-[13px] leading-none flex-shrink-0"
            :class="day.isSame(dayjs(), 'day')
              ? 'rounded-full bg-blue-600 text-white text-[11px]'
              : day.day() === 0
                ? 'text-red-500'
                : day.day() === 6
                  ? 'text-blue-500'
                  : 'text-gray-800 dark:text-gray-100'"
          >
            {{ day.format('D') }}
          </span>
        </button>

        <template v-for="room in rooms" :key="room.id">
          <!-- 회의실 라벨 (일간 헤더와 동일 패턴) -->
          <div
            data-room-label
            class="sticky left-0 z-20 flex items-center gap-1.5 px-2 py-2 min-h-[74px] bg-white dark:bg-gray-900 border-b border-r border-gray-200 dark:border-gray-700"
          >
            <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{ background: room.colorCode }" />
            <div class="min-w-0">
              <p class="text-[12px] font-bold text-gray-800 dark:text-gray-100 leading-tight truncate">{{ room.name }}</p>
              <p class="text-[10px] text-gray-400 dark:text-gray-500 mt-0.5">{{ room.capacity }}인</p>
            </div>
          </div>

          <div
            v-for="day in weekDays"
            :key="`${room.id}-${day.format('YYYYMMDD')}`"
            class="border-b border-r border-gray-100 dark:border-gray-800 last:border-r-0 p-1 flex flex-col gap-0.5 min-h-[74px] transition-colors"
            :class="day.isSame(dayjs(), 'day')
              ? 'bg-blue-50/20 dark:bg-blue-900/10'
              : 'bg-white dark:bg-gray-900'"
            @touchstart.passive="onCellTouchStart"
            @touchmove.passive="onCellTouchMove"
            @click="handleCellClick(room.id, day)"
          >
            <div
              v-for="b in filterBookings(room.id, day).slice(0, 2)"
              :key="b.id"
              class="rounded-md px-1.5 py-1 cursor-pointer transition-all duration-150 hover:shadow-md"
              :style="{
                background: room.colorCode + '18',
                border: `1px solid ${room.colorCode}35`,
                borderLeft: `3px solid ${room.colorCode}`,
              }"
              @click.stop="pinTooltip(b, $event)"
            >
              <p class="text-[10px] font-semibold text-gray-800 dark:text-gray-100 truncate leading-tight">{{ b.title }}</p>
              <p class="text-[8.5px] text-gray-600 dark:text-gray-300 tabular-nums mt-0.5">
                {{ dayjs(b.startTime).format('HH:mm') }}–{{ dayjs(b.endTime).format('HH:mm') }}
              </p>
            </div>

            <div
              v-if="filterBookings(room.id, day).length > 2"
              class="text-[8px] font-semibold text-blue-600 dark:text-blue-400 text-center py-0.5 cursor-pointer"
              @click.stop="goToDay(day)"
            >
              +{{ filterBookings(room.id, day).length - 2 }}
            </div>

            <div
              v-if="filterBookings(room.id, day).length === 0"
              class="flex-1 flex items-center justify-center py-1 min-h-[28px]"
            >
              <div
                class="w-6 h-6 rounded-full flex items-center justify-center bg-gray-100 dark:bg-gray-800 text-gray-300 dark:text-gray-600 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 hover:text-indigo-500 transition-colors"
              >
                <svg width="10" height="10" viewBox="0 0 12 12" fill="none">
                  <path d="M6 1v10M1 6h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                </svg>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { useApp } from '../../../composables/useApp';
import dayjs from 'dayjs';

const {
  rooms, weekDays,
  pinTooltip, openQuickModal,
  filterBookings,
  goToDay,
} = useApp();

// ── 오늘 컬럼으로 자동 스크롤 ──────────────────────────────────
const scrollContainerRef = ref(null);

// smooth=false: 즉시 이동 (초기 마운트용)
// smooth=true : 부드럽게 이동 (화살표 버튼 등 visible 상태 전환용)
const scrollToToday = async (smooth = false) => {
  await nextTick();
  const container = scrollContainerRef.value;
  if (!container) return;

  const gridEl = container.firstElementChild;
  if (!gridEl) return;

  const colW     = (gridEl.scrollWidth - 104) / 7;
  const todayIdx = weekDays.value.findIndex(d => d.isSame(dayjs(), 'day'));
  const target   = todayIdx === -1
    ? 0  // 오늘 없는 주 → 일요일(왼쪽 끝)
    : Math.max(0, 104 + todayIdx * colW - (container.clientWidth - colW) / 2);

  if (smooth) {
    container.scrollTo({ left: target, behavior: 'smooth' });
  } else {
    container.scrollLeft = target;
  }
};

onMounted(() => scrollToToday(false));
// 페이드 애니메이션(160ms) 후 실행 — 스와이프 시엔 App.vue가 미리 scrollLeft=0
// 으로 리셋해두므로 깜빡임 없이 today 위치로만 이동
watch(weekDays, () => setTimeout(() => scrollToToday(true), 200));

// 모바일에서 스크롤(터치 이동) 중 click이 발생해 예약 모달이 연속으로 뜨는 것 방지
const _touchStart = ref({ x: 0, y: 0, moved: false });
const MOVE_PX = 8;
const onCellTouchStart = (e) => {
  const t = e.touches?.[0];
  if (!t) return;
  _touchStart.value = { x: t.clientX, y: t.clientY, moved: false };
};
const onCellTouchMove = (e) => {
  const t = e.touches?.[0];
  if (!t) return;
  const dx = Math.abs(t.clientX - _touchStart.value.x);
  const dy = Math.abs(t.clientY - _touchStart.value.y);
  if (dx > MOVE_PX || dy > MOVE_PX) _touchStart.value.moved = true;
};

const handleCellClick = (roomId, day) => {
  if (_touchStart.value.moved) return;
  openQuickModal(roomId, 9, day);
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
