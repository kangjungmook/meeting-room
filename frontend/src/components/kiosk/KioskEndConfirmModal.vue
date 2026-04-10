<template>
  <div v-if="modelValue" class="modal-overlay" @click.self="$emit('update:modelValue', false)">
    <div class="modal-box">
      <div class="modal-head">
        <div class="modal-title">예약을 지금 종료할까요?</div>
        <button class="modal-close" @click="$emit('update:modelValue', false)">
          <AppIcon name="close" :size="18" />
        </button>
      </div>
      <div class="modal-body">
        <p class="modal-copy">
          <strong>{{ booking?.title }}</strong> 예약을 바로 종료합니다.
        </p>
      </div>
      <div class="modal-btns modal-btns-padded">
        <button class="btn-cancel" @click="$emit('update:modelValue', false)">취소</button>
        <button class="btn-confirm btn-danger" @click="$emit('confirm')">지금 종료</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import AppIcon from '../icons/AppIcon.vue';

defineProps({
  modelValue: { type: Boolean, required: true },
  booking:    { type: Object,  default: null },
});

defineEmits(['update:modelValue', 'confirm']);
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0; z-index: 50;
  display: flex; align-items: center; justify-content: center; padding: 24px;
  background: rgba(15,23,42,0.42);
  backdrop-filter: blur(10px);
}
.modal-box {
  width: 100%; max-width: 560px; overflow: hidden;
  border-radius: 34px;
  background: rgba(255,255,255,0.96);
  box-shadow: 0 30px 90px rgba(15,23,42,0.24);
}
.modal-head {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding: 30px 34px 22px;
  border-bottom: 1px solid #edf2f7;
}
.modal-title { font-size: 28px; font-weight: 900; color: #0f172a; letter-spacing: -0.04em; }
.modal-close {
  width: 46px; height: 46px; border: none;
  border-radius: 16px; background: #f8fafc; color: #64748b;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: background 0.16s ease, transform 0.16s ease;
}
.modal-close:hover { background: #e2e8f0; transform: translateY(-1px); }
.modal-body { padding: 18px 34px 6px; }
.modal-copy { margin: 0; font-size: 16px; line-height: 1.6; color: #475569; }
.modal-copy strong { color: #0f172a; }
.modal-btns { display: flex; gap: 12px; margin-top: 4px; }
.modal-btns-padded { padding: 18px 34px 32px; }
.btn-cancel, .btn-confirm {
  min-height: 60px; border: none; border-radius: 20px;
  font-size: 18px; font-weight: 900; cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
}
.btn-cancel { flex: 1; background: #edf2f7; color: #475569; }
.btn-cancel:hover { background: #e2e8f0; transform: translateY(-1px); }
.btn-confirm { flex: 1.6; background: linear-gradient(180deg, #10b981 0%, #059669 100%); color: #ffffff; box-shadow: 0 14px 30px rgba(16,185,129,0.22); }
.btn-confirm:hover { transform: translateY(-1px); box-shadow: 0 18px 34px rgba(16,185,129,0.26); }
.btn-danger { background: linear-gradient(180deg, #ef4444 0%, #dc2626 100%); box-shadow: 0 14px 30px rgba(239,68,68,0.22); }
.btn-danger:hover { box-shadow: 0 18px 34px rgba(239,68,68,0.26); }
</style>
