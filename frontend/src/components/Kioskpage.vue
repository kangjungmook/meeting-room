<template>
  <div class="kiosk-root">

    <!-- ── 헤더 ── -->
    <header class="kiosk-header">
      <div class="flex items-center gap-3">
        <button @click="exitKiosk" class="back-btn">
          <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
            <path d="M11 3L5 9l6 6" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div>
          <div class="room-name">{{ room?.name ?? '...' }}</div>
          <div class="room-cap">수용 {{ room?.capacity }}인</div>
        </div>
      </div>
      <div class="clock-block">
        <div class="clock-time">{{ clockTime }}</div>
        <div class="clock-date">{{ clockDate }}</div>
      </div>
    </header>

    <!-- ── 본문 ── -->
    <div class="kiosk-body">

      <!-- LEFT: 상태 히어로 -->
      <div class="hero-panel" :class="isInUse ? 'hero-red' : 'hero-green'">

        <!-- 사용 가능 -->
        <template v-if="!isInUse">
          <div class="hero-icon-wrap">
            <svg width="48" height="48" viewBox="0 0 56 56" fill="none">
              <path d="M12 28l12 12 20-20" stroke="white" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="hero-status-text">사용 가능</div>
          <div class="hero-sub" v-if="nextBooking">
            다음 예약 <strong>{{ dayjs(nextBooking.startTime).format('HH:mm') }}</strong> · {{ nextBooking.title }}
            <span class="hero-sub-muted"> ({{ timeUntilNext }})</span>
          </div>
          <div class="hero-sub" v-else>오늘 남은 예약이 없습니다</div>
          <button @click="openBooking" class="hero-book-btn">
            <svg width="18" height="18" viewBox="0 0 20 20" fill="none">
              <path d="M10 3v14M3 10h14" stroke="currentColor" stroke-width="2.6" stroke-linecap="round"/>
            </svg>
            지금 바로 예약
          </button>
        </template>

        <!-- 사용 중 -->
        <template v-else>
          <div class="hero-icon-wrap">
            <svg width="42" height="42" viewBox="0 0 56 56" fill="none">
              <rect x="10" y="10" width="13" height="36" rx="4" fill="white"/>
              <rect x="33" y="10" width="13" height="36" rx="4" fill="white"/>
            </svg>
          </div>
          <div class="hero-status-text">사용 중</div>
          <div class="hero-meeting-title">{{ currentBooking?.title }}</div>
          <div class="hero-organizer">{{ currentBooking?.organizer }}</div>

          <div class="hero-countdown-wrap">
            <div class="hero-countdown-label">종료까지</div>
            <div class="hero-countdown">{{ timeUntilEnd }}</div>
            <div class="hero-progress-track">
              <div class="hero-progress-bar" :style="{ width: progressPct + '%' }"></div>
            </div>
            <div class="hero-progress-times">
              <span>{{ dayjs(currentBooking?.startTime).format('HH:mm') }}</span>
              <span>{{ dayjs(currentBooking?.endTime).format('HH:mm') }}</span>
            </div>
          </div>

          <button @click="showEndConfirm = true" class="hero-end-btn">
            <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
              <rect x="1" y="1" width="12" height="12" rx="2.5" fill="currentColor"/>
            </svg>
            지금 종료
          </button>
        </template>
      </div>

      <!-- RIGHT: 일정 패널 -->
      <div class="schedule-panel">
        <div class="schedule-header">
          <div class="schedule-title">오늘의 일정</div>
          <div class="schedule-count">{{ todayBookings.length }}건</div>
        </div>

        <div class="schedule-list">
          <div v-if="todayBookings.length === 0" class="schedule-empty">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none" style="margin:0 auto 10px;display:block;opacity:0.25">
              <rect x="2" y="6" width="28" height="24" rx="4" stroke="#64748b" stroke-width="2"/>
              <path d="M2 12h28M10 2v5M22 2v5" stroke="#64748b" stroke-width="2" stroke-linecap="round"/>
            </svg>
            오늘 예약된 일정이 없습니다
          </div>

          <div v-for="b in todayBookings" :key="b.id"
            class="schedule-item"
            :class="{
              'sitem-done': getState(b) === 'done',
              'sitem-ongoing': getState(b) === 'ongoing'
            }">
            <div class="sitem-bar" :class="{
              'sbar-ongoing': getState(b) === 'ongoing',
              'sbar-upcoming': getState(b) === 'upcoming',
              'sbar-done': getState(b) === 'done'
            }"></div>

            <div class="sitem-time">
              <div class="sitem-start">{{ dayjs(b.startTime).format('HH:mm') }}</div>
              <div class="sitem-end">{{ dayjs(b.endTime).format('HH:mm') }}</div>
            </div>

            <div class="sitem-info">
              <div class="sitem-title">{{ b.title }}</div>
              <div class="sitem-org">{{ b.organizer }}</div>
            </div>

            <div class="sitem-badge" :class="{
              'sbadge-ongoing': getState(b) === 'ongoing',
              'sbadge-done': getState(b) === 'done',
              'sbadge-upcoming': getState(b) === 'upcoming'
            }">
              {{ getState(b) === 'ongoing' ? '진행 중' : getState(b) === 'done' ? '종료' : '예정' }}
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- ── 타임라인 ── -->
    <section class="timeline-section">
      <div class="timeline-labels">
        <span v-for="h in [6, 9, 12, 15, 18, 21]" :key="h">{{ String(h).padStart(2, '0') }}</span>
      </div>
      <div class="timeline-track">
        <div v-for="(slot, i) in timelineSlots" :key="i"
          class="timeline-block"
          :class="{
            'tblock-ongoing': slot.state === 'ongoing',
            'tblock-done': slot.state === 'done',
            'tblock-upcoming': slot.state === 'upcoming'
          }"
          :style="{ left: slot.left, width: slot.width }">
        </div>
        <div class="now-line" :style="{ left: nowLineLeft }"></div>
      </div>
    </section>

    <!-- ── 종료 확인 모달 ── -->
    <div v-if="showEndConfirm" class="modal-overlay" @click.self="showEndConfirm = false">
      <div class="modal-box">
        <div class="modal-head">
          <div class="modal-title">예약을 지금 종료할까요?</div>
          <button class="modal-close" @click="showEndConfirm = false">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M3 3l12 12M15 3L3 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div style="padding:12px 28px 8px">
          <p style="font-size:15px;color:#64748b;line-height:1.6">
            <strong style="color:#1e293b">{{ currentBooking?.title }}</strong> 예약이<br>지금 바로 종료됩니다.
          </p>
        </div>
        <div class="modal-btns" style="padding:16px 28px 28px">
          <button class="btn-cancel" @click="showEndConfirm = false">취소</button>
          <button class="btn-confirm" style="background:#ef4444" @click="endNow">지금 종료</button>
        </div>
      </div>
    </div>

    <!-- ── 예약 모달 ── -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-box">
        <div class="modal-head">
          <div>
            <div class="modal-title">{{ authStep === 1 ? '본인 확인' : '예약 등록' }}</div>
            <div class="modal-sub">{{ room?.name }}</div>
          </div>
          <button class="modal-close" @click="showModal = false">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
              <path d="M4 4l14 14M18 4L4 18" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <!-- Step 1: 인증 -->
        <div v-if="authStep === 1" class="modal-form">
          <div class="modal-field">
            <label>사번</label>
            <input v-model="authForm.employeeId" type="text" placeholder="사번을 입력하세요" autocomplete="off"
              style="width:100%;background:#f8fafc;border:1.5px solid #e2e8f0;border-radius:16px;padding:16px 18px;font-size:22px;font-weight:700;color:#1e293b;outline:none;" />
          </div>
          <div class="modal-field">
            <label>비밀번호</label>
            <input v-model="authForm.password" type="password" placeholder="비밀번호를 입력하세요" autocomplete="off"
              style="width:100%;background:#f8fafc;border:1.5px solid #e2e8f0;border-radius:16px;padding:16px 18px;font-size:22px;font-weight:700;color:#1e293b;outline:none;" />
          </div>
          <div v-if="authError" class="modal-error">{{ authError }}</div>
          <div class="modal-btns">
            <button type="button" class="btn-cancel" @click="showModal = false">취소</button>
            <button type="button" class="btn-confirm" :disabled="authLoading" @click="verifyUser">
              {{ authLoading ? '확인 중...' : '다음' }}
            </button>
          </div>
        </div>

        <!-- Step 2: 예약 폼 -->
        <form v-else @submit.prevent="submitBooking" class="modal-form">
          <div class="modal-field">
            <label>예약자</label>
            <div style="width:100%;background:#f1f5f9;border:1.5px solid #e2e8f0;border-radius:16px;padding:16px 18px;font-size:22px;font-weight:700;color:#64748b;">
              {{ bookForm.organizer }}
            </div>
          </div>
          <div class="modal-row-2">
            <div class="modal-field">
              <label>시작 시간</label>
              <input v-model="bookForm.startStr" type="time" required />
            </div>
            <div class="modal-field">
              <label>종료 시간</label>
              <select v-model="bookForm.endStr" required style="width:100%;background:#f8fafc;border:1.5px solid #e2e8f0;border-radius:16px;padding:16px 18px;font-size:22px;font-weight:700;color:#1e293b;outline:none;">
                <option v-for="opt in endOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
          </div>
          <div class="modal-field">
            <label>회의 제목</label>
            <input v-model="bookForm.title" type="text" placeholder="예) 팀 미팅" required />
          </div>
          <div v-if="bookError" class="modal-error">{{ bookError }}</div>
          <div class="modal-btns">
            <button type="button" class="btn-cancel" @click="authStep = 1">뒤로</button>
            <button type="submit" class="btn-confirm">예약 확정</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dayjs from 'dayjs';
