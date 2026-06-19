<template>
  <div v-if="modelValue" class="modal-overlay" @click.self="$emit('update:modelValue', false)">
    <div class="modal-box">
      <div class="modal-head">
        <div>
          <div class="modal-title">{{ authStep === 1 ? '본인 확인' : '예약 등록' }}</div>
          <div class="modal-sub">{{ room?.name }}</div>
        </div>
        <button class="modal-close" @click="$emit('update:modelValue', false)">
          <AppIcon name="close" :size="22" />
        </button>
      </div>

      <div v-if="authStep === 1" class="modal-form">
        <div class="modal-field">
          <label>사번</label>
          <input v-model="authForm.employeeId" type="text" placeholder="사번을 입력하세요" autocomplete="off" />
        </div>
        <div class="modal-field">
          <label>비밀번호</label>
          <input v-model="authForm.password" type="password" placeholder="비밀번호를 입력하세요" autocomplete="off" />
        </div>
        <div v-if="authError" class="modal-error">{{ authError }}</div>
        <div class="modal-btns">
          <button type="button" class="btn-cancel" @click="$emit('update:modelValue', false)">취소</button>
          <button type="button" class="btn-confirm" :disabled="authLoading" @click="verifyUser">
            {{ authLoading ? '확인 중...' : '다음' }}
          </button>
        </div>
      </div>

      <form v-else @submit.prevent="submitBooking" class="modal-form">
        <div class="modal-field">
          <label>예약자</label>
          <div class="modal-static">{{ bookForm.organizer }}</div>
        </div>
        <div class="modal-row-2">
          <div class="modal-field">
            <label>시작 시간</label>
            <input v-model="bookForm.startStr" type="time" required />
          </div>
          <div class="modal-field">
            <label>종료 시간</label>
            <select v-model="bookForm.endStr" required>
              <option v-for="opt in endOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        <div class="modal-field">
          <label>회의 제목</label>
          <input v-model="bookForm.title" type="text" required />
        </div>
        <div v-if="bookError" class="modal-error">{{ bookError }}</div>
        <div class="modal-btns">
          <button type="button" class="btn-cancel" @click="authStep = 1">이전</button>
          <button type="submit" class="btn-confirm">예약 확정</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import axios from 'axios';
import AppIcon from '../icons/AppIcon.vue';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  room:       { type: Object,  default: null },
  now:        { type: Object,  required: true }, // dayjs instance
  roomId:     { type: Number,  required: true },
});

const emit = defineEmits(['update:modelValue', 'booked']);

const api = axios.create({ baseURL: '/api', headers: { 'Content-Type': 'application/json' } });

const authStep    = ref(1);
const authError   = ref('');
const authLoading = ref(false);
const bookError   = ref('');
const kioskToken  = ref('');
const authForm = reactive({ employeeId: '', password: '' });
const bookForm = reactive({ startStr: '', endStr: '', title: '', organizer: '' });

// Reset form when modal opens
watch(() => props.modelValue, (open) => {
  if (!open) return;
  bookForm.startStr = props.now.format('HH:mm');
  bookForm.endStr   = '';
  bookForm.title    = '';
  bookForm.organizer = '';
  bookError.value   = '';
  authForm.employeeId = '';
  authForm.password   = '';
  authError.value   = '';
  authStep.value    = 1;
  kioskToken.value  = '';
});

const endOptions = computed(() => {
  if (!bookForm.startStr) return [];
  const [h, m] = bookForm.startStr.split(':').map(Number);
  const opts = [];
  for (let add = 30; add <= 240; add += 30) {
    const total = h * 60 + m + add;
    const eh = Math.floor(total / 60);
    const em = total % 60;
    if (eh >= 21) break;
    const value = `${String(eh).padStart(2, '0')}:${String(em).padStart(2, '0')}`;
    const label = add < 60 ? `${add}분` : add % 60 === 0 ? `${add / 60}시간` : `${Math.floor(add / 60)}시간 ${add % 60}분`;
    opts.push({ value, label: `${value} (${label})` });
  }
  return opts;
});

