import { createRouter, createWebHistory } from 'vue-router';
import App                from '../App.vue';
import MyBookingsPage     from '../pages/app/MyBookingsPage.vue';
import SettingsPage       from '../pages/app/SettingsPage.vue';
import AdminPage          from '../pages/admin/AdminPage.vue';
import AdminDashboardPage from '../pages/admin/AdminDashboardPage.vue';
import AdminRoomsPage     from '../pages/admin/AdminRoomsPage.vue';
import AdminBookingsPage  from '../pages/admin/AdminBookingsPage.vue';
import AdminUsersAllPage  from '../pages/admin/AdminUsersAllPage.vue';
import AdminUsersPendingPage from '../pages/admin/AdminUsersPendingPage.vue';
import AdminUsersApprovedPage from '../pages/admin/AdminUsersApprovedPage.vue';
import AdminUsersRejectedPage from '../pages/admin/AdminUsersRejectedPage.vue';
import AdminLogsPage      from '../pages/admin/AdminLogsPage.vue';
import AdminNotificationPage from '../pages/admin/AdminNotificationPage.vue';
import AdminSystemPage    from '../pages/admin/AdminSystemPage.vue';
import AdminResetPage     from '../pages/admin/AdminResetPage.vue';
import KioskSelector      from '../pages/kiosk/KioskSelectorPage.vue';
import KioskPage          from '../pages/kiosk/KioskPage.vue';
import LoginPage          from '../pages/auth/LoginPage.vue';
import ChangePasswordPage from '../pages/auth/ChangePasswordPage.vue';
import MaintenancePage    from '../pages/system/MaintenancePage.vue';
import { refreshAccessToken } from '../api';
import { parseJwt }       from '../utils/parseJwt';

const routes = [
  { path: '/',               component: LoginPage },
  { path: '/maintenance',    component: MaintenancePage },
  { path: '/main',           component: App,               meta: { requiresLogin: true } },
  { path: '/my-bookings',   component: MyBookingsPage,    meta: { requiresLogin: true } },
  { path: '/settings',     component: SettingsPage,      meta: { requiresLogin: true } },
  { path: '/change-password', component: ChangePasswordPage, meta: { requiresLogin: true } },
  {
    path: '/admin',
    component: AdminPage,
    meta: { requiresLogin: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: AdminDashboardPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'dashboard' } },
      { path: 'rooms', component: AdminRoomsPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'rooms' } },
      { path: 'bookings', component: AdminBookingsPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'bookings' } },
      { path: 'users', component: AdminUsersAllPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'users-all' } },
      { path: 'users/pending', component: AdminUsersPendingPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'users-pending' } },
      { path: 'users/approved', component: AdminUsersApprovedPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'users-approved' } },
      { path: 'users/rejected', component: AdminUsersRejectedPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'users-rejected' } },
      { path: 'logs', component: AdminLogsPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'logs' } },
      { path: 'notification', component: AdminNotificationPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'notification' } },
      { path: 'system', component: AdminSystemPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'system' } },
      { path: 'reset', component: AdminResetPage, meta: { requiresLogin: true, requiresAdmin: true, adminKey: 'reset' } },
    ],
  },
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
