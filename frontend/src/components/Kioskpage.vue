<template>
  <div class="kiosk-root">
    <header class="kiosk-header">
      <div class="header-room">
        <button @click="exitKiosk" class="back-btn" aria-label="뒤로가기">
          <svg width="15" height="15" viewBox="0 0 18 18" fill="none">
            <path d="M11 3L5 9l6 6" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>
        <div>
          <div class="room-name">{{ room?.name ?? '회의실' }}</div>
          <div class="room-cap">수용 {{ room?.capacity ?? '-' }}명</div>
        </div>
      </div>

      <div class="clock-block">
        <div class="clock-time">{{ clockTime }}</div>
        <div class="clock-date">{{ clockDate }}</div>
      </div>
    </header>

    <div class="kiosk-body">
      <section class="hero-panel" :class="isInUse ? 'hero-red' : 'hero-green'">
        <div class="hero-topbar">
          <div class="hero-chip" :class="isInUse ? 'hero-chip-busy' : 'hero-chip-free'">
            <span class="hero-chip-dot"></span>
            <span>{{ isInUse ? '사용 중' : '사용 가능' }}</span>
          </div>
        </div>

        <div class="hero-content">
          <template v-if="!isInUse">
            <div class="hero-icon-wrap">
              <svg width="48" height="48" viewBox="0 0 56 56" fill="none">
                <path d="M12 28l12 12 20-20" stroke="white" stroke-width="5" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </div>

            <div class="hero-status-text">사용 가능</div>

            <div class="hero-sub" v-if="nextBooking">
              다음 예약 <strong>{{ dayjs(nextBooking.startTime).format('HH:mm') }}</strong> · {{ nextBooking.title }}
              <span class="hero-sub-muted">({{ timeUntilNext }})</span>
            </div>
            <div class="hero-sub" v-else>오늘 남은 예약이 없습니다</div>

            <button @click="openBooking" class="hero-book-btn">
              <svg width="18" height="18" viewBox="0 0 20 20" fill="none">
                <path d="M10 3v14M3 10h14" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" />
              </svg>
              지금 바로 예약
            </button>
          </template>

          <template v-else>
            <div class="hero-icon-wrap">
              <svg width="42" height="42" viewBox="0 0 56 56" fill="none">
                <rect x="10" y="10" width="13" height="36" rx="4" fill="white" />
                <rect x="33" y="10" width="13" height="36" rx="4" fill="white" />
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
                <rect x="1" y="1" width="12" height="12" rx="2.5" fill="currentColor" />
              </svg>
              지금 종료
            </button>
          </template>
        </div>

        <div class="hero-footer">
          <div class="hero-footer-card">
            <span class="hero-footer-label">{{ isInUse ? '시작 시간' : '다음 시작' }}</span>
            <strong>
              {{
                isInUse
                    ? dayjs(currentBooking?.startTime).format('HH:mm')
                    : nextBooking
                        ? dayjs(nextBooking.startTime).format('HH:mm')
                        : '--:--'
              }}
            </strong>
          </div>
        </div>
      </section>

      <aside class="schedule-panel">
        <div class="schedule-header">
          <div>
            <div class="schedule-title">오늘의 일정</div>
            <div class="schedule-subtitle">{{ room?.name ?? '회의실' }}의 예약 현황</div>
          </div>
          <div class="schedule-count">{{ todayBookings.length }}건</div>
        </div>

        <div class="schedule-list">
          <div v-if="todayBookings.length === 0" class="schedule-empty">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none" style="margin: 0 auto 10px; display: block; opacity: 0.25">
              <rect x="2" y="6" width="28" height="24" rx="4" stroke="#64748b" stroke-width="2" />
              <path d="M2 12h28M10 2v5M22 2v5" stroke="#64748b" stroke-width="2" stroke-linecap="round" />
            </svg>
            오늘 등록된 일정이 없습니다
          </div>

          <div
              v-for="b in todayBookings"
              :key="b.id"
              class="schedule-item"
              :class="{
              'sitem-done': getState(b) === 'done',
              'sitem-ongoing': getState(b) === 'ongoing'
            }"
          >
            <div
                class="sitem-bar"
                :class="{
                'sbar-ongoing': getState(b) === 'ongoing',
                'sbar-upcoming': getState(b) === 'upcoming',
                'sbar-done': getState(b) === 'done'
              }"
            ></div>

            <div class="sitem-time">
              <div class="sitem-start">{{ dayjs(b.startTime).format('HH:mm') }}</div>
              <div class="sitem-end">{{ dayjs(b.endTime).format('HH:mm') }}</div>
            </div>

            <div class="sitem-info">
              <div class="sitem-title">{{ b.title }}</div>
              <div class="sitem-org">{{ b.organizer }}</div>
            </div>

            <div
                class="sitem-badge"
                :class="{
                'sbadge-ongoing': getState(b) === 'ongoing',
                'sbadge-done': getState(b) === 'done',
                'sbadge-upcoming': getState(b) === 'upcoming'
              }"
            >
              {{ getState(b) === 'ongoing' ? '진행 중' : getState(b) === 'done' ? '종료' : '예정' }}
            </div>
          </div>
        </div>
      </aside>
    </div>

    <section class="timeline-section">
      <div class="timeline-card">
        <div class="timeline-head">
          <div class="timeline-title">Daily Flow</div>
          <div class="timeline-now">NOW</div>
        </div>

        <div class="timeline-labels">
          <span v-for="h in [6, 9, 12, 15, 18, 21]" :key="h">{{ String(h).padStart(2, '0') }}</span>
        </div>

        <div class="timeline-track">
          <div
              v-for="(slot, i) in timelineSlots"
              :key="i"
              class="timeline-block"
              :class="{
              'tblock-ongoing': slot.state === 'ongoing',
              'tblock-done': slot.state === 'done',
              'tblock-upcoming': slot.state === 'upcoming'
            }"
              :style="{ left: slot.left, width: slot.width }"
          ></div>
          <div class="now-line" :style="{ left: nowLineLeft }"></div>
        </div>
      </div>
    </section>

    <div v-if="showEndConfirm" class="modal-overlay" @click.self="showEndConfirm = false">
      <div class="modal-box">
        <div class="modal-head">
          <div class="modal-title">예약을 지금 종료할까요?</div>
          <button class="modal-close" @click="showEndConfirm = false">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M3 3l12 12M15 3L3 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <p class="modal-copy">
            <strong>{{ currentBooking?.title }}</strong> 예약을 바로 종료합니다.
          </p>
        </div>
        <div class="modal-btns modal-btns-padded">
          <button class="btn-cancel" @click="showEndConfirm = false">취소</button>
          <button class="btn-confirm btn-danger" @click="endNow">지금 종료</button>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-box">
        <div class="modal-head">
          <div>
            <div class="modal-title">{{ authStep === 1 ? '본인 확인' : '예약 등록' }}</div>
            <div class="modal-sub">{{ room?.name }}</div>
          </div>
          <button class="modal-close" @click="showModal = false">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
              <path d="M4 4l14 14M18 4L4 18" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
            </svg>
          </button>
        </div>

        <div v-if="authStep === 1" class="modal-form">
          <div class="modal-field">
            <label>사번</label>
            <input
                v-model="authForm.employeeId"
                type="text"
                placeholder="사번을 입력하세요"
                autocomplete="off"
            />
          </div>
          <div class="modal-field">
            <label>비밀번호</label>
            <input
                v-model="authForm.password"
                type="password"
                placeholder="비밀번호를 입력하세요"
                autocomplete="off"
            />
          </div>
          <div v-if="authError" class="modal-error">{{ authError }}</div>
          <div class="modal-btns">
            <button type="button" class="btn-cancel" @click="showModal = false">취소</button>
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
            <input v-model="bookForm.title" type="text"  required />
          </div>

          <div v-if="bookError" class="modal-error">{{ bookError }}</div>

          <div class="modal-btns">
            <button type="button" class="btn-cancel" @click="authStep = 1">이전</button>
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

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

