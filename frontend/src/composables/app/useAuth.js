import { reactive } from 'vue';
import { parseJwt } from '../../utils/parseJwt';

const _p = parseJwt();

// userId 클레임을 Long으로 변환
const _toId = (raw) => {
  if (raw == null) return null;
  const n = Number(raw);
  return Number.isFinite(n) ? n : null;
};

// ── 현재 사용자 (모듈 싱글턴) ──────────────────────────────────
export const currentUser = reactive({
  id:         _toId(_p.userId),
  name:       _p.name  || '',
  employeeId: _p.sub   || '',
  role:       _p.role  || '',
});

export let isAdmin = _p.role === 'ADMIN';

// 토큰 갱신 후 호출 — 로그인/재발급 시 사용
export const refreshCurrentUser = () => {
  const p = parseJwt();
  currentUser.id         = _toId(p.userId);
  currentUser.name       = p.name  || '';
  currentUser.employeeId = p.sub   || '';
  currentUser.role       = p.role  || '';
  isAdmin                = p.role === 'ADMIN';
};
