<template>
  <div class="flex flex-col gap-4">

    <!-- 통계 헤더 -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
      <div class="bg-white rounded-2xl border border-slate-200 px-4 py-3 flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-indigo-50 flex items-center justify-center flex-shrink-0">
          <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#6366f1" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#6366f1" stroke-width="1.6" stroke-linecap="round"/></svg>
        </div>
        <div>
          <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wide">전체</p>
          <p class="text-[22px] font-black text-slate-800 leading-none">{{ stats.total }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl border border-slate-200 px-4 py-3 flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-emerald-50 flex items-center justify-center flex-shrink-0">
          <span class="w-3 h-3 rounded-full bg-emerald-400 animate-pulse"></span>
        </div>
        <div>
          <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wide">온라인</p>
          <p class="text-[22px] font-black text-emerald-600 leading-none">{{ stats.online }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl border border-slate-200 px-4 py-3 flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-violet-50 flex items-center justify-center flex-shrink-0">
          <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><circle cx="7.5" cy="5" r="2.8" stroke="#7c3aed" stroke-width="1.5"/><path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#7c3aed" stroke-width="1.5" stroke-linecap="round"/></svg>
        </div>
        <div>
          <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wide">관리자</p>
          <p class="text-[22px] font-black text-violet-600 leading-none">{{ stats.admin }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl border border-slate-200 px-4 py-3 flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-amber-50 flex items-center justify-center flex-shrink-0">
          <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M7.5 1a4 4 0 0 1 4 4c0 2 .4 2.8.8 3.5H2.7C3.1 7.8 3.5 7 3.5 5a4 4 0 0 1 4-4z" stroke="#d97706" stroke-width="1.4"/><path d="M5.5 9a2 2 0 0 0 4 0" stroke="#d97706" stroke-width="1.4" stroke-linecap="round"/></svg>
        </div>
        <div>
          <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wide">FCM</p>
          <p class="text-[22px] font-black text-amber-600 leading-none">{{ stats.fcm }}</p>
        </div>
      </div>
    </div>

    <!-- 검색 + 역할 필터 + 온라인 필터 -->
    <div class="flex gap-2 flex-wrap items-center">
      <div class="relative flex-1 min-w-[200px]">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400">
          <circle cx="6" cy="6" r="4.5" stroke="currentColor" stroke-width="1.5"/>
          <path d="M10 10l2.5 2.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <input v-model="search" type="text" placeholder="이름 또는 사번 검색..."
          class="w-full bg-white border border-slate-200 rounded-xl pl-9 pr-4 py-2 text-[13px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-300 placeholder:text-slate-400" />
      </div>
      <div class="flex bg-white border border-slate-200 rounded-xl p-0.5 gap-0.5">
        <button v-for="rf in roleFilters" :key="rf.k" @click="roleFilter = rf.k"
          :class="['px-3 py-1.5 rounded-lg text-[12px] font-bold transition-all',
            roleFilter === rf.k ? 'bg-slate-800 text-white' : 'text-slate-500 hover:bg-slate-50']">
          {{ rf.l }}
        </button>
      </div>
      <div class="flex bg-white border border-slate-200 rounded-xl p-0.5 gap-0.5">
        <button v-for="of_ in onlineFilters" :key="of_.k" @click="onlineFilter = of_.k"
          :class="['px-3 py-1.5 rounded-lg text-[12px] font-bold transition-all flex items-center gap-1.5',
            onlineFilter === of_.k
              ? of_.k === 'ONLINE' ? 'bg-emerald-500 text-white'
              : of_.k === 'OFFLINE' ? 'bg-slate-400 text-white'
              : 'bg-slate-800 text-white'
              : 'text-slate-500 hover:bg-slate-50']">
          <span v-if="of_.k === 'ONLINE'" class="w-1.5 h-1.5 rounded-full bg-current"></span>
          <span v-else-if="of_.k === 'OFFLINE'" class="w-1.5 h-1.5 rounded-full bg-current opacity-50"></span>
          {{ of_.l }}
        </button>
      </div>
      <span class="text-[12px] text-slate-400 font-semibold">{{ filteredList.length }}명</span>
    </div>

    <!-- 테이블 (데스크탑) -->
    <div v-if="!isMobile" class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-100">
              <th class="text-left px-5 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">회원</th>
              <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">사번</th>
              <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">역할</th>
              <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">FCM</th>
              <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">가입일</th>
              <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">접속 현황</th>
              <th class="text-right px-5 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredList.length === 0">
              <td colspan="7" class="py-14 text-center">
                <div class="flex flex-col items-center gap-2">
                  <svg width="36" height="36" viewBox="0 0 40 40" fill="none" class="text-slate-200"><circle cx="18" cy="12" r="7" stroke="currentColor" stroke-width="2"/><path d="M4 36c0-7.7 6.3-14 14-14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <p class="text-[13px] font-semibold text-slate-400">검색 결과가 없습니다</p>
                </div>
              </td>
            </tr>
            <tr v-for="u in filteredList" :key="u.id"
              class="border-b border-slate-50 hover:bg-slate-50/60 transition-colors">
              <td class="px-5 py-3">
                <div class="flex items-center gap-2">
                  <span :class="['w-2 h-2 rounded-full flex-shrink-0',
                    onlineUsers.includes(u.employeeId) ? 'bg-emerald-400 animate-pulse' : 'bg-slate-200']"></span>
                  <span class="text-[14px] font-semibold text-slate-800">{{ u.name }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-[13px] font-mono font-bold text-slate-600">{{ u.employeeId }}</td>
              <td class="px-4 py-3">
                <span :class="['px-2.5 py-1 rounded-lg text-[11px] font-bold',
                  u.role === 'ADMIN' ? 'bg-violet-100 text-violet-600 border border-violet-200/60' : 'bg-slate-100 text-slate-500']">
                  {{ u.role }}
                </span>
              </td>
              <td class="px-4 py-3">
                <span v-if="u.fcmToken" class="flex items-center gap-1 text-[12px] font-bold text-emerald-500">
                  <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 flex-shrink-0"></span>ON
                </span>
                <span v-else class="text-[12px] text-slate-300">—</span>
              </td>
              <td class="px-4 py-3 text-[12px] text-slate-500">{{ dayjs(u.createdAt).format('MM/DD HH:mm') }}</td>
              <td class="px-4 py-3">
                <template v-if="onlineUsers.includes(u.employeeId)">
                  <span class="flex items-center gap-1.5 text-[12px] font-bold text-emerald-500">
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse flex-shrink-0"></span>온라인
                  </span>
                  <span v-if="u.lastLoginAt" class="block text-[11px] text-slate-400 tabular-nums mt-0.5">
                    접속 {{ dayjs(u.lastLoginAt).format('MM/DD HH:mm') }}
                  </span>
                </template>
                <template v-else>
                  <span class="text-[12px] text-slate-400">오프라인</span>
                  <span v-if="u.lastLoginAt" class="block text-[11px] text-slate-400 tabular-nums mt-0.5">
                    접속 {{ dayjs(u.lastLoginAt).format('MM/DD HH:mm') }}
                  </span>
                  <span v-if="u.lastLogoutAt" class="block text-[11px] text-slate-400 tabular-nums">
                    종료 {{ dayjs(u.lastLogoutAt).format('MM/DD HH:mm') }}
                  </span>
                  <span v-else-if="!u.lastLoginAt" class="text-slate-300">—</span>
                </template>
              </td>
              <td class="px-5 py-3">
                <div class="flex items-center justify-end gap-1.5 flex-wrap">
                  <button @click="resetUserPassword(u)"
                    class="px-2.5 py-1.5 rounded-lg text-[12px] font-semibold text-amber-600 bg-amber-50 hover:bg-amber-100 border border-amber-200/50 transition-all whitespace-nowrap">
                    비번 초기화
                  </button>
                  <button @click="openSetPasswordModal(u)"
                    class="px-2.5 py-1.5 rounded-lg text-[12px] font-semibold text-indigo-500 bg-indigo-50 hover:bg-indigo-100 border border-indigo-200/50 transition-all whitespace-nowrap">
                    비번 변경
                  </button>
                  <button @click="changeRole(u)"
                    :class="['px-2.5 py-1.5 rounded-lg text-[12px] font-semibold border transition-all whitespace-nowrap',
                      u.role === 'ADMIN'
                        ? 'text-slate-500 bg-slate-50 hover:bg-slate-100 border-slate-200/60'
                        : 'text-violet-500 bg-violet-50 hover:bg-violet-100 border-violet-200/50']">
                    {{ u.role === 'ADMIN' ? '권한 해제' : '관리자' }}
                  </button>
                  <button v-if="u.fcmToken" @click="clearFcmToken(u)"
                    class="px-2.5 py-1.5 rounded-lg text-[12px] font-semibold text-slate-400 bg-slate-50 hover:bg-slate-100 border border-slate-200/60 transition-all">
                    FCM
                  </button>
                  <button @click="confirmDelete(u)"
                    class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 transition-all">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v3M8.5 6.5v3M3 4l.5 8a.5.5 0 0 0 .5.5h6a.5.5 0 0 0 .5-.5l.5-8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 모바일 카드 -->
    <div v-else class="flex flex-col gap-3">
      <div v-if="filteredList.length === 0"
        class="bg-white rounded-2xl border border-slate-200 py-12 flex flex-col items-center gap-2">
        <p class="text-[14px] font-semibold text-slate-400">검색 결과가 없습니다</p>
      </div>
      <div v-for="u in filteredList" :key="u.id"
        class="bg-white rounded-2xl border border-slate-200 p-4">
        <div class="flex items-center gap-3 mb-3">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <span :class="['w-2 h-2 rounded-full flex-shrink-0',
                onlineUsers.includes(u.employeeId) ? 'bg-emerald-400 animate-pulse' : 'bg-slate-200']"></span>
              <span class="text-[15px] font-bold text-slate-800">{{ u.name }}</span>
              <span :class="['px-2 py-0.5 rounded-lg text-[10px] font-bold',
                u.role === 'ADMIN' ? 'bg-violet-100 text-violet-600' : 'bg-slate-100 text-slate-700']">
                {{ u.role }}
              </span>
              <span v-if="u.fcmToken" class="flex items-center gap-1 text-[11px] font-semibold text-emerald-500">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>FCM
              </span>
            </div>
            <p class="text-[12px] text-slate-500 mt-0.5">{{ u.employeeId }}</p>
            <div class="flex items-center gap-2 mt-0.5 flex-wrap">
              <template v-if="onlineUsers.includes(u.employeeId)">
                <span class="flex items-center gap-1 text-[11px] font-bold text-emerald-500">
                  <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>온라인
                </span>
                <span v-if="u.lastLoginAt" class="text-[11px] text-slate-400 tabular-nums">
                  접속 {{ dayjs(u.lastLoginAt).format('MM/DD HH:mm') }}
                </span>
              </template>
              <template v-else>
                <span class="text-[11px] text-slate-400">오프라인</span>
                <span v-if="u.lastLoginAt" class="text-[11px] text-slate-400 tabular-nums">
                  접속 {{ dayjs(u.lastLoginAt).format('MM/DD HH:mm') }}
                </span>
                <span v-if="u.lastLogoutAt" class="text-[11px] text-slate-400 tabular-nums">
                  · 종료 {{ dayjs(u.lastLogoutAt).format('MM/DD HH:mm') }}
                </span>
              </template>
            </div>
          </div>
        </div>
        <div class="flex gap-2 pt-3 border-t border-slate-50 flex-wrap">
          <button @click="resetUserPassword(u)"
            class="flex-1 py-2 rounded-xl text-[12px] font-semibold text-amber-600 bg-amber-50 hover:bg-amber-100 transition-all">비번 초기화</button>
          <button @click="openSetPasswordModal(u)"
            class="flex-1 py-2 rounded-xl text-[12px] font-semibold text-indigo-500 bg-indigo-50 hover:bg-indigo-100 transition-all">비번 변경</button>
          <button @click="changeRole(u)"
            :class="['flex-1 py-2 rounded-xl text-[12px] font-semibold transition-all',
              u.role === 'ADMIN' ? 'text-slate-500 bg-slate-50 hover:bg-slate-100' : 'text-violet-500 bg-violet-50 hover:bg-violet-100']">
            {{ u.role === 'ADMIN' ? '권한 해제' : '관리자' }}
          </button>
          <button @click="confirmDelete(u)"
            class="w-10 py-2 rounded-xl text-[12px] font-bold text-red-400 bg-red-50 hover:bg-red-100 transition-all flex items-center justify-center">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v3M8.5 6.5v3M3 4l.5 8a.5.5 0 0 0 .5.5h6a.5.5 0 0 0 .5-.5l.5-8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 비밀번호 설정 모달 -->
    <div v-if="pwModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
          <div>
            <h3 class="text-[17px] font-bold text-slate-800">비밀번호 설정</h3>
            <p class="text-[13px] text-slate-600 mt-0.5">{{ pwModal.userName }}</p>
          </div>
          <button @click="pwModal.show = false"
            class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 transition-all">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <form @submit.prevent="submitSetPassword" class="px-6 py-5 flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">새 비밀번호</label>
            <input v-model="pwModal.password" type="password" required placeholder="4자 이상"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">비밀번호 확인</label>
            <input v-model="pwModal.confirm" type="password" required
              :class="['w-full bg-slate-50 border rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 transition-all',
                pwModal.confirm && pwModal.password !== pwModal.confirm
                  ? 'border-red-300 focus:ring-red-300' : 'border-slate-200 focus:ring-indigo-400']" />
            <p v-if="pwModal.confirm && pwModal.password !== pwModal.confirm"
              class="text-[12px] text-red-400 font-semibold">비밀번호가 일치하지 않습니다</p>
          </div>
          <p v-if="pwModal.error" class="text-[12px] font-semibold text-red-500 -mt-1">{{ pwModal.error }}</p>
          <div class="flex gap-3 pt-1">
            <button type="button" @click="pwModal.show = false"
              class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
            <button type="submit"
              class="flex-[2] py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[14px] font-bold transition-all">설정</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 유저 삭제 확인 모달 -->
    <div v-if="deleteConfirm.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="px-7 pt-7 pb-5">
          <div class="w-12 h-12 rounded-2xl bg-red-50 flex items-center justify-center mb-4">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none"><path d="M11 8v4M11 15h.01M3 11a8 8 0 1 0 16 0 8 8 0 0 0-16 0z" stroke="#ef4444" stroke-width="1.8" stroke-linecap="round"/></svg>
          </div>
          <h3 class="text-[17px] font-bold text-slate-800">유저를 삭제할까요?</h3>
          <p class="text-[14px] text-slate-700 mt-2 leading-relaxed">
            <span class="font-bold text-slate-600">{{ deleteConfirm.target?.name }}</span>
            ({{ deleteConfirm.target?.employeeId }}) 유저가 삭제됩니다.
            <span class="text-red-500 font-bold">되돌릴 수 없습니다.</span>
          </p>
        </div>
        <div class="flex gap-3 px-7 pb-7">
          <button @click="deleteConfirm.show = false"
            class="flex-1 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
          <button @click="doDelete"
            class="flex-1 py-3 rounded-xl bg-red-500 hover:bg-red-600 text-white text-[14px] font-bold transition-all">삭제</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue';
