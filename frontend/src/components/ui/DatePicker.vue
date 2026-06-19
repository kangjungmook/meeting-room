<template>
  <div ref="root" class="relative">
    <button
      type="button"
      @pointerdown.stop
      @click="toggle"
      class="flex w-full items-center gap-2 rounded-xl border-2 bg-white px-3 py-1.5 text-[13px] font-bold outline-none transition-all dark:bg-gray-800"
      :class="open
        ? 'border-indigo-400'
        : modelValue
          ? 'border-slate-200 text-slate-700 dark:border-gray-600 dark:text-gray-200'
          : 'border-slate-200 text-slate-300 dark:border-gray-600 dark:text-gray-600'"
    >
      <svg width="13" height="13" viewBox="0 0 18 18" fill="none" class="flex-shrink-0 text-indigo-400">
        <rect x="1.5" y="3.5" width="15" height="13" rx="2" stroke="currentColor" stroke-width="1.6" />
        <path d="M5.5 1.5v3M12.5 1.5v3M1.5 8h15" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
      </svg>
      <span class="flex-1 text-left">{{ displayLabel }}</span>
      <svg width="11" height="11" viewBox="0 0 14 14" fill="none" class="flex-shrink-0 text-slate-400 transition-transform" :class="open ? 'rotate-180' : ''">
        <path d="M2 5l5 5 5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </button>

    <Teleport to="body">
      <div
        v-if="open"
        ref="panel"
        @pointerdown.stop
        class="fixed z-[999] overflow-y-auto rounded-2xl border border-gray-200 bg-white shadow-2xl dark:border-gray-700 dark:bg-gray-900"
        :style="panelStyle"
      >
        <div class="flex items-center justify-between border-b border-gray-100 px-4 py-3 dark:border-gray-800">
          <button type="button" @click.prevent="prevMonth" class="flex h-7 w-7 items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-gray-100 dark:hover:bg-gray-800">
            <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
              <path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>

          <button
            type="button"
            @click.prevent="toggleHeaderEdit"
            class="min-w-[124px] rounded-lg px-2 py-1 text-[13.5px] font-black text-gray-800 transition-colors hover:bg-gray-100 dark:text-gray-100 dark:hover:bg-gray-800"
          >
            <span v-if="!headerEdit">{{ cursor.format('YYYY년 MM월') }}</span>
            <span v-else class="flex items-center gap-1">
              <input
                ref="yearFieldRef"
                v-model="yearInput"
                type="number"
                min="1900"
                max="2100"
                @click.stop
                @keydown.enter.prevent="applyHeaderChange"
                @keydown.esc.prevent="cancelHeaderEdit"
                class="w-[62px] rounded-md border border-gray-200 bg-white px-1.5 py-1 text-center text-[12px] font-black text-gray-800 outline-none focus:border-indigo-400 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100"
              />
              <select
                v-model="monthInput"
                @click.stop
                @change="applyHeaderChange"
                class="rounded-md border border-gray-200 bg-white px-1.5 py-1 text-[12px] font-black text-gray-800 outline-none focus:border-indigo-400 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100"
              >
                <option v-for="month in monthOptions" :key="month.value" :value="month.value">{{ month.label }}</option>
              </select>
            </span>
          </button>

          <button type="button" @click.prevent="nextMonth" class="flex h-7 w-7 items-cen  ter justify-center rounded-lg text-gray-400 transition-colors hover:bg-gray-100 dark:hover:bg-gray-800">
            <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
              <path d="M5 2l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>
        </div>

        <div class="grid grid-cols-7 px-3 pt-2">
          <div
            v-for="(label, index) in dayLabels"
            :key="label"
            class="py-1 text-center text-[11px] font-bold"
            :class="index === 0 ? 'text-red-400' : index === 6 ? 'text-blue-400' : 'text-gray-400'"
          >
            {{ label }}
          </div>
        </div>

        <div class="grid grid-cols-7 gap-0.5 px-3 pb-3">
          <div v-for="cell in cells" :key="cell.key">
            <button
              v-if="cell.date"
              type="button"
              @click.stop.prevent="select(cell)"
              :aria-disabled="!cell.selectable"
              class="flex h-9 w-full select-none items-center justify-center rounded-xl text-[12.5px] font-bold transition-all"
              :class="cellClass(cell)"
            >
              {{ cell.date.format('D') }}
            </button>
            <div v-else class="h-9 w-full"></div>
          </div>
        </div>

        <div class="px-4 pb-3">
          <button
            v-if="showClear"
            type="button"
            @click.prevent="clear"
            class="w-full rounded-xl border border-gray-200 py-1.5 text-[12px] font-bold text-gray-400 transition-colors hover:bg-gray-50 dark:border-gray-700 dark:hover:bg-gray-800"
          >
            초기화
          </button>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue';
