<template>
  <CalendarDayDesktop v-if="!isMobile" />
  <CalendarDayMobile v-else />
</template>

<script setup>
import { useApp } from '../../composables/useApp';
import CalendarDayDesktop from './day/CalendarDayDesktop.vue';
import CalendarDayMobile from './day/CalendarDayMobile.vue';

const { isMobile } = useApp();
</script>
