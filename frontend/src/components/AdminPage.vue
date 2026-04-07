<template>
  <div class="flex h-screen bg-[#f1f5f9] font-sans text-slate-900">

    <!-- ── 사이드바 ── -->
    <aside class="sidebar-panel flex-shrink-0 bg-slate-900 h-full z-10"
      :style="showDrawer ? 'width:256px;overflow:hidden' : 'width:64px;overflow:visible'">

      <!-- 접힌 상태 -->
      <div v-if="!showDrawer" style="height:100%;display:flex;flex-direction:column;align-items:center;padding:20px 0;gap:16px;">
        <div class="w-9 h-9 rounded-xl bg-indigo-500 flex items-center justify-center flex-shrink-0">
          <svg width="16" height="16" viewBox="0 0 18 18" fill="none">
            <rect x="1" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
            <rect x="10" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
            <rect x="1" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
            <rect x="10" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
          </svg>
        </div>
        <button @click="showDrawer = true"
          class="w-9 h-9 flex items-center justify-center rounded-lg hover:bg-white/10 text-slate-500 hover:text-slate-300 transition-all">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M4 3l5 4-5 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </button>
        <div style="display:flex;flex-direction:column;gap:4px;margin-top:4px;">
          <div v-for="tab in tabs" :key="tab.key" class="relative group/tip">
            <button @click="activeTab = tab.key"
              :class="['w-9 h-9 flex items-center justify-center rounded-lg transition-all relative',
                activeTab === tab.key ? 'bg-indigo-500/20 text-indigo-300' : 'text-slate-500 hover:bg-white/10 hover:text-slate-300']">
              <span v-html="tab.icon" class="w-4 h-4"></span>
              <span v-if="tab.key === 'users-pending' && pendingCount > 0"
                class="absolute top-1 right-1 w-2 h-2 rounded-full bg-amber-500"></span>
            </button>
            <div class="pointer-events-none absolute left-full top-1/2 -translate-y-1/2 ml-3 z-50 opacity-0 group-hover/tip:opacity-100 transition-opacity duration-150">
              <div class="flex items-center gap-2 bg-slate-800 border border-slate-700 rounded-lg px-3 py-1.5 shadow-xl whitespace-nowrap">
                <span class="text-[12px] font-semibold text-white">{{ tab.label }}</span>
                <span v-if="tab.key === 'users-pending' && pendingCount > 0"
                  class="min-w-[18px] h-[18px] px-1.5 rounded-full bg-amber-500 text-white text-[10px] font-bold flex items-center justify-center">
                  {{ pendingCount }}
                </span>
              </div>
              <div class="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-slate-800"></div>
            </div>
          </div>
        </div>
        <div style="margin-top:auto;margin-bottom:16px;" class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></div>
      </div>

      <!-- 펼친 상태 -->
      <div v-else style="height:100%;display:flex;flex-direction:column;overflow:hidden;">
        <div style="flex-shrink:0;padding:20px 20px 16px;">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
            <div style="display:flex;align-items:center;gap:10px;">
              <div class="w-8 h-8 rounded-lg bg-indigo-500 flex items-center justify-center flex-shrink-0">
                <svg width="14" height="14" viewBox="0 0 18 18" fill="none">
                  <rect x="1" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
                  <rect x="10" y="1" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
                  <rect x="1" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
                  <rect x="10" y="10" width="7" height="7" rx="1.5" fill="white" opacity="0.95"/>
                </svg>
              </div>
              <div>
                <p class="text-[13px] font-black text-white leading-tight whitespace-nowrap">회의실 예약</p>
                <p class="text-[10px] text-slate-400 leading-tight">관리자 콘솔</p>
              </div>
            </div>
            <button @click="showDrawer = false"
              class="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-white/10 text-slate-500 hover:text-slate-300 transition-all flex-shrink-0">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M10 3L5 7l5 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
          </div>
        </div>

        <!-- 네비게이션 -->
        <div style="flex:1;overflow-y:auto;padding:0 12px;" class="custom-scrollbar">
          <nav class="flex flex-col gap-0.5 pb-2">
            <template v-for="group in navGroups" :key="group.label">
              <button @click="toggleNavGroup(group.label)"
                class="flex items-center justify-between w-full px-2 mt-3 mb-0.5 py-0.5 rounded-lg hover:bg-white/5 transition-colors group/hdr">
                <p class="text-[10px] font-bold tracking-widest uppercase text-slate-500 group-hover/hdr:text-slate-400 transition-colors">{{ group.label }}</p>
                <svg width="10" height="10" viewBox="0 0 10 10" fill="none"
                  class="text-slate-600 transition-transform duration-200 flex-shrink-0"
                  :class="isNavGroupExpanded(group.label) ? 'rotate-0' : '-rotate-90'">
                  <path d="M2 3.5l3 3 3-3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
              <template v-if="isNavGroupExpanded(group.label)">
                <button v-for="key in group.keys" :key="key"
                  @click="activeTab = key"
                  :class="['flex items-center gap-3 px-3 py-2.5 rounded-xl text-[13px] font-semibold text-left transition-colors w-full outline-none focus-visible:outline-none border',
                    activeTab === key
                      ? 'bg-indigo-500/20 text-indigo-300 border-indigo-500/20'
                      : 'border-transparent text-slate-400 hover:bg-white/8 hover:text-white']">
                  <span v-html="tabs.find(t=>t.key===key)?.icon" class="flex-shrink-0 w-4 h-4 opacity-80"></span>
                  {{ tabs.find(t=>t.key===key)?.label }}
                  <span v-if="key === 'users-pending' && pendingCount > 0"
                    class="ml-auto min-w-[18px] h-[18px] px-1 rounded-full bg-amber-500 text-white text-[10px] font-bold flex items-center justify-center">
                    {{ pendingCount }}
                  </span>
                </button>
              </template>
            </template>
          </nav>
        </div>

        <!-- 하단 -->
        <div style="flex-shrink:0;padding:16px 16px 20px;border-top:1px solid rgba(255,255,255,0.08);">
          <a href="/"
            class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-[13px] font-semibold text-slate-400 hover:text-white hover:bg-white/10 transition-all">
            <svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M10 3L5 8l5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            예약 현황으로 이동
          </a>
        </div>
      </div>
    </aside>

    <!-- ── Content ── -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">

      <!-- Top bar -->
      <header class="flex-shrink-0 bg-white border-b border-slate-200 flex items-center justify-between px-4 h-[60px] gap-3 shadow-sm">
        <div class="flex items-center gap-2">
          <span v-html="currentTab?.icon" class="w-5 h-5 text-indigo-500 flex-shrink-0"></span>
          <h1 class="text-[16px] font-black text-slate-800">{{ currentTab?.label }}</h1>
          <span v-if="activeTab === 'users-pending' && pendingCount > 0"
            class="ml-1 min-w-[20px] h-5 px-1.5 rounded-full bg-amber-500 text-white text-[11px] font-bold flex items-center justify-center">
            {{ pendingCount }}
          </span>
        </div>
        <div class="flex items-center gap-3">
          <button v-if="notifSetting.maintenanceMode" @click="activeTab = 'system'"
            class="flex items-center gap-1.5 px-3 py-1.5 bg-red-500 text-white rounded-xl text-[12px] font-bold animate-pulse">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M6 4v2.5M6 9h.01M2 10h8L6 2 2 10z" stroke="white" stroke-width="1.5" stroke-linecap="round"/></svg>
            점검 모드
          </button>
          <button v-if="activeTab === 'rooms'" @click="roomsTabRef?.openModal()"
            class="flex items-center gap-2 px-4 py-2.5 bg-indigo-500 hover:bg-indigo-600 text-white rounded-xl text-[14px] font-bold transition-all">
            <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M7.5 2v11M2 7.5h11" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
            회의실 추가
          </button>
          <div class="flex items-center gap-2 pl-3 border-l border-slate-200">
            <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0">
              <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><circle cx="7.5" cy="5" r="3" stroke="#6366f1" stroke-width="1.5"/><path d="M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/></svg>
            </div>
            <div class="hidden sm:block">
              <p class="text-[13px] font-bold text-slate-700 leading-tight">{{ currentUser.name }}</p>
              <p class="text-[11px] text-slate-600 leading-tight">관리자 · {{ currentUser.employeeId }}</p>
            </div>
            <button @click="logout" title="로그아웃"
              class="w-7 h-7 flex items-center justify-center rounded-lg text-slate-600 hover:text-red-500 hover:bg-red-50 transition-all ml-1">
              <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M6 2H3a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h3M10 10l3-3-3-3M13 7H6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
          </div>
        </div>
      </header>

      <!-- Main content -->
      <main class="flex-1 overflow-auto px-4 py-5 md:px-6 md:py-6 custom-scrollbar">
        <AdminDashboard    v-if="activeTab === 'dashboard'"    :isMobile="isMobile" />
        <AdminRooms        v-if="activeTab === 'rooms'"        :isMobile="isMobile" ref="roomsTabRef" />
        <AdminBookings     v-if="activeTab === 'bookings'"     :isMobile="isMobile" />
        <AdminLogs         v-if="activeTab === 'logs'" />
        <AdminUsers v-show="activeTab === 'users-all'"      filter="ALL"      :isMobile="isMobile" />
        <AdminUsers v-show="activeTab === 'users-pending'"  filter="PENDING"  :isMobile="isMobile" />
        <AdminUsers v-show="activeTab === 'users-approved'" filter="APPROVED" :isMobile="isMobile" />
        <AdminUsers v-show="activeTab === 'users-rejected'" filter="REJECTED" :isMobile="isMobile" />
        <AdminNotification v-if="activeTab === 'notification'" />
        <AdminSystem       v-if="activeTab === 'system'" />
        <AdminReset        v-if="activeTab === 'reset'" />
      </main>
    </div>

    <!-- ── 관리자 Toast ── -->
    <transition name="toast">
      <div v-if="adminToast.show"
           class="fixed bottom-6 right-6 z-[9999] flex items-center gap-2.5 px-4 py-3 rounded-2xl shadow-lg text-[14px] font-bold pointer-events-none"
           :class="adminToast.type === 'success' ? 'bg-emerald-500 text-white' : 'bg-red-500 text-white'">
        <svg v-if="adminToast.type === 'success'" width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M2.5 8l4 4 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 5v4M8 11.5h.01M2 8a6 6 0 1 0 12 0A6 6 0 0 0 2 8z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
        {{ adminToast.msg }}
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useAdmin } from '../composables/useAdmin';
import { adminToast } from '../composables/admin/useAdminToast';
import api from '../api';

