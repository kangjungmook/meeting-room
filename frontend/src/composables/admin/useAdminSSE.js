import { onlineUsers, fetchAll } from './useAdminState';

// ── 관리자 SSE 연결 ───────────────────────────────────────────
let sseSource = null;

export const connectSse = () => {
  if (sseSource) sseSource.close();
  const token = localStorage.getItem('token') || '';
  sseSource = new EventSource(`/api/sse/subscribe?token=${token}`);
  sseSource.addEventListener('booking', () => fetchAll());
  sseSource.addEventListener('ADMIN',   () => fetchAll());
  sseSource.addEventListener('ONLINE',  (e) => {
    try { onlineUsers.value = JSON.parse(e.data) ?? []; } catch {}
  });
  sseSource.onerror = () => { sseSource.close(); setTimeout(connectSse, 5000); };
};

export const disconnectSse = () => { if (sseSource) sseSource.close(); };
