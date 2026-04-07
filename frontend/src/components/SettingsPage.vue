<template>
  <div class="flex h-screen bg-[#F5F7FA] dark:bg-gray-950 font-sans text-gray-900 dark:text-gray-50 overflow-hidden">

    <AppSidebar v-if="!isMobile" />

    <div class="flex-1 flex flex-col overflow-hidden min-w-0">

      <!-- 헤더 -->
      <header class="h-[56px] flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between px-5 gap-3">
        <div class="flex items-center gap-3">
          <button @click="router.push('/main')"
                  class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 transition-colors">
            <svg width="16" height="16" viewBox="0 0 18 18" fill="none">
              <path d="M11 4L7 9l4 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <h1 class="text-[15px] font-semibold text-gray-800 dark:text-gray-100">설정</h1>
        </div>
        <button @click="handleSave"
                class="px-4 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-[13px] font-semibold transition-colors">
          저장
        </button>
      </header>

      <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col min-h-0">

        <div class="flex flex-1 min-h-0" :class="isMobile ? 'flex-col' : ''">

          <!-- 탭 네비게이션 -->
          <div :class="isMobile
            ? 'flex-shrink-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex px-4 pt-3 gap-1'
            : 'w-[200px] flex-shrink-0 border-r border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 px-3 py-4 flex flex-col gap-0.5'">
            <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
                    :class="isMobile ? [
                      'flex items-center gap-1.5 px-3 py-2 rounded-t-lg text-[13px] font-semibold transition-colors border-b-2 -mb-px',
                      activeTab === tab.key
                        ? 'border-blue-600 text-blue-700 dark:text-blue-400'
                        : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'
                    ] : [
                      'flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-[13px] text-left w-full transition-colors',
                      activeTab === tab.key
                        ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-400 font-semibold'
                        : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 font-medium'
                    ]">
              <span v-if="!isMobile" v-html="tab.icon"
                    :class="activeTab === tab.key ? 'text-blue-600 dark:text-blue-400' : 'text-gray-400 dark:text-gray-500'"
                    class="flex-shrink-0 w-[15px] h-[15px]"></span>
              {{ tab.label }}
            </button>
          </div>

          <!-- 콘텐츠 -->
          <div class="flex-1 overflow-y-auto custom-scrollbar">

            <!-- 계정 탭 -->
            <div v-if="activeTab === 'account'" class="px-6 py-6 max-w-2xl flex flex-col gap-6">
              <div>
                <h2 class="text-[15px] font-bold text-gray-800 dark:text-gray-100">계정 정보</h2>
                <p class="text-[12px] text-gray-400 dark:text-gray-500 mt-0.5">현재 로그인된 계정 정보입니다</p>
              </div>
              <div class="border border-gray-200 dark:border-gray-700 rounded-2xl overflow-hidden bg-white dark:bg-gray-900">
                <div class="flex items-center px-5 py-4 border-b border-gray-100 dark:border-gray-800">
                  <p class="w-24 text-[12px] font-semibold text-gray-400 dark:text-gray-500 flex-shrink-0">이름</p>
                  <p class="text-[14px] font-semibold text-gray-800 dark:text-gray-100">{{ currentUser.name }}</p>
                </div>
                <div class="flex items-center px-5 py-4 border-b border-gray-100 dark:border-gray-800">
                  <p class="w-24 text-[12px] font-semibold text-gray-400 dark:text-gray-500 flex-shrink-0">사번</p>
                  <p class="text-[14px] text-gray-800 dark:text-gray-100 tabular-nums">{{ currentUser.employeeId }}</p>
                </div>
                <div class="flex items-center px-5 py-4 border-b border-gray-100 dark:border-gray-800">
                  <p class="w-24 text-[12px] font-semibold text-gray-400 dark:text-gray-500 flex-shrink-0">권한</p>
                  <span :class="currentUser.role === 'ADMIN' ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'"
                        class="text-[12px] font-semibold px-2.5 py-1 rounded-full">
                    {{ currentUser.role === 'ADMIN' ? '관리자' : '사용자' }}
                  </span>
                </div>
                <!-- 화면 테마 -->
                <div class="flex items-center px-5 py-4">
                  <p class="w-24 text-[12px] font-semibold text-gray-400 dark:text-gray-500 flex-shrink-0">화면 테마</p>
                  <button @click="toggleDarkMode"
                          class="flex items-center gap-2 px-3 py-1.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-[13px] font-semibold text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                    <svg v-if="darkMode" width="14" height="14" viewBox="0 0 18 18" fill="none">
                      <circle cx="9" cy="9" r="4" stroke="currentColor" stroke-width="1.6"/>
                      <path d="M9 1v2M9 15v2M1 9h2M15 9h2M3.5 3.5l1.5 1.5M13 13l1.5 1.5M3.5 14.5l1.5-1.5M13 5l1.5-1.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                    </svg>
                    <svg v-else width="14" height="14" viewBox="0 0 18 18" fill="none">
                      <path d="M15 10.5A7 7 0 0 1 7.5 3a7 7 0 0 0 7.5 7.5z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    {{ darkMode ? '라이트 모드로 전환' : '다크 모드로 전환' }}
                  </button>
                </div>
              </div>
            </div>

            <!-- 알림 탭 -->
            <div v-if="activeTab === 'notifications'" class="px-6 py-6 max-w-2xl flex flex-col gap-7">
              <div>
                <h2 class="text-[15px] font-bold text-gray-800 dark:text-gray-100">알림 설정</h2>
                <p class="text-[12px] text-gray-400 dark:text-gray-500 mt-0.5">알림 수신 방식을 설정합니다</p>
              </div>

              <div>
                <p class="text-[11px] font-bold uppercase tracking-widest text-gray-400 dark:text-gray-500 mb-3">푸시 알림 (PWA)</p>
                <div class="border border-gray-200 dark:border-gray-700 rounded-2xl overflow-hidden bg-white dark:bg-gray-900">
                  <div v-for="(item, i) in pushItems" :key="item.key"
                       class="flex items-center justify-between px-5 py-4"
                       :class="i < pushItems.length - 1 ? 'border-b border-gray-100 dark:border-gray-800' : ''">
                    <div>
                      <p class="text-[13.5px] font-semibold text-gray-700 dark:text-gray-200">{{ item.label }}</p>
                      <p class="text-[12px] text-gray-400 dark:text-gray-500 mt-0.5">{{ item.desc }}</p>
                    </div>
                    <button type="button" @click="userNotifPrefs[item.key] = !userNotifPrefs[item.key]"
                            :class="['relative flex-shrink-0 w-11 h-6 rounded-full transition-colors duration-200',
                                     userNotifPrefs[item.key] ? 'bg-blue-500' : 'bg-gray-200 dark:bg-gray-700']">
                      <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-all duration-200',
                                     userNotifPrefs[item.key] ? 'left-[22px]' : 'left-0.5']"></span>
                    </button>
                  </div>
                </div>
              </div>

              <!-- 리마인더 시점 -->
              <div>
                <p class="text-[11px] font-bold uppercase tracking-widest text-gray-400 dark:text-gray-500 mb-3">미리 알림 시점</p>
                <div class="border border-gray-200 dark:border-gray-700 rounded-2xl overflow-hidden bg-white dark:bg-gray-900 px-5 py-4 flex flex-col gap-3">
                  <p class="text-[12px] text-gray-400 dark:text-gray-500">회의 시작 몇 분 전에 알림을 받을지 직접 설정합니다.</p>
                  <div class="flex flex-wrap gap-2">
                    <div v-for="(m, i) in reminderMinuteList" :key="i"
                         class="flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-700 rounded-xl text-[13px] font-bold text-blue-600 dark:text-blue-400">
                      {{ m }}분 전
                      <button @click="removeReminderMinute(i)"
                              class="w-4 h-4 flex items-center justify-center rounded-full hover:bg-blue-200 dark:hover:bg-blue-700 transition-all">
                        <svg width="8" height="8" viewBox="0 0 10 10" fill="none"><path d="M2 2l6 6M8 2L2 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      </button>
                    </div>
                    <div class="flex items-center gap-1.5">
                      <input v-model.number="newReminderMinute" type="number" min="1" max="120" placeholder="분"
                             class="w-16 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-xl px-2.5 py-1.5 text-[13px] font-bold text-gray-700 dark:text-gray-200 outline-none focus:border-blue-400 text-center"
                             @keyup.enter="addReminderMinute" />
                      <button @click="addReminderMinute"
                              class="px-3 py-1.5 bg-gray-100 dark:bg-gray-800 hover:bg-blue-50 dark:hover:bg-blue-900/20 hover:text-blue-600 dark:hover:text-blue-400 border border-gray-200 dark:border-gray-700 rounded-xl text-[12px] font-bold text-gray-600 dark:text-gray-300 transition-all">
                        추가
                      </button>
                    </div>
                  </div>
                  <p v-if="reminderMinuteList.length === 0" class="text-[12px] text-gray-400 dark:text-gray-500 italic">설정된 시점이 없습니다. 추가해 주세요.</p>
                </div>
              </div>

              <div>
                <p class="text-[11px] font-bold uppercase tracking-widest text-gray-400 dark:text-gray-500 mb-3">화면 토스트 알림</p>
                <div class="border border-gray-200 dark:border-gray-700 rounded-2xl overflow-hidden bg-white dark:bg-gray-900">
                  <div v-for="item in toastItems" :key="item.key"
                       class="flex items-center justify-between px-5 py-4 border-b border-gray-100 dark:border-gray-800">
                    <div>
                      <p class="text-[13.5px] font-semibold text-gray-700 dark:text-gray-200">{{ item.label }}</p>
                      <p class="text-[12px] text-gray-400 dark:text-gray-500 mt-0.5">{{ item.desc }}</p>
                    </div>
                    <button type="button" @click="userNotifPrefs[item.key] = !userNotifPrefs[item.key]"
                            :class="['relative flex-shrink-0 w-11 h-6 rounded-full transition-colors duration-200',
                                     userNotifPrefs[item.key] ? 'bg-blue-500' : 'bg-gray-200 dark:bg-gray-700']">
                      <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-all duration-200',
                                     userNotifPrefs[item.key] ? 'left-[22px]' : 'left-0.5']"></span>
                    </button>
                  </div>
                  <div class="flex items-center justify-between px-5 py -4">
                    <div>
                      <p class="text-[13.5px] font-semibold text-gray-700 dark:text-gray-200">토스트 유지 시간</p>
                      <p class="text-[12px] text-gray-400 dark:text-gray-500 mt-0.5">알림 배너가 사라지기까지의 시간</p>
                    </div>
                    <div class="flex items-center gap-2">
                      <input v-model.number="userNotifPrefs.toastDuration" type="number" min="1" max="30"
                             class="w-16 text-center border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-xl px-2 py-1.5 text-[13.5px] font-bold text-gray-700 dark:text-gray-200 outline-none focus:border-blue-400 transition-colors" />
                      <span class="text-[13px] text-gray-400 dark:text-gray-500">초</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>

      </main>
    </div>
    <ToastContainer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useApp } from '../composables/useApp';
