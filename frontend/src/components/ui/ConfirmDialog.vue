<template>
  <BaseModal
    :show="show"
    :close-on-overlay="closeOnOverlay"
    :max-width-class="maxWidthClass"
    @close="$emit('cancel')"
  >
    <template #default>
      <div class="px-7 pt-7 pb-5">
        <slot name="icon" />
        <h3 class="text-[17px] font-bold text-slate-800">{{ title }}</h3>
        <slot name="description">
          <p v-if="description" class="text-[14px] text-slate-700 mt-2 leading-relaxed">
            {{ description }}
          </p>
        </slot>
      </div>
      <div class="flex gap-3 px-7 pb-7">
        <button
          type="button"
          class="flex-1 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all"
          @click="$emit('cancel')"
        >{{ cancelText }}</button>
        <button
          type="button"
          :class="[
            'flex-1 py-3 rounded-xl text-white text-[14px] font-bold transition-all',
            tone === 'danger' ? 'bg-red-500 hover:bg-red-600' : '',
            tone === 'success' ? 'bg-emerald-500 hover:bg-emerald-600' : '',
            tone === 'primary' ? 'bg-indigo-500 hover:bg-indigo-600' : '',
          ]"
          @click="$emit('confirm')"
        >{{ confirmText }}</button>
      </div>
    </template>
  </BaseModal>
</template>

<script setup>
import BaseModal from './BaseModal.vue';

defineProps({
  show: { type: Boolean, required: true },
  title: { type: String, required: true },
  description: { type: String, default: '' },
  cancelText: { type: String, default: '취소' },
  confirmText: { type: String, default: '확인' },
  tone: { type: String, default: 'primary' }, // primary | danger | success
  closeOnOverlay: { type: Boolean, default: true },
  maxWidthClass: { type: String, default: 'max-w-sm' },
});

defineEmits(['confirm', 'cancel']);
</script>

