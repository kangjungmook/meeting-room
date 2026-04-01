<template>
  <div>
    <!-- 필터 툴바 -->
    <div class="bg-white rounded-2xl border border-slate-200 p-3 mb-4 flex flex-col gap-3">
      <div class="flex flex-col sm:flex-row gap-2">
        <input v-model="bookingFilter.keyword" type="text" placeholder="제목 또는 예약자 검색"
          class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 text-[14px] font-medium text-slate-800 outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent placeholder:text-slate-400" />
        <div class="flex gap-2 flex-wrap">
          <div class="flex gap-1">
            <button @click="bookingFilter.date = dayjs().format('YYYY-MM-DD')"
              :class="['px-3 py-2 rounded-xl text-[12px] font-bold transition-all', bookingFilter.date === dayjs().format('YYYY-MM-DD') ? 'bg-indigo-500 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200']">오늘</button>
            <button @click="bookingFilter.date = ''"
              :class="['px-3 py-2 rounded-xl text-[12px] font-bold transition-all', bookingFilter.date === '' ? 'bg-indigo-500 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200']">전체</button>
          </div>
          <select v-model="bookingFilter.roomId"
            class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-[14px] font-medium text-slate-800 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer">
            <option value="">전체 회의실</option>
            <option v-for="r in allRooms" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
          <input v-model="bookingFilter.date" type="date"
            class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-[14px] font-medium text-slate-800 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer" />
        </div>
      </div>
      <div class="flex items-center gap-1.5 flex-wrap">
        <span class="text-[11px] font-bold text-slate-500 mr-1">정렬:</span>
        <button v-for="s in sortOptions" :key="s.key" @click="setBookingSort(s.key)"
          :class="['flex items-center gap-1 px-3 py-1 rounded-lg text-[12px] font-semibold transition-all',
            bookingSort.key === s.key ? 'bg-indigo-500 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200']">
          {{ s.label }}
          <svg v-if="bookingSort.key === s.key" width="10" height="10" viewBox="0 0 10 10" fill="none"
            :class="bookingSort.dir === 'desc' ? 'rotate-180' : ''" class="transition-transform">
            <path d="M2 4l3-3 3 3M5 1v8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <span class="ml-auto text-[12px] text-slate-500 font-semibold">{{ filteredBookings.length }}건</span>
      </div>
    </div>

    <!-- 데스크탑 테이블 -->
    <div v-if="!isMobile" class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-100">
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">회의실</th>
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">제목</th>
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">예약자</th>
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">일시</th>
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">상태</th>
              <th class="text-right px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredBookings.length === 0">
              <td colspan="6" class="py-16 text-center">
                <div class="flex flex-col items-center gap-2">
                  <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="text-slate-200"><rect x="4" y="6" width="32" height="28" rx="4" stroke="currentColor" stroke-width="2"/><path d="M4 14h32M13 4v5M27 4v5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <p class="text-[14px] font-semibold text-slate-400">예약이 없습니다</p>
                </div>
              </td>
            </tr>
            <tr v-for="b in filteredBookings" :key="b.id"
              class="border-b border-slate-50 hover:bg-indigo-50/20 transition-colors group">
              <td class="px-6 py-3.5">
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{background: getRoomColor(b.roomId)}"></span>
                  <span class="text-[14px] font-semibold text-slate-700">{{ getRoomName(b.roomId) }}</span>
                </div>
              </td>
              <td class="px-6 py-3.5">
                <p class="text-[14px] font-semibold text-slate-800">{{ b.title }}</p>
                <p v-if="b.description" class="text-[12px] text-slate-500 truncate max-w-[180px]">{{ b.description }}</p>
              </td>
              <td class="px-6 py-3.5 text-[14px] text-slate-700">{{ b.organizer }}</td>
              <td class="px-6 py-3.5">
                <p class="text-[13px] font-semibold text-slate-700">{{ dayjs(b.startTime).format('MM/DD (ddd)') }}</p>
                <p class="text-[12px] text-slate-500">{{ dayjs(b.startTime).format('HH:mm') }} – {{ dayjs(b.endTime).format('HH:mm') }}</p>
              </td>
              <td class="px-6 py-3.5">
                <span v-if="dayjs().isAfter(dayjs(b.startTime)) && dayjs().isBefore(dayjs(b.endTime))"
                  class="flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-600">
                  <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span> 진행중
                </span>
                <span v-else-if="dayjs().isBefore(dayjs(b.startTime))"
                  class="text-[11px] font-bold px-2 py-0.5 rounded-full bg-blue-50 text-blue-500">예정</span>
                <span v-else class="text-[11px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-400">종료</span>
              </td>
              <td class="px-6 py-3.5 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button @click="openEdit(b)" class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-indigo-500 hover:bg-indigo-50 transition-all">수정</button>
                  <button @click="cancelBooking(b)" class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-red-400 hover:bg-red-50 transition-all">취소</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 모바일 카드 -->
    <div v-else class="flex flex-col gap-3">
      <div v-if="filteredBookings.length === 0" class="bg-white rounded-2xl border border-slate-200 py-12 text-center text-slate-500 text-[15px] font-semibold">예약이 없습니다</div>
      <div v-for="b in filteredBookings" :key="b.id" class="bg-white rounded-2xl border border-slate-200 p-4">
        <div class="flex items-start justify-between gap-2 mb-2">
          <div class="flex items-center gap-2 min-w-0">
            <span class="w-2.5 h-2.5 rounded-full flex-shrink-0 mt-0.5" :style="{background: getRoomColor(b.roomId)}"></span>
            <p class="text-[15px] font-bold text-slate-800 truncate">{{ b.title }}</p>
          </div>
          <span class="text-[12px] font-semibold text-slate-600 flex-shrink-0">{{ dayjs(b.startTime).format('MM/DD') }}</span>
        </div>
        <div class="flex items-center gap-3 text-[12px] text-slate-800 mb-3">
          <span class="font-semibold text-indigo-500">{{ getRoomName(b.roomId) }}</span>
          <span>·</span><span>{{ dayjs(b.startTime).format('HH:mm') }} – {{ dayjs(b.endTime).format('HH:mm') }}</span>
          <span>·</span><span>{{ b.organizer }}</span>
        </div>
        <p v-if="b.description" class="text-[12px] text-slate-800 mb-3 truncate">{{ b.description }}</p>
        <div class="flex gap-2 pt-3 border-t border-slate-50">
          <button @click="openEdit(b)" class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-indigo-500 bg-indigo-50 hover:bg-indigo-100 transition-all">수정</button>
          <button @click="cancelBooking(b)" class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-red-400 bg-red-50 hover:bg-red-100 transition-all">취소</button>
        </div>
      </div>
    </div>

    <!-- 예약 수정 모달 -->
    <div v-if="editModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-md rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
          <h3 class="text-[17px] font-bold text-slate-800">예약 수정</h3>
          <button @click="editModal.show = false" class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 transition-all">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <form @submit.prevent="submitEdit" class="px-6 py-5 flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">제목</label>
            <input v-model="editModal.form.title" type="text" required
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">회의실</label>
            <select v-model="editModal.form.roomId" required
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer">
              <option v-for="r in allRooms" :key="r.id" :value="r.id">{{ r.name }}</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div class="flex flex-col gap-1.5">
              <label class="text-[13px] font-bold text-slate-700">시작 시간</label>
              <input v-model="editModal.form.startTime" type="datetime-local" required
                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2.5 text-[14px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
            </div>
            <div class="flex flex-col gap-1.5">
              <label class="text-[13px] font-bold text-slate-700">종료 시간</label>
              <input v-model="editModal.form.endTime" type="datetime-local" required
                 class="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2.5 text-[14px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
            </div>
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">예약자</label>
            <input v-model="editModal.form.organizer" type="text"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">참석자 ID (쉼표 구분 숫자)</label>
            <input v-model="editModal.form.attendeeIdsStr" type="text" placeholder="예) 2,3,5"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex gap-3 pt-1">
            <button type="button" @click="editModal.show = false"
              class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
            <button type="submit"
              class="flex-[2] py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[14px] font-bold transition-all">수정 완료</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useAdmin } from '../../composables/useAdmin';

