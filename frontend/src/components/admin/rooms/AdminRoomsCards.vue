<template>
  <div class="flex flex-col gap-3">
    <div v-if="rooms.length === 0" class="bg-white rounded-2xl border border-slate-200 py-12 text-center text-slate-500 text-[15px] font-semibold">
      회의실이 없습니다
    </div>
    <div v-for="room in rooms" :key="room.id" class="bg-white rounded-2xl border border-slate-200 p-4">
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-3">
          <span class="w-9 h-9 rounded-xl flex-shrink-0 border border-slate-200" :style="{background: room.colorCode}"></span>
          <div>
            <p class="text-[15px] font-bold text-slate-800">{{ room.name }}</p>
            <p class="text-[12px] text-slate-600">{{ room.capacity }}인 · {{ room.colorCode }}</p>
          </div>
        </div>
        <button
          type="button"
          @click="$emit('toggle', room)"
          :class="[
            'px-3 py-1 rounded-lg text-[12px] font-bold transition-all',
            room.isActive ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-700',
          ]"
        >
          {{ room.isActive ? '활성' : '비활성' }}
        </button>
      </div>
      <div class="flex gap-2 pt-3 border-t border-slate-50">
        <a :href="`/kiosk/${room.id}`" target="_blank" class="flex-1 py-2 text-center rounded-xl text-[13px] font-semibold text-emerald-500 bg-emerald-50 hover:bg-emerald-100 transition-all">키오스크</a>
        <button type="button" @click="$emit('edit', room)" class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-slate-700 bg-slate-50 hover:bg-slate-100 transition-all">수정</button>
        <button type="button" @click="$emit('delete', room)" class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-red-400 bg-red-50 hover:bg-red-100 transition-all">삭제</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  rooms: { type: Array, required: true },
});
defineEmits(['toggle', 'edit', 'delete']);
</script>

