<template>
  <div class="flex h-screen bg-[#F5F7FA] dark:bg-gray-950 font-sans text-gray-900 dark:text-gray-50 overflow-hidden">
    <AppSidebar v-if="!isMobile" />

    <div class="flex-1 flex flex-col overflow-hidden min-w-0">
      <header class="h-[56px] flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-stretch px-4 relative">
        <div class="flex items-center gap-3 flex-shrink-0 min-w-0">
          <button v-if="isMobile" @click="showDrawer = true"
                  class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-500 dark:text-gray-400 transition-colors">
            <AppIcon name="menu" :size="17" />
          </button>
          <div class="flex items-center gap-2 min-w-0">
            <div class="w-8 h-8 rounded-xl bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center flex-shrink-0">
              <AppIcon name="user" :size="14" class="text-blue-600 dark:text-blue-400" />
            </div>
            <div class="leading-tight min-w-0">
              <p class="text-[13px] font-bold text-gray-800 dark:text-gray-100 truncate">내 예약</p>
              <p class="text-[10.5px] text-gray-400 dark:text-gray-500">{{ myBookings.length }}건 관리</p>
            </div>
          </div>
        </div>
      </header>

      <div class="flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 px-4 py-2.5 flex items-center gap-1.5 overflow-x-auto">
        <div v-for="stat in stats" :key="stat.label"
             class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[12px] font-semibold flex-shrink-0 whitespace-nowrap transition-colors"
             :class="stat.active
               ? 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-200'



               : 'text-gray-400 dark:text-gray-600'">
          <span class="tabular-nums font-black"
                :style="stat.active ? { color: stat.color } : {}">{{ stat.count }}</span>
          <span>{{ stat.label }}</span>
        </div>
      </div>

      <div class="flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 px-4 py-2.5 flex items-center gap-2.5 flex-wrap">
        <div class="flex items-center gap-2 px-3 py-1.5 bg-gray-100 dark:bg-gray-800 rounded-xl min-w-[140px] flex-1 max-w-[260px]">
          <AppIcon name="search" :size="12" cls="text-gray-400 flex-shrink-0" />
          <input v-model="searchQuery" type="text" placeholder="제목 검색..."
            class="bg-transparent text-[13px] outline-none w-full text-gray-700 dark:text-gray-200 placeholder-gray-400 dark:placeholder-gray-600" />
          <button v-if="searchQuery" @click="searchQuery = ''"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 flex-shrink-0">
            <AppIcon name="close" :size="10" />
          </button>
        </div>

        <select v-model="filterRoom"
          class="text-[13px] px-3 py-1.5 bg-gray-100 dark:bg-gray-800 rounded-xl outline-none text-gray-600 dark:text-gray-300 cursor-pointer">
          <option value="">전체 회의실</option>
          <option v-for="r in uniqueRooms" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>

        <div class="flex gap-1">
          <button v-for="opt in roleOpts" :key="opt.value"
            @click="filterRole = filterRole === opt.value ? '' : opt.value"
            :class="filterRole === opt.value
              ? 'bg-indigo-500 text-white'
              : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'"
            class="text-[12px] font-semibold px-3 py-1.5 rounded-xl transition-colors">
            {{ opt.label }}
          </button>
        </div>

        <button v-if="hasFilter" @click="searchQuery = ''; filterRoom = ''; filterRole = ''"
          class="ml-auto flex items-center gap-1 text-[12px] text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors">
          <AppIcon name="close" :size="10" />
          초기화
        </button>
        <button @click="refresh" :disabled="refreshing"
          class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-gray-800 text-[12px] font-semibold text-gray-500 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors disabled:opacity-40">
          <AppIcon name="refresh" :size="12" :spin="refreshing" />
          새로고침
        </button>
      </div>

      <!-- 상세 모달 -->
      <transition name="modal">
        <div v-if="detailBooking"
             class="fixed inset-0 z-50 flex items-center justify-center p-4"
             @click.self="detailBooking = null">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="detailBooking = null"></div>
          <div class="relative w-full max-w-[420px] bg-white dark:bg-gray-900 rounded-2xl shadow-2xl flex flex-col overflow-hidden max-h-[85vh]">
            <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100 dark:border-gray-800 flex-shrink-0">
              <div class="flex items-center gap-2.5 min-w-0">
                <span class="w-3 h-3 rounded-full flex-shrink-0" :style="{ background: getRoomColor(detailBooking.roomId) }"></span>
                <p class="text-[15px] font-bold text-gray-800 dark:text-gray-100 truncate">{{ detailBooking.title }}</p>
              </div>
              <button @click="detailBooking = null"
                      class="w-8 h-8 flex items-center justify-center rounded-lg text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors flex-shrink-0 ml-2">
                <AppIcon name="close" :size="14" />
              </button>
            </div>

            <div class="overflow-y-auto px-5 py-5 flex flex-col gap-5 custom-scrollbar">
              <div class="grid grid-cols-2 gap-3">
                <div class="flex items-center gap-3 px-4 py-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                  <AppIcon name="calendar" :size="14" cls="text-gray-400 flex-shrink-0" />
                  <div class="min-w-0">
                    <p class="text-[10px] text-gray-400 dark:text-gray-500">회의실</p>
                    <p class="text-[12.5px] font-bold text-gray-800 dark:text-gray-100 truncate">{{ getRoomName(detailBooking.roomId) }}</p>
                  </div>
                </div>
                <div class="flex items-center gap-3 px-4 py-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                  <AppIcon name="clock" :size="14" cls="text-gray-400 flex-shrink-0" />
                  <div class="min-w-0">
                    <p class="text-[10px] text-gray-400 dark:text-gray-500">시간</p>
                    <p class="text-[12.5px] font-bold text-gray-800 dark:text-gray-100 tabular-nums">
                      {{ dayjs(detailBooking.startTime).format('HH:mm') }} – {{ dayjs(detailBooking.endTime).format('HH:mm') }}
                    </p>
                  </div>
                </div>
              </div>

              <div class="flex items-center gap-2 px-4 py-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                <AppIcon name="calendar" :size="13" cls="text-gray-400 flex-shrink-0" />
                <p class="text-[12.5px] font-semibold text-gray-700 dark:text-gray-300 tabular-nums">
                  {{ dayjs(detailBooking.startTime).format('YYYY년 MM월 DD일 (dd)') }}
                </p>
              </div>

              <div>
                <p class="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider mb-2">예약자</p>
                <div class="flex items-center gap-2.5 px-4 py-3 bg-blue-50 dark:bg-blue-900/20 rounded-xl">
                  <div class="w-7 h-7 rounded-full bg-blue-200 dark:bg-blue-800 flex items-center justify-center flex-shrink-0">
                    <svg width="11" height="11" viewBox="0 0 15 15" fill="none">
                      <circle cx="7.5" cy="5" r="3" stroke="#3b82f6" stroke-width="1.5"/>
                      <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#3b82f6" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <p class="text-[13px] font-semibold text-blue-700 dark:text-blue-300">{{ detailBooking.organizer }}</p>
                </div>
              </div>

              <div v-if="detailBooking.attendeeIds?.length || detailBooking.externalAttendeeNames?.length">
                <p class="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider mb-2">
                  참석자 <span class="text-gray-300 dark:text-gray-600 font-normal normal-case">{{ (detailBooking.attendeeIds?.length || 0) + (detailBooking.externalAttendeeNames?.length || 0) }}명</span>
                </p>
                <div class="flex flex-col gap-1.5">
                  <div v-for="id in (detailBooking.attendeeIds || [])" :key="id"
                       class="flex items-center gap-2.5 px-4 py-2.5 bg-gray-50 dark:bg-gray-800 rounded-xl">
                    <div class="w-6 h-6 rounded-full bg-indigo-100 dark:bg-indigo-900/40 flex items-center justify-center flex-shrink-0">
                      <svg width="10" height="10" viewBox="0 0 15 15" fill="none">
                        <circle cx="7.5" cy="5" r="3" stroke="#6366f1" stroke-width="1.5"/>
                        <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                    </div>
                    <p class="text-[12.5px] font-medium text-gray-700 dark:text-gray-300">{{ resolveAttendees([id]) }}</p>
                  </div>
                  <div v-for="name in (detailBooking.externalAttendeeNames || [])" :key="name"
                       class="flex items-center gap-2.5 px-4 py-2.5 bg-gray-50 dark:bg-gray-800 rounded-xl">
                    <div class="w-6 h-6 rounded-full bg-amber-100 dark:bg-amber-900/40 flex items-center justify-center flex-shrink-0">
                      <svg width="10" height="10" viewBox="0 0 15 15" fill="none">
                        <circle cx="7.5" cy="5" r="3" stroke="#f59e0b" stroke-width="1.5"/>
                        <path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#f59e0b" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                    </div>
                    <p class="text-[12.5px] font-medium text-gray-700 dark:text-gray-300">{{ name }}</p>
                    <span class="ml-auto text-[10px] text-amber-500 font-semibold bg-amber-50 dark:bg-amber-900/20 px-1.5 py-0.5 rounded-full">미가입</span>
                  </div>
                </div>
              </div>

              <div v-if="detailBooking.description">
                <p class="text-[11px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider mb-2">메모</p>
                <div class="px-4 py-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                  <p class="text-[13px] text-gray-600 dark:text-gray-300 leading-relaxed whitespace-pre-wrap">{{ detailBooking.description }}</p>
                </div>
              </div>
            </div>

            <div v-if="canEditOrCancel(detailBooking)" class="px-5 py-4 border-t border-gray-100 dark:border-gray-800 flex gap-2 flex-shrink-0">
              <button @click="openEditModal(detailBooking); detailBooking = null; router.push('/main')"
                      class="flex-1 py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[13px] font-bold transition-colors">
                수정
              </button>
              <button @click="confirmCancel(detailBooking); detailBooking = null"
                      class="flex-1 py-2.5 rounded-xl bg-red-50 dark:bg-red-900/20 hover:bg-red-100 text-red-500 text-[13px] font-bold transition-colors">
                예약 취소
              </button>
            </div>
          </div>
        </div>
      </transition>

      <main class="flex-1 overflow-y-auto custom-scrollbar" style="padding-bottom: calc(8rem + env(safe-area-inset-bottom, 0px))">
        <div v-if="isLoadingMyBookings && myBookings.length === 0" class="px-4 py-4">
          <SkeletonBookingList :count="4" />
        </div>

        <div v-else-if="myBookings.length === 0"
             class="flex flex-col items-center justify-center h-full gap-4">
          <div class="w-16 h-16 rounded-2xl bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
            <svg width="26" height="26" viewBox="0 0 18 18" fill="none">
              <rect x="1.5" y="3.5" width="15" height="13" rx="2" stroke="#cbd5e1" stroke-width="1.6"/>
              <path d="M5.5 1.5v4M12.5 1.5v4M1.5 8h15" stroke="#cbd5e1" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </div>
          <p class="text-[14px] font-semibold text-gray-400 dark:text-gray-500">예약이 없습니다</p>
        </div>

        <template v-for="section in sections" :key="section.label">
          <template v-if="section.list.length > 0">
            <div class="sticky top-0 z-10 bg-[#F5F7FA] dark:bg-gray-950 px-4 py-2.5 flex items-center gap-2">
              <span class="w-1.5 h-1.5 rounded-full flex-shrink-0" :style="{ background: section.color }"></span>
              <p class="text-[11px] font-bold uppercase tracking-widest" :style="{ color: section.color }">{{ section.label }}</p>
              <span class="text-[10.5px] font-bold text-white px-1.5 py-0.5 rounded-full" :style="{ background: section.color }">{{ section.list.length }}</span>
              <span class="text-[10px] text-gray-400 dark:text-gray-600 font-medium ml-1">{{ section.dateRange }}</span>
            </div>

            <div class="px-4 pb-2 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2">
              <div v-for="b in section.list" :key="b.id"
                   class="bg-white dark:bg-gray-900 rounded-xl border border-gray-100 dark:border-gray-800 overflow-hidden cursor-pointer hover:border-gray-200 dark:hover:border-gray-700 hover:shadow-sm transition-all"
                   :class="section.past ? 'opacity-55' : ''"
                   @click="detailBooking = detailBooking?.id === b.id ? null : b">
                <div class="flex items-start gap-2.5 px-3 py-2.5">
                  <div class="w-0.5 self-stretch rounded-full flex-shrink-0 mt-0.5" :style="{ background: getRoomColor(b.roomId) }"></div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between gap-1 mb-1">
                      <span class="text-[10px] font-bold px-1.5 py-0.5 rounded-full text-white truncate"
                            :style="{ background: getRoomColor(b.roomId) }">
                        {{ getRoomName(b.roomId) }}
                      </span>
                      <span v-if="!section.past"
                            :class="b.userId === currentUser.id
                              ? 'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400'
                              : 'bg-amber-50 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400'"
                            class="text-[10px] font-semibold px-1.5 py-0.5 rounded-full flex-shrink-0">
                        {{ b.userId === currentUser.id ? '예약자' : '참석자' }}
                      </span>
                      <span v-else class="text-[10px] font-semibold px-1.5 py-0.5 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-400 flex-shrink-0">완료</span>
                    </div>

                    <p class="text-[13px] font-semibold text-gray-800 dark:text-gray-100 truncate leading-tight">{{ b.title }}</p>

                    <div class="flex items-center justify-between mt-1 gap-1">
                      <p class="text-[11px] text-gray-400 dark:text-gray-500 tabular-nums truncate">{{ section.fmt(b) }}</p>
                      <div v-if="!section.past && canEditOrCancel(b)" class="flex gap-1 flex-shrink-0">
                        <button @click.stop="openEditModal(b); router.push('/main')"
                                class="w-6 h-6 flex items-center justify-center rounded-md bg-blue-50 dark:bg-blue-900/30 text-blue-500 hover:bg-blue-100 transition-colors">
                          <AppIcon name="edit" :size="10" />
                        </button>
                        <button @click.stop="confirmCancel(b)"
                                class="w-6 h-6 flex items-center justify-center rounded-md bg-red-50 dark:bg-red-900/30 text-red-400 hover:bg-red-100 transition-colors">
                          <AppIcon name="trash" :size="10" />
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </template>
      </main>
    </div>

    <CancelModal />
    <MobileDrawer />
    <ToastContainer />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { useApp } from '../../composables/useApp';
