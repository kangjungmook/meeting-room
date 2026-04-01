<template>
  <div class="flex flex-col gap-4">

    <!-- 상단 툴바: 필터 탭 + 액션 -->
    <div class="flex items-center gap-3 flex-wrap">
      <div class="flex bg-white border border-slate-200 rounded-2xl p-1 gap-1">
        <button v-for="mt in manageTabs" :key="mt.key" @click="manageUserFilter = mt.key"
          :class="['flex items-center gap-2 px-3.5 py-2 rounded-xl text-[13px] font-bold transition-all',
            manageUserFilter === mt.key ? mt.activeClass : 'text-slate-500 hover:bg-slate-50']">
          {{ mt.label }}
          <span :class="['min-w-[18px] h-5 px-1.5 rounded-full text-[11px] font-bold flex items-center justify-center',
            manageUserFilter === mt.key ? mt.badgeActive : 'bg-slate-100 text-slate-500',
            mt.key === 'PENDING' && mt.count > 0 && manageUserFilter !== 'PENDING' ? 'bg-amber-100 text-amber-600 animate-pulse' : '']">
            {{ mt.count }}
          </span>
        </button>
      </div>

      <template v-if="manageUserFilter === 'PENDING'">
        <div class="flex items-center gap-2 text-[13px] text-slate-700">
          <input type="checkbox" :checked="allChecked" @change="toggleAll"
            class="w-4 h-4 rounded accent-indigo-500 cursor-pointer" />
          <span>{{ selectedIds.length > 0 ? `${selectedIds.length}명 선택됨` : '전체 선택' }}</span>
        </div>
        <button @click="bulkApprove" :disabled="filteredUsers.length === 0"
          class="px-3.5 py-2 rounded-xl text-[13px] font-bold text-emerald-600 bg-emerald-50 hover:bg-emerald-100 disabled:opacity-40 border border-emerald-200/60 transition-all">
          {{ selectedIds.length > 0 ? '선택 승인' : '전체 승인' }}
        </button>
        <button @click="bulkReject" :disabled="filteredUsers.length === 0"
          class="px-3.5 py-2 rounded-xl text-[13px] font-bold text-red-500 bg-red-50 hover:bg-red-100 disabled:opacity-40 border border-red-200/60 transition-all">
          {{ selectedIds.length > 0 ? '선택 거절' : '전체 거절' }}
        </button>
      </template>

      <div class="ml-auto flex items-center gap-2">
        <div v-if="manageUserFilter !== 'PENDING'" class="flex items-center gap-2 text-[13px] text-slate-700">
          <input type="checkbox" :checked="allChecked" @change="toggleAll"
            class="w-4 h-4 rounded accent-indigo-500 cursor-pointer" />
          <span>{{ selectedIds.length > 0 ? `${selectedIds.length}명 선택됨` : '전체 선택' }}</span>
        </div>
        <button @click="openCreateModal()"
          class="flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-[13px] font-bold text-white bg-indigo-500 hover:bg-indigo-600 transition-all">
          <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><path d="M6.5 2v9M2 6.5h9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          유저 생성
        </button>
      </div>
    </div>

    <!-- 데스크탑 테이블 -->
    <div v-if="!isMobile" class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
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
              <th class="text-left px-4 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">최근 접속</th>
              <th class="text-right px-5 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredUsers.length === 0">
              <td colspan="8" class="py-16 text-center">
                <div class="flex flex-col items-center gap-2">
                  <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="text-slate-200"><circle cx="18" cy="12" r="7" stroke="currentColor" stroke-width="2"/><path d="M4 36c0-7.7 6.3-14 14-14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M28 24l8 8M36 24l-8 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <p class="text-[14px] font-semibold text-slate-400">{{ manageUserFilter === 'PENDING' ? '승인 대기중인 회원이 없습니다' : '해당 회원이 없습니다' }}</p>
                </div>
              </td>
            </tr>
            <tr v-for="u in filteredUsers" :key="u.id"
              :class="['border-b border-slate-50 transition-colors group', selectedIds.includes(u.id) ? 'bg-indigo-50/40' : 'hover:bg-indigo-50/20']">
              <td class="px-5 py-3">
                <input type="checkbox" :checked="selectedIds.includes(u.id)" @change="toggleSelect(u.id)"
                  class="w-4 h-4 rounded accent-indigo-500 cursor-pointer" />
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <span :class="['w-2 h-2 rounded-full flex-shrink-0', onlineUsers.includes(u.employeeId) ? 'bg-emerald-400' : 'bg-slate-200']"></span>
                  <span class="text-[14px] font-semibold text-slate-800">{{ u.name }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-[14px] font-mono font-bold text-slate-700">{{ u.employeeId }}</td>
              <td class="px-4 py-3">
                <span :class="['px-2.5 py-1 rounded-lg text-[11px] font-bold', u.role === 'ADMIN' ? 'bg-violet-100 text-violet-600 border border-violet-200/60' : 'bg-slate-100 text-slate-500']">{{ u.role === 'ADMIN' ? 'ADMIN' : 'USER' }}</span>
              </td>
              <td class="px-4 py-3">
                <span v-if="u.fcmToken" class="flex items-center gap-1.5 text-[12px] font-bold text-emerald-500">
                  <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse flex-shrink-0"></span>ON
                </span>
                <span v-else class="text-[12px] text-slate-300 font-semibold">—</span>
              </td>
              <td class="px-4 py-3 text-[12px] text-slate-500">{{ dayjs(u.createdAt).format('MM/DD HH:mm') }}</td>
              <td class="px-4 py-3 text-[12px]">
                <span v-if="u.lastLoginAt" class="text-slate-500">{{ dayjs(u.lastLoginAt).format('MM/DD HH:mm') }}</span>
                <span v-else class="text-slate-300">—</span>
              </td>
              <td class="px-5 py-3 text-right">
                <div class="flex items-center justify-end gap-2">
                  <template v-if="u.status === 'APPROVED'">
                    <button @click="resetUserPassword(u)"
                      class="px-3 py-1.5 rounded-lg text-[12px] font-semibold text-amber-600 bg-amber-50 hover:bg-amber-100 border border-amber-200/60 transition-all">비번 초기화</button>
                    <div class="relative">
                      <button @click.stop="menuOpen = menuOpen === u.id ? null : u.id"
                        :class="['flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-[12px] font-semibold border transition-all',
                          menuOpen === u.id ? 'bg-slate-100 border-slate-300 text-slate-700' : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50']">
                        관리
                        <svg width="10" height="10" viewBox="0 0 10 10" fill="none" :class="menuOpen === u.id ? 'rotate-180' : ''" class="transition-transform duration-150">
                          <path d="M2 3.5l3 3 3-3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                      </button>
                      <div v-if="menuOpen === u.id"
                        class="absolute right-0 bottom-full mb-1.5 w-40 bg-white rounded-xl border border-slate-200 shadow-lg z-50 overflow-hidden py-1">
                        <button @click="openSetPasswordModal(u); menuOpen = null"
                          class="flex items-center gap-2.5 w-full px-3.5 py-2.5 text-[13px] font-semibold text-indigo-500 hover:bg-indigo-50 transition-colors">
                          <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><rect x="1" y="6" width="11" height="6.5" rx="1.5" stroke="currentColor" stroke-width="1.4"/><path d="M4 6V4a2.5 2.5 0 0 1 5 0v2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                          비번 변경
                        </button>
                        <button @click="changeRole(u); menuOpen = null"
                          :class="['flex items-center gap-2.5 w-full px-3.5 py-2.5 text-[13px] font-semibold transition-colors',
                            u.role === 'ADMIN' ? 'text-slate-500 hover:bg-slate-50' : 'text-violet-500 hover:bg-violet-50']">
                          <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><circle cx="6.5" cy="4.5" r="2.5" stroke="currentColor" stroke-width="1.4"/><path d="M1 12c0-3 2.5-5 5.5-5M10 8l1.5 1.5L14 7" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
                          {{ u.role === 'ADMIN' ? '권한 해제' : '관리자 설정' }}
                        </button>
                        <button v-if="u.fcmToken" @click="clearFcmToken(u); menuOpen = null"
                          class="flex items-center gap-2.5 w-full px-3.5 py-2.5 text-[13px] font-semibold text-slate-500 hover:bg-slate-50 transition-colors">
                          <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><path d="M6.5 1a4 4 0 0 1 4 4c0 2 .4 2.8.8 3.6H1.7C2.1 7.8 2.5 7 2.5 5a4 4 0 0 1 4-4z" stroke="currentColor" stroke-width="1.3"/><path d="M5 9a1.5 1.5 0 0 0 3 0M9.5 1.5l3 3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
                          FCM 초기화
                        </button>
                        <div class="h-px bg-slate-100 mx-2 my-1"></div>
                        <button @click="confirmDelete(u); menuOpen = null"
                          class="flex items-center gap-2.5 w-full px-3.5 py-2.5 text-[13px] font-semibold text-red-400 hover:bg-red-50 transition-colors">
                          <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><path d="M2 3.5h9M4.5 3.5V2.5h4v1M4 3.5l.5 7h4l.5-7" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                          삭제
                        </button>
                      </div>
                    </div>
                  </template>
                  <template v-if="u.status === 'PENDING'">
                    <button @click="approveUser(u)" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[12px] font-bold bg-emerald-500 text-white hover:bg-emerald-600 transition-all">
                      <svg width="11" height="11" viewBox="0 0 11 11" fill="none"><path d="M1.5 5.5l3 3 5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      승인
                    </button>
                    <button @click="openRejectModal(u)" class="px-3 py-1.5 rounded-lg text-[12px] font-bold border border-slate-200 text-slate-600 hover:bg-slate-100 transition-all">거절</button>
                    <button @click="confirmDelete(u)" class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 transition-all">
                      <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v3M8.5 6.5v3M3 4l.5 8a.5.5 0 0 0 .5.5h6a.5.5 0 0 0 .5-.5l.5-8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                    </button>
                  </template>
                  <template v-if="u.status === 'REJECTED'">
                    <button @click="approveUser(u)" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[12px] font-bold bg-emerald-500 text-white hover:bg-emerald-600 transition-all">재승인</button>
                    <button @click="confirmDelete(u)" class="px-3 py-1.5 rounded-lg text-[12px] font-semibold text-red-400 hover:bg-red-50 transition-all">삭제</button>
                  </template>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 모바일 카드 -->
    <div v-else class="flex flex-col gap-3">
      <div v-if="filteredUsers.length === 0" class="bg-white rounded-2xl border border-slate-200 py-12 flex flex-col items-center gap-2">
        <svg width="36" height="36" viewBox="0 0 40 40" fill="none" class="text-slate-200"><circle cx="18" cy="12" r="7" stroke="currentColor" stroke-width="2"/><path d="M4 36c0-7.7 6.3-14 14-14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M28 24l8 8M36 24l-8 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        <p class="text-[14px] font-semibold text-slate-400">{{ manageUserFilter === 'PENDING' ? '승인 대기중인 회원이 없습니다' : '해당 회원이 없습니다' }}</p>
      </div>
      <div v-for="u in filteredUsers" :key="u.id"
        :class="['rounded-2xl border p-4 transition-all', selectedIds.includes(u.id) ? 'bg-indigo-50/60 border-indigo-200' : 'bg-white border-slate-200']">
        <div class="flex items-center gap-3 mb-3">
          <input type="checkbox" :checked="selectedIds.includes(u.id)" @change="toggleSelect(u.id)"
            class="w-4 h-4 rounded accent-indigo-500 cursor-pointer flex-shrink-0" />
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <span :class="['w-2 h-2 rounded-full flex-shrink-0', onlineUsers.includes(u.employeeId) ? 'bg-emerald-400' : 'bg-slate-200']"></span>
              <span class="text-[15px] font-bold text-slate-800">{{ u.name }}</span>
              <span :class="['px-2 py-0.5 rounded-lg text-[10px] font-bold', u.role === 'ADMIN' ? 'bg-violet-100 text-violet-600' : 'bg-slate-100 text-slate-700']">{{ u.role }}</span>
              <span v-if="u.fcmToken" class="flex items-center gap-1 text-[11px] font-semibold text-emerald-500"><span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>FCM</span>
            </div>
            <p class="text-[12px] text-slate-600 mt-0.5">{{ u.employeeId }} · 가입 {{ dayjs(u.createdAt).format('MM/DD') }}<span v-if="u.lastLoginAt"> · 접속 {{ dayjs(u.lastLoginAt).format('MM/DD HH:mm') }}</span></p>
          </div>
        </div>
        <div class="flex gap-2 pt-3 border-t border-slate-50">
          <template v-if="u.status === 'APPROVED'">
            <button @click="resetUserPassword(u)" class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-amber-600 bg-amber-50 hover:bg-amber-100 transition-all">비번 초기화</button>
            <div class="relative">
              <button @click.stop="menuOpen = menuOpen === u.id ? null : u.id"
                :class="['px-4 py-2 rounded-xl text-[13px] font-semibold border transition-all flex items-center gap-1',
                  menuOpen === u.id ? 'bg-slate-100 border-slate-300 text-slate-700' : 'bg-white border-slate-200 text-slate-600']">
                관리
                <svg width="10" height="10" viewBox="0 0 10 10" fill="none" :class="menuOpen === u.id ? 'rotate-180' : ''" class="transition-transform">
                  <path d="M2 3.5l3 3 3-3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                </svg>
              </button>
              <div v-if="menuOpen === u.id"
                class="absolute right-0 bottom-full mb-1.5 w-40 bg-white rounded-xl border border-slate-200 shadow-lg z-50 overflow-hidden py-1">
                <button @click="openSetPasswordModal(u); menuOpen = null" class="flex items-center gap-2 w-full px-3.5 py-2.5 text-[13px] font-semibold text-indigo-500 hover:bg-indigo-50">비번 변경</button>
                <button @click="changeRole(u); menuOpen = null"
                  :class="['flex items-center gap-2 w-full px-3.5 py-2.5 text-[13px] font-semibold', u.role === 'ADMIN' ? 'text-slate-500 hover:bg-slate-50' : 'text-violet-500 hover:bg-violet-50']">
                  {{ u.role === 'ADMIN' ? '권한 해제' : '관리자 설정' }}
                </button>
                <button v-if="u.fcmToken" @click="clearFcmToken(u); menuOpen = null" class="flex items-center gap-2 w-full px-3.5 py-2.5 text-[13px] font-semibold text-slate-500 hover:bg-slate-50">FCM 초기화</button>
                <div class="h-px bg-slate-100 mx-2 my-1"></div>
                <button @click="confirmDelete(u); menuOpen = null" class="flex items-center gap-2 w-full px-3.5 py-2.5 text-[13px] font-semibold text-red-400 hover:bg-red-50">삭제</button>
              </div>
            </div>
          </template>
          <template v-if="u.status === 'PENDING'">
            <button @click="approveUser(u)" class="flex-1 py-2 rounded-xl text-[13px] font-bold text-emerald-600 bg-emerald-50 hover:bg-emerald-100 transition-all">승인</button>
            <button @click="openRejectModal(u)" class="flex-1 py-2 rounded-xl text-[13px] font-bold text-slate-600 bg-slate-50 hover:bg-slate-100 transition-all">거절</button>
            <button @click="confirmDelete(u)" class="w-10 py-2 rounded-xl text-[13px] font-bold text-red-400 bg-red-50 hover:bg-red-100 transition-all flex items-center justify-center">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v3M8.5 6.5v3M3 4l.5 8a.5.5 0 0 0 .5.5h6a.5.5 0 0 0 .5-.5l.5-8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
            </button>
          </template>
          <template v-if="u.status === 'REJECTED'">
            <button @click="approveUser(u)" class="flex-1 py-2 rounded-xl text-[13px] font-bold text-emerald-600 bg-emerald-50 hover:bg-emerald-100 transition-all">재승인</button>
            <button @click="confirmDelete(u)" class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-red-400 bg-red-50 hover:bg-red-100 transition-all">삭제</button>
          </template>
        </div>
      </div>
    </div>

    <!-- 유저 생성 모달 -->
    <div v-if="createModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
          <h3 class="text-[17px] font-bold text-slate-800">유저 생성</h3>
          <button @click="createModal.show = false" class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 transition-all">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <form @submit.prevent="submitCreate" class="px-6 py-5 flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">사번</label>
            <input v-model="createModal.form.employeeId" type="text" required placeholder="예) EMP001"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">이름</label>
            <input v-model="createModal.form.name" type="text" required placeholder="예) 홍길동"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">비밀번호</label>
            <input v-model="createModal.form.password" type="password" required placeholder="4자 이상"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div class="flex flex-col gap-1.5">
              <label class="text-[13px] font-bold text-slate-700">역할</label>
              <select v-model="createModal.form.role"
                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer">
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
            <div class="flex flex-col gap-1.5">
              <label class="text-[13px] font-bold text-slate-700">초기 상태</label>
              <select v-model="createModal.form.status"
                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer">
                <option value="APPROVED">승인됨</option>
                <option value="PENDING">대기중</option>
              </select>
            </div>
          </div>
          <div class="flex gap-3 pt-1">
            <button type="button" @click="createModal.show = false"
              class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
            <button type="submit"
              class="flex-[2] py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[14px] font-bold transition-all">생성</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 비밀번호 설정 모달 -->
    <div v-if="pwModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
          <div>
            <h3 class="text-[17px] font-bold text-slate-800">비밀번호 설정</h3>
            <p class="text-[13px] text-slate-600 mt-0.5">{{ pwModal.userName }}</p>
          </div>
          <button @click="pwModal.show = false" class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 transition-all">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <form @submit.prevent="submitSetPassword" class="px-6 py-5 flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">새 비밀번호</label>
            <input v-model="pwModal.password" type="password" required placeholder="4자 이상"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">비밀번호 확인</label>
            <input v-model="pwModal.confirm" type="password" required
              :class="['w-full bg-slate-50 border rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 transition-all',
                pwModal.confirm && pwModal.password !== pwModal.confirm ? 'border-red-300 focus:ring-red-300' : 'border-slate-200 focus:ring-indigo-400']" />
            <p v-if="pwModal.confirm && pwModal.password !== pwModal.confirm"
              class="text-[12px] text-red-400 font-semibold">비밀번호가 일치하지 않습니다</p>
          </div>
          <div class="flex gap-3 pt-1">
            <button type="button" @click="pwModal.show = false"
              class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
            <button type="submit"
              class="flex-[2] py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[14px] font-bold transition-all">설정</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 거절 사유 입력 모달 -->
    <div v-if="rejectModal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="px-6 py-5 border-b border-slate-100">
          <h3 class="text-[17px] font-bold text-slate-800">가입 거절</h3>
          <p class="text-[13px] text-slate-500 mt-0.5">
            <span class="font-semibold text-slate-700">{{ rejectModal.isBulk ? `${rejectModal.targets.length}명` : rejectModal.target?.name }}</span>
            의 가입을 거절합니다
          </p>
        </div>
        <div class="px-6 py-5 flex flex-col gap-3">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">거절 사유 <span class="text-slate-400 font-normal">(선택)</span></label>
            <textarea v-model="rejectModal.reason" rows="3" placeholder="예) 부서 정보가 일치하지 않습니다."
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-[14px] text-slate-700 outline-none focus:ring-2 focus:ring-red-300 resize-none placeholder:text-slate-300">
            </textarea>
            <p class="text-[11px] text-slate-400">입력한 사유는 해당 사용자가 로그인 시 확인할 수 있습니다.</p>
          </div>
        </div>
        <div class="flex gap-3 px-6 pb-6">
          <button @click="rejectModal.show = false; rejectModal.reason = ''"
            class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
          <button @click="submitReject"
            class="flex-1 py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white text-[14px] font-bold transition-all">거절 확인</button>
        </div>
      </div>
    </div>

    <!-- 유저 삭제 확인 모달 -->
    <div v-if="deleteConfirm.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="px-7 pt-7 pb-5">
          <div class="w-12 h-12 rounded-2xl bg-red-50 flex items-center justify-center mb-4">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none"><path d="M11 8v4M11 15h.01M3 11a8 8 0 1 0 16 0 8 8 0 0 0-16 0z" stroke="#ef4444" stroke-width="1.8" stroke-linecap="round"/></svg>
          </div>
          <h3 class="text-[17px] font-bold text-slate-800">유저를 삭제할까요?</h3>
          <p class="text-[14px] text-slate-700 mt-2 leading-relaxed">
            <span class="font-bold text-slate-600">{{ deleteConfirm.target?.name }}</span>
            ({{ deleteConfirm.target?.employeeId }}) 유저가 삭제됩니다.
            <span class="text-red-500 font-bold">되돌릴 수 없습니다.</span>
          </p>
        </div>
        <div class="flex gap-3 px-7 pb-7">
          <button @click="deleteConfirm.show = false"
            class="flex-1 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
          <button @click="doDelete"
            class="flex-1 py-3 rounded-xl bg-red-500 hover:bg-red-600 text-white text-[14px] font-bold transition-all">삭제</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { useAdmin } from '../../composables/useAdmin';

