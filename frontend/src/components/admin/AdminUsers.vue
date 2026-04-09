<template>
  <div class="flex flex-col gap-4">
    <AdminUsersStatsCards
      :users="users"
      :current-employee-id="currentUser.employeeId"
      :online-employee-ids="onlineUsers"
      :effective-filter="effectiveFilter"
    />

    <AdminUsersToolbar
      :effective-filter="effectiveFilter"
      :search-keyword="searchKeyword"
      :online-filter="onlineFilter"
      :user-count="displayUsers.length"
      :selected-count="selectedIds.length"
      @update:searchKeyword="v => (searchKeyword.value = v)"
      @update:onlineFilter="v => (onlineFilter.value = v)"
      @bulkApprove="bulkApprove"
      @bulkReject="bulkReject"
      @openCreateModal="openCreateModal()"
    />

    <AdminUsersListDesktop
      v-if="!isMobile"
      :users="displayUsers"
      :selected-ids="selectedIds"
      :online-employee-ids="onlineUsers"
      :effective-filter="effectiveFilter"
      :dayjs="dayjs"
      @toggleSelect="toggleSelect"
      @approveUser="approveUser"
      @openRejectModal="openRejectModal"
      @openSetPasswordModal="openSetPasswordModal"
      @changeRole="changeRole"
      @clearFcmToken="clearFcmToken"
      @confirmDelete="confirmDelete"
    />

    <AdminUsersListMobile
      v-else
      :users="displayUsers"
      :selected-ids="selectedIds"
      :online-employee-ids="onlineUsers"
      :effective-filter="effectiveFilter"
      :dayjs="dayjs"
      @toggleSelect="toggleSelect"
      @approveUser="approveUser"
      @openRejectModal="openRejectModal"
      @openSetPasswordModal="openSetPasswordModal"
      @changeRole="changeRole"
      @clearFcmToken="clearFcmToken"
      @confirmDelete="confirmDelete"
    />

    <AdminUsersBulkApproveConfirmModal
      :show="bulkApproveConfirm.show"
      :count="bulkApproveConfirm.targets.length"
      @cancel="bulkApproveConfirm.show = false"
      @confirm="doBulkApprove"
    />

    <AdminUsersCreateModal
      :show="createModal.show"
      :form="createModal.form"
      :error="createModal.error"
      @close="createModal.show = false"
      @submit="submitCreate"
      @update:form="v => (createModal.form = v)"
    />

    <AdminUsersSetPasswordModal
      :show="pwModal.show"
      :user-name="pwModal.userName"
      :password="pwModal.password"
      :confirm="pwModal.confirm"
      :error="pwModal.error"
      @close="pwModal.show = false"
      @submit="submitSetPassword"
      @update:password="v => (pwModal.password = v)"
      @update:confirm="v => (pwModal.confirm = v)"
    />

    <AdminUsersRejectModal
      :show="rejectModal.show"
      :is-bulk="rejectModal.isBulk"
      :target-name="rejectModal.target?.name || ''"
      :target-count="rejectModal.targets.length"
      :reason="rejectModal.reason"
      @update:reason="v => (rejectModal.reason = v)"
      @cancel="rejectModal.show = false; rejectModal.reason = ''"
      @submit="submitReject"
    />

    <AdminUsersDeleteConfirmModal
      :show="deleteConfirm.show"
      :target-name="deleteConfirm.target?.name || ''"
      :target-employee-id="deleteConfirm.target?.employeeId || ''"
      @cancel="deleteConfirm.show = false"
      @confirm="doDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { useAdmin } from '../../composables/useAdmin';
import { showAdminToast } from '../../composables/admin/useAdminToast';
import AdminUsersStatsCards from './users/AdminUsersStatsCards.vue';
import AdminUsersToolbar from './users/AdminUsersToolbar.vue';
import AdminUsersListDesktop from './users/AdminUsersListDesktop.vue';
import AdminUsersListMobile from './users/AdminUsersListMobile.vue';
import AdminUsersBulkApproveConfirmModal from './users/modals/AdminUsersBulkApproveConfirmModal.vue';
import AdminUsersCreateModal from './users/modals/AdminUsersCreateModal.vue';
import AdminUsersSetPasswordModal from './users/modals/AdminUsersSetPasswordModal.vue';
import AdminUsersRejectModal from './users/modals/AdminUsersRejectModal.vue';
import AdminUsersDeleteConfirmModal from './users/modals/AdminUsersDeleteConfirmModal.vue';

const {
  users, currentUser,
  approveUser, rejectUser, changeRole, deleteUser, clearFcmToken, createUser, setUserPassword,
  onlineUsers,
  dayjs,
} = useAdmin();

const props = defineProps({ isMobile: Boolean, filter: String });

// ── props.filter 기반 독립 필터 (v-show 시 4개 인스턴스 충돌 방지) ──
const effectiveFilter = computed(() => props.filter ?? 'ALL');

