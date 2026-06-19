<template>
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0">
    <div class="grid grid-cols-7 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
      <div v-for="(d, i) in ['일','월','화','수','목','금','토']" :key="d"
           class="text-center font-bold tracking-wide py-2.5"
           :class="['text-[13px]', i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-500' : 'text-gray-400']">
        {{ d }}
      </div>
    </div>

    <div class="grid grid-cols-7">
      <div v-for="(cell, i) in monthCells" :key="i"
           :class="[
             'border-r border-b border-gray-100 dark:border-gray-800 flex flex-col cursor-pointer transition-colors',
             'p-2 min-h-[110px]',
             (i + 1) % 7 === 0 ? 'border-r-0' : '',
             selectedDay && cell.date.isSame(selectedDay, 'day') ? 'bg-blue-50/80 dark:bg-blue-900/25 ring-2 ring-inset ring-blue-400 dark:ring-blue-500' :
             cell.date.isSame(dayjs(), 'day')  ? 'bg-blue-50/60 dark:bg-blue-900/20' :
             !cell.isCurrentMonth              ? 'bg-gray-50/60 dark:bg-gray-800/40' :
             cell.date.day() === 0             ? 'bg-red-50/20 hover:bg-red-50/40' :
             cell.date.day() === 6             ? 'bg-blue-50/20 hover:bg-blue-50/40' :
             'hover:bg-gray-50/60 dark:hover:bg-gray-800/30'
           ]"
           @click="$emit('selectDay', cell.date)">

        <div class="mb-1.5">
          <span :class="[
                  'font-bold inline-flex items-center justify-center',
                  'text-[15px]',
                  cell.date.isSame(dayjs(), 'day')
                    ? 'w-7 h-7 text-[14px] rounded-full bg-blue-600 text-white'
                    : !cell.isCurrentMonth ? 'text-gray-300 dark:text-gray-600'
                    : cell.date.day() === 0 ? 'text-red-500'
                    : cell.date.day() === 6 ? 'text-blue-500'
                    : 'text-gray-700 dark:text-gray-200'
                ]">
            {{ cell.date.format('D') }}
          </span>
        </div>

        <div class="flex flex-col gap-0.5">
          <div v-for="b in (isExpanded('m-' + cell.date.format('YYYYMMDD'))
                 ? getBookingsForDate(cell.date)
                 : getBookingsForDate(cell.date).slice(0, 3))" :key="b.id"
               class="px-1.5 py-0.5 rounded-md text-[12px] truncate cursor-pointer leading-snug border-l-[2.5px]"
               :style="{ borderColor: getRoomColor(b.roomId), background: getRoomColor(b.roomId) + '15' }"
               @mouseenter="showTooltip(b, $event)"
               @mouseleave="!tooltip.pinned && (tooltip.show = false)"
               @click.stop="pinTooltip(b, $event)">
            <span class="font-bold" :style="{ color: getRoomColor(b.roomId) }">{{ dayjs(b.startTime).format('HH:mm') }}</span>
            <span class="ml-1 font-medium text-gray-600 dark:text-gray-300">{{ b.title }}</span>
          </div>
          <div v-if="getBookingsForDate(cell.date).length > 3"
               @click.stop="toggleExpand('m-' + cell.date.format('YYYYMMDD'))"
               class="px-1.5 py-0.5 rounded text-[11px] font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 transition-colors cursor-pointer text-center leading-tight">
            <span v-if="!isExpanded('m-' + cell.date.format('YYYYMMDD'))">+{{ getBookingsForDate(cell.date).length - 3 }}개</span>
            <span v-else>접기</span>
          </div>
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
  isExpanded: { type: Function, required: true },
  toggleExpand: { type: Function, required: true },
  tooltip: { type: Object, required: true },
  showTooltip: { type: Function, required: true },
  pinTooltip: { type: Function, required: true },
});

defineEmits(['selectDay']);
</script>

