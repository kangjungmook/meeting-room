<template>
  <div class="min-h-screen bg-slate-100 flex items-center justify-center p-4">
    <div class="w-full max-w-sm">

      <div class="text-center mb-8">
        <div class="w-14 h-14 rounded-2xl bg-amber-500 flex items-center justify-center mx-auto mb-4 shadow-lg shadow-amber-200">
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none">
            <path d="M12 2a5 5 0 0 1 5 5v2H7V7a5 5 0 0 1 5-5zM7 9h10a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2zm5 4a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3z" fill="white"/>
          </svg>
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
              <svg v-if="!showNew" width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M1 9s3-6 8-6 8 6 8 6-3 6-8 6-8-6-8-6z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="9" cy="9" r="2.5" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M1 1l16 16M7.5 7.6A2.5 2.5 0 0 0 11.4 11M5 4.1C2.9 5.5 1 9 1 9s3 6 8 6c1.5 0 2.9-.4 4.1-1.1M8 3.1A7.3 7.3 0 0 1 9 3c5 0 8 6 8 6a13.5 13.5 0 0 1-1.7 2.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
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
              <svg v-if="!showConfirm" width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M1 9s3-6 8-6 8 6 8 6-3 6-8 6-8-6-8-6z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="9" cy="9" r="2.5" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M1 1l16 16M7.5 7.6A2.5 2.5 0 0 0 11.4 11M5 4.1C2.9 5.5 1 9 1 9s3 6 8 6c1.5 0 2.9-.4 4.1-1.1M8 3.1A7.3 7.3 0 0 1 9 3c5 0 8 6 8 6a13.5 13.5 0 0 1-1.7 2.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <div v-if="errorMsg" class="flex items-center gap-2 px-4 py-3 bg-red-50 border border-red-100 rounded-xl">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="flex-shrink-0">
            <path d="M7 4v3M7 9h.01M1 7a6 6 0 1 0 12 0A6 6 0 0 0 1 7z" stroke="#ef4444" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
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
    router.push('/main');
  } catch (e) {
    errorMsg.value = e.response?.data || '변경에 실패했습니다.';
  }
};
</script>