dayjs.locale('ko');

const route = useRoute();
const router = useRouter();
const roomId = computed(() => Number(route.params.roomId));

const room = ref(null);
const todayBookings = ref([]);
const now = ref(dayjs());

const clockTime = computed(() => now.value.format('A hh:mm:ss'));
const clockDate = computed(() => now.value.format('YYYY년 M월 D일 dddd'));

const currentBooking = computed(() =>
    todayBookings.value.find(
        (b) => now.value.isAfter(dayjs(b.startTime)) && now.value.isBefore(dayjs(b.endTime))
    ) ?? null
);

const nextBooking = computed(() =>
    todayBookings.value
        .filter((b) => now.value.isBefore(dayjs(b.startTime)))
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

  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }

  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

const timeUntilNext = computed(() => {
  if (!nextBooking.value) return '';
  const diff = dayjs(nextBooking.value.startTime).diff(now.value, 'minute');
  if (diff < 60) return `${diff}분 후`;
  return `${Math.floor(diff / 60)}시간 ${diff % 60}분 후`;
});

const progressPct = computed(() => {
  if (!currentBooking.value) return 0;
  const total = dayjs(currentBooking.value.endTime).diff(dayjs(currentBooking.value.startTime), 'second');
  const elapsed = now.value.diff(dayjs(currentBooking.value.startTime), 'second');
  return Math.min(100, Math.max(0, (elapsed / total) * 100));
});

