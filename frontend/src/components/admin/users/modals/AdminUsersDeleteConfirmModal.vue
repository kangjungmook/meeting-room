<template>
  <ConfirmDialog
    :show="show"
    title="유저를 삭제할까요?"
    confirm-text="삭제"
    cancel-text="취소"
    tone="danger"
    @cancel="$emit('cancel')"
    @confirm="$emit('confirm')"
  >
    <template #icon>
      <div class="w-12 h-12 rounded-2xl bg-red-50 flex items-center justify-center mb-4">
        <AppIcon name="alert-circle" :size="22" class="text-red-500" />
      </div>
    </template>
    <template #description>
      <p class="text-[14px] text-slate-700 mt-2 leading-relaxed">
        <span class="font-bold text-slate-600">{{ targetName }}</span>
        <template v-if="targetEmployeeId">({{ targetEmployeeId }})</template>
        유저가 삭제됩니다.
        <span class="text-red-500 font-bold">되돌릴 수 없습니다.</span>
      </p>
    </template>
  </ConfirmDialog>
</template>

<script setup>
import AppIcon from '../../../icons/AppIcon.vue';
import ConfirmDialog from '../../../ui/ConfirmDialog.vue';

defineProps({
  show: { type: Boolean, required: true },
  targetName: { type: String, default: '' },
  targetEmployeeId: { type: String, default: '' },
});
defineEmits(['confirm', 'cancel']);
</script>