const {
  filteredUsers, manageTabs, manageUserFilter,
  approveUser, rejectUser, resetUserPassword, changeRole, deleteUser, clearFcmToken, createUser, setUserPassword,
  onlineUsers,
  dayjs,
} = useAdmin();

const props = defineProps({ isMobile: Boolean });

// ── Selection ──
const selectedIds = ref([]);
watch(manageUserFilter, () => { selectedIds.value = []; });

const allChecked = computed(() =>
  filteredUsers.value.length > 0 && filteredUsers.value.every(u => selectedIds.value.includes(u.id))
);

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id);
  if (idx === -1) selectedIds.value.push(id);
  else selectedIds.value.splice(idx, 1);
};

const toggleAll = () => {
  if (allChecked.value) selectedIds.value = [];
  else selectedIds.value = filteredUsers.value.map(u => u.id);
};

const bulkApprove = async () => {
  const targets = selectedIds.value.length > 0 ? selectedIds.value : filteredUsers.value.map(u => u.id);
  await Promise.all(targets.map(id => approveUser({ id })));
  selectedIds.value = [];
};

const bulkReject = () => {
  const targets = selectedIds.value.length > 0
    ? filteredUsers.value.filter(u => selectedIds.value.includes(u.id))
    : filteredUsers.value;
  rejectModal.isBulk  = true;
  rejectModal.targets = targets;
  rejectModal.target  = null;
  rejectModal.reason  = '';
  rejectModal.show    = true;
};

