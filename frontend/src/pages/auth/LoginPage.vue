<template>
  <div class="min-h-screen bg-white flex items-center justify-center px-5 py-4 sm:p-6">
    <div class="w-full max-w-[360px] flex flex-col gap-4 sm:gap-7">

        <!-- 브랜드 -->
        <div class="flex flex-col items-center gap-2 sm:gap-3">
          <div class="text-center">
            <h1 class="text-[18px] sm:text-[22px] font-bold text-gray-900 tracking-tight">회의실 예약 시스템</h1>
            <p class="hidden sm:block text-[14px] text-gray-400 mt-1">{{ mode === 'login' ? '계정에 로그인하세요' : '새 계정을 만드세요' }}</p>
          </div>
        </div>

        <!-- 탭 -->
        <div class="flex border-b border-gray-100">
          <button @click="mode = 'login'; errorMsg = ''; rejectedReason = null"
            :class="['flex-1 pb-3 text-[14px] font-semibold transition-all border-b-2 -mb-px',
              mode === 'login' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-400 hover:text-gray-600']">
            로그인
          </button>
          <button @click="mode = 'register'; errorMsg = ''"
            :class="['flex-1 pb-3 text-[14px] font-semibold transition-all border-b-2 -mb-px',
              mode === 'register' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-400 hover:text-gray-600']">
            회원가입
          </button>
        </div>

        <!-- ── 로그인 폼 ── -->
        <form v-if="mode === 'login'" @submit.prevent="submitLogin" class="flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-medium text-gray-600">사번</label>
            <input v-model="form.employeeId" type="text" placeholder="사번을 입력하세요" required
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-[15px] text-gray-900 outline-none
                     focus:border-indigo-500 focus:ring-4 focus:ring-indigo-50 transition-all placeholder:text-gray-300" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-medium text-gray-600">비밀번호</label>
            <div class="relative">
              <input v-model="form.password" :type="showPw ? 'text' : 'password'" placeholder="비밀번호를 입력하세요" required
                class="w-full border border-gray-200 rounded-xl px-4 py-3 pr-12 text-[15px] text-gray-900 outline-none
                       focus:border-indigo-500 focus:ring-4 focus:ring-indigo-50 transition-all placeholder:text-gray-300" />
              <button type="button" @click="showPw = !showPw"
                class="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center text-gray-300 hover:text-gray-500 transition-colors">
                <AppIcon :name="showPw ? 'eye-off' : 'eye'" :size="18" />
              </button>
            </div>
          </div>

          <div v-if="errorMsg" class="flex items-start gap-2.5 px-4 py-3 bg-red-50 rounded-xl">
            <AppIcon name="alert-circle" :size="15" cls="flex-shrink-0 mt-px text-red-400" />
            <span class="text-[13px] text-red-500 leading-snug">{{ errorMsg }}</span>
          </div>

          <div v-if="rejectedReason !== null" class="px-4 py-3.5 bg-red-50 rounded-xl flex flex-col gap-1.5">
            <p class="text-[13px] font-semibold text-red-600">가입이 거절되었습니다</p>
            <p v-if="rejectedReason" class="text-[12px] text-red-500">사유: {{ rejectedReason }}</p>
            <p class="text-[12px] text-red-400">관리자에게 문의하세요.</p>
          </div>

          <button type="submit"
            class="w-full py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-[0.99] text-white text-[15px] font-semibold transition-all shadow-sm mt-1">
            로그인
          </button>
        </form>

        <!-- ── 회원가입 폼 ── -->
        <form v-else @submit.prevent="submitRegister" class="flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-medium text-gray-600">사번</label>
            <input v-model="form.employeeId" type="text" placeholder="사번을 입력하세요" required
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-[15px] text-gray-900 outline-none
                     focus:border-indigo-500 focus:ring-4 focus:ring-indigo-50 transition-all placeholder:text-gray-300" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-medium text-gray-600">이름</label>
            <input v-model="form.name" type="text" placeholder="이름을 입력하세요" required
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-[15px] text-gray-900 outline-none
                     focus:border-indigo-500 focus:ring-4 focus:ring-indigo-50 transition-all placeholder:text-gray-300" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-medium text-gray-600">비밀번호</label>
            <div class="relative">
              <input v-model="form.password" :type="showPw ? 'text' : 'password'" placeholder="비밀번호를 입력하세요" required
                class="w-full border border-gray-200 rounded-xl px-4 py-3 pr-12 text-[15px] text-gray-900 outline-none
                       focus:border-indigo-500 focus:ring-4 focus:ring-indigo-50 transition-all placeholder:text-gray-300" />
              <button type="button" @click="showPw = !showPw"
                class="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center text-gray-300 hover:text-gray-500 transition-colors">
                <AppIcon :name="showPw ? 'eye-off' : 'eye'" :size="18" />
              </button>
            </div>
            <p class="text-[12px] text-gray-400 mt-0.5">6자 이상 · 대문자 · 소문자 · 숫자 · 특수문자 각 1개 이상</p>
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-medium text-gray-600">비밀번호 확인</label>
            <div class="relative">
              <input v-model="form.confirmPassword" :type="showPw ? 'text' : 'password'" placeholder="비밀번호를 다시 입력하세요" required
                :class="['w-full border rounded-xl px-4 py-3 pr-12 text-[15px] text-gray-900 outline-none transition-all placeholder:text-gray-300',
                  form.confirmPassword && form.confirmPassword !== form.password
                    ? 'border-red-300 focus:border-red-400 focus:ring-4 focus:ring-red-50'
                    : 'border-gray-200 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-50']" />
              <div v-if="form.confirmPassword && form.confirmPassword === form.password"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-emerald-500">
                <AppIcon name="check" :size="16" />
              </div>
            </div>
            <p v-if="form.confirmPassword && form.confirmPassword !== form.password"
               class="text-[12px] text-red-400">비밀번호가 일치하지 않습니다</p>
          </div>

          <div v-if="errorMsg" class="flex items-start gap-2.5 px-4 py-3 bg-red-50 rounded-xl">
            <AppIcon name="alert-circle" :size="15" cls="flex-shrink-0 mt-px text-red-400" />
            <span class="text-[13px] text-red-500 leading-snug">{{ errorMsg }}</span>
          </div>
          <div v-if="successMsg" class="flex items-start gap-2.5 px-4 py-3 bg-emerald-50 rounded-xl">
            <AppIcon name="check" :size="15" cls="flex-shrink-0 mt-px text-emerald-400" />
            <span class="text-[13px] text-emerald-600 leading-snug">{{ successMsg }}</span>
          </div>

          <button type="submit"
            class="w-full py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-[0.99] text-white text-[15px] font-semibold transition-all shadow-sm mt-1">
            회원가입
          </button>
        </form>

        <!-- 키오스크 (모바일 숨김) -->
        <div class="hidden sm:block pt-2 border-t border-gray-100 text-center">
          <button @click="enterKioskMode"
            class="inline-flex items-center gap-1.5 text-[13px] text-gray-400 hover:text-gray-600 transition-colors">
            <AppIcon name="monitor" :size="13" />
            키오스크 모드로 입장
          </button>
        </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import api from '../../api';
