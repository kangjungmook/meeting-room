<template>
  <div class="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex-shrink-0">
    <div class="overflow-x-auto">
      <div class="grid" style="min-width: 820px; grid-template-columns: 112px repeat(7, minmax(100px, 1fr));">

        <div class="sticky left-0 z-30 bg-gray-50 dark:bg-gray-800 border-b border-r border-gray-200 dark:border-gray-700 h-14"></div>

        <div v-for="day in weekDays" :key="day.format()"
             class="flex flex-col items-center justify-center gap-1 border-b border-r border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 last:border-r-0 cursor-pointer select-none h-14"
             :class="day.day() === 0 ? 'bg-red-50/50 dark:bg-red-900/10' : day.day() === 6 ? 'bg-blue-50/50 dark:bg-blue-900/10' : ''"
             @click.stop="goToDay(day)">
          <span class="text-[10px] font-bold uppercase tracking-wider"
                :style="day.day() === 0 ? { color: '#f87171' } : day.day() === 6 ? { color: '#60a5fa' } : { color: '#94a3b8' }">
            {{ day.format('ddd') }}
          </span>
          <span :class="[
                  'text-[13px] w-7 h-7 font-bold flex items-center justify-center rounded-full transition-all',
                  day.isSame(dayjs(), 'day') ? 'bg-blue-600 text-white' :
                  day.day() === 0 ? 'text-red-500 hover:bg-red-50' :
                  day.day() === 6 ? 'text-blue-500 hover:bg-blue-50' : 'text-gray-800 dark:text-gray-100 hover:bg-gray-100 dark:hover:bg-gray-700'
                ]">
            {{ day.format('D') }}
          </span>
        </div>

        <template v-for="room in rooms" :key="room.id">
          <div class="sticky left-0 z-20 flex items-center gap-2 px-2.5 bg-gray-50 dark:bg-gray-800 border-b border-r border-gray-200 dark:border-gray-700"
               style="min-height: 110px;">
            <span class="w-1.5 h-8 rounded-full flex-shrink-0" :style="{ background: room.colorCode }"></span>
            <div class="min-w-0">
              <p class="text-[13px] font-bold text-gray-800 dark:text-gray-100 leading-tight truncate">{{ room.name }}</p>
              <p class="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">{{ room.capacity }}인</p>
            </div>
          </div>

          <div v-for="day in weekDays" :key="day.format()"
               :class="[
                 'border-b border-r border-gray-100 dark:border-gray-800 last:border-r-0 p-2 flex flex-col gap-1.5 transition-colors',
                 day.isSame(dayjs(), 'day') ? 'bg-blue-50/50 dark:bg-blue-900/10' : 'hover:bg-gray-50 dark:hover:bg-gray-800/50'
               ]"
               @click="openQuickModal(room.id, 9, day)">

            <div v-for="b in filterBookings(room.id, day).slice(0, 2)" :key="b.id"
                 @click.stop="pinTooltip(b, $event)"
                 class="rounded-lg px-1.5 py-1.5 cursor-pointer transition-all hover:brightness-95 border-l-2"
                 :style="{ borderLeftColor: room.colorCode, background: room.colorCode + '18' }">
              <p class="text-[10px] font-bold truncate leading-tight" :style="{ color: room.colorCode }">
                {{ dayjs(b.startTime).format('HH:mm') }}
              </p>
              <p class="text-[9px] font-semibold text-gray-700 dark:text-gray-200 leading-tight mt-0.5 truncate">{{ b.title }}</p>

              <div v-if="b.organizer" class="flex items-center gap-0.5 mt-0.5">
                <svg width="8" height="8" viewBox="0 0 15 15" fill="none" class="flex-shrink-0 text-gray-400">
                  <circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <p class="text-[9px] text-gray-500 dark:text-gray-400 truncate leading-tight">{{ b.organizer }}</p>
              </div>

              <div v-if="resolveAttendees(b.attendeeIds, b.externalAttendeeNames)" class="flex items-center gap-0.5 mt-0.5">
                <svg width="10" height="8" viewBox="0 0 18 14" fill="none" class="flex-shrink-0 text-gray-400">
                  <circle cx="6" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M0.5 13c0-3 2.5-5.2 5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                  <circle cx="12" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M17.5 13c0-3-2.5-5.2-5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                </svg>
                <p class="text-[9px] text-gray-500 dark:text-gray-400 truncate leading-tight">{{ resolveAttendees(b.attendeeIds, b.externalAttendeeNames) }}</p>
              </div>
            </div>

            <div v-if="filterBookings(room.id, day).length > 2"
                 @click.stop="goToDay(day)"
                 class="text-[9px] font-bold text-blue-500 text-center py-0.5">
              +{{ filterBookings(room.id, day).length - 2 }}
            </div>

            <div v-if="filterBookings(room.id, day).length === 0"
                 class="flex-1 flex items-center justify-center py-1">
              <div class="w-6 h-6 rounded-full flex items-center justify-center bg-gray-100 dark:bg-gray-800 text-gray-300 dark:text-gray-600 hover:bg-blue-50 hover:text-blue-400 transition-colors">
                <svg width="9" height="9" viewBox="0 0 12 12" fill="none">
                  <path d="M6 1v10M1 6h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
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
import { useApp } from '../../../composables/useApp';
import dayjs from 'dayjs';

const {
  rooms, weekDays,
  pinTooltip, openQuickModal,
  filterBookings, resolveAttendees,
  goToDay,
} = useApp();
</script>

<style scoped>
.grid {
  display: grid;
}

</style>