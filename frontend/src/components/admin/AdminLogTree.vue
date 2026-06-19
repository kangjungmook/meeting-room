<template>
  <!-- 리프 (원시값) -->
  <span v-if="isLeaf" :class="leafClass">{{ leafDisplay }}</span>

  <!-- 복합 (object / array) -->
  <span v-else>
    <button @click.stop="open = !open"
      class="inline-flex items-center justify-center w-4 h-4 rounded hover:bg-slate-200 transition-colors align-middle mr-0.5">
      <svg width="9" height="9" viewBox="0 0 10 10" fill="none"
        class="transition-transform duration-150 text-slate-400"
        :class="open ? 'rotate-90' : 'rotate-0'">
        <path d="M3 2l4 3-4 3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>
    <span class="text-slate-400 font-bold">{{ bracket[0] }}</span>

    <!-- 접힌 상태 -->
    <span v-if="!open" class="text-[11px] text-slate-400 mx-1 cursor-pointer" @click.stop="open = true">
      {{ entries.length }}개 {{ isArray ? '항목' : '필드' }}
    </span>
    <span v-if="!open" class="text-slate-400 font-bold">{{ bracket[1] }}</span>

    <!-- 펼친 상태 -->
    <template v-if="open">
      <div class="ml-4 pl-3 border-l border-slate-200 flex flex-col gap-0.5 mt-0.5">
        <div v-for="([k, v], i) in entries" :key="k" class="flex items-baseline gap-1 flex-wrap">
          <span v-if="!isArray" class="text-slate-500 flex-shrink-0">"{{ k }}"<span class="text-slate-300 mx-0.5">:</span></span>
          <AdminLogTree :value="v" />
          <span v-if="i < entries.length - 1" class="text-slate-300">,</span>
        </div>
      </div>
      <div class="ml-0">
        <span class="text-slate-400 font-bold">{{ bracket[1] }}</span>
      </div>
    </template>
  </span>
</template>

<script setup>
import { ref, computed } from 'vue';

defineOptions({ name: 'AdminLogTree' });

const props = defineProps({ value: { required: true } });

const open = ref(true);

const type = computed(() => {
  if (props.value === null) return 'null';
  if (Array.isArray(props.value)) return 'array';
  return typeof props.value;
});

const isLeaf  = computed(() => type.value !== 'object' && type.value !== 'array');
const isArray = computed(() => type.value === 'array');

const entries = computed(() => {
  if (type.value === 'array')  return props.value.map((v, i) => [i, v]);
  if (type.value === 'object') return Object.entries(props.value);
  return [];
});

const bracket = computed(() => isArray.value ? ['[', ']'] : ['{', '}']);

const leafClass = computed(() => {
  if (type.value === 'string')  return 'text-emerald-600';
  if (type.value === 'number')  return 'text-blue-600 font-semibold';
  if (type.value === 'boolean') return 'text-amber-500 font-semibold';
  return 'text-slate-400 italic'; // null
});

const leafDisplay = computed(() => {
  if (type.value === 'string') return `"${props.value}"`;
  if (type.value === 'null')   return 'null';
  return String(props.value);
});
</script>
