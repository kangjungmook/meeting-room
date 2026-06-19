import { onlineUsers, adminLiveNow, fetchAll } from './useAdminState';
import dayjs from 'dayjs';
import { createSseManager } from '../../utils/sse';

// ── 관리자 SSE 연결 ───────────────────────────────────────────
let _isFirst = true; // 최초 연결 시 fetchAll 중복 방지
const _sse = createSseManager({
  getUrl: () => {
    const token = localStorage.getItem('token') || '';
    return `/api/sse/subscribe?token=${token}`;
  },
  listeners: {
    booking: () => { adminLiveNow.value = dayjs(); fetchAll(); },
    ADMIN:   () => { adminLiveNow.value = dayjs(); fetchAll(); },
    ONLINE:  (e) => { try { onlineUsers.value = JSON.parse(e.data) ?? []; } catch {} },
  },
  onOpen: () => {
    adminLiveNow.value = dayjs();
    if (_isFirst) { _isFirst = false; return; } // 최초 연결은 AdminPage.vue의 onMounted에서 처리
    fetchAll();
  },
});

export const connectSse = () => {
  _sse.connect();
};

export const disconnectSse = () => {
  _sse.disconnect();
  _isFirst = true;
};
