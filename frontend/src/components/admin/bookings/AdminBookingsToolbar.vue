<template>
  <div class="bg-white rounded-2xl border border-slate-200 p-3 mb-4 flex flex-col gap-3">
    <div class="flex flex-col sm:flex-row gap-2">
      <input
        v-model="bookingFilter.keyword"
        type="text"
        placeholder="제목 또는 예약자 검색"
        class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 text-[14px] font-medium text-slate-800 outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent placeholder:text-slate-400"
      />
      <div class="flex gap-2 flex-wrap">
        <div class="flex gap-1">
          <button
            type="button"
            @click="bookingFilter.date = dayjs().format('YYYY-MM-DD')"
            :class="[
              'px-3 py-2 rounded-xl text-[12px] font-bold transition-all',
              bookingFilter.date === dayjs().format('YYYY-MM-DD')
                ? 'bg-indigo-500 text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200',
            ]"
          >오늘</button>
          <button
            type="button"
            @click="bookingFilter.date = ''"
            :class="[
              'px-3 py-2 rounded-xl text-[12px] font-bold transition-all',
              bookingFilter.date === ''
                ? 'bg-indigo-500 text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200',
            ]"
          >전체</button>
        </div>
        <select
          v-model="bookingFilter.roomId"
          class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-[14px] font-medium text-slate-800 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer"
        >
          <option value="">전체 회의실</option>
          <option v-for="r in allRooms" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>
        <input
          v-model="bookingFilter.date"
          type="date"
          class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-[14px] font-medium text-slate-800 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer"
        />
      </div>
    </div>
    <div class="flex items-center gap-1.5 flex-wrap">
      <span class="text-[11px] font-bold text-slate-500 mr-1">정렬:</span>
      <button
        v-for="s in sortOptions"
        :key="s.key"
        type="button"
        @click="$emit('setSort', s.key)"
        :class="[
          'flex items-center gap-1 px-3 py-1 rounded-lg text-[12px] font-semibold transition-all',
          bookingSort.key === s.key ? 'bg-indigo-500 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200',
        ]"
      >
        {{ s.label }}
        <AppIcon
          v-if="bookingSort.key === s.key"
          name="arrow-right"
          :size="10"
          :class="bookingSort.dir === 'desc' ? 'rotate-90' : '-rotate-90'"
          cls="transition-transform"
        />
      </button>
      <span class="ml-auto text-[12px] text-slate-500 font-semibold">{{ count }}건</span>
    </div>
  </div>
</template>

<script setup>
import AppIcon from '../../icons/AppIcon.vue';

defineProps({
  allRooms: { type: Array, required: true },
  bookingFilter: { type: Object, required: true },
  bookingSort: { type: Object, required: true },
  sortOptions: { type: Array, required: true },
  count: { type: Number, required: true },
  dayjs: { type: Function, required: true },
});

defineEmits(['setSort']);
</script>

