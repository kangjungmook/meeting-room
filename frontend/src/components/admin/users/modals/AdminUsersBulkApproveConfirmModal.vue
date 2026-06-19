<template>
  <ConfirmDialog
    :show="show"
    :title="`${count}명을 승인할까요?`"
    description="승인된 회원은 즉시 로그인이 가능합니다."
    confirm-text="승인"
    cancel-text="취소"
    tone="success"
    @cancel="$emit('cancel')"
    @confirm="$emit('confirm')"
  />
</template>

<script setup>
import ConfirmDialog from '../../../ui/ConfirmDialog.vue';

defineProps({
  show: { type: Boolean, required: true },
  count: { type: Number, required: true },
});
defineEmits(['confirm', 'cancel']);
</script>