import AppSidebar from './layout/AppSidebar.vue';
import ToastContainer from './layout/ToastContainer.vue';
import api from '../api';

const router = useRouter();
const { isMobile, currentUser, userNotifPrefs, saveNotifPrefs, darkMode, toggleDarkMode,
        connectSse, disconnectSse, applyNotifPrefs } = useApp();

onMounted(() => {
  connectSse();
  api.get('/users/notification-preference').then(res => applyNotifPrefs(res.data)).catch(() => {});
});

onUnmounted(() => { disconnectSse(); });

const activeTab = ref('account');

// ── 리마인더 시점 관리 ──────────────────────────────────
const reminderMinuteList = computed(() => {
  const raw = userNotifPrefs.reminderMinutes;
  if (!raw || raw === '') return [];
  return raw.split(',').map(s => parseInt(s.trim(), 10)).filter(n => !isNaN(n));
});

const newReminderMinute = ref(null);

const addReminderMinute = () => {
  const m = parseInt(newReminderMinute.value, 10);
  if (!m || m < 1 || m > 120) return;
  const list = reminderMinuteList.value;
  if (list.includes(m)) return;
  userNotifPrefs.reminderMinutes = [...list, m].sort((a, b) => a - b).join(',');
  newReminderMinute.value = null;
};

