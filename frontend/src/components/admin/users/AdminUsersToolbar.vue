<template>
  <div class="flex items-center gap-3 flex-wrap">
    <div class="relative flex-1 min-w-[180px]">
      <AppIcon name="search" :size="14" cls="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
      <input
        :value="searchKeyword"
        type="text"
        placeholder="이름 또는 사번 검색..."
        class="w-full bg-white border border-slate-200 rounded-xl pl-9 pr-4 py-2.5 text-[13px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-300 placeholder:text-slate-400"
        @input="$emit('update:searchKeyword', $event.target.value)"
      />
    </div>

    <span class="text-[12px] text-slate-400 font-semibold whitespace-nowrap">{{ userCount }}명</span>

    <div
      v-if="effectiveFilter === 'APPROVED' || effectiveFilter === 'ALL'"
      class="flex bg-white border border-slate-200 rounded-xl p-0.5 gap-0.5"
    >
      <button
        v-for="of_ in onlineFilters"
        :key="of_.k"
        type="button"
        @click="$emit('update:onlineFilter', of_.k)"
        :class="[
          'px-3 py-1.5 rounded-lg text-[12px] font-bold transition-all flex items-center gap-1.5',
          onlineFilter === of_.k
            ? of_.k === 'ONLINE'
              ? 'bg-emerald-500 text-white'
              : of_.k === 'OFFLINE'
                ? 'bg-slate-400 text-white'
                : 'bg-slate-800 text-white'
            : 'text-slate-500 hover:bg-slate-50',
        ]"
      >
        <span v-if="of_.k === 'ONLINE'" class="w-1.5 h-1.5 rounded-full bg-current"></span>
        <span v-else-if="of_.k === 'OFFLINE'" class="w-1.5 h-1.5 rounded-full bg-current opacity-50"></span>
        {{ of_.l }}
      </button>
    </div>

    <template v-if="effectiveFilter === 'PENDING' && selectedCount > 0">
      <span class="text-[13px] text-slate-500 font-semibold">{{ selectedCount }}명 선택됨</span>
      <button
        type="button"
        @click="$emit('bulkApprove')"
        class="px-3.5 py-2 rounded-xl text-[13px] font-bold text-emerald-600 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200/60 transition-all"
      >
        선택 승인
      </button>
      <button
        type="button"
        @click="$emit('bulkReject')"
        class="px-3.5 py-2 rounded-xl text-[13px] font-bold text-red-500 bg-red-50 hover:bg-red-100 border border-red-200/60 transition-all"
      >
        선택 거절
      </button>
    </template>

    <button
      type="button"
      @click="$emit('openCreateModal')"
      class="ml-auto flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-[13px] font-bold text-white bg-indigo-500 hover:bg-indigo-600 transition-all"
    >
      <AppIcon name="plus" :size="13" />
      유저 생성
    </button>
  </div>
</template>

<script setup>
import AppIcon from '../../icons/AppIcon.vue';

defineProps({
  effectiveFilter: { type: String, required: true },
  searchKeyword: { type: String, required: true },
  onlineFilter: { type: String, required: true },
  userCount: { type: Number, required: true },
  selectedCount: { type: Number, required: true },
});

defineEmits([
  'update:searchKeyword',
  'update:onlineFilter',
  'bulkApprove',
  'bulkReject',
  'openCreateModal',
]);

const onlineFilters = [
  { k: 'ALL', l: '전체' },
  { k: 'ONLINE', l: '온라인' },
  { k: 'OFFLINE', l: '오프라인' },
];
</script>

