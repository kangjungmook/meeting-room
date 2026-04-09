<template>
  <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
    <div class="overflow-x-auto">
      <table class="w-full">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100">
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">회의실명</th>
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">수용인원</th>
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">색상</th>
            <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">상태</th>
            <th class="text-right px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="rooms.length === 0">
            <td colspan="5" class="py-16 text-center">
              <div class="flex flex-col items-center gap-2">
                <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="text-slate-200">
                  <rect x="4" y="8" width="32" height="24" rx="4" stroke="currentColor" stroke-width="2" />
                  <path d="M12 8V6M28 8V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                </svg>
                <p class="text-[14px] font-semibold text-slate-400">회의실이 없습니다</p>
              </div>
            </td>
          </tr>

          <tr
            v-for="room in rooms"
            :key="room.id"
            class="border-b border-slate-50 hover:bg-indigo-50/20 transition-colors group"
          >
            <td class="px-6 py-3.5">
              <div class="flex items-center gap-3">
                <span class="w-8 h-8 rounded-xl flex-shrink-0 border border-slate-200/80 shadow-sm" :style="{background: room.colorCode}"></span>
                <span class="text-[15px] font-bold text-slate-800">{{ room.name }}</span>
              </div>
            </td>
            <td class="px-6 py-3.5">
              <span class="flex items-center gap-1 text-[14px] text-slate-600">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="text-slate-400">
                  <circle cx="7" cy="5.5" r="2.5" stroke="currentColor" stroke-width="1.4" />
                  <path d="M2 12c0-2.8 2.2-5 5-5s5 2.2 5 5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
                </svg>
                {{ room.capacity }}인
              </span>
            </td>
            <td class="px-6 py-3.5">
              <div class="flex items-center gap-2">
                <span class="w-5 h-5 rounded-md border border-slate-200/80 shadow-sm" :style="{background: room.colorCode}"></span>
                <span class="text-[12px] text-slate-500 font-mono">{{ room.colorCode }}</span>
              </div>
            </td>
            <td class="px-6 py-3.5">
              <button
                type="button"
                @click="$emit('toggle', room)"
                :class="[
                  'flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[12px] font-bold transition-all',
                  room.isActive
                    ? 'bg-emerald-50 text-emerald-600 hover:bg-emerald-100 border border-emerald-200/60'
                    : 'bg-slate-100 text-slate-500 hover:bg-slate-200 border border-slate-200/60',
                ]"
              >
                <span :class="['w-1.5 h-1.5 rounded-full', room.isActive ? 'bg-emerald-500' : 'bg-slate-400']"></span>
                {{ room.isActive ? '활성' : '비활성' }}
              </button>
            </td>
            <td class="px-6 py-3.5">
              <div class="flex items-center justify-end gap-2">
                <a :href="`/kiosk/${room.id}`" target="_blank" class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-emerald-500 hover:bg-emerald-50 transition-all">키오스크</a>
                <button type="button" @click="$emit('edit', room)" class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-slate-700 hover:bg-slate-100 transition-all">수정</button>
                <button type="button" @click="$emit('delete', room)" class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-red-400 hover:bg-red-50 transition-all">삭제</button>
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
  rooms: { type: Array, required: true },
});
defineEmits(['toggle', 'edit', 'delete']);
</script>