import dayjs from 'dayjs';
import { useClickOutside } from '../../composables/admin/ui/useClickOutside';
import { useAnchoredMenu } from '../../composables/admin/ui/useAnchoredMenu';

const props = defineProps({
  modelValue: { type: String, default: '' },
  rangeEnd: { type: String, default: '' },
  mode: { type: String, default: 'single' },
  min: { type: String, default: '' },
  max: { type: String, default: '' },
  placeholder: { type: String, default: '날짜 선택' },
});

const emit = defineEmits(['update:modelValue', 'update:rangeEnd']);

const dayLabels = ['일', '월', '화', '수', '목', '금', '토'];
const monthOptions = Array.from({ length: 12 }, (_, index) => ({
  value: String(index + 1).padStart(2, '0'),
  label: `${index + 1}월`,
}));

const root = ref(null);
const panel = ref(null);
const yearFieldRef = ref(null);
const open = ref(false);
const headerEdit = ref(false);
const isRangeMode = computed(() => props.mode === 'range');
const cursor = ref(dayjs(props.modelValue || props.rangeEnd || props.min || undefined).startOf('month'));

const { style: panelStyle } = useAnchoredMenu({
  open,
  anchorEl: root,
  panelEl: panel,
  mode: 'left',
  minWidth: 280,
  gap: 6,
  viewportPadding: 8,
  defaultHeight: 360,
  minAvailableHeight: 220,
});
const yearInput = ref(String(cursor.value.year()));
const monthInput = ref(cursor.value.format('MM'));

const displayValue = computed(() =>
  props.modelValue ? dayjs(props.modelValue).format('YYYY년 MM월 DD일') : ''
);
const rangeEndDisplayValue = computed(() =>
  props.rangeEnd ? dayjs(props.rangeEnd).format('YYYY년 MM월 DD일') : ''
);
const displayLabel = computed(() => {
  if (!isRangeMode.value) {
    return props.modelValue ? displayValue.value : props.placeholder;
  }
  if (props.modelValue && props.rangeEnd) {
    return `${displayValue.value} ~ ${rangeEndDisplayValue.value}`;
  }
  if (props.modelValue) {
    return `${displayValue.value} ~ 종료일 선택`;
  }
  return props.placeholder;
});
const showClear = computed(() => Boolean(props.modelValue || props.rangeEnd));
const rangeStartTs = computed(() => props.modelValue ? dayjs(props.modelValue).startOf('day').valueOf() : null);
const rangeEndTs = computed(() => props.rangeEnd ? dayjs(props.rangeEnd).startOf('day').valueOf() : null);

const cells = computed(() => {
  const start = cursor.value.startOf('month');
  const end = cursor.value.endOf('month');
  const result = [];

  for (let i = 0; i < start.day(); i += 1) {
    result.push({ key: `empty-start-${i}`, date: null });
  }

  for (let date = start; !date.isAfter(end); date = date.add(1, 'day')) {
    const dateStr = date.format('YYYY-MM-DD');
    const selectable = (!props.min || dateStr >= props.min) && (!props.max || dateStr <= props.max);
    result.push({
      key: dateStr,
      date,
      dateStr,
      selectable,
      dow: date.day(),
    });
  }

  const trailing = result.length % 7 === 0 ? 0 : 7 - (result.length % 7);
  for (let i = 0; i < trailing; i += 1) {
    result.push({ key: `empty-end-${i}`, date: null });
  }

  return result;
});

