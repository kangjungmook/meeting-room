<template>
  <div class="flex flex-col gap-4">

    <!-- 점검 모드 경고 배너 -->
    <div v-if="notifSetting.maintenanceMode"
      class="flex items-center gap-3 px-5 py-4 bg-red-500 text-white rounded-2xl font-semibold text-[14px]">
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none" class="flex-shrink-0">
        <path d="M9 7v3M9 13h.01M3 15h12l-6-12L3 15z" stroke="white" stroke-width="1.8" stroke-linecap="round"/>
      </svg>
      점검 모드 활성화 중 — ADMIN 외 모든 API 요청이 차단됩니다.
    </div>

    <!-- 회원가입 제어 -->
    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
      <div class="px-6 py-5 border-b border-slate-100">
        <h2 class="text-[16px] font-bold text-slate-800">회원 가입 제어</h2>
        <p class="text-[13px] text-slate-600 mt-1">신규 가입을 허용할지 제어합니다.</p>
      </div>
      <div class="px-6 py-5 flex items-center justify-between gap-4">
        <div>
          <p class="text-[14px] font-semibold text-slate-700">자가 가입 허용</p>
          <p class="text-[13px] mt-0.5" :class="notifSetting.allowSelfRegistration ? 'text-slate-600' : 'text-amber-600 font-semibold'">
            {{ notifSetting.allowSelfRegistration
              ? '사용자가 직접 가입 신청을 할 수 있습니다.'
              : '⚠️ 자가 가입이 차단됩니다. 관리자가 직접 계정을 생성해야 합니다.' }}
          </p>
        </div>
        <button type="button" @click="notifSetting.allowSelfRegistration = !notifSetting.allowSelfRegistration"
          :class="['relative flex-shrink-0 w-12 h-6 rounded-full transition-colors duration-300 focus:outline-none',
            notifSetting.allowSelfRegistration ? 'bg-indigo-500' : 'bg-amber-400']">
          <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-md transition-all duration-300',
            notifSetting.allowSelfRegistration ? 'left-[26px]' : 'left-0.5']"></span>
        </button>
      </div>
    </div>

    <!-- 점검 모드 -->
    <div :class="['rounded-2xl border overflow-hidden transition-all',
      notifSetting.maintenanceMode ? 'bg-red-50/60 border-red-200' : 'bg-white border-slate-200']">
      <div class="px-6 py-5 border-b" :class="notifSetting.maintenanceMode ? 'border-red-100' : 'border-slate-100'">
        <h2 class="text-[16px] font-bold text-slate-800">점검 모드</h2>
        <p class="text-[13px] text-slate-600 mt-1">활성화 시 ADMIN을 제외한 모든 API 요청이 503으로 차단됩니다.</p>
      </div>
      <div class="px-6 py-5 flex items-center justify-between gap-4">
        <div>
          <p class="text-[14px] font-semibold" :class="notifSetting.maintenanceMode ? 'text-red-600' : 'text-slate-700'">
            점검 모드 {{ notifSetting.maintenanceMode ? '활성화됨' : '비활성화' }}
          </p>
          <p class="text-[13px] mt-0.5" :class="notifSetting.maintenanceMode ? 'text-red-500 font-semibold' : 'text-slate-600'">
            {{ notifSetting.maintenanceMode ? '⚠️ 일반 유저는 현재 시스템을 사용할 수 없습니다.' : '시스템이 정상 운영 중입니다.' }}
          </p>
        </div>
        <button type="button" @click="notifSetting.maintenanceMode = !notifSetting.maintenanceMode"
          :class="['relative flex-shrink-0 w-12 h-6 rounded-full transition-colors duration-300 focus:outline-none',
            notifSetting.maintenanceMode ? 'bg-red-500' : 'bg-slate-300']">
          <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-md transition-all duration-300',
            notifSetting.maintenanceMode ? 'left-[26px]' : 'left-0.5']"></span>
        </button>
      </div>
    </div>

    <!-- 관리자 계정 현황 -->
    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
      <div class="px-6 py-5 border-b border-slate-100 flex items-center justify-between">
        <div>
          <h2 class="text-[16px] font-bold text-slate-800">관리자 계정 현황</h2>
          <p class="text-[13px] text-slate-600 mt-1">현재 ADMIN 권한을 가진 계정 목록입니다.</p>
        </div>
        <button @click="openCreateAdmin"
          class="flex items-center gap-2 px-4 py-2.5 bg-violet-500 hover:bg-violet-600 text-white rounded-xl text-[13px] font-bold transition-all">
          <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><path d="M6.5 2v9M2 6.5h9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          관리자 추가
        </button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-100">
              <th class="text-left px-6 py-3.5 text-[12px] font-bold text-slate-700 uppercase tracking-wider">이름</th>
              <th class="text-left px-6 py-3.5 text-[12px] font-bold text-slate-700 uppercase tracking-wider">사번</th>
              <th class="text-left px-6 py-3.5 text-[12px] font-bold text-slate-700 uppercase tracking-wider">가입일</th>
              <th class="text-right px-6 py-3.5 text-[12px] font-bold text-slate-700 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="adminUsers.length === 0">
              <td colspan="4" class="py-10 text-center text-slate-500 text-[15px] font-semibold">관리자 계정이 없습니다</td>
            </tr>
            <tr v-for="u in adminUsers" :key="u.id" class="border-b border-slate-50 hover:bg-slate-50/60 transition-colors">
              <td class="px-6 py-3.5">
                <div class="flex items-center gap-2">
                  <div class="w-7 h-7 rounded-full bg-violet-100 flex items-center justify-center flex-shrink-0">
                    <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><circle cx="6.5" cy="4" r="2.5" stroke="#7c3aed" stroke-width="1.4"/><path d="M1 12c0-3 2.5-5 5.5-5s5.5 2 5.5 5" stroke="#7c3aed" stroke-width="1.4" stroke-linecap="round"/></svg>
                  </div>
                  <span class="text-[14px] font-bold text-slate-800">{{ u.name }}</span>
                  <span v-if="u.employeeId === currentUser.employeeId"
                    class="text-[11px] px-2 py-0.5 bg-indigo-100 text-indigo-600 rounded-full font-bold">나</span>
                </div>
              </td>
              <td class="px-6 py-3.5 text-[14px] font-mono text-slate-700">{{ u.employeeId }}</td>
              <td class="px-6 py-3.5 text-[13px] text-slate-600">{{ dayjs(u.createdAt).format('YYYY/MM/DD') }}</td>
              <td class="px-6 py-3.5 text-right">
                <button v-if="u.employeeId !== currentUser.employeeId"
                  @click="changeRole(u)"
                  class="px-3 py-1.5 rounded-lg text-[12px] font-semibold text-slate-700 hover:bg-slate-100 transition-all">권한 해제</button>
                <span v-else class="text-[12px] text-slate-300 font-semibold">현재 계정</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="flex justify-end">
      <button @click="saveSystemSetting"
        class="flex items-center gap-2 px-6 py-3 bg-indigo-500 hover:bg-indigo-600 text-white rounded-xl text-[14px] font-bold transition-all">
        <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M2 8l4 4 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        시스템 설정 저장
      </button>
    </div>

    <!-- 관리자 추가 모달 -->
    <div v-if="createAdminModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
          <h3 class="text-[17px] font-bold text-slate-800">관리자 계정 생성</h3>
          <button @click="createAdminModal.show = false" class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 transition-all">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <form @submit.prevent="submitCreateAdmin" class="px-6 py-5 flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">사번</label>
            <input v-model="createAdminModal.form.employeeId" type="text" required placeholder="예) EMP001"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">이름</label>
            <input v-model="createAdminModal.form.name" type="text" required placeholder="예) 홍길동"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">비밀번호</label>
            <input v-model="createAdminModal.form.password" type="password" required placeholder="4자 이상"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex gap-3 pt-1">
            <button type="button" @click="createAdminModal.show = false"
              class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
            <button type="submit"
              class="flex-[2] py-2.5 rounded-xl bg-violet-500 hover:bg-violet-600 text-white text-[14px] font-bold transition-all">생성</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useAdmin } from '../../composables/useAdmin';

const { notifSetting, adminUsers, currentUser, changeRole, createUser, saveSystemSetting, dayjs } = useAdmin();

const createAdminModal = reactive({
  show: false,
  form: { employeeId: '', name: '', password: '', role: 'ADMIN', status: 'APPROVED' },
});

const openCreateAdmin = () => {
  createAdminModal.form = { employeeId: '', name: '', password: '', role: 'ADMIN', status: 'APPROVED' };
  createAdminModal.show = true;
};

const submitCreateAdmin = async () => {
  try {
    if (createAdminModal.form.password.length < 4) { alert('비밀번호는 4자 이상이어야 합니다.'); return; }
    await createUser(createAdminModal.form);
    createAdminModal.show = false;
  } catch (e) {
    alert(e.response?.data?.message || '생성 중 오류가 발생했습니다.');
  }
};
</script>