const getState = (booking) => {
  if (now.value.isAfter(dayjs(booking.endTime))) return 'done';
  if (now.value.isAfter(dayjs(booking.startTime))) return 'ongoing';
  return 'upcoming';
};

const timelineSlots = computed(() => {
  const START = 6;
  const TOTAL = 15 * 60;

  return todayBookings.value.map((booking) => {
    const start = dayjs(booking.startTime);
    const end = dayjs(booking.endTime);
    const startMin = Math.max(0, start.hour() * 60 + start.minute() - START * 60);
    const endMin = Math.min(TOTAL, end.hour() * 60 + end.minute() - START * 60);

    return {
      left: `${((startMin / TOTAL) * 100).toFixed(2)}%`,
      width: `${(((endMin - startMin) / TOTAL) * 100).toFixed(2)}%`,
      state: getState(booking),
    };
  });
});

const nowLineLeft = computed(() => {
  const START = 6;
  const TOTAL = 15 * 60;
  const min = now.value.hour() * 60 + now.value.minute() - START * 60;
  return `${Math.max(0, Math.min(100, (min / TOTAL) * 100)).toFixed(2)}%`;
});

const showModal = ref(false);
const showEndConfirm = ref(false);
const bookError = ref('');
const bookForm = reactive({ startStr: '', endStr: '', title: '', organizer: '' });
const authStep = ref(1);
const authForm = reactive({ employeeId: '', password: '' });
const authError = ref('');
const authLoading = ref(false);
const kioskToken = ref('');

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
    const payload = JSON.parse(
        new TextDecoder().decode(Uint8Array.from(atob(b64), (c) => c.charCodeAt(0)))
    );

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

    const value = `${String(eh).padStart(2, '0')}:${String(em).padStart(2, '0')}`;
    const label =
        add < 60
            ? `${add}분`
            : add % 60 === 0
                ? `${add / 60}시간`
                : `${Math.floor(add / 60)}시간 ${add % 60}분`;

    opts.push({ value, label: `${value} (${label})` });
  }

  return opts;
});

const openBooking = () => {
  bookForm.startStr = now.value.format('HH:mm');
  bookForm.endStr = '';
  bookForm.title = '';
  bookForm.organizer = '';
  bookError.value = '';
  authForm.employeeId = '';
  authForm.password = '';
  authError.value = '';
  authStep.value = 1;
  kioskToken.value = '';
  showModal.value = true;
};

const endNow = async () => {
  try {
    await api.patch(`/bookings/${currentBooking.value.id}/end`);
    showEndConfirm.value = false;
    fetchBookings();
  } catch (e) {
    console.error(e);
  }
};

