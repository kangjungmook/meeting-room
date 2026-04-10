/**
 * 공용 SSE(EventSource) 매니저
 * - 자동 재연결(reconnectMs)
 * - connect/disconnect 수명주기 관리
 * - 이벤트 리스너 일괄 등록
 */

export function createSseManager({
  getUrl,
  reconnectMs = 5000,
  listeners = {},
  onOpen,
  onError,
} = {}) {
  let source = null;
  let reconnectTimer = null;
  let stopped = false;

  const clearReconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
  };

  const closeSource = () => {
    if (source) {
      try { source.close(); } catch { /* ignore */ }
      source = null;
    }
  };

  const connect = () => {
    stopped = false;
    clearReconnect();
    closeSource();

    const url = typeof getUrl === 'function' ? getUrl() : null;
    if (!url) return;

    source = new EventSource(url);

    for (const [event, handler] of Object.entries(listeners || {})) {
      if (typeof handler === 'function') {
        source.addEventListener(event, handler);
      }
    }

    if (typeof onOpen === 'function') source.onopen = onOpen;

    source.onerror = (e) => {
      if (typeof onError === 'function') onError(e);
      closeSource();
      if (stopped) return;
      reconnectTimer = setTimeout(connect, reconnectMs);
    };
  };

  const disconnect = () => {
    stopped = true;
    clearReconnect();
    closeSource();
  };

  const getSource = () => source;

  return { connect, disconnect, getSource };
}

