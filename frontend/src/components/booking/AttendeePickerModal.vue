<template>
  <div v-if="show" class="fixed inset-0 z-[60] flex items-center justify-center p-4">

    <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm"
         @click="$emit('close')"
         @touchmove.prevent>
    </div>

    <div class="relative bg-white dark:bg-gray-900 w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 dark:border-gray-700 overflow-hidden flex flex-col h-[500px] max-h-[calc(100dvh-2rem)] min-h-[320px]"
         @click.stop>

      <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100 dark:border-gray-700">
        <h3 class="text-[14px] font-black text-slate-700 dark:text-gray-200">참석자 선택</h3>
        <button type="button" @click="$emit('close')"
                class="w-7 h-7 flex items-center justify-center rounded-full hover:bg-slate-100 dark:hover:bg-gray-700 transition-colors">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M1 1l10 10M11 1L1 11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" class="text-slate-400 dark:text-gray-500"/>
          </svg>
        </button>
      </div>

      <div class="px-4 py-3 border-b border-slate-100 dark:border-gray-700">
        <div class="flex items-center gap-2 px-3 py-2 bg-slate-50 dark:bg-gray-800 rounded-xl border border-slate-200 dark:border-gray-600">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="none" class="text-slate-400 flex-shrink-0">
            <circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.5"/>
            <path d="M11 11l3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <input v-model="query" type="text" placeholder="이름 또는 사번 검색..."
                 class="flex-1 bg-transparent text-[13px] font-semibold text-slate-700 dark:text-gray-200 outline-none placeholder:text-slate-300 dark:placeholder:text-gray-600 placeholder:font-normal" />
          <button v-if="query" type="button" @click="query = ''"
                  class="text-slate-300 dark:text-gray-600 hover:text-slate-400 dark:hover:text-gray-500">
            <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
              <path d="M1 1l8 8M9 1L1 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="overflow-y-auto overscroll-contain flex-1 touch-pan-y">
        <div v-if="filtered.length === 0" class="flex flex-col items-center justify-center py-10 text-slate-400 dark:text-gray-500">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" class="mb-2">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.5"/>
            <path d="M16.5 16.5l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <p class="text-[12px] font-semibold">검색 결과가 없습니다</p>
        </div>

        <button v-for="u in filtered" :key="u.id" type="button"
                @click="toggleUser(u)"
                class="w-full flex items-center gap-3 px-4 py-2.5 hover:bg-slate-50 dark:hover:bg-gray-800 transition-colors text-left">
          <div class="w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 transition-colors"
               :class="isChecked(u) ? 'bg-indigo-500' : 'bg-slate-100 dark:bg-gray-700'">
            <svg v-if="isChecked(u)" width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M2 6l3 3 5-5" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg v-else width="11" height="11" viewBox="0 0 15 15" fill="none">
              <circle cx="7.5" cy="5" r="3" stroke="#94a3b8" stroke-width="1.5"/>
              <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-[13px] font-bold text-slate-700 dark:text-gray-200 truncate">{{ u.name }}</p>
            <p class="text-[11px] text-slate-600 dark:text-gray-300">{{ u.employeeId }}</p>
          </div>
          <div v-if="isChecked(u)"
               class="text-[10px] font-bold text-indigo-400 dark:text-indigo-500 flex-shrink-0">선택됨</div>
        </button>
      </div>

      <div class="flex items-center justify-between px-4 py-3 border-t border-slate-100 dark:border-gray-700 bg-slate-50 dark:bg-gray-800/50">
        <span class="text-[12px] font-bold text-slate-700 dark:text-gray-200">
          {{ pendingCount > 0 ? `${pendingCount}명 선택` : '선택된 인원 없음' }}
        </span>
        <div class="flex items-center gap-2">
          <button type="button" @click="$emit('close')"
                  class="px-3 py-1.5 text-[12px] font-bold text-slate-700 dark:text-gray-200 hover:bg-slate-200 dark:hover:bg-gray-700 rounded-lg transition-colors">
            취소
          </button>
          <button type="button" @click="confirm"
                  class="px-4 py-1.5 text-[12px] font-bold rounded-lg transition-colors bg-indigo-500 hover:bg-indigo-600 text-white">
            확인
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue';

const props = defineProps({
  show: { type: Boolean, required: true },
  allUsers: { type: Array, default: () => [] },
  alreadySelected: { type: Array, default: () => [] },
});

const emit = defineEmits(['close', 'confirm']);

const query = ref('');
const pendingSelections = ref([]);

// 스크롤 방지 로직 간소화 (body fixed 제거)
const lockBodyScroll = () => {
  if (typeof document !== 'undefined') {
    document.body.style.overflow = 'hidden';
  }
};

const unlockBodyScroll = () => {
  if (typeof document !== 'undefined') {
    document.body.style.overflow = '';
  }
};

watch(() => props.show, (val) => {
  if (val) {
    lockBodyScroll();
    query.value = '';
    pendingSelections.value = (props.alreadySelected || [])
        .filter(a => a && a.id != null)
        .map(a => ({ id: a.id, name: a.name, employeeId: a.employeeId }));
  } else {
    unlockBodyScroll();
  }
});

onBeforeUnmount(() => {
  unlockBodyScroll();
});

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return props.allUsers;
  return props.allUsers.filter(u =>
      u.name.toLowerCase().includes(q) || u.employeeId.toLowerCase().includes(q)
  );
});

const isChecked = (u) =>
    pendingSelections.value.some(a => Number(a.id) === Number(u.id));

const pendingCount = computed(() => pendingSelections.value.length);

const toggleUser = (u) => {
  const idx = pendingSelections.value.findIndex(a => Number(a.id) === Number(u.id));
  if (idx >= 0) {
    pendingSelections.value.splice(idx, 1);
  } else {
    pendingSelections.value.push(u);
  }
};

const confirm = () => {
  emit('confirm', [...pendingSelections.value]);
  emit('close');
};
</script>