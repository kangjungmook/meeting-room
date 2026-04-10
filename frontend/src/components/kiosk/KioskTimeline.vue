<template>
  <section class="timeline-section">
    <div class="timeline-card">
      <div class="timeline-head">
        <div class="timeline-title">Daily Flow</div>
        <div class="timeline-now">NOW</div>
      </div>
      <div class="timeline-labels">
        <span v-for="h in [6, 9, 12, 15, 18, 21]" :key="h">{{ String(h).padStart(2, '0') }}</span>
      </div>
      <div class="timeline-track">
        <div v-for="(slot, i) in timelineSlots" :key="i"
             class="timeline-block"
             :class="{ 'tblock-ongoing': slot.state === 'ongoing', 'tblock-done': slot.state === 'done', 'tblock-upcoming': slot.state === 'upcoming' }"
             :style="{ left: slot.left, width: slot.width }">
        </div>
        <div class="now-line" :style="{ left: nowLineLeft }"></div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  timelineSlots: { type: Array,  required: true },
  nowLineLeft:   { type: String, required: true },
});
</script>

<style scoped>
.timeline-section { flex-shrink: 0; padding: 0 18px 18px; }
.timeline-card {
  padding: 16px 20px 20px;
  border-radius: 28px;
  background: rgba(255,255,255,0.82);
  border: 1px solid rgba(203,213,225,0.56);
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 44px rgba(15,23,42,0.08);
}
.timeline-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.timeline-title {
  font-size: 14px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}
.timeline-now {
  padding: 7px 12px;
  border-radius: 999px;
  background: #0f172a;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.1em;
}
.timeline-labels {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 12px;
  font-weight: 800;
  color: #64748b;
  letter-spacing: 0.04em;
}
.timeline-track {
  position: relative;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, #e2e8f0 0%, #cbd5e1 100%);
  overflow: visible;
}
.timeline-block { position: absolute; top: 0; height: 100%; border-radius: 999px; }
.tblock-ongoing  { background: linear-gradient(90deg, #ef4444 0%, #fb7185 100%); }
.tblock-done     { background: #94a3b8; }
.tblock-upcoming { background: linear-gradient(90deg, #10b981 0%, #2dd4bf 100%); }
.now-line {
  position: absolute;
  top: -6px; bottom: -6px;
  width: 3px;
  border-radius: 999px;
  background: #0f172a;
  box-shadow: 0 0 10px rgba(15,23,42,0.3);
}
</style>
