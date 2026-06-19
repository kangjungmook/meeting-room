<template>
  <aside class="schedule-panel">
    <div class="schedule-header">
      <div>
        <div class="schedule-title">오늘의 일정</div>
        <div class="schedule-subtitle">{{ room?.name ?? '회의실' }}의 예약 현황</div>
      </div>
      <div class="schedule-count">{{ todayBookings.length }}건</div>
    </div>

    <div class="schedule-list">
      <div v-if="todayBookings.length === 0" class="schedule-empty">
        <AppIcon name="calendar" :size="32" style="margin: 0 auto 10px; display: block; opacity: 0.25; color: #64748b" />
        오늘 등록된 일정이 없습니다
      </div>

      <div v-for="b in todayBookings" :key="b.id"
           class="schedule-item"
           :class="{ 'sitem-done': getState(b) === 'done', 'sitem-ongoing': getState(b) === 'ongoing' }">
        <div class="sitem-bar"
             :class="{ 'sbar-ongoing': getState(b) === 'ongoing', 'sbar-upcoming': getState(b) === 'upcoming', 'sbar-done': getState(b) === 'done' }">
        </div>
        <div class="sitem-time">
          <div class="sitem-start">{{ dayjs(b.startTime).format('HH:mm') }}</div>
          <div class="sitem-end">{{ dayjs(b.endTime).format('HH:mm') }}</div>
        </div>
        <div class="sitem-info">
          <div class="sitem-title">{{ b.title }}</div>
          <div class="sitem-org">{{ b.organizer }}</div>
        </div>
        <div class="sitem-badge"
             :class="{ 'sbadge-ongoing': getState(b) === 'ongoing', 'sbadge-done': getState(b) === 'done', 'sbadge-upcoming': getState(b) === 'upcoming' }">
          {{ getState(b) === 'ongoing' ? '진행 중' : getState(b) === 'done' ? '종료' : '예정' }}
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import dayjs from 'dayjs';
import AppIcon from '../icons/AppIcon.vue';

const props = defineProps({
  todayBookings: { type: Array,  required: true },
  room:          { type: Object, default: null },
  now:           { type: Object, required: true }, // dayjs instance
});

const getState = (booking) => {
  if (props.now.isAfter(dayjs(booking.endTime)))   return 'done';
  if (props.now.isAfter(dayjs(booking.startTime))) return 'ongoing';
  return 'upcoming';
};
</script>

<style scoped>
.schedule-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 24px;
  border-radius: 34px;
  background: rgba(255,255,255,0.84);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(203,213,225,0.58);
  box-shadow: 0 28px 72px rgba(15,23,42,0.09);
  overflow: hidden;
}
.schedule-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(226,232,240,0.86);
}
.schedule-title {
  font-size: 24px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.04em;
}
.schedule-subtitle {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 700;
  color: #64748b;
}
.schedule-count {
  flex-shrink: 0;
  min-width: 58px;
  padding: 10px 14px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 14px;
  font-weight: 900;
  text-align: center;
}
.schedule-list {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}
.schedule-list::-webkit-scrollbar { width: 6px; }
.schedule-list::-webkit-scrollbar-track { background: transparent; }
.schedule-list::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 999px; }
.schedule-empty {
  margin: auto 0;
  padding: 56px 18px;
  text-align: center;
  color: #94a3b8;
  font-size: 15px;
  font-weight: 700;
}
.schedule-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 18px 18px 16px;
  border-radius: 24px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 24px rgba(148,163,184,0.1);
  overflow: hidden;
  transition: transform 0.16s ease, box-shadow 0.16s ease, opacity 0.16s ease;
}
.schedule-item:hover { transform: translateY(-1px); box-shadow: 0 14px 28px rgba(148,163,184,0.14); }
.sitem-done { opacity: 0.52; }
.sitem-ongoing { background: linear-gradient(180deg, #fff7f7 0%, #fff1f2 100%); border-color: #fecdd3; }
.sitem-bar { position: absolute; left: 0; top: 0; bottom: 0; width: 5px; }
.sbar-ongoing  { background: linear-gradient(180deg, #dc2626 0%, #fb7185 100%); }
.sbar-upcoming { background: linear-gradient(180deg, #0f766e 0%, #14b8a6 100%); }
.sbar-done     { background: #cbd5e1; }
.sitem-time { flex-shrink: 0; width: 72px; padding-left: 10px; }
.sitem-start {
  font-size: 20px;
  font-weight: 900;
  line-height: 1;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.04em;
}
.sitem-end { margin-top: 6px; font-size: 12px; font-weight: 700; color: #64748b; font-variant-numeric: tabular-nums; }
.sitem-info { flex: 1; min-width: 0; }
.sitem-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sitem-org {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sitem-badge {
  flex-shrink: 0;
  padding: 9px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.04em;
}
.sbadge-ongoing  { background: #fee2e2; color: #dc2626; }
.sbadge-done     { background: #e2e8f0; color: #64748b; }
.sbadge-upcoming { background: #dcfce7; color: #047857; }
</style>
