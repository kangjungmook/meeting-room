<template>
  <button
    type="button"
    @click="$emit('update:modelValue', !modelValue)"
    :class="[
      'relative flex-shrink-0 w-11 h-6 rounded-full transition-colors duration-200 focus:outline-none',
      modelValue ? activeClass : inactiveClass,
    ]"
  >
    <span :class="[
      'absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-all duration-200',
      modelValue ? 'left-[22px]' : 'left-0.5',
    ]"></span>
  </button>
</template>

<script setup>
defineProps({
  modelValue:    { type: Boolean, required: true },
  activeClass:   { type: String,  default: 'bg-indigo-500' },
  inactiveClass: { type: String,  default: 'bg-gray-200 dark:bg-gray-700' },
});
defineEmits(['update:modelValue']);
</script>
