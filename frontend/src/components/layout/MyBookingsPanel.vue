<template>
  <!-- 모바일: 바텀 시트 -->
  <Transition name="my-bookings">
    <div v-if="showMyBookings && isMobile" class="fixed inset-0 z-50 flex flex-col justify-end">
      <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="showMyBookings = false"></div>
      <div class="relative flex flex-col bg-white dark:bg-gray-900 rounded-t-3xl shadow-2xl overflow-hidden"
           style="max-height:92dvh;min-height:60dvh">
        <MyBookingsPanelContent />
      </div>
    </div>
  </Transition>

  <!-- 데스크톱: 우측 슬라이드 패널 -->
  <Transition name="my-bookings-panel">
    <div v-if="showMyBookings && !isMobile"
         class="fixed right-0 top-0 bottom-0 z-50 w-[380px] flex flex-col bg-white dark:bg-gray-900 border-l border-gray-200 dark:border-gray-700 shadow-2xl overflow-hidden">
      <div class="fixed inset-0 z-[-1]" @click="showMyBookings = false"></div>
      <MyBookingsPanelContent />
    </div>
  </Transition>
</template>

<script setup>
import { showMyBookings, isMobile } from '../../composables/useApp';
import MyBookingsPanelContent from './MyBookingsPanelContent.vue';
</script>

<style scoped>
/* ── 내 예약 패널 (모바일 바텀시트) ── */
.my-bookings-enter-active { transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1); }
.my-bookings-leave-active { transition: all 0.2s ease-in; }
.my-bookings-enter-from   { opacity: 0; transform: translateY(100%); }
.my-bookings-leave-to     { opacity: 0; transform: translateY(100%); }

/* ── 내 예약 패널 (데스크톱 우측 슬라이드) ── */
.my-bookings-panel-enter-active { transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1), opacity 0.3s ease; }
.my-bookings-panel-leave-active { transition: transform 0.25s ease-in, opacity 0.2s ease-in; }
.my-bookings-panel-enter-from   { transform: translateX(100%); opacity: 0; }
.my-bookings-panel-leave-to     { transform: translateX(100%); opacity: 0; }
</style>
