<template>
  <div class="flex flex-col gap-3">
    <div v-if="bookings.length === 0" class="bg-white rounded-2xl border border-slate-200 py-12 text-center text-slate-500 text-[15px] font-semibold">
      예약이 없습니다
    </div>
    <div v-for="b in bookings" :key="b.id" class="bg-white rounded-2xl border border-slate-200 p-4">
      <div class="flex items-start justify-between gap-2 mb-2">
        <div class="flex items-center gap-2 min-w-0">
          <span class="w-2.5 h-2.5 rounded-full flex-shrink-0 mt-0.5" :style="{background: getRoomColor(b.roomId)}"></span>
          <p class="text-[15px] font-bold text-slate-800 truncate">{{ b.title }}</p>
        </div>
        <span class="text-[12px] font-semibold text-slate-600 flex-shrink-0">{{ dayjs(b.startTime).format('MM/DD') }}</span>
      </div>
      <div class="flex items-center gap-3 text-[12px] text-slate-800 mb-3">
        <span class="font-semibold text-indigo-500">{{ getRoomName(b.roomId) }}</span>
        <span>·</span><span>{{ dayjs(b.startTime).format('HH:mm') }} – {{ dayjs(b.endTime).format('HH:mm') }}</span>
        <span>·</span><span>{{ b.organizer }}</span>
      </div>
      <p v-if="b.description" class="text-[12px] text-slate-800 mb-3 truncate">{{ b.description }}</p>
      <div class="flex gap-2 pt-3 border-t border-slate-50">
        <button
          type="button"
          @click="$emit('edit', b)"
          class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-indigo-500 bg-indigo-50 hover:bg-indigo-100 transition-all"
        >수정</button>
        <button
          type="button"
          @click="$emit('cancel', b)"
          class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-red-400 bg-red-50 hover:bg-red-100 transition-all"
        >취소</button>
      </div>
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