const submitBooking = async () => {
  bookError.value = '';

  if (!bookForm.endStr || bookForm.startStr >= bookForm.endStr) {
    bookError.value = '종료 시간은 시작 시간보다 뒤여야 합니다.';
    return;
  }

  try {
    const dateStr = now.value.format('YYYY-MM-DD');
    await api.post('/bookings', {
      roomId: roomId.value,
      title: bookForm.title,
      organizer: bookForm.organizer,
      startTime: `${dateStr}T${bookForm.startStr}:00`,
      endTime: `${dateStr}T${bookForm.endStr}:00`,
    }, { headers: { Authorization: `Bearer ${kioskToken.value}` } });

    showModal.value = false;
    fetchBookings();
  } catch (e) {
    const msg = e.response?.data;
    bookError.value = typeof msg === 'string' ? msg : '이미 예약된 시간입니다.';
  }
};

const fetchRoom = async () => {
  try {
    const res = await api.get('/rooms');
    room.value = res.data.find((r) => r.id === roomId.value) ?? null;
  } catch (e) {
    console.error(e);
  }
};

const fetchBookings = async () => {
  try {
    const today = now.value.format('YYYY-MM-DD');
    const res = await api.get(`/bookings?startDate=${today}&endDate=${today}`);
    todayBookings.value = res.data
        .filter((b) => b.roomId === roomId.value)
        .sort((a, b) => dayjs(a.startTime).diff(dayjs(b.startTime)));
  } catch (e) {
    console.error(e);
  }
};

let clockTimer = null;
let sseSource = null;
let sseReconnectTimer = null;
let popstateHandler = null;

const connectSse = () => {
  if (sseSource) return;
  sseSource = new EventSource('/api/sse/subscribe');

  sseSource.addEventListener('booking', () => {
    fetchBookings();
  });

  sseSource.onerror = () => {
    sseSource.close();
    sseSource = null;
    sseReconnectTimer = setTimeout(connectSse, 5000);
  };
};

const handleBeforeUnload = (e) => {
  e.preventDefault();
  e.returnValue = '';
};

const handleKeydown = (e) => {
  if (
      e.key === 'F5' ||
      (e.ctrlKey && e.key === 'r') ||
      (e.ctrlKey && e.key === 'w') ||
      (e.ctrlKey && e.key === 'F4') ||
      (e.altKey && e.key === 'F4') ||
      (e.altKey && e.key === 'ArrowLeft')
  ) {
    e.preventDefault();
    e.stopPropagation();
    return false;
  }
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

  clockTimer = setInterval(() => {
    now.value = dayjs();
  }, 1000);

  connectSse();

  try {
    await document.documentElement.requestFullscreen();
  } catch (e) {
    // ignore fullscreen errors
  }

  window.addEventListener('beforeunload', handleBeforeUnload);
  window.addEventListener('keydown', handleKeydown, true);
  window.addEventListener('contextmenu', handleContextMenu);

  history.pushState(null, '', location.href);
  popstateHandler = () => {
    history.pushState(null, '', location.href);
  };
  window.addEventListener('popstate', popstateHandler);
});

onUnmounted(() => {
  clearInterval(clockTimer);
  clearTimeout(sseReconnectTimer);
  if (sseSource) { sseSource.close(); sseSource = null; }

  window.removeEventListener('beforeunload', handleBeforeUnload);
  window.removeEventListener('keydown', handleKeydown, true);
  window.removeEventListener('contextmenu', handleContextMenu);
  if (popstateHandler) window.removeEventListener('popstate', popstateHandler);

  document.documentElement.style.overflow = '';
  document.body.style.overflow = '';
});
</script>

