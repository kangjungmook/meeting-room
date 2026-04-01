<template>
  <div class="fixed top-4 right-4 z-[9999] flex flex-col gap-2 pointer-events-none">
    <Transition v-for="toast in toasts" :key="toast.id" name="toast">
      <div class="flex items-center gap-3 px-4 py-3 rounded-2xl shadow-lg text-[14px] font-semibold pointer-events-auto max-w-[320px]"
           :class="toast.type === 'success' ? 'bg-emerald-500 text-white' : 'bg-slate-800 text-white'">
        <span>{{ toast.message }}</span>
        <button @click="dismiss(toast.id)"
                class="ml-auto flex-shrink-0 opacity-70 hover:opacity-100 transition-opacity">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
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
.toast-enter-from   { opacity: 0; transform: translateX(40px) scale(0.95); }
.toast-leave-to     { opacity: 0; transform: translateX(40px) scale(0.95); }
</style>
