<template>
  <section class="hero-panel" :class="isInUse ? 'hero-red' : 'hero-green'">
    <div class="hero-topbar">
      <div class="hero-chip" :class="isInUse ? 'hero-chip-busy' : 'hero-chip-free'">
        <span class="hero-chip-dot"></span>
        <span>{{ isInUse ? '사용 중' : '사용 가능' }}</span>
      </div>
    </div>

    <div class="hero-content">
      <template v-if="!isInUse">
        <div class="hero-icon-wrap">
          <svg width="48" height="48" viewBox="0 0 56 56" fill="none">
            <path d="M12 28l12 12 20-20" stroke="white" stroke-width="5" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <div class="hero-status-text">사용 가능</div>
        <div class="hero-sub" v-if="nextBooking">
          다음 예약 <strong>{{ dayjs(nextBooking.startTime).format('HH:mm') }}</strong> · {{ nextBooking.title }}
          <span class="hero-sub-muted">({{ timeUntilNext }})</span>
        </div>
        <div class="hero-sub" v-else>오늘 남은 예약이 없습니다</div>
        <button @click="$emit('open-booking')" class="hero-book-btn">
          <AppIcon name="plus" :size="18" />
          지금 바로 예약
        </button>
      </template>

      <template v-else>
        <div class="hero-icon-wrap">
          <svg width="42" height="42" viewBox="0 0 56 56" fill="none">
            <rect x="10" y="10" width="13" height="36" rx="4" fill="white" />
            <rect x="33" y="10" width="13" height="36" rx="4" fill="white" />
          </svg>
        </div>
        <div class="hero-status-text">사용 중</div>
        <div class="hero-meeting-title">{{ currentBooking?.title }}</div>
        <div class="hero-organizer">{{ currentBooking?.organizer }}</div>
        <div class="hero-countdown-wrap">
          <div class="hero-countdown-label">종료까지</div>
          <div class="hero-countdown">{{ timeUntilEnd }}</div>
          <div class="hero-progress-track">
            <div class="hero-progress-bar" :style="{ width: progressPct + '%' }"></div>
          </div>
          <div class="hero-progress-times">
            <span>{{ dayjs(currentBooking?.startTime).format('HH:mm') }}</span>
            <span>{{ dayjs(currentBooking?.endTime).format('HH:mm') }}</span>
          </div>
        </div>
        <button @click="$emit('show-end-confirm')" class="hero-end-btn">
          <AppIcon name="stop" :size="13" />
          지금 종료
        </button>
      </template>
    </div>

    <div class="hero-footer">
      <div class="hero-footer-card">
        <span class="hero-footer-label">{{ isInUse ? '시작 시간' : '다음 시작' }}</span>
        <strong>
          {{
            isInUse
              ? dayjs(currentBooking?.startTime).format('HH:mm')
              : nextBooking
                ? dayjs(nextBooking.startTime).format('HH:mm')
                : '--:--'
          }}
        </strong>
      </div>
    </div>
  </section>
</template>

<script setup>
import dayjs from 'dayjs';
import AppIcon from '../icons/AppIcon.vue';

defineProps({
  isInUse:       { type: Boolean, required: true },
  currentBooking: { type: Object,  default: null },
  nextBooking:    { type: Object,  default: null },
  timeUntilEnd:  { type: String,  required: true },
  timeUntilNext: { type: String,  default: '' },
  progressPct:   { type: Number,  required: true },
});

defineEmits(['open-booking', 'show-end-confirm']);
</script>

