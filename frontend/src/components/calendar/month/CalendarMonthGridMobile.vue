<template>
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0">
    <div class="grid grid-cols-7 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
      <div v-for="(d, i) in ['일','월','화','수','목','금','토']" :key="d"
           class="text-center font-bold tracking-wide py-2.5"
           :class="['text-[10px]', i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-500' : 'text-gray-400']">
        {{ d }}
      </div>
    </div>

    <div class="grid grid-cols-7">
      <div v-for="(cell, i) in monthCells" :key="i"
           :class="[
             'border-r border-b border-gray-100 dark:border-gray-800 flex flex-col cursor-pointer transition-colors',
             'p-1 min-h-[60px]',
             (i + 1) % 7 === 0 ? 'border-r-0' : '',
             selectedDay && cell.date.isSame(selectedDay, 'day') ? 'bg-blue-50/80 dark:bg-blue-900/25 ring-2 ring-inset ring-blue-400 dark:ring-blue-500' :
             cell.date.isSame(dayjs(), 'day')  ? 'bg-blue-50/60 dark:bg-blue-900/20' :
             !cell.isCurrentMonth              ? 'bg-gray-50/60 dark:bg-gray-800/40' :
             cell.date.day() === 0             ? 'bg-red-50/20 hover:bg-red-50/40' :
             cell.date.day() === 6             ? 'bg-blue-50/20 hover:bg-blue-50/40' :
             'hover:bg-gray-50/60 dark:hover:bg-gray-800/30'
           ]"
           @click="$emit('selectDay', cell.date)">

        <div class="mb-1">
          <span :class="[
                  'font-bold inline-flex items-center justify-center',
                  'text-[12px]',
                  cell.date.isSame(dayjs(), 'day')
                    ? 'w-5 h-5 text-[11px] rounded-full bg-blue-600 text-white'
                    : !cell.isCurrentMonth ? 'text-gray-300 dark:text-gray-600'
                    : cell.date.day() === 0 ? 'text-red-500'
                    : cell.date.day() === 6 ? 'text-blue-500'
                    : 'text-gray-700 dark:text-gray-200'
                ]">
            {{ cell.date.format('D') }}
          </span>
        </div>

        <div v-if="getBookingsForDate(cell.date).length > 0"
             class="flex flex-col items-start gap-0.5">
          <div class="flex gap-0.5 flex-wrap">
            <span v-for="b in getBookingsForDate(cell.date).slice(0, 3)" :key="b.id"
                  class="w-1.5 h-1.5 rounded-full flex-shrink-0"
                  :style="{ background: getRoomColor(b.roomId) }">
            </span>
          </div>
          <span v-if="getBookingsForDate(cell.date).length > 3"
                class="text-[8px] font-bold text-blue-500 leading-none">
            +{{ getBookingsForDate(cell.date).length - 3 }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import dayjs from 'dayjs';

defineProps({
  monthCells: { type: Array, required: true },
  selectedDay: { type: Object, default: null },
  getBookingsForDate: { type: Function, required: true },
  getRoomColor: { type: Function, required: true },
});

defineEmits(['selectDay']);
</script>

