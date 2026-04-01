import { allRooms } from './useAdminState';

// ── 룸 헬퍼 ──────────────────────────────────────────────────
export const getRoomName  = (id) => allRooms.value.find(r => r.id === id)?.name      ?? '-';
export const getRoomColor = (id) => allRooms.value.find(r => r.id === id)?.colorCode ?? '#94a3b8';

// ── User-Agent 파싱 ───────────────────────────────────────────
export const parseDevice = (ua) => {
  if (!ua) return '-';
  return /Mobile|Android|iPhone|iPad|iPod/.test(ua) ? 'Mobile' : 'PC';
};

export const parseBrowser = (ua) => {
  if (!ua) return '-';
  if (/Edg\//.test(ua))     return 'Edge';
  if (/Chrome\//.test(ua))  return 'Chrome';
  if (/Firefox\//.test(ua)) return 'Firefox';
  if (/Safari\//.test(ua))  return 'Safari';
  return '-';
};

// ── JSON 유틸 ─────────────────────────────────────────────────
export const parseDetail   = (jsonStr) => { try { return JSON.parse(jsonStr); } catch { return null; } };
export const formatRawJson = (jsonStr) => { try { return JSON.stringify(JSON.parse(jsonStr), null, 2); } catch { return jsonStr; } };

export const formatJsonHtml = (jsonStr) => {
  const parsed = parseDetail(jsonStr);
  if (!parsed) return '';
  const raw = JSON.stringify(parsed, null, 2);
  return raw
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"([^"\\]*(\\.[^"\\]*)*)"(\s*:)/g, '<span class="jk">"$1"</span>$3')
    .replace(/:\s*"([^"\\]*(\\.[^"\\]*)*)"/g,   ': <span class="jstr">"$1"</span>')
    .replace(/:\s*(-?\d+\.?\d*)/g,               ': <span class="jnum">$1</span>')
    .replace(/:\s*(true|false)/g,                 ': <span class="jbool">$1</span>')
    .replace(/:\s*(null)/g,                       ': <span class="jnull">$1</span>');
};
