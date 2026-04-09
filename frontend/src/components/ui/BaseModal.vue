<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex items-center justify-center p-4"
    :class="overlayWrapperClass"
  >
    <div
      class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm"
      :class="overlayClass"
      @click="onOverlayClick"
    ></div>

    <div
      class="relative bg-white w-full rounded-2xl shadow-xl border border-slate-200 overflow-hidden"
      :class="[maxWidthClass, contentClass]"
      @click.stop
    >
      <slot name="header" />
      <slot />
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  show: { type: Boolean, required: true },
  closeOnOverlay: { type: Boolean, default: true },
  maxWidthClass: { type: String, default: 'max-w-sm' },
  overlayWrapperClass: { type: String, default: '' },
  overlayClass: { type: String, default: '' },
  contentClass: { type: String, default: '' },
});

const emit = defineEmits(['close']);

const onOverlayClick = () => {
  if (!props.closeOnOverlay) return;
  emit('close');
};
</script>

