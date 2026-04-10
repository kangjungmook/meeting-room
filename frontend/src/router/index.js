import { createRouter, createWebHistory } from 'vue-router';
import App                from '../App.vue';
import MyBookingsPage     from '../components/MyBookingsPage.vue';
import SettingsPage       from '../components/SettingsPage.vue';
import AdminPage          from '../components/AdminPage.vue';
import KioskSelector      from '../components/Kioskselector.vue';
import KioskPage          from '../components/Kioskpage.vue';
import LoginPage          from '../components/LoginPage.vue';
import ChangePasswordPage from '../components/ChangePasswordPage.vue';
import MaintenancePage    from '../components/MaintenancePage.vue';
import { refreshAccessToken } from '../api';
import { parseJwt }       from '../utils/parseJwt';

const routes = [
  { path: '/',               component: LoginPage },
  { path: '/maintenance',    component: MaintenancePage },
  { path: '/main',           component: App,               meta: { requiresLogin: true } },
  { path: '/my-bookings',   component: MyBookingsPage,    meta: { requiresLogin: true } },
  { path: '/settings',     component: SettingsPage,      meta: { requiresLogin: true } },
  { path: '/change-password', component: ChangePasswordPage, meta: { requiresLogin: true } },
  { path: '/admin',          component: AdminPage,         meta: { requiresLogin: true, requiresAdmin: true } },
  { path: '/kiosk',          component: KioskSelector },
  { path: '/kiosk/:roomId',  component: KioskPage },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 자동 로그인: refreshToken이 있으면 JWT 재발급 시도
async function tryAutoLogin() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) return;
  try {
    const data = await refreshAccessToken(refreshToken);
    if (data.token) localStorage.setItem('token', data.token);
    if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);

    const payload = data.token ? parseJwt(data.token) : null;
    if (payload?.role === 'ADMIN') sessionStorage.setItem('adminAuth', 'true');
    else sessionStorage.removeItem('adminAuth');
  } catch {
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('token');
    sessionStorage.removeItem('adminAuth');
  }
}

let autoLoginDone = false;

router.beforeEach(async (to) => {
  // 점검 페이지는 인증과 무관하게 접근 허용
  if (to.path === '/maintenance') return true;

  if (!autoLoginDone) {
    autoLoginDone = true;
    if (!localStorage.getItem('token') && localStorage.getItem('refreshToken')) {
      await tryAutoLogin();
    }
  }

  const token = localStorage.getItem('token');

  // 이미 로그인된 상태에서 로그인 페이지 접근 시 자동 이동
  if (to.path === '/' && token) {
    const payload = parseJwt(token);
    if (payload.role) return { path: payload.role === 'ADMIN' ? '/admin' : '/main' };
  }

  if (to.meta.requiresLogin && !token) return { path: '/' };
  if (to.meta.requiresAdmin && !sessionStorage.getItem('adminAuth')) return { path: '/' };

  // 비밀번호 변경 강제: change-password 외 모든 페이지 차단
  if (sessionStorage.getItem('passwordResetRequired') === 'true' && to.path !== '/change-password') {
    return { path: '/change-password' };
  }

  return true;
});

export default router;