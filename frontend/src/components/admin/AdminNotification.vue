<template>
  <div class="flex flex-col gap-4">

    <!-- 전역 알림 토글 -->
    <div :class="['rounded-2xl border overflow-hidden transition-all', notifSetting.enabled ? 'bg-white border-slate-200' : 'bg-amber-50/60 border-amber-200']">
      <div class="px-6 py-5 flex items-center justify-between gap-4">
        <div>
          <h2 class="text-[16px] font-bold text-slate-800">전체 알림 기능</h2>
          <p class="text-[13px] mt-1" :class="notifSetting.enabled ? 'text-slate-600' : 'text-amber-600 font-semibold'">
            {{ notifSetting.enabled ? 'FCM 알림이 활성화되어 있습니다.' : '⚠️ 알림이 비활성화되어 있습니다. 모든 FCM 알림이 전송되지 않습니다.' }}
          </p>
        </div>
        <button type="button" @click="notifSetting.enabled = !notifSetting.enabled"
          :class="['relative flex-shrink-0 w-12 h-6 rounded-full transition-colors duration-300 focus:outline-none', notifSetting.enabled ? 'bg-indigo-500' : 'bg-slate-300']">
          <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-md transition-all duration-300', notifSetting.enabled ? 'left-[26px]' : 'left-0.5']"></span>
        </button>
      </div>
    </div>

    <!-- 세부 알림 설정 -->
    <div :class="['bg-white rounded-2xl border overflow-hidden transition-all', notifSetting.enabled ? 'border-slate-200' : 'border-slate-200 opacity-50 pointer-events-none']">
      <div class="px-6 py-5 border-b border-slate-100">
        <h2 class="text-[16px] font-bold text-slate-800">세부 알림 설정</h2>
        <p class="text-[13px] text-slate-600 mt-1">알림 유형별로 개별 ON/OFF 할 수 있습니다.</p>
      </div>
      <div class="divide-y divide-slate-50">
        <div v-for="item in toggleItems" :key="item.key" class="px-6 py-4 flex items-center justify-between gap-4">
          <div>
            <p class="text-[14px] font-semibold text-slate-700">{{ item.label }}</p>
            <p class="text-[12px] text-slate-600 mt-0.5">{{ item.desc }}</p>
          </div>
          <button type="button" @click="notifSetting[item.key] = !notifSetting[item.key]"
            :class="['relative flex-shrink-0 w-12 h-6 rounded-full transition-colors duration-300 focus:outline-none', notifSetting[item.key] ? 'bg-indigo-500' : 'bg-slate-300']">
            <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-md transition-all duration-300', notifSetting[item.key] ? 'left-[26px]' : 'left-0.5']"></span>
          </button>
        </div>
      </div>
    </div>

    <!-- 알림 메시지 -->
    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
      <div class="px-6 py-5 border-b border-slate-100">
        <h2 class="text-[16px] font-bold text-slate-800">알림 메시지</h2>
        <p class="text-[13px] text-slate-600 mt-1">사용 가능한 변수: <code class="bg-slate-100 px-1 rounded">{minutes}</code> 분, <code class="bg-slate-100 px-1 rounded">{title}</code> 회의명, <code class="bg-slate-100 px-1 rounded">{time}</code> 시작시간</p>
      </div>
      <div class="px-6 py-5 flex flex-col gap-3">
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-black text-slate-700 uppercase tracking-wider">제목</label>
          <input v-model="notifSetting.titleTemplate" type="text"
            class="w-full border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-bold text-slate-800 outline-none focus:border-indigo-400 transition-all" />
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-black text-slate-700 uppercase tracking-wider">내용</label>
          <input v-model="notifSetting.bodyTemplate" type="text"
            class="w-full border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-bold text-slate-800 outline-none focus:border-indigo-400 transition-all" />
        </div>
        <div class="bg-slate-50 rounded-xl px-4 py-3 border border-slate-200">
          <p class="text-[12px] text-slate-600 mb-1">미리보기 (10분 전, "테스트 · 14:30 시작")</p>
          <p class="text-[14px] font-bold text-slate-700">{{ notifSetting.titleTemplate.replace('{minutes}', '10') }}</p>
          <p class="text-[13px] text-slate-700">{{ notifSetting.bodyTemplate.replace('{title}', '테스트').replace('{time}', '14:30') }}</p>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-black text-slate-700 uppercase tracking-wider">토스트 유지 시간 (초)</label>
          <div class="flex items-center gap-3">
            <input v-model.number="notifSetting.toastDuration" type="number" min="1" max="60"
              class="w-24 border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-bold text-slate-800 outline-none focus:border-indigo-400 transition-all text-center" />
            <span class="text-[13px] text-slate-600">초 동안 표시</span>
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-end">
      <button @click="saveNotifSetting"
        class="flex items-center gap-2 px-6 py-3 bg-indigo-500 hover:bg-indigo-600 text-white rounded-xl text-[14px] font-bold transition-all">
        <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M2 8l4 4 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        저장
      </button>
    </div>

    <!-- 전체 공지 알림 -->
    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
      <div class="px-6 py-5 border-b border-slate-100">
        <h2 class="text-[16px] font-bold text-slate-800">전체 공지 알림</h2>
        <p class="text-[13px] text-slate-600 mt-1">FCM 토큰이 등록된 모든 유저에게 알림을 즉시 전송합니다.</p>
      </div>
      <div class="px-6 py-5 flex flex-col gap-3">
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-black text-slate-700 uppercase tracking-wider">제목</label>
          <input v-model="broadcast.title" type="text" placeholder="알림 제목"
            class="w-full border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-bold text-slate-800 outline-none focus:border-indigo-400 transition-all placeholder:font-normal placeholder:text-slate-400" />
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-black text-slate-700 uppercase tracking-wider">내용</label>
          <input v-model="broadcast.body" type="text" placeholder="알림 내용"
            class="w-full border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-bold text-slate-800 outline-none focus:border-indigo-400 transition-all placeholder:font-normal placeholder:text-slate-400" />
        </div>
        <div class="flex justify-end">
          <button @click="sendBroadcast" :disabled="!broadcast.title || !broadcast.body"
            class="flex items-center gap-2 px-6 py-3 bg-indigo-500 hover:bg-indigo-600 disabled:opacity-40 disabled:cursor-not-allowed text-white rounded-xl text-[14px] font-bold transition-all">
            <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M13 2L2 7l4.5 2.5L9 14l1.5-4L13 2z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/></svg>
            전송
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAdmin } from '../../composables/useAdmin';

const { notifSetting, broadcast, saveNotifSetting, sendBroadcast } = useAdmin();

const toggleItems = [
  { key: 'enableInvitation',     label: '참조 초대 알림',   desc: '예약에 참조자로 추가될 때 즉시 알림 전송' },
  { key: 'enableBookingUpdated', label: '예약 수정 알림',   desc: '참조 중인 예약이 수정될 때 알림 전송' },
  { key: 'enableBookingCreated', label: '예약 생성 알림',   desc: '새 예약이 생성될 때 참조자에게 알림 전송' },
  { key: 'enableReminder',       label: '회의 전 리마인더', desc: '회의 시작 전 설정된 분에 미리 알림 전송' },

];
</script>