const localFilteredUsers = computed(() => {
  const f = effectiveFilter.value;
  const cu = currentUser.employeeId;
  if (f === 'PENDING') return users.value.filter(u => u.role !== 'ADMIN' && u.status === 'PENDING');
  if (f === 'APPROVED') return users.value.filter(u => u.status === 'APPROVED' && u.employeeId !== cu);
  if (f === 'REJECTED') return users.value.filter(u => u.role !== 'ADMIN' && u.status === 'REJECTED');
  return users.value.filter(u => u.employeeId !== cu); // ALL
});

// ── Selection ──
const selectedIds = ref([]);

// ── 검색 + 온라인 필터 ──
const searchKeyword = ref('');
const onlineFilter = ref('ALL');

watch(effectiveFilter, () => { selectedIds.value = []; searchKeyword.value = ''; onlineFilter.value = 'ALL'; });

const displayUsers = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase();
  return localFilteredUsers.value.filter(u => {
    const matchKw = !kw || u.name.toLowerCase().includes(kw) || u.employeeId.toLowerCase().includes(kw);
    const isOnline = onlineUsers.value.includes(u.employeeId);
    const matchOnline = onlineFilter.value === 'ALL'
      || (onlineFilter.value === 'ONLINE' && isOnline)
      || (onlineFilter.value === 'OFFLINE' && !isOnline);
    return matchKw && matchOnline;
  });
});

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id);
  if (idx === -1) selectedIds.value.push(id);
  else selectedIds.value.splice(idx, 1);
};

// ── Bulk approve confirm ──
const bulkApproveConfirm = reactive({ show: false, targets: [] });

const bulkApprove = () => {
  bulkApproveConfirm.targets = selectedIds.value.slice();
  bulkApproveConfirm.show = true;
};

const doBulkApprove = async () => {
  try {
    await Promise.all(bulkApproveConfirm.targets.map(id => approveUser({ id })));
    showAdminToast(`${bulkApproveConfirm.targets.length}명 승인 완료`);
    selectedIds.value = [];
  } catch {
    showAdminToast('승인 중 오류가 발생했습니다.', 'error');
  } finally {
    bulkApproveConfirm.show = false;
  }
};

const bulkReject = () => {
  rejectModal.isBulk = true;
  rejectModal.targets = displayUsers.value.filter(u => selectedIds.value.includes(u.id));
  rejectModal.target = null;
  rejectModal.reason = '';
  rejectModal.show = true;
};

// ── Reject modal ──
const rejectModal = reactive({ show: false, target: null, targets: [], isBulk: false, reason: '' });

const openRejectModal = (u) => {
  rejectModal.isBulk = false;
  rejectModal.target = u;
  rejectModal.targets = [];
  rejectModal.reason = '';
  rejectModal.show = true;
};

const submitReject = async () => {
  const reason = rejectModal.reason.trim();
  if (rejectModal.isBulk) {
    await Promise.all(rejectModal.targets.map(u => rejectUser(u, reason)));
    selectedIds.value = [];
  } else {
    await rejectUser(rejectModal.target, reason);
  }
  rejectModal.show = false;
  rejectModal.reason = '';
};

// ── Create modal ──
const createModal = reactive({
  show: false, error: '',
  form: { employeeId: '', name: '', password: '', role: 'USER', status: 'APPROVED' },
});

const openCreateModal = (defaults = {}) => {
  createModal.form = { employeeId: '', name: '', password: '', role: 'USER', status: 'APPROVED', ...defaults };
  createModal.show = true;
};

const submitCreate = async () => {
  if (createModal.form.password.length < 4) { createModal.error = '비밀번호는 4자 이상이어야 합니다.'; return; }
  createModal.error = '';
  try {
    await createUser(createModal.form);
    createModal.show = false;
    showAdminToast('유저가 생성되었습니다.');
  } catch (e) {
    createModal.error = e.response?.data?.message || '생성 중 오류가 발생했습니다.';
  }
};

// ── Set password modal ──
const pwModal = reactive({ show: false, error: '', userId: null, userName: '', password: '', confirm: '' });

const openSetPasswordModal = (u) => {
  pwModal.userId = u.id;
  pwModal.userName = u.name;
  pwModal.password = '';
  pwModal.confirm = '';
  pwModal.error = '';
  pwModal.show = true;
};

const submitSetPassword = async () => {
  if (pwModal.password.length < 4) { pwModal.error = '비밀번호는 4자 이상이어야 합니다.'; return; }
  if (pwModal.password !== pwModal.confirm) { pwModal.error = '비밀번호가 일치하지 않습니다.'; return; }
  pwModal.error = '';
  try {
    await setUserPassword(pwModal.userId, pwModal.password);
    const name = pwModal.userName;
    pwModal.show = false;
    showAdminToast(`${name} 님의 비밀번호가 설정되었습니다.`);
  } catch (e) {
    pwModal.error = e.response?.data?.message || '설정 중 오류가 발생했습니다.';
  }
};

// ── Delete confirm ──
const deleteConfirm = reactive({ show: false, target: null });

const confirmDelete = (u) => {
  deleteConfirm.target = u;
  deleteConfirm.show = true;
};

const doDelete = async () => {
  if (!deleteConfirm.target) return;
  await deleteUser(deleteConfirm.target.id);
  deleteConfirm.show = false;
  deleteConfirm.target = null;
};
</script>
