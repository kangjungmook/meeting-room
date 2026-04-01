<template>
  <div>
    <!-- 통계 카드 -->
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3 mb-4">
      <div v-for="stat in stats" :key="stat.label"
        :class="[
          'bg-white rounded-2xl border p-4 transition-all relative overflow-hidden group cursor-pointer',
          stat.label === '승인 대기' && pendingCount > 0
            ? 'border-amber-300 ring-1 ring-amber-200 hover:shadow-md hover:-translate-y-0.5'
            : 'border-slate-200 hover:border-indigo-200 hover:shadow-sm hover:-translate-y-0.5'
        ]"
        @click="onStatClick(stat)">
        <div class="absolute top-0 left-0 right-0 h-0.5 rounded-t-2xl" :class="stat.bgColor"></div>
        <div class="flex items-start justify-between mb-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0" :class="stat.bgColor">
            <span v-html="stat.icon"></span>
          </div>
          <svg v-if="stat.label === '승인 대기' && pendingCount > 0" width="14" height="14" viewBox="0 0 14 14" fill="none" class="text-amber-400 mt-1">
            <path d="M3 5l4 4 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <p :class="['text-[28px] font-black leading-none mb-1', stat.label === '승인 대기' && pendingCount > 0 ? 'text-amber-500' : 'text-slate-800']">{{ stat.value }}</p>
        <p class="text-[12px] font-semibold text-slate-500">{{ stat.label }}</p>
        <p class="text-[11px] text-slate-400 mt-0.5">{{ stat.sub }}</p>
      </div>
    </div>

    <!-- 회의실별 오늘 예약 현황 -->
    <div v-if="dashboard.roomStats?.length" class="bg-white rounded-2xl border border-slate-200 p-5 mb-4">
      <div class="flex items-center gap-2 mb-4">
        <h2 class="text-[14px] font-bold text-slate-800">회의실별 오늘 예약</h2>
        <span class="text-[11px] font-semibold text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full">{{ dayjs().format('MM월 DD일') }}</span>
      </div>
      <div class="flex flex-col gap-3">
        <div v-for="rs in dashboard.roomStats" :key="rs.roomId" class="flex items-center gap-3">
          <div class="flex items-center gap-2 w-28 flex-shrink-0">
            <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{background: getRoomColor(rs.roomId)}"></span>
            <span class="text-[13px] font-semibold text-slate-600 truncate">{{ rs.roomName }}</span>
          </div>
          <div class="flex-1 bg-slate-100 rounded-full h-2">
            <div class="h-2 rounded-full bg-indigo-400 transition-all"
              :style="{width: maxRoomCount > 0 ? (rs.todayCount / maxRoomCount * 100) + '%' : '0%'}"></div>
          </div>
          <span class="text-[13px] font-bold text-slate-700 w-8 text-right">{{ rs.todayCount }}건</span>
        </div>
      </div>
    </div>

    <!-- 오늘 예약 목록 -->
    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden mb-4">
      <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
        <h2 class="text-[15px] font-bold text-slate-700">오늘 예약 현황</h2>
        <span class="text-[12px] text-slate-600">{{ dayjs().format('YYYY년 MM월 DD일') }}</span>
      </div>
      <div v-if="todayBookings.length === 0" class="py-16 text-center text-slate-500 text-[15px] font-semibold">
        오늘 예약이 없습니다
      </div>
      <div v-else-if="!isMobile" class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-100">
              <th class="text-left px-6 py-3 text-[12px] font-bold text-slate-700 uppercase tracking-wider">회의실</th>
              <th class="text-left px-6 py-3 text-[12px] font-bold text-slate-700 uppercase tracking-wider">제목</th>
              <th class="text-left px-6 py-3 text-[12px] font-bold text-slate-700 uppercase tracking-wider">예약자</th>
              <th class="text-left px-6 py-3 text-[12px] font-bold text-slate-700 uppercase tracking-wider">시간</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in todayBookings" :key="b.id" class="border-b border-slate-50 hover:bg-slate-50/60 transition-colors">
              <td class="px-6 py-3.5">
                <div class="flex items-center gap-2">
                  <span class="w-2 h-2 rounded-full flex-shrink-0" :style="{background: getRoomColor(b.roomId)}"></span>
                  <span class="text-[14px] font-semibold text-slate-700">{{ getRoomName(b.roomId) }}</span>
                </div>
              </td>
              <td class="px-6 py-3.5 text-[14px] text-slate-700 font-medium">{{ b.title }}</td>
              <td class="px-6 py-3.5 text-[14px] text-slate-700">{{ b.organizer }}</td>
              <td class="px-6 py-3.5 text-[13px] text-slate-600 font-medium">{{ dayjs(b.startTime).format('HH:mm') }} – {{ dayjs(b.endTime).format('HH:mm') }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="flex flex-col divide-y divide-slate-50">
        <div v-for="b in todayBookings" :key="b.id" class="px-4 py-3 flex items-center gap-3">
          <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{background: getRoomColor(b.roomId)}"></span>
          <div class="flex-1 min-w-0">
            <p class="text-[14px] font-semibold text-slate-800 truncate">{{ b.title }}</p>
            <p class="text-[12px] text-slate-600">{{ getRoomName(b.roomId) }} · {{ b.organizer }}</p>
          </div>
          <span class="text-[12px] font-semibold text-slate-700 flex-shrink-0">{{ dayjs(b.startTime).format('HH:mm') }}–{{ dayjs(b.endTime).format('HH:mm') }}</span>
        </div>
      </div>
    </div>

    <!-- 참여 통계 TOP5 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-4">
      <!-- 예약자 TOP5 -->
      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div class="px-5 py-4 border-b border-slate-100 flex items-center gap-2">
          <span class="w-8 h-8 rounded-xl bg-indigo-50 flex items-center justify-center flex-shrink-0">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="1" y="4" width="14" height="10" rx="2" stroke="#6366f1" stroke-width="1.5"/><path d="M4 4V2M12 4V2" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/></svg>
          </span>
          <h3 class="text-[14px] font-bold text-slate-800">예약 많이 한 사람</h3>
          <span class="ml-auto text-[11px] font-semibold text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full">TOP 5</span>
        </div>
        <div v-if="!participationStats.topBookers?.length" class="px-5 py-10 flex flex-col items-center gap-2">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none" class="text-slate-200"><rect x="2" y="6" width="28" height="20" rx="3" stroke="currentColor" stroke-width="2"/><path d="M2 12h28" stroke="currentColor" stroke-width="2"/></svg>
          <p class="text-[12px] text-slate-400 font-semibold">데이터 없음</p>
        </div>
        <div v-else class="divide-y divide-slate-50">
          <div v-for="(item, i) in participationStats.topBookers" :key="item.userId"
            class="px-5 py-3 flex items-center gap-3">
            <span :class="['w-6 h-6 rounded-full flex items-center justify-center text-[12px] font-black flex-shrink-0',
              i === 0 ? 'bg-yellow-100 text-yellow-600' : i === 1 ? 'bg-slate-100 text-slate-500' : i === 2 ? 'bg-orange-100 text-orange-500' : 'bg-slate-50 text-slate-400']">
              {{ i + 1 }}
            </span>
            <span class="flex-1 text-[14px] font-semibold text-slate-700 truncate">{{ item.name }}</span>
            <div class="flex items-center gap-2">
              <div class="w-20 bg-slate-100 rounded-full h-1.5">
                <div class="h-1.5 rounded-full bg-indigo-400 transition-all"
                  :style="{width: participationStats.topBookers[0]?.count > 0 ? (item.count / participationStats.topBookers[0].count * 100) + '%' : '0%'}"></div>
              </div>
              <span class="text-[13px] font-bold text-indigo-600 w-8 text-right">{{ item.count }}건</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 참석자 TOP5 -->
      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div class="px-5 py-4 border-b border-slate-100 flex items-center gap-2">
          <span class="w-8 h-8 rounded-xl bg-emerald-50 flex items-center justify-center flex-shrink-0">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="6" cy="5" r="3" stroke="#10b981" stroke-width="1.5"/><path d="M1 14c0-3 2.2-5 5-5" stroke="#10b981" stroke-width="1.5" stroke-linecap="round"/><circle cx="12" cy="6" r="2.5" stroke="#10b981" stroke-width="1.4"/><path d="M9.5 14c0-2 1.1-3.5 2.5-3.5s2.5 1.5 2.5 3.5" stroke="#10b981" stroke-width="1.4" stroke-linecap="round"/></svg>
          </span>
          <h3 class="text-[14px] font-bold text-slate-800">참석 많이 한 사람</h3>
          <span class="ml-auto text-[11px] font-semibold text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full">TOP 5</span>
        </div>
        <div v-if="!participationStats.topAttendees?.length" class="px-5 py-10 flex flex-col items-center gap-2">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none" class="text-slate-200"><circle cx="12" cy="10" r="6" stroke="currentColor" stroke-width="2"/><path d="M2 28c0-5.5 4.5-10 10-10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          <p class="text-[12px] text-slate-400 font-semibold">데이터 없음</p>
        </div>
        <div v-else class="divide-y divide-slate-50">
          <div v-for="(item, i) in participationStats.topAttendees" :key="item.userId"
            class="px-5 py-3 flex items-center gap-3">
            <span :class="['w-6 h-6 rounded-full flex items-center justify-center text-[12px] font-black flex-shrink-0',
              i === 0 ? 'bg-yellow-100 text-yellow-600' : i === 1 ? 'bg-slate-100 text-slate-500' : i === 2 ? 'bg-orange-100 text-orange-500' : 'bg-slate-50 text-slate-400']">
              {{ i + 1 }}
            </span>
            <span class="flex-1 text-[14px] font-semibold text-slate-700 truncate">{{ item.name }}</span>
            <div class="flex items-center gap-2">
              <div class="w-20 bg-slate-100 rounded-full h-1.5">
                <div class="h-1.5 rounded-full bg-emerald-400 transition-all"
                  :style="{width: participationStats.topAttendees[0]?.count > 0 ? (item.count / participationStats.topAttendees[0].count * 100) + '%' : '0%'}"></div>
              </div>
              <span class="text-[13px] font-bold text-emerald-600 w-8 text-right">{{ item.count }}건</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 진행중 + 최근 활동 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 지금 진행중인 예약 -->
      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div class="px-5 py-4 border-b border-slate-100 flex items-center gap-2">
          <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
          <h3 class="text-[14px] font-bold text-slate-800">지금 진행중</h3>
          <span class="ml-auto text-[12px] font-bold text-emerald-500 bg-emerald-50 px-2 py-0.5 rounded-full">{{ activeBookings.length }}건</span>
        </div>
        <div v-if="activeBookings.length === 0" class="px-5 py-10 flex flex-col items-center gap-2">
          <svg width="36" height="36" viewBox="0 0 36 36" fill="none" class="text-slate-200"><rect x="3" y="8" width="30" height="22" rx="4" stroke="currentColor" stroke-width="2"/><path d="M3 14h30M12 3v5M24 3v5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          <p class="text-[13px] text-slate-400 font-semibold">진행중인 예약이 없습니다</p>
        </div>
        <div v-else class="divide-y divide-slate-50">
          <div v-for="b in activeBookings" :key="b.id" class="px-5 py-3 flex items-center gap-3">
            <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{background: getRoomColor(b.roomId)}"></span>
            <div class="flex-1 min-w-0">
              <p class="text-[14px] font-bold text-slate-800 truncate">{{ b.title }}</p>
              <p class="text-[12px] text-slate-500">{{ getRoomName(b.roomId) }} · {{ b.organizer }}</p>
            </div>
            <div class="text-right flex-shrink-0">
              <p class="text-[11px] font-bold text-emerald-500">종료까지</p>
              <p class="text-[13px] font-bold text-slate-700">{{ Math.max(0, dayjs(b.endTime).diff(dayjs(), 'minute')) }}분</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 최근 관리 활동 -->
      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
          <h3 class="text-[14px] font-bold text-slate-800">최근 관리 활동</h3>
          <button @click="activeTab = 'logs'" class="text-[12px] font-semibold text-indigo-400 hover:text-indigo-600 transition-colors">전체 보기</button>
        </div>
        <div v-if="recentActivity.length === 0" class="px-5 py-10 flex flex-col items-center gap-2">
          <svg width="36" height="36" viewBox="0 0 36 36" fill="none" class="text-slate-200"><path d="M6 10h24M6 18h16M6 26h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          <p class="text-[13px] text-slate-400 font-semibold">관리 활동이 없습니다</p>
        </div>
        <div v-else class="divide-y divide-slate-50">
          <div v-for="log in recentActivity" :key="log.id" class="px-5 py-3 flex items-start gap-3">
            <span class="w-2 h-2 rounded-full flex-shrink-0 mt-1.5" :style="{background: logBadgeStyle(log.actionType).dot}"></span>
            <div class="flex-1 min-w-0">
              <p class="text-[13px] font-semibold text-slate-700 truncate">{{ log.description }}</p>
              <p class="text-[11px] text-slate-400 mt-0.5">{{ actionLabel[log.actionType] || log.actionType }}</p>
            </div>
            <span class="text-[11px] text-slate-400 flex-shrink-0 mt-0.5">{{ dayjs(log.createdAt).fromNow() }}</span>
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
  dashboard, todayBookings, activeBookings, recentActivity,
  stats, pendingCount, activeTab, manageUserFilter, participationStats,
  getRoomName, getRoomColor, logBadgeStyle, actionLabel, dayjs,
} = useAdmin();

const props = defineProps({ isMobile: Boolean });

const maxRoomCount = computed(() =>
  Math.max(...(dashboard.value.roomStats?.map(r => r.todayCount) ?? [0]), 0)
);

const onStatClick = (stat) => {
  if (!stat.tab) return;
  if (stat.label === '승인 대기')  manageUserFilter.value = 'PENDING';
  if (stat.label === '전체 회원')  manageUserFilter.value = 'APPROVED';
  activeTab.value = stat.tab;
};
</script>