import { useAdmin } from '../../composables/useAdmin';
import { showAdminToast } from '../../composables/admin/useAdminToast';

const {
  users, onlineUsers, currentUser,
  resetUserPassword, changeRole, deleteUser, clearFcmToken, setUserPassword,
  dayjs,
} = useAdmin();

defineProps({ isMobile: Boolean });

// ── 승인된 유저 목록 (자기 자신 제외) ──
const approvedList = computed(() =>
  users.value.filter(u => u.status === 'APPROVED' && u.employeeId !== currentUser.employeeId)
);

// ── 통계 ──
const stats = computed(() => ({
  total:  approvedList.value.length,
  online: approvedList.value.filter(u => onlineUsers.value.includes(u.employeeId)).length,
  admin:  approvedList.value.filter(u => u.role === 'ADMIN').length,
  fcm:    approvedList.value.filter(u => u.fcmToken).length,
}));

// ── 검색 + 역할 필터 + 온라인 필터 ──
const search       = ref('');
const roleFilter   = ref('ALL');
const onlineFilter = ref('ALL');

const roleFilters = [
  { k: 'ALL',   l: '전체' },
  { k: 'USER',  l: 'USER' },
  { k: 'ADMIN', l: 'ADMIN' },
];
const onlineFilters = [
  { k: 'ALL',     l: '전체' },
  { k: 'ONLINE',  l: '온라인' },
  { k: 'OFFLINE', l: '오프라인' },
];