import AppIcon from '../icons/AppIcon.vue';
import AppSidebar from '../layout/AppSidebar.vue';
import CancelModal from '../booking/CancelModal.vue';
import MobileDrawer from '../layout/MobileDrawer.vue';
import ToastContainer from '../layout/ToastContainer.vue';
import SkeletonBookingList from '../calendar/SkeletonBookingList.vue';
import api from '../../api';

defineProps({
  isMobile: { type: Boolean, required: true },
});

const router = useRouter();

const {
  showDrawer,
  myBookings, myBookingsToday, myBookingsThisWeek, myBookingsUpcoming, myBookingsPast,
  getRoomColor, getRoomName, resolveAttendees, canEditOrCancel,
  openEditModal, confirmCancel, currentUser,
  fetchMyBookings, fetchRooms,
  connectSse, disconnectSse, applyNotifPrefs,
  isLoadingMyBookings, userMap,
} = useApp();

const detailBooking = ref(null);
const refreshing = ref(false);

const searchQuery = ref('');
const filterRoom = ref('');
const filterRole = ref('');
const roleOpts = [
  { value: 'organizer', label: '예약자' },
  { value: 'attendee', label: '참석자' },
];

const hasFilter = computed(() => !!(searchQuery.value || filterRoom.value || filterRole.value));