const removeReminderMinute = (index) => {
  const list = [...reminderMinuteList.value];
  list.splice(index, 1);
  userNotifPrefs.reminderMinutes = list.join(',');
};

const tabs = [
  {
    key: 'account',
    label: '계정',
    icon: `<svg width="15" height="15" viewBox="0 0 15 15" fill="none" style="color:inherit"><circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.5"/><path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>`,
  },
  {
    key: 'notifications',
    label: '알림',
    icon: `<svg width="15" height="15" viewBox="0 0 15 15" fill="none" style="color:inherit"><path d="M7.5 1a4.5 4.5 0 0 1 4.5 4.5c0 2.5.5 3.5 1 4.5H2c.5-1 1-2 1-4.5A4.5 4.5 0 0 1 7.5 1z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/><path d="M5.5 10a2 2 0 0 0 4 0" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>`,
  },
];

const pushItems = [
  { key: 'receiveInvitation', label: '초대 알림',  desc: '내가 참석자로 추가될 때' },
  { key: 'receiveUpdates',    label: '수정 알림',  desc: '참석 중인 예약이 변경될 때' },
  { key: 'receiveReminder',   label: '미리 알림',   desc: '회의 시작 전 푸시 알림' },
];

const toastItems = [
  { key: 'toastCreated',   label: '새 예약 생성', desc: '누군가 새 예약을 등록할 때' },
  { key: 'toastUpdated',   label: '예약 수정',    desc: '예약 내용이 변경될 때' },
  { key: 'toastCancelled', label: '예약 취소',    desc: '예약이 취소될 때' },
];

const handleSave = async () => {
  await saveNotifPrefs();
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar       { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }
</style>
