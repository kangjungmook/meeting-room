<template>
  <div class="flex flex-col gap-4">

    <!-- ── 지표 카드 4개 ── -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
      <div v-for="card in metricCards" :key="card.label"
        class="bg-white rounded-2xl border border-slate-100 p-5 cursor-pointer hover:border-slate-200 hover:shadow-sm transition-all"
        @click="card.onClick?.()">
        <p class="text-[13px] font-semibold text-slate-400 mb-2">{{ card.label }}</p>
        <div class="flex items-end justify-between">
          <p class="text-[36px] font-black leading-none" :class="card.valueClass">{{ card.value }}</p>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center mb-0.5" :class="card.iconBg">
            <span v-html="card.icon"></span>
          </div>
        </div>
        <p class="text-[12px] text-slate-400 mt-2">{{ card.sub }}</p>
      </div>
    </div>

    <!-- ── 본문 2단 ── -->
    <div class="grid grid-cols-1 lg:grid-cols-5 gap-4">

      <!-- LEFT: 진행중 + 오늘 남은 예약 -->
      <div class="lg:col-span-3 flex flex-col gap-4">

        <!-- 현재 진행중 -->
        <div class="bg-white rounded-2xl border border-slate-100 overflow-hidden">
          <div class="px-5 py-4 flex items-center gap-2 border-b border-slate-50">
            <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            <h3 class="text-[14px] font-bold text-slate-700">현재 진행중</h3>
            <span class="ml-auto text-[12px] font-bold px-2 py-0.5 rounded-full"
              :class="activeBookings.length ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-50 text-slate-400'">
              {{ activeBookings.length }}건
            </span>
          </div>
          <div v-if="!activeBookings.length" class="py-10 text-center text-[13px] text-slate-300 font-semibold">
            진행중인 회의가 없습니다
          </div>
          <div v-else class="divide-y divide-slate-50">
            <div v-for="b in activeBookings" :key="b.id" class="px-5 py-3.5 flex items-center gap-3">
              <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{ background: getRoomColor(b.roomId) }"></span>
              <div class="flex-1 min-w-0">
                <p class="text-[14px] font-bold text-slate-800 truncate">{{ b.title }}</p>
                <p class="text-[12px] text-slate-400 mt-0.5">{{ getRoomName(b.roomId) }} · {{ b.organizer }}</p>
              </div>
              <div class="text-right flex-shrink-0">
                <p class="text-[13px] font-black text-emerald-500">{{ Math.ceil(Math.max(0, dayjs(b.endTime).diff(adminLiveNow, 'second')) / 60) }}분 남음</p>
                <p class="text-[11px] text-slate-400">~{{ dayjs(b.endTime).format('HH:mm') }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 오늘 남은 예약 -->
        <div class="bg-white rounded-2xl border border-slate-100 overflow-hidden">
          <div class="px-5 py-4 flex items-center gap-2 border-b border-slate-50">
            <h3 class="text-[14px] font-bold text-slate-700">오늘 남은 예약</h3>
            <span class="ml-auto text-[12px] font-semibold text-slate-400">{{ upcomingToday.length }}건</span>
          </div>
          <div v-if="!upcomingToday.length" class="py-10 text-center text-[13px] text-slate-300 font-semibold">
            남은 예약이 없습니다
          </div>
          <div v-else class="divide-y divide-slate-50">
            <div v-for="b in upcomingToday" :key="b.id" class="px-5 py-3 flex items-center gap-3">
              <div class="text-center flex-shrink-0 w-12">
                <p class="text-[13px] font-black text-slate-700 tabular-nums">{{ dayjs(b.startTime).format('HH:mm') }}</p>
                <p class="text-[11px] text-slate-400 tabular-nums">{{ dayjs(b.endTime).format('HH:mm') }}</p>
              </div>
              <span class="w-px h-8 bg-slate-100 flex-shrink-0"></span>
              <div class="flex-1 min-w-0">
                <p class="text-[14px] font-semibold text-slate-700 truncate">{{ b.title }}</p>
                <p class="text-[12px] text-slate-400 mt-0.5">{{ getRoomName(b.roomId) }} · {{ b.organizer }}</p>
              </div>
              <span class="text-[11px] font-semibold px-2 py-1 rounded-lg bg-slate-50 text-slate-500 flex-shrink-0">
                {{ dayjs(b.startTime).diff(dayjs(), 'minute') }}분 후
              </span>
            </div>
          </div>
        </div>

      </div>

      <!-- RIGHT: 최근 활동 -->
      <div class="lg:col-span-2 bg-white rounded-2xl border border-slate-100 overflow-hidden flex flex-col">
        <div class="px-5 py-4 flex items-center justify-between border-b border-slate-50 flex-shrink-0">
          <h3 class="text-[14px] font-bold text-slate-700">최근 활동</h3>
          <button @click="activeTab = 'logs'" class="text-[12px] font-semibold text-indigo-400 hover:text-indigo-600 transition-colors">
            전체 보기
          </button>
        </div>
        <div v-if="!recentActivity.length" class="flex-1 flex items-center justify-center text-[13px] text-slate-300 font-semibold">
          활동 내역이 없습니다
        </div>
        <div v-else class="divide-y divide-slate-50 overflow-y-auto">
          <div v-for="log in recentActivity" :key="log.id" class="px-5 py-3.5 flex items-start gap-3">
            <span class="w-1.5 h-1.5 rounded-full flex-shrink-0 mt-1.5" :style="{ background: logBadgeStyle(log.actionType).dot }"></span>
            <div class="flex-1 min-w-0">
              <p class="text-[13px] font-semibold text-slate-700 truncate">{{ log.description }}</p>
              <p class="text-[11px] text-slate-400 mt-0.5">{{ actionLabel[log.actionType] || log.actionType }}</p>
            </div>
            <span class="text-[11px] text-slate-400 flex-shrink-0 mt-0.5 tabular-nums">
              {{ dayjs(log.createdAt).fromNow() }}
            </span>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useAdmin } from '../../composables/useAdmin';

