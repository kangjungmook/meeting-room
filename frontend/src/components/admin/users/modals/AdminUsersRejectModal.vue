<template>
  <BaseModal :show="show" @close="$emit('cancel')">
      <div class="px-6 py-5 border-b border-slate-100">
        <h3 class="text-[17px] font-bold text-slate-800">가입 거절</h3>
        <p class="text-[13px] text-slate-500 mt-0.5">
          <span class="font-semibold text-slate-700">{{ isBulk ? `${targetCount}명` : targetName }}</span>
          의 가입을 거절합니다
        </p>
      </div>
      <div class="px-6 py-5 flex flex-col gap-3">
        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-700">거절 사유 <span class="text-slate-400 font-normal">(선택)</span></label>
          <textarea
            :value="reason"
            rows="3"
            placeholder="예) 정보가 일치하지 않습니다."
            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-[14px] text-slate-700 outline-none focus:ring-2 focus:ring-red-300 resize-none placeholder:text-slate-300"
            @input="$emit('update:reason', $event.target.value)"
          ></textarea>
          <p class="text-[11px] text-slate-400">입력한 사유는 해당 사용자가 로그인 시 확인할 수 있습니다.</p>
        </div>
      </div>
      <div class="flex gap-3 px-6 pb-6">
        <button
          type="button"
          class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all"
          @click="$emit('cancel')"
        >취소</button>
        <button
          type="button"
          class="flex-1 py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white text-[14px] font-bold transition-all"
          @click="$emit('submit')"
        >거절 확인</button>
      </div>
  </BaseModal>
</template>

<script setup>
import BaseModal from '../../../ui/BaseModal.vue';

defineProps({
  show: { type: Boolean, required: true },
  isBulk: { type: Boolean, required: true },
  targetName: { type: String, default: '' },
  targetCount: { type: Number, default: 0 },
  reason: { type: String, default: '' },
});
defineEmits(['submit', 'cancel', 'update:reason']);
</script>

