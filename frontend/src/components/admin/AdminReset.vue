<template>
  <div class="flex flex-col gap-4">

    <!-- 전체 초기화 배너 -->
    <div class="rounded-2xl border-2 border-red-200 bg-red-50/40 overflow-hidden">
      <div class="px-6 py-5 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div class="flex items-center gap-2 mb-1">
            <AppIcon name="alert-circle" :size="18" class="text-red-500" />
            <h2 class="text-[16px] font-black text-red-600">전체 초기화</h2>
          </div>
          <p class="text-[13px] text-slate-700 leading-relaxed">
            예약 · 로그 · 회원(ADMIN 제외) · 알림설정을 <strong class="text-red-500">모두 초기화</strong>합니다.<br/>
            현재: 예약 <strong>{{ allBookings.length }}건</strong> · 로그 <strong>{{ logs.length }}건</strong> · 일반회원 <strong>{{ nonAdminCount }}명</strong>
          </p>
        </div>
        <button @click="fullResetModal.show = true"
          class="flex-shrink-0 flex items-center gap-2 px-6 py-3 bg-red-500 hover:bg-red-600 active:bg-red-700 text-white rounded-xl text-[14px] font-black transition-all shadow-sm">
          <AppIcon name="trash" :size="16" />
          전체 초기화
        </button>
      </div>
    </div>

    <!-- 개별 초기화 그리드 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

      <!-- 예약 초기화 -->
      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-start gap-3">
          <div class="w-9 h-9 rounded-xl bg-red-50 flex items-center justify-center flex-shrink-0 mt-0.5">
            <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><rect x="1" y="3" width="16" height="13" rx="2" stroke="#ef4444" stroke-width="1.6"/><path d="M1 7h16M6 1v3M12 1v3" stroke="#ef4444" stroke-width="1.6" stroke-linecap="round"/></svg>
          </div>
          <div>
            <h2 class="text-[15px] font-bold text-slate-800">예약 데이터 초기화</h2>
            <p class="text-[12px] text-slate-500 mt-0.5">전체 예약 삭제 · PK 1부터 재시작</p>
          </div>
        </div>
        <div class="px-6 py-4 flex items-center justify-between">
          <div>
            <p class="text-[12px] font-semibold text-slate-400 uppercase tracking-wide">현재 예약 수</p>
            <p class="text-[28px] font-black text-slate-800 leading-none mt-1">{{ allBookings.length }}<span class="text-[14px] font-bold text-slate-400 ml-1">건</span></p>
          </div>
          <button @click="confirmReset('bookings')"
            class="flex items-center gap-2 px-4 py-2.5 bg-red-500 hover:bg-red-600 text-white rounded-xl text-[13px] font-bold transition-all">
            <AppIcon name="trash" :size="14" />
            초기화
          </button>
        </div>
      </div>

      <!-- 로그 초기화 -->
      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-start gap-3">
          <div class="w-9 h-9 rounded-xl bg-orange-50 flex items-center justify-center flex-shrink-0 mt-0.5">
            <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><path d="M3 5h12M3 9h8M3 13h5" stroke="#f97316" stroke-width="1.6" stroke-linecap="round"/></svg>
          </div>
          <div>
            <h2 class="text-[15px] font-bold text-slate-800">관리 로그 초기화</h2>
            <p class="text-[12px] text-slate-500 mt-0.5">전체 관리 로그 삭제 · PK 1부터 재시작</p>
          </div>
        </div>
        <div class="px-6 py-4 flex items-center justify-between">
          <div>
            <p class="text-[12px] font-semibold text-slate-400 uppercase tracking-wide">현재 로그 수</p>
            <p class="text-[28px] font-black text-slate-800 leading-none mt-1">{{ logs.length }}<span class="text-[14px] font-bold text-slate-400 ml-1">건</span></p>
          </div>
          <button @click="confirmReset('logs')"
            class="flex items-center gap-2 px-4 py-2.5 bg-orange-500 hover:bg-orange-600 text-white rounded-xl text-[13px] font-bold transition-all">
            <AppIcon name="trash" :size="14" />
            초기화
          </button>
        </div>
      </div>

      <!-- 회원 초기화 -->
      <div class="bg-white rounded-2xl border border-red-100 overflow-hidden">
        <div class="px-6 py-4 border-b border-red-50 bg-red-50/30 flex items-start gap-3">
          <div class="w-9 h-9 rounded-xl bg-red-100 flex items-center justify-center flex-shrink-0 mt-0.5">
            <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#ef4444" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#ef4444" stroke-width="1.6" stroke-linecap="round"/></svg>
          </div>
          <div>
            <h2 class="text-[15px] font-bold text-slate-800">회원 초기화</h2>
            <p class="text-[12px] text-slate-500 mt-0.5">ADMIN 제외 전체 회원 삭제 · 알림설정 함께 삭제</p>
          </div>
        </div>
        <div class="px-6 py-4 flex items-center justify-between">
          <div>
            <p class="text-[12px] font-semibold text-slate-400 uppercase tracking-wide">일반 회원 수</p>
            <p class="text-[28px] font-black text-slate-800 leading-none mt-1">{{ nonAdminCount }}<span class="text-[14px] font-bold text-slate-400 ml-1">명</span></p>
          </div>
          <button @click="confirmReset('users')"
            class="flex items-center gap-2 px-4 py-2.5 bg-red-500 hover:bg-red-600 text-white rounded-xl text-[13px] font-bold transition-all">
            <AppIcon name="trash" :size="14" />
            초기화
          </button>
        </div>
      </div>

      <!-- 알림/시스템 설정 초기화 -->
      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-start gap-3">
          <div class="w-9 h-9 rounded-xl bg-indigo-50 flex items-center justify-center flex-shrink-0 mt-0.5">
            <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><path d="M9 1v2M9 15v2M1 9h2M15 9h2M3.2 3.2l1.4 1.4M13.4 13.4l1.4 1.4M3.2 14.8l1.4-1.4M13.4 4.6l1.4-1.4" stroke="#6366f1" stroke-width="1.6" stroke-linecap="round"/><circle cx="9" cy="9" r="3" stroke="#6366f1" stroke-width="1.6"/></svg>
          </div>
          <div>
            <h2 class="text-[15px] font-bold text-slate-800">알림 / 시스템 설정 초기화</h2>
            <p class="text-[12px] text-slate-500 mt-0.5">알림 템플릿 · 점검모드 · 자가가입 설정을 기본값으로 복원</p>
          </div>
        </div>
        <div class="px-6 py-4 flex items-center justify-between">
          <div class="flex flex-col gap-1">
            <div class="flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full" :class="notifSetting.maintenanceMode ? 'bg-red-400' : 'bg-emerald-400'"></span>
              <p class="text-[12px] text-slate-500">점검모드: <span class="font-bold" :class="notifSetting.maintenanceMode ? 'text-red-500' : 'text-emerald-500'">{{ notifSetting.maintenanceMode ? 'ON' : 'OFF' }}</span></p>
            </div>
            <div class="flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full" :class="notifSetting.allowSelfRegistration ? 'bg-emerald-400' : 'bg-red-400'"></span>
              <p class="text-[12px] text-slate-500">자가가입: <span class="font-bold" :class="notifSetting.allowSelfRegistration ? 'text-emerald-500' : 'text-red-500'">{{ notifSetting.allowSelfRegistration ? '허용' : '차단' }}</span></p>
            </div>
          </div>
          <button @click="confirmReset('notif')"
            class="flex items-center gap-2 px-4 py-2.5 bg-indigo-500 hover:bg-indigo-600 text-white rounded-xl text-[13px] font-bold transition-all">
            <svg width="14" height="14" viewBox="0 0 15 15" fill="none"><path d="M13 7.5A5.5 5.5 0 0 1 2.5 10M2 7.5A5.5 5.5 0 0 1 12.5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><path d="M12 2.5l.5 2.5H10M3 12.5L2.5 10H5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            기본값 복원
          </button>
        </div>
      </div>

    </div>

    <!-- 단일 초기화 확인 모달 -->
    <div v-if="resetModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="px-7 pt-7 pb-5">
          <div class="w-12 h-12 rounded-2xl flex items-center justify-center mb-4"
               :class="resetModal.target === 'notif' ? 'bg-indigo-50' : 'bg-red-50'">
            <AppIcon name="alert-circle" :size="22" :class="resetModal.target === 'notif' ? 'text-indigo-500' : 'text-red-500'" />
          </div>
          <h3 class="text-[17px] font-bold text-slate-800">
            {{ resetTargetLabel }}을 초기화할까요?
          </h3>
          <p class="text-[14px] text-slate-600 mt-2 leading-relaxed">
            <template v-if="resetModal.target === 'users'">ADMIN을 제외한 전체 회원과 알림 설정이 삭제됩니다.</template>
            <template v-else-if="resetModal.target === 'notif'">알림/시스템 설정이 기본값으로 복원됩니다.</template>
            <template v-else>모든 {{ resetModal.target === 'bookings' ? '예약' : '로그' }} 데이터가 삭제되고 PK도 1부터 다시 시작합니다.</template>
            <span v-if="resetModal.target !== 'notif'" class="text-red-500 font-bold"> 되돌릴 수 없습니다.</span>
          </p>
        </div>
        <div class="flex gap-3 px-7 pb-7">
          <button @click="resetModal.show = false"
            class="flex-1 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
          <button @click="doReset" :disabled="isResetting"
            class="flex-1 py-3 rounded-xl text-white text-[14px] font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            :class="resetModal.target === 'notif' ? 'bg-indigo-500 hover:bg-indigo-600' : 'bg-red-500 hover:bg-red-600'">
            {{ isResetting ? '처리 중...' : (resetModal.target === 'notif' ? '복원' : '초기화') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 전체 초기화 확인 모달 -->
    <div v-if="fullResetModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-2xl border border-red-200 overflow-hidden">
        <div class="bg-red-500 px-6 py-5 flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center flex-shrink-0">
            <AppIcon name="alert-circle" :size="20" class="text-white" />
          </div>
          <div>
            <h3 class="text-[17px] font-black text-white">전체 초기화</h3>
            <p class="text-[12px] text-red-100 mt-0.5">이 작업은 되돌릴 수 없습니다</p>
          </div>
        </div>
        <div class="px-6 py-5">
          <p class="text-[14px] text-slate-600 leading-relaxed mb-4">아래 항목이 <strong class="text-red-500">모두 초기화</strong>됩니다:</p>
          <div class="flex flex-col gap-2 mb-5">
            <div class="flex items-center gap-2.5 px-3 py-2.5 bg-red-50 rounded-xl">
              <AppIcon name="trash" :size="14" class="text-red-500" />
              <span class="text-[13px] font-semibold text-red-600">예약 전체</span>
              <span class="ml-auto text-[12px] text-red-400 font-bold">{{ allBookings.length }}건</span>
            </div>
            <div class="flex items-center gap-2.5 px-3 py-2.5 bg-red-50 rounded-xl">
              <AppIcon name="trash" :size="14" class="text-red-500" />
              <span class="text-[13px] font-semibold text-red-600">관리 로그 전체</span>
              <span class="ml-auto text-[12px] text-red-400 font-bold">{{ logs.length }}건</span>
            </div>
            <div class="flex items-center gap-2.5 px-3 py-2.5 bg-red-50 rounded-xl">
              <AppIcon name="trash" :size="14" class="text-red-500" />
              <span class="text-[13px] font-semibold text-red-600">일반 회원 전체</span>
              <span class="ml-auto text-[12px] text-red-400 font-bold">{{ nonAdminCount }}명</span>
            </div>
            <div class="flex items-center gap-2.5 px-3 py-2.5 bg-orange-50 rounded-xl">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 1v1.5M7 11.5V13M1 7h1.5M11.5 7H13M2.6 2.6l1.1 1.1M10.3 10.3l1.1 1.1M2.6 11.4l1.1-1.1M10.3 3.7l1.1-1.1" stroke="#f97316" stroke-width="1.3" stroke-linecap="round"/><circle cx="7" cy="7" r="2.5" stroke="#f97316" stroke-width="1.3"/></svg>
              <span class="text-[13px] font-semibold text-orange-600">알림/시스템 설정</span>
              <span class="ml-auto text-[12px] text-orange-400 font-bold">기본값 복원</span>
            </div>
            <div class="flex items-center gap-2.5 px-3 py-2.5 bg-emerald-50 rounded-xl">
              <AppIcon name="check" :size="14" class="text-emerald-500" />
              <span class="text-[13px] font-semibold text-emerald-600">ADMIN 계정 · 회의실</span>
              <span class="ml-auto text-[12px] text-emerald-500 font-bold">유지됨</span>
            </div>
          </div>
          <label class="block text-[12px] font-bold text-slate-700 mb-1.5">확인을 위해 <code class="bg-red-50 text-red-500 px-1.5 py-0.5 rounded">전체초기화</code> 를 입력하세요</label>
          <input v-model="fullResetModal.confirmText" type="text" placeholder="전체초기화"
            class="w-full border-2 rounded-xl px-4 py-2.5 text-[14px] font-bold outline-none transition-all"
            :class="fullResetModal.confirmText === '전체초기화' ? 'border-red-400 bg-red-50 text-red-600' : 'border-slate-200 text-slate-700 focus:border-red-300'" />
        </div>
        <div class="flex gap-3 px-6 pb-6">
          <button @click="fullResetModal.show = false; fullResetModal.confirmText = ''"
            class="flex-1 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
          <button @click="submitFullReset" :disabled="fullResetModal.confirmText !== '전체초기화' || isResetting"
            class="flex-1 py-3 rounded-xl bg-red-500 hover:bg-red-600 disabled:opacity-30 disabled:cursor-not-allowed text-white text-[14px] font-black transition-all">
            {{ isResetting ? '처리 중...' : '전체 초기화' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive, computed, ref } from 'vue';
import { useAdmin } from '../../composables/useAdmin';
import { showAdminToast } from '../../composables/admin/useAdminToast';
import AppIcon from '../icons/AppIcon.vue';

const {
  allBookings, logs, users, notifSetting,
  resetBookings, resetLogs, resetUsers, resetNotifSetting, doFullReset,
} = useAdmin();

const nonAdminCount = computed(() => users.value.filter(u => u.role !== 'ADMIN').length);

const isResetting    = ref(false);
const resetModal     = reactive({ show: false, target: '' });
const fullResetModal = reactive({ show: false, confirmText: '' });

const resetTargetLabel = computed(() => ({
  bookings: '예약 데이터',
  logs:     '관리 로그',
  users:    '회원',
  notif:    '알림/시스템 설정',
}[resetModal.target] ?? ''));

const confirmReset = (target) => {
  resetModal.target = target;
  resetModal.show   = true;
};

const doReset = async () => {
  if (isResetting.value) return;
  isResetting.value = true;
  try {
    if (resetModal.target === 'bookings') await resetBookings();
    else if (resetModal.target === 'logs')  await resetLogs();
    else if (resetModal.target === 'users') await resetUsers();
    else if (resetModal.target === 'notif') await resetNotifSetting();
    resetModal.show = false;
    showAdminToast('초기화가 완료되었습니다.');
  } catch {
    showAdminToast('초기화 중 오류가 발생했습니다.', 'error');
  } finally {
    isResetting.value = false;
  }
};

const submitFullReset = async () => {
  if (fullResetModal.confirmText !== '전체초기화' || isResetting.value) return;
  isResetting.value = true;
  try {
    await doFullReset();
    fullResetModal.show        = false;
    fullResetModal.confirmText = '';
    showAdminToast('전체 초기화가 완료되었습니다.');
  } catch {
    showAdminToast('초기화 중 오류가 발생했습니다.', 'error');
  } finally {
    isResetting.value = false;
  }
};
</script>