import 'dayjs/locale/ko';
import axios from 'axios';
const api = axios.create({ baseURL: '/api', headers: { 'Content-Type': 'application/json' } });

dayjs.locale('ko');

const route  = useRoute();
const router = useRouter();
const roomId = computed(() => Number(route.params.roomId));

const room          = ref(null);
const todayBookings = ref([]);
const now           = ref(dayjs());

const clockTime = computed(() => now.value.format('A hh:mm:ss'));
const clockDate = computed(() => now.value.format('YYYY년 M월 D일 dddd'));

const currentBooking = computed(() =>
  todayBookings.value.find(b =>
    now.value.isAfter(dayjs(b.startTime)) && now.value.isBefore(dayjs(b.endTime))
  ) ?? null
);

const nextBooking = computed(() =>
  todayBookings.value
    .filter(b => now.value.isBefore(dayjs(b.startTime)))
    .sort((a, b) => dayjs(a.startTime).diff(dayjs(b.startTime)))[0] ?? null
);

const isInUse = computed(() => !!currentBooking.value);

const timeUntilEnd = computed(() => {
  if (!currentBooking.value) return '00:00';
  const diff = dayjs(currentBooking.value.endTime).diff(now.value, 'second');
  if (diff <= 0) return '00:00';
  const h = Math.floor(diff / 3600);
  const m = Math.floor((diff % 3600) / 60);
  const s = diff % 60;
  if (h > 0) return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
  return `${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
});

const timeUntilNext = computed(() => {
  if (!nextBooking.value) return '';
  const diff = dayjs(nextBooking.value.startTime).diff(now.value, 'minute');
  if (diff < 60) return `${diff}분 후`;
  return `${Math.floor(diff / 60)}시간 ${diff % 60}분 후`;
});

const progressPct = computed(() => {
  if (!currentBooking.value) return 0;
  const total   = dayjs(currentBooking.value.endTime).diff(dayjs(currentBooking.value.startTime), 'second');
  const elapsed = now.value.diff(dayjs(currentBooking.value.startTime), 'second');
  return Math.min(100, Math.max(0, (elapsed / total) * 100));
});

const getState = (b) => {
  if (now.value.isAfter(dayjs(b.endTime)))   return 'done';
  if (now.value.isAfter(dayjs(b.startTime))) return 'ongoing';
  return 'upcoming';
};

const timelineSlots = computed(() => {
  const START = 6, TOTAL = 15 * 60;
  return todayBookings.value.map(b => {
    const s = dayjs(b.startTime);
    const e = dayjs(b.endTime);
    const startMin = Math.max(0, s.hour() * 60 + s.minute() - START * 60);
    const endMin   = Math.min(TOTAL, e.hour() * 60 + e.minute() - START * 60);
    return {
      left:  (startMin / TOTAL * 100).toFixed(2) + '%',
      width: ((endMin - startMin) / TOTAL * 100).toFixed(2) + '%',
      state: getState(b)
    };
  });
});

const nowLineLeft = computed(() => {
  const START = 6, TOTAL = 15 * 60;
  const min = now.value.hour() * 60 + now.value.minute() - START * 60;
  return Math.max(0, Math.min(100, min / TOTAL * 100)).toFixed(2) + '%';
});

const showModal      = ref(false);
const showEndConfirm = ref(false);
const bookError      = ref('');
const bookForm       = reactive({ startStr: '', endStr: '', title: '', organizer: '' });
const authStep       = ref(1);
const authForm       = reactive({ employeeId: '', password: '' });
const authError      = ref('');
const authLoading    = ref(false);

const verifyUser = async () => {
  authError.value = '';
  if (!authForm.employeeId || !authForm.password) {
    authError.value = '사번과 비밀번호를 입력해주세요.';
    return;
  }
  authLoading.value = true;
  try {
    const res = await api.post('/auth/login', {
      employeeId: authForm.employeeId,
      password: authForm.password,
      rememberMe: false,
    });
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

const endOptions = computed(() => {
  if (!bookForm.startStr) return [];
  const [h, m] = bookForm.startStr.split(':').map(Number);
  const opts = [];
  for (let add = 30; add <= 240; add += 30) {
    const total = h * 60 + m + add;
    const eh = Math.floor(total / 60);
    const em = total % 60;
    if (eh >= 21) break;
    const val = `${String(eh).padStart(2,'0')}:${String(em).padStart(2,'0')}`;
    const label = add < 60 ? `${add}분` : add % 60 === 0 ? `${add/60}시간` : `${Math.floor(add/60)}시간 ${add%60}분`;
    opts.push({ value: val, label: `${val} (${label})` });
  }
  return opts;
});

const openBooking = () => {
  bookForm.startStr   = now.value.format('HH:mm');
  bookForm.endStr     = '';
  bookForm.title      = '';
  bookForm.organizer  = '';
  bookError.value     = '';
  authForm.employeeId = '';
  authForm.password   = '';
  authError.value     = '';
  authStep.value      = 1;
  showModal.value     = true;
};

const endNow = async () => {
  try {
    await api.patch(`/bookings/${currentBooking.value.id}/end`);
    showEndConfirm.value = false;
    fetchBookings();
  } catch (e) { console.error(e); }
};

const submitBooking = async () => {
  bookError.value = '';
  if (!bookForm.endStr || bookForm.startStr >= bookForm.endStr) {
    bookError.value = '종료 시간은 시작 시간보다 늦어야 합니다.';
    return;
  }
  try {
    const dateStr = now.value.format('YYYY-MM-DD');
    await api.post('/bookings', {
      roomId: roomId.value,
      title: bookForm.title,
      organizer: bookForm.organizer,
      startTime: `${dateStr}T${bookForm.startStr}:00`,
      endTime:   `${dateStr}T${bookForm.endStr}:00`,
    });
    showModal.value = false;
    fetchBookings();
  } catch (e) {
    const msg = e.response?.data;
    bookError.value = typeof msg === 'string' ? msg : '이미 예약된 시간대입니다.';
  }
};

const fetchRoom = async () => {
  try {
    const res = await api.get('/rooms');
    room.value = res.data.find(r => r.id === roomId.value) ?? null;
  } catch (e) { console.error(e); }
};

const fetchBookings = async () => {
  try {
    const today = now.value.format('YYYY-MM-DD');
    const res = await api.get(`/bookings?startDate=${today}&endDate=${today}`);
    todayBookings.value = res.data
      .filter(b => b.roomId === roomId.value)
      .sort((a, b) => dayjs(a.startTime).diff(dayjs(b.startTime)));
  } catch (e) { console.error(e); }
};

let clockTimer = null, refreshTimer = null;

const handleBeforeUnload = (e) => { e.preventDefault(); e.returnValue = ''; };
const handleKeydown = (e) => {
  if (
    e.key === 'F5' ||
    (e.ctrlKey && e.key === 'r') ||
    (e.ctrlKey && e.key === 'w') ||
    (e.ctrlKey && e.key === 'F4') ||
    (e.altKey  && e.key === 'F4') ||
    (e.altKey  && e.key === 'ArrowLeft')
  ) { e.preventDefault(); e.stopPropagation(); return false; }
};
const handleContextMenu = (e) => e.preventDefault();

const exitKiosk = async () => {
  if (document.fullscreenElement) await document.exitFullscreen();
  router.push('/kiosk');
};

onMounted(async () => {
  localStorage.setItem('lastKioskRoom', roomId.value);
  document.documentElement.style.overflow = 'hidden';
  document.body.style.overflow = 'hidden';
  await fetchRoom();
  await fetchBookings();
  clockTimer   = setInterval(() => { now.value = dayjs(); }, 1000);
  refreshTimer = setInterval(fetchBookings, 60000);
  try { await document.documentElement.requestFullscreen(); } catch (e) { /**/ }
  window.addEventListener('beforeunload', handleBeforeUnload);
  window.addEventListener('keydown', handleKeydown, true);
  window.addEventListener('contextmenu', handleContextMenu);
  history.pushState(null, '', location.href);
  window.addEventListener('popstate', () => { history.pushState(null, '', location.href); });
});

onUnmounted(() => {
  clearInterval(clockTimer);
  clearInterval(refreshTimer);
  window.removeEventListener('beforeunload', handleBeforeUnload);
  window.removeEventListener('keydown', handleKeydown, true);
  window.removeEventListener('contextmenu', handleContextMenu);
  document.documentElement.style.overflow = '';
  document.body.style.overflow = '';
});
</script>

<style scoped>
/* ── 루트 ── */
.kiosk-root {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #f1f5f9;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* ── 헤더 ── */
.kiosk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 28px;
  flex-shrink: 0;
  background: #ffffff;
  border-bottom: 1px solid #e8edf3;
}

.back-btn {
  width: 40px; height: 40px;
  border-radius: 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #64748b;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s;
  flex-shrink: 0;
}
.back-btn:hover { background: #e2e8f0; }

.room-name {
  font-size: clamp(18px, 2.6vw, 28px);
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.01em;
  line-height: 1.2;
}
.room-cap {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 2px;
  font-weight: 600;
}

.clock-block { text-align: right; }
.clock-time {
  font-size: clamp(20px, 3vw, 34px);
  font-weight: 900;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
  line-height: 1;
}
.clock-date {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 4px;
  font-weight: 600;
}

/* ── 본문 ── */
.kiosk-body {
  flex: 1;
  min-height: 0;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

/* ── 히어로 패널 (LEFT 57%) ── */
.hero-panel {
  flex: 0 0 57%;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 32px 48px;
  position: relative;
  overflow: hidden;
  transition: background 0.8s ease, box-shadow 0.8s ease;
}

.hero-green {
  background: linear-gradient(145deg, #059669 0%, #10b981 60%, #34d399 100%);
  box-shadow: 0 8px 48px rgba(16, 185, 129, 0.35);
}
.hero-red {
  background: linear-gradient(145deg, #b91c1c 0%, #dc2626 60%, #ef4444 100%);
  box-shadow: 0 8px 48px rgba(239, 68, 68, 0.35);
}

.hero-panel::before {
  content: '';
  position: absolute;
  width: 500px; height: 500px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  bottom: -180px; right: -120px;
  pointer-events: none;
}
.hero-panel::after {
  content: '';
  position: absolute;
  width: 280px; height: 280px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  top: -100px; left: -60px;
  pointer-events: none;
}

.hero-icon-wrap {
  width: clamp(72px, 11vw, 100px);
  height: clamp(72px, 11vw, 100px);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}

.hero-status-text {
  font-size: clamp(52px, 10vw, 96px);
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -0.03em;
  line-height: 1;
  text-align: center;
  position: relative;
  z-index: 1;
}

.hero-sub {
  font-size: clamp(13px, 2vw, 18px);
  color: rgba(255, 255, 255, 0.75);
  text-align: center;
  font-weight: 600;
  position: relative;
  z-index: 1;
  line-height: 1.5;
}
.hero-sub strong   { color: #ffffff; font-weight: 800; }
.hero-sub-muted    { color: rgba(255, 255, 255, 0.5); }

.hero-book-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: clamp(14px, 2vw, 20px) clamp(32px, 5vw, 56px);
  border-radius: 18px;
  background: #ffffff;
  border: none;
  color: #059669;
  font-size: clamp(16px, 2.4vw, 22px);
  font-weight: 800;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s, box-shadow 0.15s;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  margin-top: 8px;
  position: relative;
  z-index: 1;
}
.hero-book-btn:hover  { background: #f0fdf4; box-shadow: 0 6px 28px rgba(0,0,0,0.2); }
.hero-book-btn:active { transform: scale(0.97); }

.hero-meeting-title {
  font-size: clamp(24px, 4.5vw, 48px);
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -0.02em;
  line-height: 1.1;
  text-align: center;
  position: relative;
  z-index: 1;
}
.hero-organizer {
  font-size: clamp(14px, 2vw, 20px);
  color: rgba(255, 255, 255, 0.65);
  font-weight: 600;
  text-align: center;
  position: relative;
  z-index: 1;
  margin-top: -4px;
}

.hero-countdown-wrap {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 20px;
  padding: clamp(16px, 2.5vw, 28px) clamp(28px, 5vw, 52px);
  text-align: center;
  width: 100%;
  max-width: 400px;
  position: relative;
  z-index: 1;
}
.hero-countdown-label {
  font-size: 12px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 4px;
}
.hero-countdown {
  font-size: clamp(48px, 9vw, 88px);
  font-weight: 900;
  color: #ffffff;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.03em;
  line-height: 1;
  margin-bottom: 14px;
}
.hero-progress-track {
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 100px;
  overflow: hidden;
  margin-bottom: 8px;
}
.hero-progress-bar {
  height: 100%;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 100px;
  transition: width 1s linear;
}
.hero-progress-times {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.hero-end-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: clamp(10px, 1.5vw, 14px) clamp(24px, 4vw, 36px);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.15);
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  color: #ffffff;
  font-size: clamp(14px, 1.8vw, 18px);
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
  position: relative;
  z-index: 1;
}
.hero-end-btn:hover  { background: rgba(255, 255, 255, 0.22); }
.hero-end-btn:active { transform: scale(0.97); }

/* ── 일정 패널 (RIGHT) ── */
.schedule-panel {
  flex: 1;
  background: #ffffff;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  padding: 20px 20px 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.schedule-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  flex-shrink: 0;
  padding: 0 4px;
}
.schedule-title {
  font-size: 14px;
  font-weight: 800;
  color: #475569;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.schedule-count {
  font-size: 12px;
  font-weight: 700;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 3px 10px;
  border-radius: 100px;
}

.schedule-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  scrollbar-width: thin;
  scrollbar-color: #e2e8f0 transparent;
}
.schedule-list::-webkit-scrollbar { width: 4px; }
.schedule-list::-webkit-scrollbar-track { background: transparent; }
.schedule-list::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

.schedule-empty {
  text-align: center;
  font-size: 14px;
  color: #cbd5e1;
  padding: 40px 0;
  font-weight: 600;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px 12px 10px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  transition: opacity 0.3s;
  position: relative;
  overflow: hidden;
}
.sitem-done    { opacity: 0.45; }
.sitem-ongoing { background: #fff7f7; border-color: #fecaca; }

.sitem-bar {
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 4px;
  border-radius: 0 3px 3px 0;
}
.sbar-ongoing  { background: #ef4444; }
.sbar-upcoming { background: #10b981; }
.sbar-done     { background: #e2e8f0; }

.sitem-time {
  flex-shrink: 0;
  text-align: center;
  min-width: 46px;
  padding-left: 8px;
}
.sitem-start {
  font-size: clamp(13px, 1.8vw, 15px);
  font-weight: 800;
  color: #1e293b;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}
.sitem-end {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  margin-top: 1px;
}

.sitem-info { flex: 1; min-width: 0; }
.sitem-title {
  font-size: clamp(13px, 1.8vw, 15px);
  font-weight: 700;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}
.sitem-org {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

.sitem-badge {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 100px;
}
.sbadge-ongoing  { background: #fee2e2; color: #dc2626; }
.sbadge-done     { background: #f1f5f9; color: #94a3b8; }
.sbadge-upcoming { background: #ecfdf5; color: #059669; }

/* ── 타임라인 ── */
.timeline-section {
  flex-shrink: 0;
  padding: 10px 28px 16px;
  background: #ffffff;
  border-top: 1px solid #f1f5f9;
}

.timeline-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
  margin-bottom: 7px;
  letter-spacing: 0.03em;
}

.timeline-track {
  position: relative;
  height: 8px;
  background: #e2e8f0;
  border-radius: 100px;
  overflow: visible;
}

.timeline-block {
  position: absolute;
  top: 0; height: 100%;
  border-radius: 100px;
}
.tblock-ongoing  { background: #ef4444; }
.tblock-done     { background: #cbd5e1; }
.tblock-upcoming { background: #10b981; }

.now-line {
  position: absolute;
  top: -4px; bottom: -4px;
  width: 2.5px;
  background: #1e293b;
  border-radius: 100px;
  box-shadow: 0 0 6px rgba(30, 41, 59, 0.4);
}

/* ── 모달 ── */
.modal-overlay {
  position: fixed; inset: 0; z-index: 50;
  display: flex; align-items: center; justify-content: center;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(8px);
  padding: 24px;
}
.modal-box {
  background: white; width: 100%; max-width: 520px;
  border-radius: 32px; overflow: hidden;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.25);
}
.modal-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 28px 36px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.modal-title { font-size: 28px; font-weight: 900; color: #1e293b; }
.modal-sub   { font-size: 16px; color: #94a3b8; margin-top: 4px; }
.modal-close {
  width: 44px; height: 44px; border-radius: 14px;
  background: #f1f5f9; border: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: #64748b; transition: background 0.15s;
}
.modal-close:hover { background: #e2e8f0; }
.modal-form { padding: 24px 36px 32px; display: flex; flex-direction: column; gap: 20px; }
.modal-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.modal-field { display: flex; flex-direction: column; gap: 8px; }
.modal-field label { font-size: 13px; font-weight: 700; color: #64748b; }
.modal-field input {
  width: 100%;
  background: #f8fafc; border: 1.5px solid #e2e8f0;
  border-radius: 16px; padding: 16px 18px;
  font-size: 22px; font-weight: 700; color: #1e293b;
  outline: none; transition: border-color 0.15s, box-shadow 0.15s;
}
.modal-field input:focus {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.12);
}
.modal-error {
  background: #fef2f2; border: 1px solid #fecaca;
  color: #ef4444; border-radius: 12px;
  padding: 12px 16px; font-size: 14px; font-weight: 600;
}
.modal-btns { display: flex; gap: 12px; margin-top: 4px; }
.btn-cancel {
  flex: 1; padding: 18px; border-radius: 18px;
  background: #f1f5f9; border: none; cursor: pointer;
  font-size: 18px; font-weight: 700; color: #64748b;
  transition: background 0.15s;
}
.btn-cancel:hover { background: #e2e8f0; }
.btn-confirm {
  flex: 2; padding: 18px; border-radius: 18px;
  background: #059669; border: none; cursor: pointer;
  font-size: 18px; font-weight: 700; color: white;
  transition: background 0.15s, transform 0.1s;
}
.btn-confirm:hover  { background: #047857; }
.btn-confirm:active { transform: scale(0.98); }
</style>