const uniqueRooms = computed(() => {
  const seen = new Set();
  return myBookings.value
    .filter(b => { if (seen.has(b.roomId)) return false; seen.add(b.roomId); return true; })
    .map(b => ({ id: b.roomId, name: getRoomName(b.roomId) }));
});

const applyFilter = (list) => list.filter(b => {
  if (searchQuery.value && !b.title.toLowerCase().includes(searchQuery.value.toLowerCase())) return false;
  if (filterRoom.value && b.roomId !== filterRoom.value) return false;
  if (filterRole.value === 'organizer' && b.userId !== currentUser.id) return false;
  if (filterRole.value === 'attendee' && b.userId === currentUser.id) return false;
  return true;
});

const refresh = async () => {
  refreshing.value = true;
  await fetchMyBookings();
  refreshing.value = false;
};

onMounted(async () => {
  await fetchRooms();
  await fetchMyBookings();
  connectSse();
  api.get('/users').then(res => {
    const map = {};
    res.data.forEach(u => { map[u.id] = u.name; });
    userMap.value = map;
  }).catch(() => {});
  api.get('/users/notification-preference').then(res => applyNotifPrefs(res.data)).catch(() => {});
});

onUnmounted(() => { disconnectSse(); });

const fmtRange = b => `${dayjs(b.startTime).format('MM/DD (dd) HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}`;
const fmtToday = b => `${dayjs(b.startTime).format('HH:mm')} – ${dayjs(b.endTime).format('HH:mm')}`;

