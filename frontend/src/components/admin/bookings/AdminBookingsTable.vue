<template>
  <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
    <div class="overflow-x-auto">
      <table class="w-full">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100">
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">회의실</th>
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">제목</th>
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">예약자</th>
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">일시</th>
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">상태</th>
            <th class="text-right px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="bookings.length === 0">
            <td colspan="6" class="py-16 text-center">
              <div class="flex flex-col items-center gap-2">
                <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="text-slate-200">
                  <rect x="4" y="6" width="32" height="28" rx="4" stroke="currentColor" stroke-width="2" />
                  <path d="M4 14h32M13 4v5M27 4v5" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                </svg>
                <p class="text-[14px] font-semibold text-slate-400">예약이 없습니다</p>
              </div>
            </td>
          </tr>

          <tr
            v-for="b in bookings"
            :key="b.id"
            class="border-b border-slate-50 hover:bg-indigo-50/20 transition-colors group"
          >
            <td class="px-6 py-3.5">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{ background: getRoomColor(b.roomId) }"></span>
                <span class="text-[14px] font-semibold text-slate-700">{{ getRoomName(b.roomId) }}</span>
              </div>
            </td>
            <td class="px-6 py-3.5">
              <p class="text-[14px] font-semibold text-slate-800">{{ b.title }}</p>
              <p v-if="b.description" class="text-[12px] text-slate-500 truncate max-w-[180px]">{{ b.description }}</p>
            </td>
            <td class="px-6 py-3.5 text-[14px] text-slate-700">{{ b.organizer }}</td>
            <td class="px-6 py-3.5">
              <p class="text-[13px] font-semibold text-slate-700">{{ dayjs(b.startTime).format('MM/DD (ddd)') }}</p>
              <p class="text-[12px] text-slate-500">{{ dayjs(b.startTime).format('HH:mm') }} – {{ dayjs(b.endTime).format('HH:mm') }}</p>
            </td>
            <td class="px-6 py-3.5">
              <span
                v-if="dayjs().isAfter(dayjs(b.startTime)) && dayjs().isBefore(dayjs(b.endTime))"
                class="flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-600"
              >
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span> 진행중
              </span>
              <span
                v-else-if="dayjs().isBefore(dayjs(b.startTime))"
                class="text-[11px] font-bold px-2 py-0.5 rounded-full bg-blue-50 text-blue-500"
              >예정</span>
              <span v-else class="text-[11px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-400">종료</span>
            </td>
            <td class="px-6 py-3.5 text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                  type="button"
                  @click="$emit('edit', b)"
                  class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-indigo-500 hover:bg-indigo-50 transition-all"
                >수정</button>
                <button
                  type="button"
                  @click="$emit('cancel', b)"
                  class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-red-400 hover:bg-red-50 transition-all"
                >취소</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
defineProps({
  bookings: { type: Array, required: true },
  getRoomName: { type: Function, required: true },
  getRoomColor: { type: Function, required: true },
  dayjs: { type: Function, required: true },
});

defineEmits(['edit', 'cancel']);
</script>

