<template>
  <!-- 배경 오버레이 (모바일 고정 상태) -->
  <div v-if="activeBooking && isMobile && (tooltip.pinned || detailTarget)"
       class="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-sm"
       @click="closeAll"></div>

  <!-- 단일 툴팁/팝오버 카드 -->
  <div v-if="activeBooking"
       class="fixed z-50 bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden w-72"
       :style="activeStyle"
       @mouseenter="!isMobile && !tooltip.pinned && !detailTarget && (tooltip.show = true)"
       @mouseleave="!isMobile && !tooltip.pinned && !detailTarget && (tooltip.show = false)">

    <!-- 방 색상 라인 -->
    <div class="h-1 w-full flex-shrink-0" :style="{ background: getRoomColor(activeBooking.roomId) }"></div>

    <div class="px-5 pt-4 pb-4 border-b border-slate-100">
      <!-- 방 이름 + 소요시간 배지 -->
      <div class="flex items-center justify-between gap-2 mb-2.5">
        <div class="flex items-center gap-2">
          <span class="w-2 h-2 rounded-full flex-shrink-0" :style="{ background: getRoomColor(activeBooking.roomId) }"></span>
          <span class="text-[12px] font-bold text-slate-400">{{ getRoomName(activeBooking.roomId) }}</span>
        </div>
        <span class="text-[11px] font-bold px-2 py-0.5 rounded-full text-white flex-shrink-0"
              :style="{ background: getRoomColor(activeBooking.roomId) + 'cc' }">
          {{ tooltipDuration(activeBooking) }}
        </span>
      </div>

      <!-- 제목 -->
      <p class="text-[17px] font-black text-slate-800 leading-snug">{{ activeBooking.title }}</p>

      <!-- 시간 -->
      <p class="text-[13px] text-slate-500 mt-1.5 tabular-nums font-semibold">
        {{ dayjs(activeBooking.startTime).format('MM월 DD일') }}
        <span class="text-slate-700">{{ dayjs(activeBooking.startTime).format('HH:mm') }} – {{ dayjs(activeBooking.endTime).format('HH:mm') }}</span>
      </p>

      <!-- 예약자 -->
      <p class="flex items-center gap-1.5 text-[13px] text-slate-400 mt-2">
        <svg width="12" height="12" viewBox="0 0 15 15" fill="none" class="flex-shrink-0">
          <circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/>
          <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        {{ activeBooking.organizer }}
      </p>

      <!-- 참석자 -->
      <p v-if="activeBooking.attendeeIds?.length || activeBooking.externalAttendeeNames?.length" class="flex items-center gap-1.5 text-[13px] text-slate-400 mt-1">
        <svg width="13" height="11" viewBox="0 0 18 14" fill="none" class="flex-shrink-0">
          <circle cx="6" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
          <path d="M0.5 13c0-3 2.5-5.2 5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
          <circle cx="12" cy="4" r="2.8" stroke="currentColor" stroke-width="1.4"/>
          <path d="M17.5 13c0-3-2.5-5.2-5.5-5.2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
        </svg>
        {{ resolveAttendees(activeBooking.attendeeIds, activeBooking.externalAttendeeNames) }}
      </p>

      <!-- 메모 -->
      <p v-if="activeBooking.description" class="flex items-start gap-1.5 text-[13px] text-slate-500 mt-2 leading-relaxed">
        <svg width="12" height="12" viewBox="0 0 14 14" fill="none" class="flex-shrink-0 mt-0.5">
          <rect x="1" y="1" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.4"/>
          <path d="M4 5h6M4 8h4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
        </svg>
        {{ activeBooking.description }}
      </p>
    </div>

    <!-- 액션 버튼 -->
    <div class="flex">
      <button @click="closeAll"
              class="flex-1 py-3 text-[14px] font-bold text-slate-400 hover:bg-slate-50 transition-colors border-r border-slate-100">
        닫기
      </button>
      <button v-if="canEditOrCancel(activeBooking)"
              @click="openEditModal(activeBooking); closeAll()"
              class="flex-1 py-3 text-[14px] font-bold text-indigo-500 hover:bg-indigo-50 transition-colors border-r border-slate-100">
        수정
      </button>
      <button v-if="canEditOrCancel(activeBooking)"
              @click="confirmCancel(activeBooking); closeAll()"
              class="flex-1 py-3 text-[14px] font-bold text-red-500 hover:bg-red-50 transition-colors">
        취소
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';

const {
  isMobile, tooltip, detailTarget, popoverStyle,
  getRoomColor, getRoomName, resolveAttendees,
  canEditOrCancel, openEditModal, confirmCancel,
} = useApp();

const activeBooking = computed(() =>
  detailTarget.value || (tooltip.show && tooltip.booking ? tooltip.booking : null)
);
const activeStyle = computed(() =>
  detailTarget.value ? popoverStyle.value : tooltip.style
);

const closeAll = () => {
  tooltip.show   = false;
  tooltip.pinned = false;
  detailTarget.value = null;
};

const tooltipDuration = (b) => {
  const mins = dayjs(b.endTime).diff(dayjs(b.startTime), 'minute');
  const h = Math.floor(mins / 60), m = mins % 60;
  return h > 0 && m > 0 ? `${h}시간 ${m}분` : h > 0 ? `${h}시간` : `${m}분`;
};
</script>
