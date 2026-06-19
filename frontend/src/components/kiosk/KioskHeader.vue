<template>
  <header class="kiosk-header">
    <div class="header-room">
      <button @click="$emit('exit')" class="back-btn" aria-label="뒤로가기">
        <AppIcon name="chevron-left" :size="15" />
      </button>
      <div>
        <div class="room-name">{{ room?.name ?? '회의실' }}</div>
        <div class="room-cap">수용 {{ room?.capacity ?? '-' }}명</div>
      </div>
    </div>

    <div class="clock-block">
      <div class="clock-time">{{ clockTime }}</div>
      <div class="clock-date">{{ clockDate }}</div>
    </div>
  </header>
</template>

<script setup>
import AppIcon from '../icons/AppIcon.vue';

defineProps({
  room:      { type: Object, default: null },
  clockTime: { type: String, required: true },
  clockDate: { type: String, required: true },
});

defineEmits(['exit']);
</script>

<style scoped>
.kiosk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px 18px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}
.header-room {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}
.back-btn {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(203, 213, 225, 0.8);
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.16s ease, background 0.16s ease, border-color 0.16s ease;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  flex-shrink: 0;
}
.back-btn:hover {
  background: #ffffff;
  border-color: rgba(148, 163, 184, 0.52);
  transform: translateY(-1px);
}
.room-name {
  font-size: clamp(24px, 2.5vw, 34px);
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.03em;
  line-height: 1.05;
}
.room-cap {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 700;
  color: #64748b;
}
.clock-block {
  text-align: right;
  min-width: 280px;
}
.clock-time {
  font-size: clamp(28px, 3.6vw, 44px);
  font-weight: 900;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.04em;
  line-height: 1;
}
.clock-date {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 700;
  color: #64748b;
}
</style>