import AppIcon from '../../components/icons/AppIcon.vue';
import { refreshCurrentUser } from '../../composables/useApp';

const router = useRouter();

const enterKioskMode = () => {
  document.documentElement.requestFullscreen?.();
  router.push('/kiosk');
};
const mode = ref('login');
const showPw = ref(false);
const errorMsg = ref('');
const successMsg = ref('');
const rejectedReason = ref(null);

const form = reactive({
  employeeId: '',
  password: '',
  confirmPassword: '',
  name: '',
});

// ── 폼 초기화 ──────────────────────────────────────────────
const resetForm = () => {
  form.employeeId = '';
  form.password = '';
  form.confirmPassword = '';
  form.name = '';
  errorMsg.value = '';
  successMsg.value = '';
};

// ── 로그인 ─────────────────────────────────────────────────
const submitLogin = async () => {
  errorMsg.value = '';
  rejectedReason.value = null;
  try {
    const res = await api.post('/auth/login', {
      employeeId: form.employeeId,
      password: form.password,
    });
    localStorage.setItem('token', res.data.token);
    refreshCurrentUser();
    sessionStorage.setItem('visited', 'true');
    sessionStorage.setItem('adminAuth', 'true');

    localStorage.setItem('refreshToken', res.data.refreshToken);

    if (res.data.passwordResetRequired) {
      sessionStorage.setItem('passwordResetRequired', 'true');
      router.push('/change-password');
      return;
    } else {
      sessionStorage.removeItem('passwordResetRequired');
    }

    const b64 = res.data.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(b64), c => c.charCodeAt(0));
    const payload = JSON.parse(new TextDecoder().decode(bytes));
    router.push(payload.role === 'ADMIN' ? '/admin' : '/main');
  } catch (e) {
    const data = e.response?.data;
    if (data?.rejected) {
      rejectedReason.value = data.reason || '';
    } else {
      errorMsg.value = (typeof data === 'string' ? data : data?.message) || '로그인에 실패했습니다.';
    }
  }
};

// ── 회원가입 ───────────────────────────────────────────────
const submitRegister = async () => {
  errorMsg.value = '';
  successMsg.value = '';
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '비밀번호가 일치하지 않습니다.';
    return;
  }
  try {
    await api.post('/auth/register', {
      employeeId: form.employeeId,
      password: form.password,
      name: form.name,
    });
    successMsg.value = '회원가입 완료! 관리자 승인을 기다려주세요.';
    resetForm();
    mode.value = 'login';
  } catch (e) {
    const data = e.response?.data;
    errorMsg.value = (typeof data === 'object' ? data?.message : data) || '회원가입에 실패했습니다.';
  }
};
</script>
