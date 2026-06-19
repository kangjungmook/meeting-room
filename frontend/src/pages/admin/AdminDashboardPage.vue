<template>
  <AdminDashboard :is-mobile="isMobile" />
</template>

<script setup>
import AdminDashboard from '../../components/admin/AdminDashboard.vue';

defineProps({
  isMobile: { type: Boolean, default: false },
});
</script>