import AdminDashboard    from './admin/AdminDashboard.vue';
import AdminRooms        from './admin/AdminRooms.vue';
import AdminBookings     from './admin/AdminBookings.vue';
import AdminLogs         from './admin/AdminLogs.vue';
import AdminUsers        from './admin/AdminUsers.vue';
import AdminNotification from './admin/AdminNotification.vue';
import AdminSystem       from './admin/AdminSystem.vue';
import AdminReset        from './admin/AdminReset.vue';

const {
  activeTab, notifSetting, pendingCount, currentUser,
  fetchAll, fetchNotifSetting, connectSse, disconnectSse,
} = useAdmin();

// ── Tabs ──
const tabs = [
  { key: 'dashboard',    label: '대시보드',   desc: '오늘 예약 현황 및 통계',        icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="1" y="1" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="9" y="1" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="1" y="9" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="9" y="9" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/></svg>' },
  { key: 'rooms',        label: '회의실 관리', desc: '회의실 추가, 수정, 삭제',        icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="1" y="3" width="14" height="10" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M5 3V2M11 3V2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  { key: 'bookings',     label: '예약 관리',   desc: '전체 예약 조회 및 강제 취소',    icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="1" y="2" width="14" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M1 6h14M5 1v2M11 1v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  { key: 'users-all',      label: '전체 회원', desc: '전체 회원 조회 및 관리',        icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="5" cy="5" r="2.2" stroke="currentColor" stroke-width="1.5"/><path d="M1 13c0-2.5 1.8-4.5 4-4.5s4 2 4 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><circle cx="11.5" cy="5" r="2.2" stroke="currentColor" stroke-width="1.4"/><path d="M10 13c0-2.5 0.7-4.5 1.5-4.5s1.5 2 1.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>' },
  { key: 'users-pending',  label: '가입 대기', desc: '신규 가입 승인 처리',            icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="6" cy="5" r="2.5" stroke="currentColor" stroke-width="1.5"/><path d="M1 13c0-2.8 2.2-5 5-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><circle cx="12" cy="10" r="3.5" stroke="#f59e0b" stroke-width="1.4"/><path d="M12 8.5v2l1 1" stroke="#f59e0b" stroke-width="1.3" stroke-linecap="round"/></svg>' },
  { key: 'users-approved', label: '승인 회원', desc: '승인된 회원 관리',               icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="6" cy="5" r="2.5" stroke="currentColor" stroke-width="1.5"/><path d="M1 13c0-2.8 2.2-5 5-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><path d="M9.5 11l1.5 1.5L14 9" stroke="#10b981" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>' },
  { key: 'users-rejected', label: '거절 회원', desc: '가입 거절된 회원 조회',           icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="6" cy="5" r="2.5" stroke="currentColor" stroke-width="1.5"/><path d="M1 13c0-2.8 2.2-5 5-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><path d="M10 9l3.5 3.5M13.5 9L10 12.5" stroke="#ef4444" stroke-width="1.6" stroke-linecap="round"/></svg>' },
  { key: 'logs',         label: '관리 로그',   desc: '관리자 액션 이력',               icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M2 4h12M2 8h8M2 12h5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  { key: 'reset',        label: '초기화',      desc: '예약 및 로그 데이터 초기화',     icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M2 4h12M5 4V2.5h6V4M4 4l.5 9h7l.5-9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  { key: 'notification', label: '알림 설정',   desc: '알림 전송 시점 및 메시지 설정',  icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 1a5 5 0 0 1 5 5c0 2.5.5 3.5 1 4.5H2c.5-1 1-2 1-5a5 5 0 0 1 5-4.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/><path d="M6 11a2 2 0 0 0 4 0" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>' },
  { key: 'system',       label: '시스템 설정', desc: '가입 제어 · 점검 모드 · 관리자', icon: '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="2.5" stroke="currentColor" stroke-width="1.5"/><path d="M8 1v2M8 13v2M1 8h2M13 8h2M3 3l1.4 1.4M11.6 11.6L13 13M3 13l1.4-1.4M11.6 4.4L13 3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>' },
];

const navGroups = [
  { label: '관리',   keys: ['dashboard', 'rooms', 'bookings'] },
  { label: '회원',   keys: ['users-all', 'users-pending', 'users-approved', 'users-rejected'] },
  { label: '데이터', keys: ['logs', 'reset'] },
  { label: '설정',   keys: ['notification', 'system'] },
];

// ── UI state ──
const showDrawer = ref(true);
const isMobile   = ref(window.innerWidth < 768);
const roomsTabRef = ref(null);

const currentTab = computed(() => tabs.find(t => t.key === activeTab.value));

// ── Nav group accordion ──
const collapsedNavGroups = ref([]);
const toggleNavGroup     = (label) => {
  const idx = collapsedNavGroups.value.indexOf(label);
  if (idx === -1) collapsedNavGroups.value.push(label);
  else collapsedNavGroups.value.splice(idx, 1);
};
const isNavGroupExpanded = (label) => !collapsedNavGroups.value.includes(label);

// ── Global listeners ──
const onResize = () => { isMobile.value = window.innerWidth < 768; };
window.addEventListener('resize', onResize);

const logout = async () => {
  try { await api.post('/auth/logout'); } catch {}
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  window.location.href = '/';
};

onMounted(async () => {
  await fetchAll();
  await fetchNotifSetting();
  connectSse();
});

onUnmounted(() => {
  disconnectSse();
  window.removeEventListener('resize', onResize);
});
</script>

<style scoped>
.sidebar-panel { transition: width 0.28s cubic-bezier(0.32, 0.72, 0, 1); }
.sidebar-panel button:focus { outline: none; }
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
tbody tr { transition: background 0.12s; }
.toast-enter-active, .toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateY(12px); }
</style>
