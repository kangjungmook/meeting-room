import { onlineUsers, adminLiveNow, fetchAll } from './useAdminState';
import dayjs from 'dayjs';

// ── 관리자 SSE 연결 ───────────────────────────────────────────
let sseSource = null;
let _isFirst = true; // 최초 연결 시 fetchAll 중복 방지

export const connectSse = () => {
  if (sseSource) sseSource.close();
  const token = localStorage.getItem('token') || '';
  sseSource = new EventSource(`/api/sse/subscribe?token=${token}`);

  sseSource.addEventListener('booking', () => { adminLiveNow.value = dayjs(); fetchAll(); });
  sseSource.addEventListener('ADMIN',   () => { adminLiveNow.value = dayjs(); fetchAll(); });
  sseSource.addEventListener('ONLINE',  (e) => {
    try { onlineUsers.value = JSON.parse(e.data) ?? []; } catch {}
  });

  // 재연결 시 누락된 이벤트를 보완하기 위해 fetchAll 호출
  sseSource.onopen = () => {
    adminLiveNow.value = dayjs();
    if (_isFirst) { _isFirst = false; return; } // 최초 연결은 AdminPage.vue의 onMounted에서 처리
    fetchAll();
  };

  sseSource.onerror = () => { sseSource.close(); setTimeout(connectSse, 5000); };
};

export const disconnectSse = () => {
  if (sseSource) sseSource.close();
  _isFirst = true;
};
