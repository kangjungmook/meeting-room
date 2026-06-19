<template>
  <div class="fixed inset-0 z-50 flex items-end sm:items-center justify-center sm:p-4">
    <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="showUserSettings = false"></div>
    <div class="relative bg-white dark:bg-gray-900 rounded-t-2xl sm:rounded-2xl shadow-2xl border-t border-slate-200 dark:border-gray-700 sm:border w-full max-w-md overflow-hidden">

      <!-- 모바일 드래그 핸들 -->
      <div class="flex justify-center pt-3 pb-1 sm:hidden">
        <div class="w-10 h-1.5 rounded-full bg-slate-300 dark:bg-gray-600"></div>
      </div>

      <!-- 헤더 -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-gray-700">
        <div>
          <h2 class="text-[17px] font-black text-slate-800 dark:text-gray-100">내 알림 설정</h2>
          <p class="text-[12px] text-slate-400 dark:text-gray-500 mt-0.5">알림 수신 방식을 설정합니다</p>
        </div>
        <button @click="showUserSettings = false"
                class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 dark:text-gray-500 hover:bg-slate-100 dark:hover:bg-gray-700 transition-all">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M2 2l10 10M12 2L2 12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <div>

        <!-- FCM 푸시 알림 섹션 -->
        <div class="px-6 py-5 border-b border-slate-100 dark:border-gray-700">
          <div class="flex items-center gap-2 mb-4">
            <svg width="14" height="14" viewBox="0 0 15 15" fill="none" class="text-indigo-500">
              <path d="M7.5 1a4.5 4.5 0 0 1 4.5 4.5c0 2.5.5 3.5 1 4.5H2c.5-1 1-2 1-4.5A4.5 4.5 0 0 1 7.5 1z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
              <path d="M5.5 10a2 2 0 0 0 4 0" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
            <p class="text-[12px] font-black uppercase tracking-wider text-slate-500 dark:text-gray-400">푸시 알림 (PWA)</p>
          </div>
          <div class="flex flex-col gap-3">
            <div v-for="item in pushItems" :key="item.key" class="flex items-center justify-between gap-4">
              <div>
                <p class="text-[14px] font-semibold text-slate-700 dark:text-gray-200">{{ item.label }}</p>
                <p class="text-[12px] text-slate-400 dark:text-gray-500 mt-0.5">{{ item.desc }}</p>
              </div>
              <button type="button" @click="userNotifPrefs[item.key] = !userNotifPrefs[item.key]"
                      :class="['relative flex-shrink-0 w-11 h-6 rounded-full transition-colors duration-300 focus:outline-none',
                               userNotifPrefs[item.key] ? 'bg-indigo-500' : 'bg-slate-300']">
                <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-md transition-all duration-300',
                               userNotifPrefs[item.key] ? 'left-[22px]' : 'left-0.5']"></span>
              </button>
            </div>
          </div>
        </div>

        <!-- SSE 토스트 알림 섹션 -->
        <div class="px-6 py-5">
          <div class="flex items-center gap-2 mb-4">
            <svg width="14" height="14" viewBox="0 0 15 15" fill="none" class="text-indigo-500">
              <rect x="1.5" y="3" width="12" height="9" rx="1.5" stroke="currentColor" stroke-width="1.3"/>
              <path d="M5 7h5M5 9.5h3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
            <p class="text-[12px] font-black uppercase tracking-wider text-slate-500 dark:text-gray-400">화면 토스트 알림</p>
          </div>
          <div class="flex flex-col gap-3">
            <div v-for="item in toastItems" :key="item.key" class="flex items-center justify-between gap-4">
              <div>
                <p class="text-[14px] font-semibold text-slate-700 dark:text-gray-200">{{ item.label }}</p>
                <p class="text-[12px] text-slate-400 dark:text-gray-500 mt-0.5">{{ item.desc }}</p>
              </div>
              <button type="button" @click="userNotifPrefs[item.key] = !userNotifPrefs[item.key]"
                      :class="['relative flex-shrink-0 w-11 h-6 rounded-full transition-colors duration-300 focus:outline-none',
                               userNotifPrefs[item.key] ? 'bg-indigo-500' : 'bg-slate-300']">
                <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-md transition-all duration-300',
                               userNotifPrefs[item.key] ? 'left-[22px]' : 'left-0.5']"></span>
              </button>
            </div>
          </div>

          <!-- 토스트 유지 시간 -->
          <div class="mt-4 pt-4 border-t border-slate-100 dark:border-gray-700">
            <div class="flex items-center justify-between gap-4">
              <div>
                <p class="text-[14px] font-semibold text-slate-700 dark:text-gray-200">토스트 유지 시간</p>
                <p class="text-[12px] text-slate-400 dark:text-gray-500 mt-0.5">알림 배너가 사라지기까지의 시간</p>
              </div>
              <div class="flex items-center gap-2">
                <input v-model.number="userNotifPrefs.toastDuration" type="number" min="1" max="30"
                       class="w-16 text-center border border-slate-200 dark:border-gray-600 rounded-xl px-2 py-1.5 text-[14px] font-bold text-slate-700 dark:text-gray-200 bg-white dark:bg-gray-800 outline-none focus:border-indigo-400" />
                <span class="text-[13px] text-slate-400 dark:text-gray-500 font-semibold">초</span>
              </div>
            </div>
          </div>
        </div>

      </div>

      <!-- 하단 버튼 -->
      <div class="flex gap-3 px-6 py-5 border-t border-slate-100 dark:border-gray-700">
        <button @click="showUserSettings = false"
                class="flex-1 py-3 rounded-xl bg-slate-100 dark:bg-gray-700 hover:bg-slate-200 dark:hover:bg-gray-600 text-slate-500 dark:text-gray-300 text-[14px] font-bold transition-all">
          취소
        </button>
        <button @click="handleSave"
                class="flex-1 py-3 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[14px] font-bold transition-all">
          저장
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { useApp } from '../../composables/useApp';

const { userNotifPrefs, showUserSettings, saveNotifPrefs } = useApp();

const pushItems = [
  { key: 'receiveInvitation', label: '초대 알림',   desc: '내가 참석자로 추가될 때' },
  { key: 'receiveUpdates',    label: '수정 알림',   desc: '참석 중인 예약이 변경될 때' },
  { key: 'receiveReminder',   label: '리마인더',    desc: '회의 시작 전 푸시 알림' },
];

const toastItems = [
  { key: 'toastCreated',   label: '새 예약 생성', desc: '누군가 새 예약을 등록할 때' },
  { key: 'toastUpdated',   label: '예약 수정',    desc: '예약 내용이 변경될 때' },
  { key: 'toastCancelled', label: '예약 취소',    desc: '예약이 취소될 때' },
];

const handleSave = async () => {
  await saveNotifPrefs();
  showUserSettings.value = false;
};
</script>
