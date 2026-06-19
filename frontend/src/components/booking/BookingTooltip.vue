<template>
  <!-- 배경 오버레이 (모바일 고정 상태) -->
  <div v-if="activeBooking && (detailTarget || (isMobile && tooltip.pinned))"
       class="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-sm"
       @click="closeAll"></div>

  <!-- 단일 툴팁/팝오버 카드 -->
  <div v-if="activeBooking"
       class="fixed z-50 overflow-y-auto border bg-white shadow-2xl"
       :class="detailTarget
         ? (isMobile
             ? 'w-[calc(100vw-48px)] max-w-[420px] rounded-2xl border-slate-200'
             : 'w-[min(560px,calc(100vw-80px))] rounded-3xl border-slate-300 shadow-[0_32px_80px_rgba(15,23,42,0.22)]')
         : 'w-72 rounded-2xl border-slate-200'"
       :style="activeStyle"
       @mouseenter="!isMobile && !tooltip.pinned && !detailTarget && (tooltip.show = true)"
       @mouseleave="!isMobile && !tooltip.pinned && !detailTarget && (tooltip.show = false)">

    <!-- 방 색상 라인 -->
    <div class="h-1 w-full flex-shrink-0" :style="{ background: getRoomColor(activeBooking.roomId) }"></div>

    <div class="border-b border-slate-100"
         :class="detailTarget && !isMobile ? 'px-7 pt-6 pb-5' : 'px-5 pt-4 pb-4'">
      <!-- 방 이름 + 소요시간 배지 -->
      <div class="flex items-center justify-between gap-2"
           :class="detailTarget && !isMobile ? 'mb-3.5' : 'mb-2.5'">
        <div class="flex items-center gap-2">
          <span class="w-2 h-2 rounded-full flex-shrink-0" :style="{ background: getRoomColor(activeBooking.roomId) }"></span>
          <span class="font-bold text-slate-400"
                :class="detailTarget && !isMobile ? 'text-[13px]' : 'text-[12px]'">
            {{ getRoomName(activeBooking.roomId) }}
          </span>
        </div>
        <span class="text-[11px] font-bold px-2 py-0.5 rounded-full text-white flex-shrink-0"
              :style="{ background: getRoomColor(activeBooking.roomId) + 'cc' }">
          {{ tooltipDuration(activeBooking) }}
        </span>
      </div>

      <!-- 제목 -->
      <p class="font-black text-slate-800 leading-snug"
         :class="detailTarget && !isMobile ? 'text-[22px]' : 'text-[17px]'">
        {{ activeBooking.title }}
      </p>

      <div :class="detailTarget && !isMobile ? 'mt-5 grid grid-cols-[108px_minmax(0,1fr)] gap-x-4 gap-y-3' : 'mt-1.5 space-y-2'">
        <template v-if="detailTarget && !isMobile">
          <p class="text-[12px] font-bold tracking-wide text-slate-400 uppercase">일정</p>
          <p class="text-[14px] text-slate-600 tabular-nums font-semibold">
            {{ dayjs(activeBooking.startTime).format('YYYY년 MM월 DD일 (ddd)') }}
            <span class="text-slate-800">{{ dayjs(activeBooking.startTime).format('HH:mm') }} – {{ dayjs(activeBooking.endTime).format('HH:mm') }}</span>
          </p>

          <p class="text-[12px] font-bold tracking-wide text-slate-400 uppercase">예약자</p>
          <p class="text-[14px] text-slate-600">
            {{ activeBooking.organizer }}
          </p>

          <template v-if="activeBooking.attendeeIds?.length || activeBooking.externalAttendeeNames?.length">
            <p class="text-[12px] font-bold tracking-wide text-slate-400 uppercase">참석자</p>
            <p class="text-[14px] leading-relaxed text-slate-600">
              {{ resolveAttendees(activeBooking.attendeeIds, activeBooking.externalAttendeeNames) }}
            </p>
          </template>

          <template v-if="activeBooking.description">
            <p class="text-[12px] font-bold tracking-wide text-slate-400 uppercase">메모</p>
            <p class="text-[14px] leading-relaxed text-slate-600 whitespace-pre-wrap">
              {{ activeBooking.description }}
            </p>
          </template>
        </template>

        <template v-else>
          <p class="text-[13px] text-slate-500 tabular-nums font-semibold">
            {{ dayjs(activeBooking.startTime).format('MM월 DD일') }}
            <span class="text-slate-700">{{ dayjs(activeBooking.startTime).format('HH:mm') }} – {{ dayjs(activeBooking.endTime).format('HH:mm') }}</span>
          </p>

          <p class="flex items-center gap-1.5 text-[13px]">
            <span class="font-bold text-[11px] flex-shrink-0" :style="{ color: getRoomColor(activeBooking.roomId) }">예약자</span>
            <span class="text-slate-600">{{ activeBooking.organizer }}</span>
          </p>

          <p v-if="activeBooking.attendeeIds?.length || activeBooking.externalAttendeeNames?.length" class="flex items-center gap-1.5 text-[13px]">
            <span class="font-bold text-[11px] flex-shrink-0" :style="{ color: getRoomColor(activeBooking.roomId) }">참석자</span>
            <span class="text-slate-600">{{ resolveAttendees(activeBooking.attendeeIds, activeBooking.externalAttendeeNames) }}</span>
          </p>

          <p v-if="activeBooking.description" class="flex items-start gap-1.5 text-[13px] text-slate-500 leading-relaxed">
            <svg width="12" height="12" viewBox="0 0 14 14" fill="none" class="flex-shrink-0 mt-0.5">
              <rect x="1" y="1" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.4"/>
              <path d="M4 5h6M4 8h4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
            {{ activeBooking.description }}
          </p>
        </template>
      </div>
    </div>

    <!-- 액션 버튼 -->
    <div class="flex"
         :class="detailTarget && !isMobile ? 'justify-end gap-2 px-7 py-4 bg-slate-50/70' : ''">
      <button @click="closeAll"
              :class="detailTarget && !isMobile
                ? 'min-w-[92px] rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-[14px] font-bold text-slate-500 hover:bg-slate-50 transition-colors'
                : 'flex-1 py-3 text-[14px] font-bold text-slate-400 hover:bg-slate-50 transition-colors border-r border-slate-100'">
        닫기
      </button>
      <button v-if="canEditOrCancel(activeBooking)"
              @click="openEditModal(activeBooking); closeAll()"
              :class="detailTarget && !isMobile
                ? 'min-w-[92px] rounded-xl bg-indigo-50 px-4 py-2.5 text-[14px] font-bold text-indigo-600 hover:bg-indigo-100 transition-colors'
                : 'flex-1 py-3 text-[14px] font-bold text-indigo-500 hover:bg-indigo-50 transition-colors border-r border-slate-100'">
        수정
      </button>
      <button v-if="canCancel(activeBooking)"
              @click="confirmCancel(activeBooking); closeAll()"
              :class="detailTarget && !isMobile
                ? 'min-w-[92px] rounded-xl bg-red-50 px-4 py-2.5 text-[14px] font-bold text-red-600 hover:bg-red-100 transition-colors'
                : 'flex-1 py-3 text-[14px] font-bold text-red-500 hover:bg-red-50 transition-colors'">
        예약 취소
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
  canEditOrCancel, canCancel, openEditModal, confirmCancel,
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
