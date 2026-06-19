<template>
  <div class="flex bg-gray-100 dark:bg-gray-800 p-0.5 rounded-lg gap-0.5">
    <button v-for="opt in sortOptions" :key="opt.val" @click="sortBy = opt.val"
            :class="['px-3 py-1 rounded-md text-[12px] font-semibold transition-all',
                     sortBy === opt.val ? 'bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-100 shadow-sm' : 'text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300']">
      {{ opt.label }}
    </button>
  </div>
</template>

<script setup>
import { useApp } from '../../composables/useApp';
const { sortBy, sortOptions } = useApp();
</script>
