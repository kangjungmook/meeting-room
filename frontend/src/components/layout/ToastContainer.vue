<template>
  <div class="fixed bottom-6 left-1/2 -translate-x-1/2 z-[9999] flex flex-col items-center gap-2 pointer-events-none w-full px-4">
    <Transition v-for="toast in toasts" :key="toast.id" name="toast">
      <div class="flex items-center gap-3 px-4 py-3.5 rounded-2xl shadow-xl text-[14px] font-semibold pointer-events-auto w-full max-w-[360px]"
           :class="{
             'bg-emerald-500 text-white': toast.type === 'success',
             'bg-red-500 text-white':     toast.type === 'error',
             'bg-gray-800 text-white':    toast.type === 'info' || !toast.type,
           }">
        <!-- 아이콘 -->
        <svg v-if="toast.type === 'success'" width="18" height="18" viewBox="0 0 20 20" fill="none" class="flex-shrink-0">
          <circle cx="10" cy="10" r="9" stroke="white" stroke-width="1.5" opacity="0.5"/>
          <path d="M6 10l3 3 5-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <svg v-else-if="toast.type === 'error'" width="18" height="18" viewBox="0 0 20 20" fill="none" class="flex-shrink-0">
          <circle cx="10" cy="10" r="9" stroke="white" stroke-width="1.5" opacity="0.5"/>
          <path d="M7 7l6 6M13 7l-6 6" stroke="white" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <svg v-else width="18" height="18" viewBox="0 0 20 20" fill="none" class="flex-shrink-0">
          <circle cx="10" cy="10" r="9" stroke="white" stroke-width="1.5" opacity="0.5"/>
          <path d="M10 9v5M10 7h.01" stroke="white" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span class="flex-1">{{ toast.message }}</span>
        <button @click="dismiss(toast.id)"
                class="flex-shrink-0 opacity-60 hover:opacity-100 transition-opacity">
          <svg width="13" height="13" viewBox="0 0 12 12" fill="none">
            <path d="M1 1l10 10M11 1L1 11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { toasts } from '../../composables/app/useNotifications';

const dismiss = (id) => { toasts.value = toasts.value.filter(t => t.id !== id); };
</script>

<style scoped>
.toast-enter-active { transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
.toast-leave-active { transition: all 0.25s ease-in; }
.toast-enter-from   { opacity: 0; transform: translateY(16px) scale(0.95); }
.toast-leave-to     { opacity: 0; transform: translateY(16px) scale(0.95); }
</style>