const {
  dashboard, activeBookings, todayBookings, recentActivity,
  pendingCount, activeTab, adminLiveNow,
  getRoomName, getRoomColor, logBadgeStyle, actionLabel, dayjs,
} = useAdmin();

const upcomingToday = computed(() =>
  todayBookings.value
    .filter(b => dayjs(b.startTime).isAfter(adminLiveNow.value))
    .sort((a, b) => dayjs(a.startTime).diff(dayjs(b.startTime)))
);

const metricCards = computed(() => [
  {
    label: '오늘 예약',
    value: dashboard.value.todayBookings ?? 0,
    sub: `전체 ${dashboard.value.totalBookings ?? 0}건`,
    valueClass: 'text-indigo-600',
    iconBg: 'bg-indigo-50',
    icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="1" y="3" width="16" height="13" rx="2" stroke="#6366f1" stroke-width="1.6"/><path d="M1 7h16M6 1v3M12 1v3" stroke="#6366f1" stroke-width="1.6" stroke-linecap="round"/></svg>',
    onClick: () => { activeTab.value = 'bookings'; },
  },
  {
    label: '현재 사용중',
    value: activeBookings.value.length,
    sub: `전체 ${dashboard.value.totalRooms ?? 0}개 회의실`,
    valueClass: activeBookings.value.length ? 'text-emerald-600' : 'text-slate-700',
    iconBg: 'bg-emerald-50',
    icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="1" y="4" width="16" height="12" rx="2" stroke="#10b981" stroke-width="1.6"/><path d="M5 4V2M13 4V2" stroke="#10b981" stroke-width="1.6" stroke-linecap="round"/></svg>',
  },
  {
    label: '승인 대기',
    value: pendingCount.value,
    sub: '가입 승인 필요',
    valueClass: pendingCount.value ? 'text-amber-500' : 'text-slate-700',
    iconBg: pendingCount.value ? 'bg-amber-50' : 'bg-slate-50',
    icon: pendingCount.value
      ? '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="4" stroke="#f59e0b" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#f59e0b" stroke-width="1.6" stroke-linecap="round"/></svg>'
      : '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="4" stroke="#94a3b8" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#94a3b8" stroke-width="1.6" stroke-linecap="round"/></svg>',
    onClick: () => { activeTab.value = 'users-pending'; },
  },
  {
    label: '전체 회원',
    value: dashboard.value.totalUsers ?? 0,
    sub: `알림 ${dashboard.value.fcmUsers ?? 0}명 활성`,
    valueClass: 'text-slate-700',
    iconBg: 'bg-violet-50',
    icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="7" cy="5" r="3" stroke="#7c3aed" stroke-width="1.6"/><path d="M1 16c0-3.3 2.7-6 6-6" stroke="#7c3aed" stroke-width="1.6" stroke-linecap="round"/><circle cx="13" cy="7" r="2.5" stroke="#7c3aed" stroke-width="1.4"/><path d="M10 16c0-2.2 1.3-4 3-4s3 1.8 3 4" stroke="#7c3aed" stroke-width="1.4" stroke-linecap="round"/></svg>',
    onClick: () => { activeTab.value = 'users-all'; },
  },
]);
</script>