const syncHeaderInputs = () => {
  yearInput.value = String(cursor.value.year());
  monthInput.value = cursor.value.format('MM');
};

const cellClass = (cell) => {
  if (!cell.selectable) return 'cursor-not-allowed text-gray-200 dark:text-gray-700';
  if (isRangeMode.value && rangeStartTs.value != null) {
    const cellTs = cell.date.startOf('day').valueOf();
    const start = rangeStartTs.value;
    const end = rangeEndTs.value ?? rangeStartTs.value;
    const isStart = cellTs === start;
    const isEnd = cellTs === end;
    const inRange = cellTs >= Math.min(start, end) && cellTs <= Math.max(start, end);
    if (isStart || isEnd) {
      return 'scale-105 bg-indigo-500 text-white shadow-md shadow-indigo-200 dark:shadow-indigo-900/30';
    }
    if (inRange) {
      return 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-200';
    }
  }
  if (cell.dateStr === props.modelValue) {
    return 'scale-105 bg-indigo-500 text-white shadow-md shadow-indigo-200 dark:shadow-indigo-900/30';
  }
  if (props.min && cell.dateStr === props.min) {
    return 'ring-2 ring-indigo-300 text-indigo-600 dark:text-indigo-400 dark:ring-indigo-600';
  }
  if (cell.dateStr === dayjs().format('YYYY-MM-DD')) {
    return 'font-black text-indigo-600 underline underline-offset-2 hover:bg-indigo-50 dark:text-indigo-400 dark:hover:bg-indigo-900/20';
  }
  if (cell.dow === 0) return 'text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20';
  if (cell.dow === 6) return 'text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20';
  return 'text-gray-700 hover:bg-indigo-50 dark:text-gray-200 dark:hover:bg-indigo-900/20';
};

const toggle = () => {
  if (open.value) {
    open.value = false;
    headerEdit.value = false;
    return;
  }
  cursor.value = dayjs(props.modelValue || props.rangeEnd || props.min || undefined).startOf('month');
  syncHeaderInputs();
  open.value = true;
};

const select = (cell) => {
  if (!cell?.selectable) return;
  if (isRangeMode.value) {
    if (!props.modelValue || props.rangeEnd) {
      emit('update:modelValue', cell.dateStr);
      emit('update:rangeEnd', '');
      return;
    }
    if (cell.dateStr < props.modelValue) {
      emit('update:modelValue', cell.dateStr);
      emit('update:rangeEnd', props.modelValue);
    } else {
      emit('update:rangeEnd', cell.dateStr);
    }
    open.value = false;
    headerEdit.value = false;
    return;
  }
  emit('update:modelValue', cell.dateStr);
  open.value = false;
  headerEdit.value = false;
};

const clear = () => {
  emit('update:modelValue', '');
  if (isRangeMode.value) emit('update:rangeEnd', '');
};

const prevMonth = () => {
  cursor.value = cursor.value.subtract(1, 'month');
};

const nextMonth = () => {
  cursor.value = cursor.value.add(1, 'month');
};

const toggleHeaderEdit = async () => {
  headerEdit.value = !headerEdit.value;
  syncHeaderInputs();
  if (!headerEdit.value) return;
  await nextTick();
  yearFieldRef.value?.focus();
  yearFieldRef.value?.select?.();
};

const cancelHeaderEdit = () => {
  headerEdit.value = false;
  syncHeaderInputs();
};

const applyHeaderChange = () => {
  const nextYear = Number(yearInput.value);
  const nextMonth = Number(monthInput.value);
  if (!Number.isInteger(nextYear) || nextYear < 1900 || nextYear > 2100) {
    syncHeaderInputs();
    return;
  }
  if (!Number.isInteger(nextMonth) || nextMonth < 1 || nextMonth > 12) {
    syncHeaderInputs();
    return;
  }
  cursor.value = cursor.value.year(nextYear).month(nextMonth - 1).startOf('month');
  headerEdit.value = false;
};

useClickOutside({
  enabled: open,
  inside: [root, panel],
  onOutside: () => {
    open.value = false;
    headerEdit.value = false;
  },
  eventName: 'pointerdown',
});

watch(cursor, () => {
  syncHeaderInputs();
});
</script>