<style scoped>
.hero-panel {
  position: relative;
  overflow: hidden;
  border-radius: 34px;
  padding: 24px 28px 26px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 32px 80px rgba(15, 23, 42, 0.16);
}
.hero-green {
  background:
    radial-gradient(circle at 18% 22%, rgba(255,255,255,0.14), transparent 0 16%),
    radial-gradient(circle at 82% 84%, rgba(255,255,255,0.12), transparent 0 22%),
    linear-gradient(145deg, #0f766e 0%, #10b981 48%, #5eead4 100%);
}
.hero-red {
  background:
    radial-gradient(circle at 18% 22%, rgba(255,255,255,0.14), transparent 0 16%),
    radial-gradient(circle at 82% 84%, rgba(255,255,255,0.12), transparent 0 22%),
    linear-gradient(145deg, #991b1b 0%, #dc2626 48%, #fb7185 100%);
}
.hero-panel::before, .hero-panel::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(2px);
}
.hero-panel::before {
  width: 420px; height: 420px;
  right: -160px; bottom: -200px;
  background: rgba(255,255,255,0.09);
}
.hero-panel::after {
  width: 240px; height: 240px;
  top: -80px; left: -60px;
  background: rgba(255,255,255,0.08);
}
.hero-topbar, .hero-content, .hero-footer { position: relative; z-index: 1; }
.hero-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.hero-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 48px;
  padding: 0 18px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: rgba(255,255,255,0.16);
  border: 1px solid rgba(255,255,255,0.22);
  color: rgba(255,255,255,0.92);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.08);
}
.hero-chip-dot {
  width: 10px; height: 10px;
  border-radius: 999px;
  background: currentColor;
  box-shadow: 0 0 14px currentColor;
}
.hero-chip-free { color: #eafff6; }
.hero-chip-busy { color: #fff1f2; }
.hero-content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 18px;
  padding: 20px 0 14px;
}
.hero-icon-wrap {
  width: clamp(84px, 11vw, 112px);
  height: clamp(84px, 11vw, 112px);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  background: rgba(255,255,255,0.16);
  border: 1px solid rgba(255,255,255,0.16);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.1);
}
.hero-status-text {
  font-size: clamp(58px, 8.6vw, 108px);
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -0.06em;
  line-height: 0.95;
}
.hero-sub {
  max-width: 640px;
  font-size: clamp(18px, 2vw, 24px);
  font-weight: 700;
  color: rgba(255,255,255,0.82);
  line-height: 1.5;
}
.hero-sub strong { color: #ffffff; font-weight: 900; }
.hero-sub-muted { color: rgba(255,255,255,0.64); }
.hero-book-btn, .hero-end-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-width: 248px;
  min-height: 72px;
  padding: 0 34px;
  border-radius: 24px;
  font-size: clamp(18px, 2vw, 24px);
  font-weight: 900;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
}
.hero-book-btn {
  color: #0f766e;
  background: linear-gradient(180deg, #ffffff 0%, #ecfdf5 100%);
  border: none;
  box-shadow: 0 20px 36px rgba(8,15,23,0.14);
}
.hero-book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 42px rgba(8,15,23,0.18);
}
.hero-end-btn {
  color: #ffffff;
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.34);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.1);
}
.hero-end-btn:hover {
  transform: translateY(-2px);
  background: rgba(255,255,255,0.18);
}
.hero-meeting-title {
  max-width: 760px;
  font-size: clamp(28px, 4.4vw, 56px);
  line-height: 1.08;
  letter-spacing: -0.04em;
  font-weight: 900;
  color: #ffffff;
}
.hero-organizer {
  margin-top: -6px;
  font-size: clamp(17px, 1.9vw, 24px);
  font-weight: 700;
  color: rgba(255,255,255,0.72);
}
.hero-countdown-wrap {
  width: min(100%, 460px);
  padding: 22px 26px 20px;
  border-radius: 28px;
  background: rgba(12,18,28,0.14);
  border: 1px solid rgba(255,255,255,0.14);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.08);
}
.hero-countdown-label {
  font-size: 12px;
  font-weight: 800;
  color: rgba(255,255,255,0.64);
  letter-spacing: 0.18em;
  text-transform: uppercase;
}
.hero-countdown {
  margin: 8px 0 14px;
  font-size: clamp(56px, 7.8vw, 92px);
  line-height: 0.95;
  font-weight: 900;
  color: #ffffff;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.05em;
}
.hero-progress-track {
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255,255,255,0.18);
}
.hero-progress-bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255,255,255,0.92), rgba(255,255,255,0.64));
  transition: width 1s linear;
}
.hero-progress-times {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  font-weight: 800;
  color: rgba(255,255,255,0.7);
  font-variant-numeric: tabular-nums;
}
.hero-footer {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.hero-footer-card {
  padding: 16px 18px 18px;
  border-radius: 22px;
  background: rgba(12,18,28,0.14);
  border: 1px solid rgba(255,255,255,0.14);
  color: #ffffff;
  flex: 0 0 auto;
  min-width: 140px;
  max-width: 180px;
}
.hero-footer-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  opacity: 0.7;
}
.hero-footer-card strong {
  display: block;
  margin-top: 8px;
  font-size: clamp(24px, 2.4vw, 34px);
  line-height: 1.1;
  font-weight: 900;
  letter-spacing: -0.04em;
}
</style>
