<template>
  <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
    <div class="overflow-x-auto">
      <table class="w-full">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100">
            <th class="px-5 py-3 w-10"></th>
            <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">회원</th>
            <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">사번</th>
            <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">역할</th>
            <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">FCM</th>
            <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">가입일</th>
            <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">접속 현황</th>
            <th class="text-right px-5 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="users.length === 0">
            <td colspan="8" class="py-16 text-center">
              <div class="flex flex-col items-center gap-2">
                <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="text-slate-200">
                  <circle cx="18" cy="12" r="7" stroke="currentColor" stroke-width="2" />
                  <path d="M4 36c0-7.7 6.3-14 14-14" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                  <path d="M28 24l8 8M36 24l-8 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                </svg>
                <p class="text-[14px] font-semibold text-slate-400">
                  {{ effectiveFilter === 'PENDING' ? '승인 대기중인 회원이 없습니다' : '해당 회원이 없습니다' }}
                </p>
              </div>
            </td>
          </tr>

          <tr
            v-for="u in users"
            :key="u.id"
            :class="[
              'border-b border-slate-50 transition-colors group',
              selectedIds.includes(u.id) ? 'bg-indigo-50/40' : 'hover:bg-indigo-50/20',
            ]"
          >
            <td class="px-5 py-3">
              <input
                type="checkbox"
                :checked="selectedIds.includes(u.id)"
                class="w-4 h-4 rounded accent-indigo-500 cursor-pointer"
                @change="$emit('toggleSelect', u.id)"
              />
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <span
                  :class="[
                    'w-2 h-2 rounded-full flex-shrink-0',
                    onlineEmployeeIds.includes(u.employeeId) ? 'bg-emerald-400' : 'bg-slate-200',
                  ]"
                ></span>
                <span class="text-[14px] font-semibold text-slate-800">{{ u.name }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-[14px] font-mono font-bold text-slate-700">{{ u.employeeId }}</td>
            <td class="px-4 py-3">
              <span
                :class="[
                  'px-2.5 py-1 rounded-lg text-[11px] font-bold',
                  u.role === 'ADMIN'
                    ? 'bg-violet-100 text-violet-600 border border-violet-200/60'
                    : 'bg-slate-100 text-slate-500',
                ]"
              >{{ u.role === 'ADMIN' ? 'ADMIN' : 'USER' }}</span>
            </td>
            <td class="px-4 py-3">
              <span v-if="u.fcmToken" class="flex items-center gap-1.5 text-[12px] font-bold text-emerald-500">
                <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse flex-shrink-0"></span>ON
              </span>
              <span v-else class="text-[12px] text-slate-300 font-semibold">—</span>
            </td>
            <td class="px-4 py-3 text-[12px] text-slate-500">{{ fmtMDHM(dayjs, u.createdAt) }}</td>
            <td class="px-4 py-3">
              <template v-if="onlineEmployeeIds.includes(u.employeeId)">
                <span class="flex items-center gap-1.5 text-[12px] font-bold text-emerald-500">
                  <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse flex-shrink-0"></span>온라인
                </span>
                <span v-if="u.lastLoginAt" class="block text-[11px] text-slate-400 tabular-nums mt-0.5">
                  접속 {{ fmtMDHM(dayjs, u.lastLoginAt) }}
                </span>
              </template>
              <template v-else-if="u.lastLoginAt || u.lastLogoutAt">
                <span class="text-[12px] text-slate-400">오프라인</span>
                <span v-if="u.lastLoginAt" class="block text-[11px] text-slate-400 tabular-nums mt-0.5">
                  접속 {{ fmtMDHM(dayjs, u.lastLoginAt) }}
                </span>
                <span v-if="u.lastLogoutAt" class="block text-[11px] text-slate-400 tabular-nums">
                  종료 {{ fmtMDHM(dayjs, u.lastLogoutAt) }}
                </span>
              </template>
              <template v-else>
                <span class="text-slate-300 text-[12px]">—</span>
              </template>
            </td>

            <td class="px-5 py-3 text-right">
              <div class="flex items-center justify-end gap-2">
                <template v-if="u.status === 'APPROVED'">
                  <button
                    type="button"
                    class="px-3 py-1.5 rounded-lg text-[12px] font-semibold text-amber-600 bg-amber-50 hover:bg-amber-100 border border-amber-200/60 transition-all"
                    @click="$emit('openSetPasswordModal', u)"
                  >비번 초기화</button>
                  <div class="relative">
                    <button
                      type="button"
                      @click.stop="setMenuOpen(u.id, $event)"
                      :class="[
                        'flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-[12px] font-semibold border transition-all',
                        menuOpen === u.id
                          ? 'bg-slate-100 border-slate-300 text-slate-700'
                          : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50',
                      ]"
                    >
                      관리
                      <AppIcon
                        name="chevron-down"
                        :size="10"
                        :class="menuOpen === u.id ? 'rotate-180' : ''"
                        cls="transition-transform duration-150"
                      />
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
                            'flex items-center gap-2.5 w-full px-3.5 py-2.5 text-[13px] font-semibold transition-colors',
                            u.role === 'ADMIN' ? 'text-slate-500 hover:bg-slate-50' : 'text-violet-500 hover:bg-violet-50',
                          ]"
                        >
                          <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
                            <circle cx="6.5" cy="4.5" r="2.5" stroke="currentColor" stroke-width="1.4" />
                            <path d="M1 12c0-3 2.5-5 5.5-5M10 8l1.5 1.5L14 7" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                          </svg>
                          {{ u.role === 'ADMIN' ? '권한 해제' : '관리자 설정' }}
                        </button>
                        <button
                          v-if="u.fcmToken"
                          type="button"
                          class="flex items-center gap-2.5 w-full px-3.5 py-2.5 text-[13px] font-semibold text-slate-500 hover:bg-slate-50 transition-colors"
                          @click="$emit('clearFcmToken', u); menuOpen = null"
                        >
                          <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
                            <path d="M6.5 1a4 4 0 0 1 4 4c0 2 .4 2.8.8 3.6H1.7C2.1 7.8 2.5 7 2.5 5a4 4 0 0 1 4-4z" stroke="currentColor" stroke-width="1.3" />
                            <path d="M5 9a1.5 1.5 0 0 0 3 0M9.5 1.5l3 3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" />
                          </svg>
                          FCM 초기화
                        </button>
                        <div class="h-px bg-slate-100 mx-2 my-1"></div>
                        <button
                          type="button"
                          class="flex items-center gap-2.5 w-full px-3.5 py-2.5 text-[13px] font-semibold text-red-400 hover:bg-red-50 transition-colors"
                          @click="$emit('confirmDelete', u); menuOpen = null"
                        >
                          <AppIcon name="trash" :size="13" />
                          삭제
                        </button>
                      </div>
                    </Teleport>
                  </div>
                </template>

                <template v-if="u.status === 'PENDING'">
                  <button
                    type="button"
                    class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[12px] font-bold bg-emerald-500 text-white hover:bg-emerald-600 transition-all"
                    @click="$emit('approveUser', u)"
                  >
                    <AppIcon name="check" :size="11" />
                    승인
                  </button>
                  <button
                    type="button"
                    class="px-3 py-1.5 rounded-lg text-[12px] font-bold border border-slate-200 text-slate-600 hover:bg-slate-100 transition-all"
                    @click="$emit('openRejectModal', u)"
                  >거절</button>
                  <button
                    type="button"
                    class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 transition-all"
                    @click="$emit('confirmDelete', u)"
                  >
                    <AppIcon name="trash" :size="14" />
                  </button>
                </template>

                <template v-if="u.status === 'REJECTED'">
                  <button
                    type="button"
                    class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[12px] font-bold bg-emerald-500 text-white hover:bg-emerald-600 transition-all"
                    @click="$emit('approveUser', u)"
                  >재승인</button>
                  <button
                    type="button"
                    class="px-3 py-1.5 rounded-lg text-[12px] font-semibold text-red-400 hover:bg-red-50 transition-all"
                    @click="$emit('confirmDelete', u)"
                  >삭제</button>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import AppIcon from '../../icons/AppIcon.vue';
import { fmtMDHM } from '../../../utils/datetime';
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