// ── Reject modal ──
const rejectModal = reactive({ show: false, target: null, targets: [], isBulk: false, reason: '' });

const openRejectModal = (u) => {
  rejectModal.isBulk  = false;
  rejectModal.target  = u;
  rejectModal.targets = [];
  rejectModal.reason  = '';
  rejectModal.show    = true;
};

const submitReject = async () => {
  const reason = rejectModal.reason.trim();
  if (rejectModal.isBulk) {
    await Promise.all(rejectModal.targets.map(u => rejectUser(u, reason)));
    selectedIds.value = [];
  } else {
    await rejectUser(rejectModal.target, reason);
  }
  rejectModal.show   = false;
  rejectModal.reason = '';
};

// ── Dropdown menu ──
const menuOpen = ref(null);

// ── Create modal ──
const createModal = reactive({
  show: false,
  form: { employeeId: '', name: '', password: '', role: 'USER', status: 'APPROVED' },
});

const openCreateModal = (defaults = {}) => {
  createModal.form = { employeeId: '', name: '', password: '', role: 'USER', status: 'APPROVED', ...defaults };
  createModal.show = true;
};

const submitCreate = async () => {
  try {
    if (createModal.form.password.length < 4) { alert('비밀번호는 4자 이상이어야 합니다.'); return; }
    await createUser(createModal.form);
    createModal.show = false;
  } catch (e) {
    alert(e.response?.data?.message || '생성 중 오류가 발생했습니다.');
  }
};

// ── Set password modal ──
const pwModal = reactive({ show: false, userId: null, userName: '', password: '', confirm: '' });

const openSetPasswordModal = (u) => {
  pwModal.userId   = u.id;
  pwModal.userName = u.name;
  pwModal.password = '';
  pwModal.confirm  = '';
  pwModal.show     = true;
};

const submitSetPassword = async () => {
  if (pwModal.password.length < 4) { alert('비밀번호는 4자 이상이어야 합니다.'); return; }
  if (pwModal.password !== pwModal.confirm) { alert('비밀번호가 일치하지 않습니다.'); return; }
  await setUserPassword(pwModal.userId, pwModal.password);
  pwModal.show = false;
  alert(`${pwModal.userName} 님의 비밀번호가 설정되었습니다.`);
};

// ── Delete confirm ──
const deleteConfirm = reactive({ show: false, target: null });

const confirmDelete = (u) => {
  deleteConfirm.target = u;
  deleteConfirm.show   = true;
};

const doDelete = async () => {
  if (!deleteConfirm.target) return;
  await deleteUser(deleteConfirm.target.id);
  deleteConfirm.show   = false;
  deleteConfirm.target = null;
};
</script>
