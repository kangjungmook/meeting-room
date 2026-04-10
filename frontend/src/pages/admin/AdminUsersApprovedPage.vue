<template>
  <AdminUsers :is-mobile="isMobile" filter="APPROVED" />
</template>

<script setup>
import AdminUsers from '../../components/admin/AdminUsers.vue';

defineProps({
  isMobile: { type: Boolean, default: false },
});
</script>
