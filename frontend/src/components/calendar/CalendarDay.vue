<template>
  <div :class="isMobile ? 'flex min-h-0 w-full min-w-0 flex-1 flex-col' : 'flex min-h-0 w-full min-w-0 md:flex-1 flex-col'">
    <CalendarDayDesktop v-if="!isMobile" />
    <CalendarDayMobile v-else />
  </div>
</template>

<script setup>
import { useApp } from '../../composables/useApp';
import CalendarDayDesktop from './day/CalendarDayDesktop.vue';
import CalendarDayMobile from './day/CalendarDayMobile.vue';

const { isMobile } = useApp();
</script>
