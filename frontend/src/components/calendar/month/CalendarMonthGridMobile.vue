<template>
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0" data-calendar-grid>
    <div class="grid grid-cols-7 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900">
      <div v-for="(d, i) in ['일','월','화','수','목','금','토']" :key="d"
           class="text-center font-bold tracking-wide py-2"
           :class="['text-[11px]', i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-500' : 'text-gray-400 dark:text-gray-500']">
        {{ d }}
      </div>
    </div>

    <div data-calendar-content class="grid grid-cols-7">
      <div v-for="(cell, i) in monthCells" :key="i"
           :class="[
             'border-r border-b border-gray-100 dark:border-gray-800 flex flex-col cursor-pointer transition-colors',
             'p-1 min-h-[clamp(48px,7.2dvh,62px)]',
             (i + 1) % 7 === 0 ? 'border-r-0' : '',
             selectedDay && cell.date.isSame(selectedDay, 'day') ? 'bg-blue-50/80 dark:bg-blue-900/25 ring-2 ring-inset ring-blue-400 dark:ring-blue-500' :
             cell.date.isSame(dayjs(), 'day')  ? 'bg-blue-50/60 dark:bg-blue-900/20' :
             !cell.isCurrentMonth              ? 'bg-gray-50/60 dark:bg-gray-800/40' :
             cell.date.day() === 0             ? 'bg-red-50/20 hover:bg-red-50/40' :
             cell.date.day() === 6             ? 'bg-blue-50/20 hover:bg-blue-50/40' :
             'hover:bg-gray-50/60 dark:hover:bg-gray-800/30'
           ]"
           @click="$emit('selectDay', cell.date)">

        <div class="mb-0.5">
          <span :class="[
                  'font-bold inline-flex items-center justify-center',
                  'text-[11px]',
                  cell.date.isSame(dayjs(), 'day')
                    ? 'w-5 h-5 text-[10px] rounded-full bg-blue-600 text-white'
                    : !cell.isCurrentMonth ? 'text-gray-300 dark:text-gray-600'
                    : cell.date.day() === 0 ? 'text-red-500'
                    : cell.date.day() === 6 ? 'text-blue-500'
                    : 'text-gray-700 dark:text-gray-200'
                ]">
            {{ cell.date.format('D') }}
          </span>
        </div>

        <!-- 모바일: 회의실 색 점만 (평면, 상세는 아래 목록) -->
        <div
          v-if="bookingDots(cell.date).length > 0"
          class="mt-auto flex w-full justify-center pt-0.5"
        >
          <div class="flex flex-wrap items-center justify-center gap-1">
            <span
              v-for="(item, di) in bookingDots(cell.date)"
              :key="'dot-' + cell.date.format('YYYYMMDD') + '-' + di"
              class="h-1 w-1 shrink-0 rounded-full"
              :style="{ backgroundColor: getRoomColor(item.roomId) }"
              :title="item.title"
            />
            <span
              v-if="bookingOverflow(cell.date) > 0"
              class="text-[8px] tabular-nums text-gray-400 dark:text-gray-500"
            >
              +{{ bookingOverflow(cell.date) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import dayjs from 'dayjs';

const props = defineProps({
  monthCells: { type: Array, required: true },
  selectedDay: { type: Object, default: null },
  getBookingsForDate: { type: Function, required: true },
  getRoomColor: { type: Function, required: true },
  goToDay: { type: Function, required: true },
});

defineEmits(['selectDay']);

const MAX_DOTS = 4;

/** 표시용 점(회의실 색, 최대 MAX_DOTS — 나머지는 +N) */
function bookingDots(date) {
  const list = props.getBookingsForDate(date);
  return list.slice(0, MAX_DOTS).map((b) => ({
    roomId: b.roomId,
    title: (b.title ?? '').trim() || '예약',
  }));
}

function bookingOverflow(date) {
  const n = props.getBookingsForDate(date).length;
  return n > MAX_DOTS ? n - MAX_DOTS : 0;
}
</script>

