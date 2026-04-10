<template>
  <div class="min-h-screen flex items-center justify-center bg-[#F5F7FA] dark:bg-gray-950 px-4">
    <div class="w-full max-w-md bg-white dark:bg-gray-900 border border-slate-200 dark:border-gray-800 rounded-2xl shadow-xl p-8 text-center">
      <div class="mx-auto mb-5 w-14 h-14 rounded-2xl bg-red-50 dark:bg-red-950/30 flex items-center justify-center">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" class="text-red-500">
          <path d="M12 9v4M12 17h.01M4 20h16L12 4 4 20z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <h1 class="text-[18px] font-black text-slate-800 dark:text-slate-100">시스템 점검 중</h1>
      <p class="mt-2 text-[13px] text-slate-500 dark:text-slate-400">
        {{ message }}
      </p>

      <div class="mt-6 flex items-center justify-center gap-2">
        <button
          type="button"
          class="px-4 py-2 rounded-xl bg-slate-900 text-white text-[13px] font-bold hover:bg-slate-800 active:scale-95 transition-all"
          @click="retry"
        >
          새로고침
        </button>
        <button
          type="button"
          class="px-4 py-2 rounded-xl bg-white dark:bg-gray-900 border border-slate-200 dark:border-gray-700 text-slate-700 dark:text-slate-200 text-[13px] font-bold hover:bg-slate-50 dark:hover:bg-gray-800 active:scale-95 transition-all"
          @click="goHome"
        >
          홈으로
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const message = computed(() => {
  try {
    return sessionStorage.getItem('maintenanceMessage') || '현재 시스템 점검 중입니다.';
  } catch {
    return '현재 시스템 점검 중입니다.';
  }
});

const retry = () => window.location.reload();
const goHome = () => router.replace('/');
</script>