const filteredList = computed(() => {
  const kw = search.value.trim().toLowerCase();
  return approvedList.value.filter(u => {
    const isOnline    = onlineUsers.value.includes(u.employeeId);
    const matchRole   = roleFilter.value === 'ALL' || u.role === roleFilter.value;
    const matchKw     = !kw || u.name.toLowerCase().includes(kw) || u.employeeId.toLowerCase().includes(kw);
    const matchOnline = onlineFilter.value === 'ALL'
      || (onlineFilter.value === 'ONLINE'  &&  isOnline)
      || (onlineFilter.value === 'OFFLINE' && !isOnline);
    return matchRole && matchKw && matchOnline;
  });
});

// ── 비밀번호 설정 모달 ──
const pwModal = reactive({ show: false, error: '', userId: null, userName: '', password: '', confirm: '' });

const openSetPasswordModal = (u) => {
  pwModal.userId   = u.id;
  pwModal.userName = u.name;
  pwModal.password = '';
  pwModal.confirm  = '';
  pwModal.error    = '';
  pwModal.show     = true;
};

const submitSetPassword = async () => {
  if (pwModal.password.length < 4) { pwModal.error = '비밀번호는 4자 이상이어야 합니다.'; return; }
  if (pwModal.password !== pwModal.confirm) { pwModal.error = '비밀번호가 일치하지 않습니다.'; return; }
  pwModal.error = '';
  try {
    await setUserPassword(pwModal.userId, pwModal.password);
    const name = pwModal.userName;
    pwModal.show = false;
    showAdminToast(`${name} 님의 비밀번호가 설정되었습니다.`);
  } catch (e) {
    pwModal.error = e.response?.data?.message || '설정 중 오류가 발생했습니다.';
  }
};

// ── 삭제 확인 모달 ──
const deleteConfirm = reactive({ show: false, target: null });

const confirmDelete = (u) => {
  deleteConfirm.target = u;
  deleteConfirm.show   = true;
};

const doDelete = async () => {
  if (!deleteConfirm.target) return;
  await deleteUser(deleteConfirm.target.id);
  showAdminToast(`${deleteConfirm.target.name} 님이 삭제되었습니다.`);
  deleteConfirm.show   = false;
  deleteConfirm.target = null;
};
</script>
