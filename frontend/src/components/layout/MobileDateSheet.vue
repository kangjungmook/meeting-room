<template>
  <Transition name="date-sheet">
    <div v-if="showDateSheet" class="fixed inset-0 z-50">

      <!-- 백드롭 -->
      <div class="absolute inset-0 bg-gray-900/40 backdrop-blur-sm" @click="showDateSheet = false"></div>

      <!-- 바텀 시트 -->
      <div class="absolute bottom-0 left-0 right-0 bg-white dark:bg-gray-900 rounded-t-2xl shadow-2xl">

        <!-- 핸들 -->
        <div class="flex justify-center pt-3 pb-1">
          <div class="w-10 h-1.5 rounded-full bg-gray-200 dark:bg-gray-700"></div>
        </div>

        <!-- ── 일 뷰: 날짜 선택 ── -->
        <template v-if="viewMode === 'day'">
          <div class="flex items-center justify-between px-5 py-3">
            <button @click="prevMonth" class="w-8 h-8 flex items-center justify-center rounded-xl text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
            <span class="text-[15px] font-bold text-gray-900 dark:text-gray-50">{{ cursor.format('YYYY년 M월') }}</span>
            <button @click="nextMonth" class="w-8 h-8 flex items-center justify-center rounded-xl text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M5 2l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
          </div>
          <div class="grid grid-cols-7 px-3 pb-1">
            <div v-for="(d, i) in ['일','월','화','수','목','금','토']" :key="d"
                 class="py-1 text-center text-[11.5px] font-bold"
                 :class="i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-400' : 'text-gray-400'">{{ d }}</div>
          </div>
          <div class="grid grid-cols-7 gap-y-1 px-3 pb-4">
            <div v-for="cell in dayCells" :key="cell.key" class="flex items-center justify-center">
              <button v-if="cell.date" @click="selectDay(cell)"
                      class="w-9 h-9 rounded-full flex items-center justify-center text-[13px] font-semibold transition-all"
                      :class="dayClass(cell)">
                {{ cell.date.format('D') }}
              </button>
              <div v-else class="w-9 h-9"></div>
            </div>
          </div>
        </template>

        <!-- ── 주 뷰: 주 선택 ── -->
        <template v-else-if="viewMode === 'week'">
          <div class="flex items-center justify-between px-5 py-3">
            <button @click="prevMonth" class="w-8 h-8 flex items-center justify-center rounded-xl text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
            <span class="text-[15px] font-bold text-gray-900 dark:text-gray-50">{{ cursor.format('YYYY년 M월') }}</span>
            <button @click="nextMonth" class="w-8 h-8 flex items-center justify-center rounded-xl text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M5 2l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
          </div>
          <!-- 주 목록 -->
          <div class="flex flex-col gap-1 px-3 pb-4">
            <button v-for="week in weekRows" :key="week.key"
                    @click="selectWeek(week.start)"
                    class="flex items-center justify-between px-4 py-2.5 rounded-xl text-[13px] font-semibold transition-all"
                    :class="week.isSelected
                      ? 'bg-indigo-500 text-white'
                      : 'text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800'">
              <span>{{ week.start.format('M/D') }} – {{ week.end.format('M/D') }}</span>
              <span class="text-[11px] opacity-60">{{ week.start.format('M월') !== week.end.format('M월') ? week.end.format('M월') + ' 포함' : '' }}</span>
            </button>
          </div>
        </template>

        <!-- ── 월 뷰: 월 선택 ── -->
        <template v-else>
          <div class="flex items-center justify-between px-5 py-3">
            <button @click="cursor = cursor.subtract(1, 'year')" class="w-8 h-8 flex items-center justify-center rounded-xl text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
            <span class="text-[15px] font-bold text-gray-900 dark:text-gray-50">{{ cursor.format('YYYY년') }}</span>
            <button @click="cursor = cursor.add(1, 'year')" class="w-8 h-8 flex items-center justify-center rounded-xl text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M5 2l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
          </div>
          <div class="grid grid-cols-3 gap-2 px-4 pb-4">
            <button v-for="m in 12" :key="m"
                    @click="selectMonth(m)"
                    class="py-3 rounded-xl text-[13px] font-semibold transition-all"
                    :class="isSelectedMonth(m)
                      ? 'bg-indigo-500 text-white'
                      : isTodayMonth(m)
                        ? 'ring-2 ring-indigo-300 text-indigo-600 dark:text-indigo-400'
                        : 'text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800'">
              {{ m }}월
            </button>
          </div>
        </template>

        <!-- 오늘 버튼 -->
        <div class="px-4 pb-4">
          <button @click="selectToday"
                  class="w-full py-3 rounded-xl bg-indigo-500 text-white text-[14px] font-bold active:bg-indigo-600 transition-colors">
            오늘로 이동
          </button>
        </div>

        <div style="padding-bottom: env(safe-area-inset-bottom, 0px)"></div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import dayjs from 'dayjs';
