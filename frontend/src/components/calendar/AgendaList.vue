<template>
  <div>
    <!-- 헤더 -->
    <div class="flex items-center justify-between mb-3 px-1 flex-wrap gap-2">
      <div class="flex items-center gap-2">
        <p class="text-[14px] font-bold text-gray-700 dark:text-gray-200">{{ title }}</p>
        <span class="text-[11px] font-bold px-2 py-0.5 rounded-full"
              :class="totalCount > 0
                ? 'bg-indigo-100 text-indigo-600 dark:bg-indigo-900/40 dark:text-indigo-400'
                : 'bg-gray-100 text-gray-400 dark:bg-gray-800'">
          {{ totalCount }}건
        </span>
        <slot name="extra" />
      </div>
      <SortBar />
    </div>

    <!-- 로딩 -->
    <SkeletonBookingList v-if="loading && totalCount === 0" :count="3" />

    <!-- 빈 상태 -->
    <div v-else-if="totalCount === 0"
         class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 px-6 py-10 text-center">
      <AppIcon name="calendar" :size="32" cls="mx-auto mb-3 text-gray-200 dark:text-gray-700" />
      <p class="text-[13px] font-medium text-gray-300 dark:text-gray-600">{{ emptyText }}</p>
    </div>

    <!-- 어젠다 카드 -->
    <div v-else class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden">
      <template v-for="group in groups" :key="group.key">
        <div class="flex border-b border-gray-100 dark:border-gray-800 last:border-b-0"
             :class="group.isToday ? 'bg-blue-50/30 dark:bg-blue-900/10' : ''">

          <!-- 날짜 컬럼 (showDate=true 일 때만) -->
          <div v-if="showDate"
               class="w-[60px] flex-shrink-0 flex flex-col items-center justify-start pt-4 pb-3 border-r border-gray-100 dark:border-gray-800">
            <span :class="[
                    'text-[10px] font-bold uppercase tracking-wider mb-1',
                    group.isToday ? 'text-blue-500 dark:text-blue-400' :
                    group.dow === 0 ? 'text-red-400' :
                    group.dow === 6 ? 'text-blue-400' :
                    'text-gray-400 dark:text-gray-500'
                  ]">{{ group.dayLabel }}</span>
            <div :class="[
                   'w-8 h-8 rounded-full flex items-center justify-center text-[15px] font-black',
                   group.isToday ? 'bg-blue-600 text-white' :
                   group.dow === 0 ? 'text-red-500' :
                   group.dow === 6 ? 'text-blue-500' :
                   'text-gray-700 dark:text-gray-200'
                 ]">
              {{ group.dayNum }}
            </div>
          </div>

          <!-- 예약 행들 -->
          <div class="flex-1 min-w-0">
            <BookingListItem
              v-for="b in group.bookings"
              :key="b.id"
              :booking="b"
              flat
            />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import AppIcon from '../icons/AppIcon.vue';
import SortBar from './SortBar.vue';
import BookingListItem from './BookingListItem.vue';
import SkeletonBookingList from './SkeletonBookingList.vue';

const props = defineProps({
  title:     { type: String,  required: true },
  emptyText: { type: String,  default: '예약된 일정이 없습니다' },
  showDate:  { type: Boolean, default: false },
  loading:   { type: Boolean, default: false },
  groups:    { type: Array,   required: true },
});

const totalCount = computed(() =>
  props.groups.reduce((s, g) => s + g.bookings.length, 0)
);
</script>
