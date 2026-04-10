<template>
  <AdminBookings :is-mobile="isMobile" />
</template>

<script setup>
import AdminBookings from '../../components/admin/AdminBookings.vue';

defineProps({
  isMobile: { type: Boolean, default: false },
});
</script>
