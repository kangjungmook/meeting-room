<template>
  <div class="min-h-screen bg-slate-100 flex items-center justify-center p-4">
    <div class="w-full max-w-sm">

      <div class="text-center mb-8">
        <div class="w-14 h-14 rounded-2xl bg-amber-500 flex items-center justify-center mx-auto mb-4 shadow-lg shadow-amber-200">
          <AppIcon name="lock" :size="26" class="text-white" />
        </div>
        <h1 class="text-[22px] font-black text-slate-800">비밀번호 변경 필요</h1>
        <p class="text-[14px] text-slate-400 mt-2 leading-relaxed">
          관리자에 의해 비밀번호가 초기화되었습니다.<br>새 비밀번호를 설정해주세요.
        </p>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 flex flex-col gap-4">
        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-500">새 비밀번호</label>
          <div class="relative">
            <input v-model="newPw" :type="showNew ? 'text' : 'password'" placeholder="새 비밀번호 입력" required
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 pr-12 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent transition-all placeholder:text-slate-300" />
            <button type="button" @click="showNew = !showNew"
              class="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center text-slate-400 hover:text-slate-600">
              <AppIcon :name="showNew ? 'eye-off' : 'eye'" :size="18" />
            </button>
          </div>
          <p class="text-[11px] text-slate-400 mt-1.5 leading-relaxed">
            6자 이상 · 대문자 · 소문자 · 숫자 · 특수문자 각 1개 이상
          </p>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-500">새 비밀번호 확인</label>
          <div class="relative">
            <input v-model="confirmPw" :type="showConfirm ? 'text' : 'password'" placeholder="비밀번호 재입력" required
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 pr-12 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent transition-all placeholder:text-slate-300" />
            <button type="button" @click="showConfirm = !showConfirm"
              class="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center text-slate-400 hover:text-slate-600">
              <AppIcon :name="showConfirm ? 'eye-off' : 'eye'" :size="18" />
            </button>
          </div>
        </div>

        <div v-if="errorMsg" class="flex items-center gap-2 px-4 py-3 bg-red-50 border border-red-100 rounded-xl">
          <AppIcon name="alert-circle" :size="14" cls="flex-shrink-0 text-red-500" />
          <span class="text-[13px] font-semibold text-red-500">{{ errorMsg }}</span>
        </div>

        <button @click="submit"
          class="w-full py-3.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[15px] font-bold transition-all mt-1">
          비밀번호 변경
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api';
import AppIcon from './icons/AppIcon.vue';

const router   = useRouter();
const newPw    = ref('');
const confirmPw = ref('');
const showNew   = ref(false);
const showConfirm = ref(false);
const errorMsg  = ref('');

const submit = async () => {
  errorMsg.value = '';
  if (!newPw.value || newPw.value.length < 6) {
    errorMsg.value = '비밀번호는 6자 이상 입력해주세요.';
    return;
  }
  if (!/[A-Z]/.test(newPw.value)) { errorMsg.value = '대문자를 1개 이상 포함해야 합니다.'; return; }
  if (!/[a-z]/.test(newPw.value)) { errorMsg.value = '소문자를 1개 이상 포함해야 합니다.'; return; }
  if (!/[0-9]/.test(newPw.value)) { errorMsg.value = '숫자를 1개 이상 포함해야 합니다.'; return; }
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?`~]/.test(newPw.value)) { errorMsg.value = '특수문자를 1개 이상 포함해야 합니다.'; return; }
  if (newPw.value !== confirmPw.value) {
    errorMsg.value = '비밀번호가 일치하지 않습니다.';
    return;
  }
  try {
    await api.post('/auth/change-password', { newPassword: newPw.value });
    sessionStorage.removeItem('passwordResetRequired');
    router.push('/main');
  } catch (e) {
    errorMsg.value = e.response?.data || '변경에 실패했습니다.';
  }
};
</script>
