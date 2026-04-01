import { ref, computed } from 'vue';
import api from '../../api';
import { users, currentUser, fetchAll } from './useAdminState';

// ── 사용자 탭 네비게이션 ──────────────────────────────────────
export const manageUserFilter = ref('PENDING');

export const pendingCount = computed(() =>
  users.value.filter(u => u.status === 'PENDING').length
);

export const filteredUsers = computed(() => {
  if (manageUserFilter.value === 'PENDING')  return users.value.filter(u => u.role !== 'ADMIN' && u.status === 'PENDING');
  if (manageUserFilter.value === 'APPROVED') return users.value.filter(u => u.status === 'APPROVED' && u.employeeId !== currentUser.employeeId);
  if (manageUserFilter.value === 'REJECTED') return users.value.filter(u => u.role !== 'ADMIN' && u.status === 'REJECTED');
  return users.value.filter(u => u.employeeId !== currentUser.employeeId);
});

export const manageTabs = computed(() => [
  { key: 'PENDING',  label: '대기', count: users.value.filter(u => u.role !== 'ADMIN' && u.status === 'PENDING').length,                        activeClass: 'bg-amber-50 text-amber-600',    badgeActive: 'bg-amber-500 text-white' },
  { key: 'APPROVED', label: '승인', count: users.value.filter(u => u.status === 'APPROVED' && u.employeeId !== currentUser.employeeId).length,  activeClass: 'bg-emerald-50 text-emerald-600', badgeActive: 'bg-emerald-500 text-white' },
  { key: 'REJECTED', label: '거절', count: users.value.filter(u => u.role !== 'ADMIN' && u.status === 'REJECTED').length,                        activeClass: 'bg-red-50 text-red-500',        badgeActive: 'bg-red-500 text-white' },
  { key: 'ALL',      label: '전체', count: users.value.filter(u => u.employeeId !== currentUser.employeeId).length,                              activeClass: 'bg-slate-800 text-white',       badgeActive: 'bg-white/20 text-white' },
]);

export const adminUsers = computed(() => users.value.filter(u => u.role === 'ADMIN'));

// ── 사용자 CRUD ───────────────────────────────────────────────
export const approveUser       = async (u) => { await api.patch(`/admin/users/${u.id}/approve`);            fetchAll(); };
export const rejectUser        = async (u, reason = '') => { await api.patch(`/admin/users/${u.id}/reject?reason=${encodeURIComponent(reason)}`); fetchAll(); };
export const resetUserPassword = async (u) => {
  await api.patch(`/admin/users/${u.id}/reset-password`);
  alert(`${u.name} 님의 비밀번호가 1111로 초기화되었습니다.`);
};
export const changeRole = async (u) => {
  const newRole = u.role === 'ADMIN' ? 'USER' : 'ADMIN';
  if (!confirm(`"${u.name}" 님을 ${newRole === 'ADMIN' ? '관리자로 지정' : '일반 유저로 변경'}할까요?`)) return;
  await api.patch(`/admin/users/${u.id}/role?role=${newRole}`);
  fetchAll();
};
export const deleteUser      = async (id)           => { await api.delete(`/admin/users/${id}`);                 fetchAll(); };
export const clearFcmToken   = async (u)            => { await api.delete(`/admin/users/${u.id}/fcm-token`);     fetchAll(); };
export const createUser      = async (form)         => { await api.post('/admin/users', form);                   fetchAll(); };
export const setUserPassword = async (userId, password) => {
  await api.patch(`/admin/users/${userId}/password?password=${encodeURIComponent(password)}`);
};