<style scoped>
.kiosk-root {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  color: #0f172a;
  background:
      radial-gradient(circle at top left, rgba(59, 130, 246, 0.08), transparent 22%),
      radial-gradient(circle at top right, rgba(16, 185, 129, 0.08), transparent 24%),
      linear-gradient(180deg, #f8fbff 0%, #eef4f9 100%);
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, sans-serif;
}

.kiosk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px 18px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.header-room {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.back-btn {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(203, 213, 225, 0.8);
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.16s ease, background 0.16s ease, border-color 0.16s ease;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  flex-shrink: 0;
}

.back-btn:hover {
  background: #ffffff;
  border-color: rgba(148, 163, 184, 0.52);
  transform: translateY(-1px);
}

.room-name {
  font-size: clamp(24px, 2.5vw, 34px);
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.03em;
  line-height: 1.05;
}

.room-cap {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 700;
  color: #64748b;
}

.clock-block {
  text-align: right;
  min-width: 280px;
}

.clock-time {
  font-size: clamp(28px, 3.6vw, 44px);
  font-weight: 900;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.04em;
  line-height: 1;
}

.clock-date {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 700;
  color: #64748b;
}

.kiosk-body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(420px, 0.82fr);
  gap: 18px;
  padding: 18px;
  overflow: hidden;
}

.hero-panel {
  position: relative;
  overflow: hidden;
  border-radius: 34px;
  padding: 24px 28px 26px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 32px 80px rgba(15, 23, 42, 0.16);
}

.hero-green {
  background:
      radial-gradient(circle at 18% 22%, rgba(255, 255, 255, 0.14), transparent 0 16%),
      radial-gradient(circle at 82% 84%, rgba(255, 255, 255, 0.12), transparent 0 22%),
      linear-gradient(145deg, #0f766e 0%, #10b981 48%, #5eead4 100%);
}

.hero-red {
  background:
      radial-gradient(circle at 18% 22%, rgba(255, 255, 255, 0.14), transparent 0 16%),
      radial-gradient(circle at 82% 84%, rgba(255, 255, 255, 0.12), transparent 0 22%),
      linear-gradient(145deg, #991b1b 0%, #dc2626 48%, #fb7185 100%);
}

.hero-panel::before,
.hero-panel::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(2px);
}

.hero-panel::before {
  width: 420px;
  height: 420px;
  right: -160px;
  bottom: -200px;
  background: rgba(255, 255, 255, 0.09);
}

.hero-panel::after {
  width: 240px;
  height: 240px;
  top: -80px;
  left: -60px;
  background: rgba(255, 255, 255, 0.08);
}

.hero-topbar,
.hero-content,
.hero-footer {
  position: relative;
  z-index: 1;
}

.hero-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 48px;
  padding: 0 18px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: rgba(255, 255, 255, 0.92);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.hero-chip-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: currentColor;
  box-shadow: 0 0 14px currentColor;
}

.hero-chip-free {
  color: #eafff6;
}

.hero-chip-busy {
  color: #fff1f2;
}

.hero-summary-card {
  min-width: 116px;
  padding: 12px 16px;
  border-radius: 20px;
  background: rgba(12, 18, 28, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.14);
  text-align: right;
  color: white;
}

.hero-summary-label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  opacity: 0.72;
}

.hero-summary-card strong {
  display: block;
  margin-top: 6px;
  font-size: 28px;
  line-height: 1;
  font-weight: 900;
}

.hero-content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 18px;
  padding: 20px 0 14px;
}

.hero-icon-wrap {
  width: clamp(84px, 11vw, 112px);
  height: clamp(84px, 11vw, 112px);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.hero-status-text {
  font-size: clamp(58px, 8.6vw, 108px);
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -0.06em;
  line-height: 0.95;
}

.hero-sub {
  max-width: 640px;
  font-size: clamp(18px, 2vw, 24px);
  font-weight: 700;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.5;
}

.hero-sub strong {
  color: #ffffff;
  font-weight: 900;
}

.hero-sub-muted {
  color: rgba(255, 255, 255, 0.64);
}

.hero-book-btn,
.hero-end-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-width: 248px;
  min-height: 72px;
  padding: 0 34px;
  border-radius: 24px;
  font-size: clamp(18px, 2vw, 24px);
  font-weight: 900;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
}

.hero-book-btn {
  color: #0f766e;
  background: linear-gradient(180deg, #ffffff 0%, #ecfdf5 100%);
  border: none;
  box-shadow: 0 20px 36px rgba(8, 15, 23, 0.14);
}

.hero-book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 42px rgba(8, 15, 23, 0.18);
}

.hero-end-btn {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.34);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.hero-end-btn:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.18);
}

