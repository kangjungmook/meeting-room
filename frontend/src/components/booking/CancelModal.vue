<template>
  <div v-if="cancelTarget" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
    <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
      <div class="px-7 pt-7 pb-5">
        <div class="w-12 h-12 rounded-2xl bg-red-50 flex items-center justify-center mb-4">
          <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
            <path d="M11 8v4M11 15h.01M3 11a8 8 0 1 0 16 0 8 8 0 0 0-16 0z" stroke="#ef4444" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </div>
        <h3 class="text-[17px] font-bold text-slate-800">예약을 취소할까요?</h3>
        <p class="text-[14px] text-slate-400 mt-1.5">
          <span class="font-bold text-slate-600">{{ cancelTarget.title }}</span> 예약이 삭제됩니다.
        </p>
        <div v-if="cancelError" class="mt-4 flex items-center gap-2 px-3 py-2 bg-red-50 border border-red-100 rounded-xl">
          <svg width="13" height="13" viewBox="0 0 13 13" fill="none" class="flex-shrink-0">
            <path d="M6.5 4v3M6.5 9h.01M1 6.5a5.5 5.5 0 1 0 11 0 5.5 5.5 0 0 0-11 0z" stroke="#ef4444" stroke-width="1.3" stroke-linecap="round"/>
          </svg>
          <span class="text-[12px] font-semibold text-red-500">{{ cancelError }}</span>
        </div>
      </div>
      <div class="flex gap-3 px-7 pb-7">
        <button @click="cancelTarget = null; cancelError = ''"
                class="flex-1 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-500 text-[14px] font-bold transition-all">
          돌아가기
        </button>
        <button @click="doCancel"
                class="flex-1 py-3 rounded-xl bg-red-500 hover:bg-red-600 text-white text-[14px] font-bold transition-all">
          삭제
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useApp } from '../../composables/useApp';

const { cancelTarget, cancelError, doCancel } = useApp();
</script>