const verifyUser = async () => {
  authError.value = '';
  if (!authForm.employeeId || !authForm.password) {
    authError.value = '사번과 비밀번호를 입력해 주세요.';
    return;
  }
  authLoading.value = true;
  try {
    const res = await api.post('/auth/login', {
      employeeId: authForm.employeeId,
      password: authForm.password,
      rememberMe: false,
    });
    kioskToken.value = res.data.token;
    const b64 = res.data.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(b64), c => c.charCodeAt(0))));
    bookForm.organizer = payload.name || authForm.employeeId;
    authStep.value = 2;
  } catch (e) {
    authError.value = e.response?.data || '인증에 실패했습니다.';
  } finally {
    authLoading.value = false;
  }
};

const submitBooking = async () => {
  bookError.value = '';
  if (!bookForm.endStr || bookForm.startStr >= bookForm.endStr) {
    bookError.value = '종료 시간은 시작 시간보다 뒤여야 합니다.';
    return;
  }
  try {
    const dateStr = props.now.format('YYYY-MM-DD');
    await api.post('/bookings', {
      roomId: props.roomId,
      title: bookForm.title,
      organizer: bookForm.organizer,
      startTime: `${dateStr}T${bookForm.startStr}:00`,
      endTime: `${dateStr}T${bookForm.endStr}:00`,
    }, { headers: { Authorization: `Bearer ${kioskToken.value}` } });
    emit('update:modelValue', false);
    emit('booked');
  } catch (e) {
    const msg = e.response?.data;
    bookError.value = typeof msg === 'string' ? msg : '이미 예약된 시간입니다.';
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0; z-index: 50;
  display: flex; align-items: center; justify-content: center; padding: 24px;
  background: rgba(15,23,42,0.42);
  backdrop-filter: blur(10px);
}
.modal-box {
  width: 100%; max-width: 560px; overflow: hidden;
  border-radius: 34px;
  background: rgba(255,255,255,0.96);
  box-shadow: 0 30px 90px rgba(15,23,42,0.24);
}
.modal-head {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding: 30px 34px 22px;
  border-bottom: 1px solid #edf2f7;
}
.modal-title { font-size: 28px; font-weight: 900; color: #0f172a; letter-spacing: -0.04em; }
.modal-sub { margin-top: 4px; font-size: 15px; font-weight: 700; color: #64748b; }
.modal-close {
  width: 46px; height: 46px; border: none;
  border-radius: 16px; background: #f8fafc; color: #64748b;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: background 0.16s ease, transform 0.16s ease;
}
.modal-close:hover { background: #e2e8f0; transform: translateY(-1px); }
.modal-form { display: flex; flex-direction: column; gap: 20px; padding: 26px 34px 32px; }
.modal-row-2 { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.modal-field { display: flex; flex-direction: column; gap: 8px; }
.modal-field label { font-size: 13px; font-weight: 800; color: #64748b; }
.modal-field input, .modal-field select, .modal-static {
  width: 100%; min-height: 58px; padding: 0 18px;
  border-radius: 18px; border: 1.5px solid #dbe4ee;
  background: #f8fbff;
  font-size: 20px; font-weight: 800; color: #0f172a;
  outline: none;
  transition: border-color 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
  box-sizing: border-box;
}
.modal-field input:focus, .modal-field select:focus {
  border-color: #10b981;
  box-shadow: 0 0 0 4px rgba(16,185,129,0.12);
  background: #ffffff;
}
.modal-static { display: flex; align-items: center; color: #475569; background: #f1f5f9; }
.modal-error {
  padding: 14px 16px; border-radius: 16px;
  border: 1px solid #fecaca; background: #fef2f2;
  color: #dc2626; font-size: 14px; font-weight: 700;
}
.modal-btns { display: flex; gap: 12px; margin-top: 4px; }
.btn-cancel, .btn-confirm {
  min-height: 60px; border: none; border-radius: 20px;
  font-size: 18px; font-weight: 900; cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
}
.btn-cancel { flex: 1; background: #edf2f7; color: #475569; }
.btn-cancel:hover { background: #e2e8f0; transform: translateY(-1px); }
.btn-confirm { flex: 1.6; background: linear-gradient(180deg, #10b981 0%, #059669 100%); color: #ffffff; box-shadow: 0 14px 30px rgba(16,185,129,0.22); }
.btn-confirm:hover { transform: translateY(-1px); box-shadow: 0 18px 34px rgba(16,185,129,0.26); }
.btn-confirm:disabled { cursor: wait; opacity: 0.74; }
</style>
