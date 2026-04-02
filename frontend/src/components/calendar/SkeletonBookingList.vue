<template>
  <div class="flex flex-col gap-2.5">
    <div v-for="i in count" :key="i"
         class="bg-white dark:bg-gray-900 rounded-2xl overflow-hidden flex shadow-sm border border-gray-100 dark:border-gray-800 animate-pulse">
      <!-- 컬러 바 -->
      <div class="w-[5px] flex-shrink-0 bg-gray-200 dark:bg-gray-700"></div>

      <div class="flex-1 px-4 py-3.5 flex items-center gap-3 min-w-0">
        <!-- 메인 콘텐츠 -->
        <div class="flex-1 min-w-0 space-y-2">
          <!-- 배지 자리 -->
          <div class="flex items-center gap-1.5">
            <div class="h-4 w-16 rounded-full bg-gray-200 dark:bg-gray-700"></div>
          </div>
          <!-- 제목 자리 -->
          <div class="h-4 w-3/5 rounded-md bg-gray-200 dark:bg-gray-700"></div>
          <!-- 주최자 자리 -->
          <div class="h-3 w-2/5 rounded-md bg-gray-100 dark:bg-gray-800"></div>
        </div>

        <!-- 우측 시간 자리 -->
        <div class="flex-shrink-0 flex flex-col items-end gap-1.5">
          <div class="h-4 w-12 rounded-md bg-gray-200 dark:bg-gray-700"></div>
          <div class="h-3 w-10 rounded-md bg-gray-100 dark:bg-gray-800"></div>
          <div class="h-4 w-14 rounded-full bg-gray-200 dark:bg-gray-700"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  count: { type: Number, default: 3 },
});
</script>
