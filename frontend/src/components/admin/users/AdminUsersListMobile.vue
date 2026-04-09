<template>
  <div class="flex flex-col gap-3">
    <div
      v-if="users.length === 0"
      class="bg-white rounded-2xl border border-slate-200 py-12 flex flex-col items-center gap-2"
    >
      <svg width="36" height="36" viewBox="0 0 40 40" fill="none" class="text-slate-200">
        <circle cx="18" cy="12" r="7" stroke="currentColor" stroke-width="2" />
        <path d="M4 36c0-7.7 6.3-14 14-14" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
        <path d="M28 24l8 8M36 24l-8 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
      </svg>
      <p class="text-[14px] font-semibold text-slate-400">
        {{ effectiveFilter === 'PENDING' ? '승인 대기중인 회원이 없습니다' : '해당 회원이 없습니다' }}
      </p>
    </div>

    <div
      v-for="u in users"
      :key="u.id"
      :class="[
        'rounded-2xl border p-4 transition-all',
        selectedIds.includes(u.id) ? 'bg-indigo-50/60 border-indigo-200' : 'bg-white border-slate-200',
      ]"
    >
      <div class="flex items-center gap-3 mb-3">
        <input
          type="checkbox"
          :checked="selectedIds.includes(u.id)"
          class="w-4 h-4 rounded accent-indigo-500 cursor-pointer flex-shrink-0"
          @change="$emit('toggleSelect', u.id)"
        />
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 flex-wrap">
            <span
              :class="[
                'w-2 h-2 rounded-full flex-shrink-0',
                onlineEmployeeIds.includes(u.employeeId) ? 'bg-emerald-400' : 'bg-slate-200',
              ]"
            ></span>
            <span class="text-[15px] font-bold text-slate-800">{{ u.name }}</span>
            <span
              :class="[
                'px-2 py-0.5 rounded-lg text-[10px] font-bold',
                u.role === 'ADMIN' ? 'bg-violet-100 text-violet-600' : 'bg-slate-100 text-slate-700',
              ]"
            >{{ u.role }}</span>
            <span v-if="u.fcmToken" class="flex items-center gap-1 text-[11px] font-semibold text-emerald-500">
              <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>FCM
            </span>
          </div>
          <p class="text-[12px] text-slate-600 mt-0.5">{{ u.employeeId }} · 가입 {{ fmtMD(dayjs, u.createdAt) }}</p>
          <div class="flex items-center gap-2 mt-0.5 flex-wrap">
            <template v-if="onlineEmployeeIds.includes(u.employeeId)">
              <span class="flex items-center gap-1 text-[11px] font-bold text-emerald-500">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>온라인
              </span>
              <span v-if="u.lastLoginAt" class="text-[11px] text-slate-400 tabular-nums">
                접속 {{ fmtMDHM(dayjs, u.lastLoginAt) }}
              </span>
            </template>
            <template v-else-if="u.lastLoginAt || u.lastLogoutAt">
              <span class="text-[11px] text-slate-400">오프라인</span>
              <span v-if="u.lastLoginAt" class="text-[11px] text-slate-400 tabular-nums">
                접속 {{ fmtMDHM(dayjs, u.lastLoginAt) }}
              </span>
              <span v-if="u.lastLogoutAt" class="text-[11px] text-slate-400 tabular-nums">
                · 종료 {{ fmtMDHM(dayjs, u.lastLogoutAt) }}
              </span>
            </template>
          </div>
        </div>
      </div>

      <div class="flex gap-2 pt-3 border-t border-slate-50">
        <template v-if="u.status === 'APPROVED'">
          <button
            type="button"
            class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-amber-600 bg-amber-50 hover:bg-amber-100 transition-all"
            @click="$emit('openSetPasswordModal', u)"
          >비번 초기화</button>

          <div class="relative">
            <button
              type="button"
              @click.stop="setMenuOpen(u.id, $event)"
              :class="[
                'px-4 py-2 rounded-xl text-[13px] font-semibold border transition-all flex items-center gap-1',
                menuOpen === u.id ? 'bg-slate-100 border-slate-300 text-slate-700' : 'bg-white border-slate-200 text-slate-600',
              ]"
            >
              관리
              <AppIcon name="chevron-down" :size="10" :class="menuOpen === u.id ? 'rotate-180' : ''" cls="transition-transform" />
            </button>
            <Teleport to="body">
              <div
                v-if="menuOpen === u.id"
                class="fixed w-40 bg-white rounded-xl border border-slate-200 shadow-lg z-[9999] overflow-hidden py-1"
                :style="menuStyle"
                @click.stop
              >
                <button
                  type="button"
                  @click="$emit('changeRole', u); menuOpen = null"
                  :class="[
                    'flex items-center gap-2 w-full px-3.5 py-2.5 text-[13px] font-semibold',
                    u.role === 'ADMIN' ? 'text-slate-500 hover:bg-slate-50' : 'text-violet-500 hover:bg-violet-50',
                  ]"
                >
                  {{ u.role === 'ADMIN' ? '권한 해제' : '관리자 설정' }}
                </button>
                <button
                  v-if="u.fcmToken"
                  type="button"
                  class="flex items-center gap-2 w-full px-3.5 py-2.5 text-[13px] font-semibold text-slate-500 hover:bg-slate-50"
                  @click="$emit('clearFcmToken', u); menuOpen = null"
                >FCM 초기화</button>
                <div class="h-px bg-slate-100 mx-2 my-1"></div>
                <button
                  type="button"
                  class="flex items-center gap-2 w-full px-3.5 py-2.5 text-[13px] font-semibold text-red-400 hover:bg-red-50"
                  @click="$emit('confirmDelete', u); menuOpen = null"
                >삭제</button>
              </div>
            </Teleport>
          </div>
        </template>

        <template v-if="u.status === 'PENDING'">
          <button
            type="button"
            class="flex-1 py-2 rounded-xl text-[13px] font-bold text-emerald-600 bg-emerald-50 hover:bg-emerald-100 transition-all"
            @click="$emit('approveUser', u)"
          >승인</button>
          <button
            type="button"
            class="flex-1 py-2 rounded-xl text-[13px] font-bold text-slate-600 bg-slate-50 hover:bg-slate-100 transition-all"
            @click="$emit('openRejectModal', u)"
          >거절</button>
          <button
            type="button"
            class="w-10 py-2 rounded-xl text-[13px] font-bold text-red-400 bg-red-50 hover:bg-red-100 transition-all flex items-center justify-center"
            @click="$emit('confirmDelete', u)"
          >
            <AppIcon name="trash" :size="14" />
          </button>
        </template>

        <template v-if="u.status === 'REJECTED'">
          <button
            type="button"
            class="flex-1 py-2 rounded-xl text-[13px] font-bold text-emerald-600 bg-emerald-50 hover:bg-emerald-100 transition-all"
            @click="$emit('approveUser', u)"
          >재승인</button>
          <button
            type="button"
            class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-red-400 bg-red-50 hover:bg-red-100 transition-all"
            @click="$emit('confirmDelete', u)"
          >삭제</button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import AppIcon from '../../icons/AppIcon.vue';
