<template>
  <!-- 헤더 -->
  <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 dark:border-gray-800 flex-shrink-0">
    <div class="flex items-center gap-3">
      <div class="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
        <AppIcon name="user" :size="16" class="text-white" />
      </div>
      <div>
        <h3 class="text-[15px] font-black text-gray-800 dark:text-gray-100 leading-tight">내 예약</h3>
        <p class="text-[11px] text-gray-400 dark:text-gray-500 leading-tight">{{ myBookings.length }}건</p>
      </div>
    </div>
    <button @click="showMyBookings = false"
            class="w-8 h-8 flex items-center justify-center rounded-lg text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-all">
      <AppIcon name="close" :size="16" />
    </button>
  </div>

  <!-- 내용 -->
  <div class="flex-1 overflow-y-auto px-4 py-4 flex flex-col gap-5 custom-scrollbar">

    <!-- 빈 상태 -->
    <div v-if="myBookings.length === 0"
         class="flex flex-col items-center justify-center py-16 gap-3">
      <div class="w-14 h-14 rounded-2xl bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
        <AppIcon name="calendar" :size="22" class="text-slate-400" />
      </div>
      <p class="text-[14px] font-semibold text-gray-400">예약이 없습니다</p>
    </div>

    <template v-else>
      <template v-for="section in sections" :key="section.label">
        <div v-if="section.list.length > 0">
          <p class="text-[11px] font-black uppercase tracking-widest text-gray-400 dark:text-gray-500 px-1 mb-2">{{ section.label }}</p>
          <div class="flex flex-col gap-2">
            <div v-for="b in section.list" :key="b.id"
                 class="bg-white dark:bg-gray-800 rounded-2xl border border-slate-200 dark:border-gray-700 overflow-hidden flex hover:shadow-md transition-all">
              <div class="w-1.5 flex-shrink-0" :style="{ background: getRoomColor(b.roomId) }"></div>
              <div class="flex-1 px-4 py-3">
                <div class="flex items-start justify-between gap-2">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-1.5 mb-1.5 flex-wrap">
                      <span class="text-[10px] font-bold px-2 py-0.5 rounded-full text-white flex-shrink-0"
                            :style="{ background: getRoomColor(b.roomId) }">{{ getRoomName(b.roomId) }}</span>
                      <span :class="b.userId === currentUser.id ? 'bg-blue-100 text-indigo-600' : 'bg-amber-100 text-amber-600'"
                            class="text-[10px] font-bold px-2 py-0.5 rounded-full flex-shrink-0">
                        {{ b.userId === currentUser.id ? '예약자' : '참석자' }}
                      </span>
                    </div>
                    <p class="text-[13px] font-bold text-gray-800 dark:text-gray-100 truncate">{{ b.title }}</p>
                    <p class="text-[12px] font-semibold text-slate-500 dark:text-gray-400 mt-0.5 tabular-nums">{{ section.fmt(b) }}</p>
                    <p class="flex items-center gap-1 text-[11px] text-gray-400 dark:text-gray-500 mt-1">
                      <AppIcon name="user" :size="10" cls="flex-shrink-0" />
                      {{ b.organizer }}
                    </p>
                    <p v-if="b.attendeeIds?.length || b.externalAttendeeNames?.length"
                       class="flex items-center gap-1 text-[11px] text-gray-400 mt-0.5">
                      <AppIcon name="users" :size="11" cls="flex-shrink-0" />
                      {{ resolveAttendees(b.attendeeIds, b.externalAttendeeNames) }}
                    </p>
                  </div>
                  <div v-if="canEditOrCancel(b)" class="flex gap-1.5 flex-shrink-0 pt-0.5">
                    <button @click="handleEdit(b)"
                            class="w-7 h-7 flex items-center justify-center rounded-lg bg-indigo-50 text-blue-600 hover:bg-blue-100 transition-all">
                      <AppIcon name="edit" :size="13" />
                    </button>
                    <button @click="handleCancel(b)"
                            class="w-7 h-7 flex items-center justify-center rounded-lg bg-red-50 text-red-400 hover:bg-red-100 transition-all">
                      <AppIcon name="trash" :size="13" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import dayjs from 'dayjs';
import AppIcon from '../icons/AppIcon.vue';
import {
  showMyBookings, myBookings, myBookingsToday, myBookingsThisWeek, myBookingsUpcoming,
  getRoomColor, getRoomName, resolveAttendees, canEditOrCancel,
  openEditModal, confirmCancel, currentUser,
} from '../../composables/useApp';

const sections = computed(() => [
  {
    label: '오늘 회의',
    list: myBookingsToday.value,
    fmt: b => `${dayjs(b.startTime).format('HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}`,
  },
  {
    label: '이번 주 회의',
    list: myBookingsThisWeek.value,
    fmt: b => `${dayjs(b.startTime).format('MM/DD (dd) HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}`,
  },
  {
    label: '이후 일정',
    list: myBookingsUpcoming.value,
    fmt: b => `${dayjs(b.startTime).format('MM/DD (dd) HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}`,
  },
]);

const handleEdit = (b) => {
  openEditModal(b);
  showMyBookings.value = false;
};

const handleCancel = (b) => {
  confirmCancel(b);
  showMyBookings.value = false;
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
</style>
