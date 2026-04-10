<template>
  <AdminRooms ref="roomsView" :is-mobile="isMobile" />
</template>

<script setup>
import { ref } from 'vue';
import AdminRooms from '../../components/admin/AdminRooms.vue';

defineProps({
  isMobile: { type: Boolean, default: false },
});

const roomsView = ref(null);

defineExpose({
  openModal: () => roomsView.value?.openModal(),
});
</script>
