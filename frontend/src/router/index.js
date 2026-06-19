import { createRouter, createWebHistory } from 'vue-router';
const App                    = () => import('../App.vue');
const MyBookingsPage         = () => import('../pages/app/MyBookingsPage.vue');
const SettingsPage           = () => import('../pages/app/SettingsPage.vue');
const AdminPage              = () => import('../pages/admin/AdminPage.vue');
const AdminDashboardPage     = () => import('../pages/admin/AdminDashboardPage.vue');
const AdminRoomsPage         = () => import('../pages/admin/AdminRoomsPage.vue');
const AdminBookingsPage      = () => import('../pages/admin/AdminBookingsPage.vue');
const AdminUsersAllPage      = () => import('../pages/admin/AdminUsersAllPage.vue');
const AdminUsersPendingPage  = () => import('../pages/admin/AdminUsersPendingPage.vue');
const AdminUsersApprovedPage = () => import('../pages/admin/AdminUsersApprovedPage.vue');
const AdminUsersRejectedPage = () => import('../pages/admin/AdminUsersRejectedPage.vue');
const AdminLogsPage          = () => import('../pages/admin/AdminLogsPage.vue');
const AdminNotificationPage  = () => import('../pages/admin/AdminNotificationPage.vue');
const AdminSystemPage        = () => import('../pages/admin/AdminSystemPage.vue');
const AdminResetPage         = () => import('../pages/admin/AdminResetPage.vue');
const KioskSelector          = () => import('../pages/kiosk/KioskSelectorPage.vue');
const KioskPage              = () => import('../pages/kiosk/KioskPage.vue');
const LoginPage              = () => import('../pages/auth/LoginPage.vue');
const ChangePasswordPage     = () => import('../pages/auth/ChangePasswordPage.vue');
const MaintenancePage        = () => import('../pages/system/MaintenancePage.vue');
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
  } catch (e) {
    const isNetworkError = e?.code === 'ECONNABORTED' || !e?.response;
    if (!isNetworkError) {
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('token');
      sessionStorage.removeItem('adminAuth');
    }
  }
}

let autoLoginDone = false;

router.beforeEach(async (to) => {
  if (to.path === '/maintenance') return true;

  if (!autoLoginDone) {
    autoLoginDone = true;
    const _token = localStorage.getItem('token');
    const _refreshToken = localStorage.getItem('refreshToken');
    const _isExpired = _token ? (parseJwt(_token).exp || 0) * 1000 < Date.now() : true;
    if ((!_token || _isExpired) && _refreshToken) {
      await tryAutoLogin();
    }
  }

  const token = localStorage.getItem('token');
  const payload = token ? parseJwt(token) : null;

  if (payload?.role === 'ADMIN') sessionStorage.setItem('adminAuth', 'true');
  else if (token) sessionStorage.removeItem('adminAuth');

// 이미 로그인된 상태에서 로그인 페이지 접근 시 자동 이동
  if (to.path === '/' && token) {
    if (payload?.role) return { path: payload.role === 'ADMIN' ? '/admin' : '/main' };
  }

  if (to.meta.requiresLogin && !token) return { path: '/' };
  if (to.meta.requiresAdmin && !sessionStorage.getItem('adminAuth')) return { path: '/' };

  if (sessionStorage.getItem('passwordResetRequired') === 'true' && to.path !== '/change-password') {
    return { path: '/change-password' };
  }

  return true;
});

export default router;