.hero-meeting-title {
  max-width: 760px;
  font-size: clamp(28px, 4.4vw, 56px);
  line-height: 1.08;
  letter-spacing: -0.04em;
  font-weight: 900;
  color: #ffffff;
}

.hero-organizer {
  margin-top: -6px;
  font-size: clamp(17px, 1.9vw, 24px);
  font-weight: 700;
  color: rgba(255, 255, 255, 0.72);
}

.hero-countdown-wrap {
  width: min(100%, 460px);
  padding: 22px 26px 20px;
  border-radius: 28px;
  background: rgba(12, 18, 28, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.hero-countdown-label {
  font-size: 12px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.64);
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-countdown {
  margin: 8px 0 14px;
  font-size: clamp(56px, 7.8vw, 92px);
  line-height: 0.95;
  font-weight: 900;
  color: #ffffff;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.05em;
}

.hero-progress-track {
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.18);
}

.hero-progress-bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.64));
  transition: width 1s linear;
}

.hero-progress-times {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.7);
  font-variant-numeric: tabular-nums;
}

.hero-footer {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.hero-footer-card {
  padding: 16px 18px 18px;
  border-radius: 22px;
  background: rgba(12, 18, 28, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.14);
  color: #ffffff;
  flex: 0 0 auto;
  min-width: 140px;
  max-width: 180px;
}

.hero-footer-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  opacity: 0.7;
}

.hero-footer-card strong {
  display: block;
  margin-top: 8px;
  font-size: clamp(24px, 2.4vw, 34px);
  line-height: 1.1;
  font-weight: 900;
  letter-spacing: -0.04em;
}

.schedule-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 24px;
  border-radius: 34px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(203, 213, 225, 0.58);
  box-shadow: 0 28px 72px rgba(15, 23, 42, 0.09);
  overflow: hidden;
}

.schedule-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.86);
}

.schedule-title {
  font-size: 24px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.04em;
}

.schedule-subtitle {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 700;
  color: #64748b;
}

.schedule-count {
  flex-shrink: 0;
  min-width: 58px;
  padding: 10px 14px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 14px;
  font-weight: 900;
  text-align: center;
}

.schedule-list {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.schedule-list::-webkit-scrollbar {
  width: 6px;
}

.schedule-list::-webkit-scrollbar-track {
  background: transparent;
}

.schedule-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

.schedule-empty {
  margin: auto 0;
  padding: 56px 18px;
  text-align: center;
  color: #94a3b8;
  font-size: 15px;
  font-weight: 700;
}

.schedule-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 18px 18px 16px;
  border-radius: 24px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 24px rgba(148, 163, 184, 0.1);
  overflow: hidden;
  transition: transform 0.16s ease, box-shadow 0.16s ease, opacity 0.16s ease;
}

.schedule-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(148, 163, 184, 0.14);
}

.sitem-done {
  opacity: 0.52;
}

.sitem-ongoing {
  background: linear-gradient(180deg, #fff7f7 0%, #fff1f2 100%);
  border-color: #fecdd3;
}

.sitem-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
}

