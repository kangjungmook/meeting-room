// ── JWT 페이로드 파싱 유틸 (UTF-8 안전) ───────────────────────
export const parseJwt = (token = localStorage.getItem('token')) => {
  try {
    if (!token) return {};
    const b64   = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(b64), c => c.charCodeAt(0));
    return JSON.parse(new TextDecoder().decode(bytes));
  } catch { return {}; }
};