const sections = computed(() => [
  { label: '오늘 회의', color: '#3b82f6', list: applyFilter(myBookingsToday.value), fmt: fmtToday, past: false,
    dateRange: dayjs().format('M/D (ddd)') },
  { label: '이번 주', color: '#8b5cf6', list: applyFilter(myBookingsThisWeek.value), fmt: fmtRange, past: false,
    dateRange: `${dayjs().startOf('week').format('M/D')} – ${dayjs().endOf('week').format('M/D')}` },
  { label: '이후 일정', color: '#10b981', list: applyFilter(myBookingsUpcoming.value), fmt: fmtRange, past: false,
    dateRange: `${dayjs().endOf('week').add(1, 'day').format('M/D')} ~` },
  { label: '지난 회의', color: '#94a3b8', list: applyFilter(myBookingsPast.value), fmt: fmtRange, past: true,
    dateRange: `~ ${dayjs().subtract(1, 'day').format('M/D')}` },
]);

const stats = computed(() => [
  { label: '전체', count: myBookings.value.length, color: '#374151', active: false },
  { label: '오늘', count: myBookingsToday.value.length, color: '#3b82f6', active: myBookingsToday.value.length > 0 },
  { label: '이번 주', count: myBookingsThisWeek.value.length, color: '#8b5cf6', active: myBookingsThisWeek.value.length > 0 },
  { label: '이후', count: myBookingsUpcoming.value.length, color: '#10b981', active: myBookingsUpcoming.value.length > 0 },
  { label: '지난', count: myBookingsPast.value.length, color: '#94a3b8', active: false },
]);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.25s ease; }
.slide-right-enter-from, .slide-right-leave-to       { transform: translateX(100%); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>