import { showDateSheet, targetDate, viewMode } from '../../composables/app/useCalendar';
import { fetchBookings } from '../../composables/app/useBookingData';

const cursor = ref(targetDate.value.startOf('month'));

// 시트 열릴 때 커서 동기화
watch(showDateSheet, (v) => { if (v) cursor.value = targetDate.value.startOf('month'); });

// ── 일 뷰 ────────────────────────────────────────────────
const dayCells = computed(() => {
  const start = cursor.value.startOf('month');
  const end   = cursor.value.endOf('month');
  const result = [];
  for (let i = 0; i < start.day(); i++) result.push({ key: `s${i}`, date: null });
  for (let d = start; !d.isAfter(end); d = d.add(1, 'day'))
    result.push({ key: d.format('YYYY-MM-DD'), date: d, dow: d.day() });
  const trailing = result.length % 7 === 0 ? 0 : 7 - (result.length % 7);
  for (let i = 0; i < trailing; i++) result.push({ key: `e${i}`, date: null });
  return result;
});

const dayClass = (cell) => {
  const today   = dayjs().format('YYYY-MM-DD');
  const dateStr = cell.date.format('YYYY-MM-DD');
  if (dateStr === targetDate.value.format('YYYY-MM-DD')) return 'bg-indigo-500 text-white shadow-md';
  if (dateStr === today) return 'ring-2 ring-indigo-300 text-indigo-600 dark:text-indigo-400 font-black';
  if (cell.dow === 0) return 'text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20';
  if (cell.dow === 6) return 'text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20';
  return 'text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800';
};

const selectDay = (cell) => {
  targetDate.value = cell.date;
  fetchBookings();
  showDateSheet.value = false;
};

// ── 주 뷰 ────────────────────────────────────────────────
const weekRows = computed(() => {
  const monthStart = cursor.value.startOf('month');
  const monthEnd   = cursor.value.endOf('month');
  const weeks = [];
  let cur = monthStart.startOf('week');
  while (cur.isBefore(monthEnd) || cur.isSame(monthEnd, 'day')) {
    const start = cur;
    const end   = cur.endOf('week');
    const selectedWeekStart = targetDate.value.startOf('week');
    weeks.push({
      key: start.format('YYYY-MM-DD'),
      start,
      end,
      isSelected: start.isSame(selectedWeekStart, 'day'),
    });
    cur = cur.add(1, 'week');
  }
  return weeks;
});

const selectWeek = (weekStart) => {
  targetDate.value = weekStart;
  fetchBookings();
  showDateSheet.value = false;
};

// ── 월 뷰 ────────────────────────────────────────────────
const isSelectedMonth = (m) =>
  cursor.value.year() === targetDate.value.year() && m === targetDate.value.month() + 1;

const isTodayMonth = (m) =>
  cursor.value.year() === dayjs().year() && m === dayjs().month() + 1;

const selectMonth = (m) => {
  targetDate.value = cursor.value.month(m - 1).startOf('month');
  fetchBookings();
  showDateSheet.value = false;
};

// ── 공통 ─────────────────────────────────────────────────
const prevMonth = () => { cursor.value = cursor.value.subtract(1, 'month'); };
const nextMonth = () => { cursor.value = cursor.value.add(1, 'month'); };

const selectToday = () => {
  targetDate.value = dayjs();
  cursor.value = dayjs().startOf('month');
  fetchBookings();
  showDateSheet.value = false;
};
</script>

<style scoped>
.date-sheet-enter-active { transition: opacity 0.25s ease; }
.date-sheet-leave-active { transition: opacity 0.2s ease; }
.date-sheet-enter-from, .date-sheet-leave-to { opacity: 0; }

.date-sheet-enter-active .absolute.bottom-0,
.date-sheet-leave-active .absolute.bottom-0 {
  transition: transform 0.28s cubic-bezier(0.32, 0.72, 0, 1);
}
.date-sheet-enter-from .absolute.bottom-0,
.date-sheet-leave-to   .absolute.bottom-0 { transform: translateY(100%); }
</style>