const {
  allRooms, filteredBookings, bookingFilter, bookingSort,
  cancelBooking, updateBooking, setBookingSort,
  getRoomName, getRoomColor, dayjs,
} = useAdmin();

const props = defineProps({ isMobile: Boolean });

const sortOptions = [
  { key: 'startTime', label: '날짜' },
  { key: 'organizer', label: '예약자' },
  { key: 'title',     label: '제목' },
];

const editModal = reactive({
  show: false,
  form: { id: null, roomId: null, title: '', startTime: '', endTime: '', description: '', attendeeIdsStr: '', organizer: '' },
});

const openEdit = (b) => {
  editModal.form = {
    id: b.id, roomId: b.roomId, title: b.title,
    startTime:   dayjs(b.startTime).format('YYYY-MM-DDTHH:mm'),
    endTime:     dayjs(b.endTime).format('YYYY-MM-DDTHH:mm'),
    description:    b.description    || '',
    attendeeIdsStr: (b.attendeeIds || []).join(','),
    organizer:      b.organizer      || '',
  };
  editModal.show = true;
};

const submitEdit = async () => {
  try {
    const f = editModal.form;
    await updateBooking(f.id, {
      roomId: Number(f.roomId), title: f.title,
      startTime: f.startTime, endTime: f.endTime,
      description: f.description,
      attendeeIds: f.attendeeIdsStr.split(',').map(s => s.trim()).filter(Boolean).map(Number).filter(n => !isNaN(n)),
      organizer: f.organizer,
    });
    editModal.show = false;
  } catch (e) {
    alert('수정 중 오류: ' + (e.response?.data?.message || e.message));
  }
};
</script>
