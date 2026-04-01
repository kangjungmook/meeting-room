<template>
  <Transition name="room-detail">
    <div v-if="selectedRoom" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="selectedRoom = null"></div>
      <div class="relative bg-[#1e2535] rounded-2xl shadow-2xl w-full max-w-[360px] overflow-hidden">

        <div class="flex items-center gap-3 px-5 pt-5 pb-4 border-b border-white/10">
          <span class="w-3 h-3 rounded-full flex-shrink-0"
                :style="isRoomInUse(selectedRoom.id)
                  ? { background: selectedRoom.colorCode, boxShadow: `0 0 8px 2px ${selectedRoom.colorCode}88` }
                  : { background: selectedRoom.colorCode, opacity: 0.7 }"></span>
          <div class="flex-1 min-w-0">
            <p class="text-[17px] font-black text-slate-50 leading-none">{{ selectedRoom.name }}</p>
            <p class="text-[12px] text-slate-500 mt-1">최대 {{ selectedRoom.capacity }}인</p>
          </div>
          <span class="text-[11px] font-bold px-2.5 py-1 rounded-full"
                :class="isRoomInUse(selectedRoom.id) ? 'bg-red-500/20 text-red-400' : 'bg-emerald-500/20 text-emerald-400'">
            {{ isRoomInUse(selectedRoom.id) ? '사용 중' : '사용 가능' }}
          </span>
          <button @click="selectedRoom = null" class="w-7 h-7 flex items-center justify-center rounded-lg bg-white/10 hover:bg-white/20 text-slate-400 hover:text-slate-200 transition-colors flex-shrink-0">
            <svg width="11" height="11" viewBox="0 0 12 12" fill="none"><path d="M1 1l10 10M11 1L1 11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>

        <div class="px-5 py-4 flex flex-col gap-4 max-h-[60vh] overflow-y-auto">
          <!-- 진행 중인 예약 -->
          <div v-if="selectedRoomCurrent" class="bg-red-500/10 border border-red-500/20 rounded-xl px-4 py-3">
            <p class="text-[11px] font-bold uppercase tracking-wider text-red-400 mb-2">현재 진행 중</p>
            <p class="text-[16px] font-black text-slate-100">{{ selectedRoomCurrent.title }}</p>
            <p class="text-[13px] text-slate-400 mt-1">주최: {{ selectedRoomCurrent.organizer }}</p>
            <p v-if="selectedRoomCurrent.attendeeIds?.length" class="text-[13px] text-slate-400 mt-0.5">참석: {{ resolveAttendees(selectedRoomCurrent.attendeeIds) }}</p>
            <p class="text-[13px] text-slate-500 mt-1">
              {{ dayjs(selectedRoomCurrent.startTime).format('HH:mm') }} – {{ dayjs(selectedRoomCurrent.endTime).format('HH:mm') }}
            </p>
          </div>

          <!-- 오늘 일정 -->
          <div>
            <p class="text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-2">오늘 일정</p>
            <div v-if="selectedRoomBookings.length === 0" class="text-[13px] text-slate-600 py-2">오늘 예약 없음</div>
            <div v-else class="flex flex-col gap-1">
              <div v-for="b in selectedRoomBookings" :key="b.id" class="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-white/5">
                <div class="flex-1 min-w-0">
                  <p class="text-[13px] font-semibold text-slate-200 truncate">{{ b.title }}</p>
                  <p class="text-[11px] text-slate-500 mt-0.5">
                    {{ dayjs(b.startTime).format('HH:mm') }} – {{ dayjs(b.endTime).format('HH:mm') }}
                    <span class="ml-1">· {{ b.organizer }}</span>
                  </p>
                </div>
                <span class="text-[10px] font-bold px-2 py-0.5 rounded-full flex-shrink-0" :class="getRoomBookingStatus(b).cls">
                  {{ getRoomBookingStatus(b).label }}
                </span>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </Transition>
</template>

<script setup>
import { useApp } from '../../composables/useApp';
import dayjs from 'dayjs';

const {
  selectedRoom, selectedRoomBookings, selectedRoomCurrent,
  isRoomInUse, resolveAttendees, getRoomBookingStatus,
} = useApp();
</script>

<style scoped>
.room-detail-enter-active { transition: opacity 0.2s ease, transform 0.2s cubic-bezier(0.16,1,0.3,1); }
.room-detail-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.room-detail-enter-from   { opacity: 0; transform: scale(0.96); }
.room-detail-leave-to     { opacity: 0; transform: scale(0.96); }
</style>