import { fmtMD, fmtMDHM } from '../../../utils/datetime';
import { useClickOutside } from '../../../composables/admin/ui/useClickOutside';
import { useAnchoredMenu } from '../../../composables/admin/ui/useAnchoredMenu';

defineProps({
  users: { type: Array, required: true },
  selectedIds: { type: Array, required: true },
  onlineEmployeeIds: { type: Array, required: true },
  effectiveFilter: { type: String, required: true },
  dayjs: { type: Function, required: true },
});

defineEmits([
  'toggleSelect',
  'approveUser',
  'openRejectModal',
  'openSetPasswordModal',
  'changeRole',
  'clearFcmToken',
  'confirmDelete',
]);

const menuOpen = ref(null);
const anchorEl = ref(null);

const menuOpenBool = computed(() => !!menuOpen.value);
const { style: menuStyle } = useAnchoredMenu({
  open: menuOpenBool,
  anchorEl,
  panelEl: null,
  mode: 'right',
  gap: 4,
});

const setMenuOpen = (id, event) => {
  if (menuOpen.value === id) { menuOpen.value = null; anchorEl.value = null; return; }
  anchorEl.value = event.currentTarget;
  menuOpen.value = id;
};

useClickOutside({
  enabled: menuOpenBool,
  inside: [anchorEl],
  onOutside: () => { menuOpen.value = null; anchorEl.value = null; },
  eventName: 'click',
});
</script>

