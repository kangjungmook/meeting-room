<template>
  <MyBookingsPageContent :is-mobile="isMobile" />
</template>

<script setup>
import { useApp } from '../../composables/useApp';
import MyBookingsPageContent from '../../components/myBookings/MyBookingsPageContent.vue';

const { isMobile } = useApp();
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.25s ease; }
.slide-right-enter-from, .slide-right-leave-to       { transform: translateX(100%); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