.sbar-ongoing {
  background: linear-gradient(180deg, #dc2626 0%, #fb7185 100%);
}

.sbar-upcoming {
  background: linear-gradient(180deg, #0f766e 0%, #14b8a6 100%);
}

.sbar-done {
  background: #cbd5e1;
}

.sitem-time {
  flex-shrink: 0;
  width: 72px;
  padding-left: 10px;
}

.sitem-start {
  font-size: 20px;
  font-weight: 900;
  line-height: 1;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.04em;
}

.sitem-end {
  margin-top: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.sitem-info {
  flex: 1;
  min-width: 0;
}

.sitem-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sitem-org {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sitem-badge {
  flex-shrink: 0;
  padding: 9px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.04em;
}

.sbadge-ongoing {
  background: #fee2e2;
  color: #dc2626;
}

.sbadge-done {
  background: #e2e8f0;
  color: #64748b;
}

.sbadge-upcoming {
  background: #dcfce7;
  color: #047857;
}

.timeline-section {
  flex-shrink: 0;
  padding: 0 18px 18px;
}

.timeline-card {
  padding: 16px 20px 20px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(203, 213, 225, 0.56);
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.08);
}

.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.timeline-now {
  padding: 7px 12px;
  border-radius: 999px;
  background: #0f172a;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.1em;
}

.timeline-labels {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 12px;
  font-weight: 800;
  color: #64748b;
  letter-spacing: 0.04em;
}

.timeline-track {
  position: relative;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, #e2e8f0 0%, #cbd5e1 100%);
  overflow: visible;
}

.timeline-block {
  position: absolute;
  top: 0;
  height: 100%;
  border-radius: 999px;
}

.tblock-ongoing {
  background: linear-gradient(90deg, #ef4444 0%, #fb7185 100%);
}

.tblock-done {
  background: #94a3b8;
}

.tblock-upcoming {
  background: linear-gradient(90deg, #10b981 0%, #2dd4bf 100%);
}

.now-line {
  position: absolute;
  top: -6px;
  bottom: -6px;
  width: 3px;
  border-radius: 999px;
  background: #0f172a;
  box-shadow: 0 0 10px rgba(15, 23, 42, 0.3);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.42);
  backdrop-filter: blur(10px);
}

.modal-box {
  width: 100%;
  max-width: 560px;
  overflow: hidden;
  border-radius: 34px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 30px 90px rgba(15, 23, 42, 0.24);
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 30px 34px 22px;
  border-bottom: 1px solid #edf2f7;
}

.modal-title {
  font-size: 28px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.04em;
}

.modal-sub {
  margin-top: 4px;
  font-size: 15px;
  font-weight: 700;
  color: #64748b;
}

.modal-close {
  width: 46px;
  height: 46px;
  border: none;
  border-radius: 16px;
  background: #f8fafc;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.16s ease, transform 0.16s ease;
}

.modal-close:hover {
  background: #e2e8f0;
  transform: translateY(-1px);
}

.modal-body {
  padding: 18px 34px 6px;
}

.modal-copy {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  color: #475569;
}

.modal-copy strong {
  color: #0f172a;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 26px 34px 32px;
}

.modal-row-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.modal-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.modal-field label {
  font-size: 13px;
  font-weight: 800;
  color: #64748b;
}

.modal-field input,
.modal-field select,
.modal-static {
  width: 100%;
  min-height: 58px;
  padding: 0 18px;
  border-radius: 18px;
  border: 1.5px solid #dbe4ee;
  background: #f8fbff;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  outline: none;
  transition: border-color 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
  box-sizing: border-box;
}

.modal-field input:focus,
.modal-field select:focus {
  border-color: #10b981;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.12);
  background: #ffffff;
}

.modal-static {
  display: flex;
  align-items: center;
  color: #475569;
  background: #f1f5f9;
}

.modal-error {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #dc2626;
  font-size: 14px;
  font-weight: 700;
}

.modal-btns {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.modal-btns-padded {
  padding: 18px 34px 32px;
}

.btn-cancel,
.btn-confirm {
  min-height: 60px;
  border: none;
  border-radius: 20px;
  font-size: 18px;
  font-weight: 900;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
}

.btn-cancel {
  flex: 1;
  background: #edf2f7;
  color: #475569;
}

.btn-cancel:hover {
  background: #e2e8f0;
  transform: translateY(-1px);
}

.btn-confirm {
  flex: 1.6;
  background: linear-gradient(180deg, #10b981 0%, #059669 100%);
  color: #ffffff;
  box-shadow: 0 14px 30px rgba(16, 185, 129, 0.22);
}

.btn-confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 34px rgba(16, 185, 129, 0.26);
}

.btn-confirm:disabled {
  cursor: wait;
  opacity: 0.74;
}

.btn-danger {
  background: linear-gradient(180deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 14px 30px rgba(239, 68, 68, 0.22);
}

.btn-danger:hover {
  box-shadow: 0 18px 34px rgba(239, 68, 68, 0.26);
}

@media (max-width: 1280px) {
  .kiosk-body {
    grid-template-columns: minmax(0, 1fr) minmax(360px, 0.72fr);
  }

  .schedule-title {
    font-size: 21px;
  }

  .sitem-title {
    font-size: 16px;
  }
}
</style>