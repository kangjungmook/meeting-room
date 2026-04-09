<template>
  <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
    <div
      v-for="card in statCards"
      :key="card.label"
      class="bg-white rounded-2xl border border-slate-100 px-4 py-3.5 flex items-center gap-3 hover:border-slate-200 hover:shadow-sm transition-all"
    >
      <div class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0" :class="card.iconBg">
        <span v-html="card.icon"></span>
      </div>
      <div>
        <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wide">{{ card.label }}</p>
        <p class="text-[26px] font-black leading-none" :class="card.valueClass">{{ card.value }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  users: { type: Array, required: true },
  currentEmployeeId: { type: String, required: true },
  onlineEmployeeIds: { type: Array, required: true },
  effectiveFilter: { type: String, required: true },
});

const statCards = computed(() => {
  const base = props.users.filter(u => u.employeeId !== props.currentEmployeeId);
  const onlineCnt = base.filter(u => props.onlineEmployeeIds.includes(u.employeeId)).length;
  const pendingCnt = base.filter(u => u.status === 'PENDING' && u.role !== 'ADMIN').length;
  const approvCnt = base.filter(u => u.status === 'APPROVED').length;
  const rejectCnt = base.filter(u => u.status === 'REJECTED' && u.role !== 'ADMIN').length;
  const adminCnt = base.filter(u => u.role === 'ADMIN').length;
  const fcmCnt = base.filter(u => u.fcmToken).length;

  if (props.effectiveFilter === 'PENDING') return [
    { label: '대기 중', value: pendingCnt, valueClass: pendingCnt ? 'text-amber-500' : 'text-slate-700', iconBg: pendingCnt ? 'bg-amber-50' : 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7.5" stroke="#f59e0b" stroke-width="1.6"/><path d="M9 5.5v4l2.5 2.5" stroke="#f59e0b" stroke-width="1.6" stroke-linecap="round"/></svg>' },
    { label: '전체 회원', value: base.length, valueClass: 'text-slate-700', iconBg: 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="7" cy="6" r="3" stroke="#64748b" stroke-width="1.6"/><path d="M1 16c0-3.3 2.7-6 6-6" stroke="#64748b" stroke-width="1.6" stroke-linecap="round"/><circle cx="13" cy="7" r="2.5" stroke="#64748b" stroke-width="1.4"/><path d="M11 16c0-2.5 0.9-4.5 2-4.5s2 2 2 4.5" stroke="#64748b" stroke-width="1.4" stroke-linecap="round"/></svg>' },
    { label: '승인됨', value: approvCnt, valueClass: 'text-emerald-600', iconBg: 'bg-emerald-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#10b981" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#10b981" stroke-width="1.6" stroke-linecap="round"/></svg>' },
    { label: '거절됨', value: rejectCnt, valueClass: rejectCnt ? 'text-red-500' : 'text-slate-400', iconBg: rejectCnt ? 'bg-red-50' : 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#ef4444" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#ef4444" stroke-width="1.6" stroke-linecap="round"/></svg>' },
  ];

  if (props.effectiveFilter === 'APPROVED') return [
    { label: '승인 회원', value: approvCnt, valueClass: 'text-emerald-600', iconBg: 'bg-emerald-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#10b981" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#10b981" stroke-width="1.6" stroke-linecap="round"/></svg>' },
    { label: '온라인', value: onlineCnt, valueClass: onlineCnt ? 'text-emerald-600' : 'text-slate-400', iconBg: onlineCnt ? 'bg-emerald-50' : 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="4" fill="#10b981" opacity="0.2"/><circle cx="9" cy="9" r="2.5" fill="#10b981"/></svg>' },
    { label: '관리자', value: adminCnt, valueClass: 'text-violet-600', iconBg: 'bg-violet-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#7c3aed" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#7c3aed" stroke-width="1.6" stroke-linecap="round"/></svg>' },
    { label: 'FCM 활성', value: fcmCnt, valueClass: fcmCnt ? 'text-amber-500' : 'text-slate-400', iconBg: fcmCnt ? 'bg-amber-50' : 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><path d="M9 1a5.5 5.5 0 0 1 5.5 5.5c0 2.5.5 3.5 1 4.5H2.5c.5-1 1-2 1-5A5.5 5.5 0 0 1 9 1z" stroke="#f59e0b" stroke-width="1.5"/><path d="M7 12a2 2 0 0 0 4 0" stroke="#f59e0b" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  ];

  if (props.effectiveFilter === 'REJECTED') return [
    { label: '거절됨', value: rejectCnt, valueClass: rejectCnt ? 'text-red-500' : 'text-slate-400', iconBg: rejectCnt ? 'bg-red-50' : 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#ef4444" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#ef4444" stroke-width="1.6" stroke-linecap="round"/></svg>' },
    { label: '전체 회원', value: base.length, valueClass: 'text-slate-700', iconBg: 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="7" cy="6" r="3" stroke="#64748b" stroke-width="1.6"/><path d="M1 16c0-3.3 2.7-6 6-6" stroke="#64748b" stroke-width="1.6" stroke-linecap="round"/><circle cx="13" cy="7" r="2.5" stroke="#64748b" stroke-width="1.4"/><path d="M11 16c0-2.5 0.9-4.5 2-4.5s2 2 2 4.5" stroke="#64748b" stroke-width="1.4" stroke-linecap="round"/></svg>' },
    { label: '대기 중', value: pendingCnt, valueClass: pendingCnt ? 'text-amber-500' : 'text-slate-400', iconBg: pendingCnt ? 'bg-amber-50' : 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7.5" stroke="#f59e0b" stroke-width="1.6"/><path d="M9 5.5v4l2.5 2.5" stroke="#f59e0b" stroke-width="1.6" stroke-linecap="round"/></svg>' },
    { label: '승인됨', value: approvCnt, valueClass: 'text-emerald-600', iconBg: 'bg-emerald-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#10b981" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#10b981" stroke-width="1.6" stroke-linecap="round"/></svg>' },
  ];

  return [
    { label: '전체 회원', value: base.length, valueClass: 'text-indigo-600', iconBg: 'bg-indigo-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="7" cy="6" r="3" stroke="#6366f1" stroke-width="1.6"/><path d="M1 16c0-3.3 2.7-6 6-6" stroke="#6366f1" stroke-width="1.6" stroke-linecap="round"/><circle cx="13" cy="7" r="2.5" stroke="#6366f1" stroke-width="1.4"/><path d="M11 16c0-2.5 0.9-4.5 2-4.5s2 2 2 4.5" stroke="#6366f1" stroke-width="1.4" stroke-linecap="round"/></svg>' },
    { label: '온라인', value: onlineCnt, valueClass: onlineCnt ? 'text-emerald-600' : 'text-slate-400', iconBg: onlineCnt ? 'bg-emerald-50' : 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="4" fill="#10b981" opacity="0.2"/><circle cx="9" cy="9" r="2.5" fill="#10b981"/></svg>' },
    { label: '가입 대기', value: pendingCnt, valueClass: pendingCnt ? 'text-amber-500' : 'text-slate-400', iconBg: pendingCnt ? 'bg-amber-50' : 'bg-slate-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7.5" stroke="#f59e0b" stroke-width="1.6"/><path d="M9 5.5v4l2.5 2.5" stroke="#f59e0b" stroke-width="1.6" stroke-linecap="round"/></svg>' },
    { label: '관리자', value: adminCnt, valueClass: 'text-violet-600', iconBg: 'bg-violet-50', icon: '<svg width="17" height="17" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="6" r="3.5" stroke="#7c3aed" stroke-width="1.6"/><path d="M2 17c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="#7c3aed" stroke-width="1.6" stroke-linecap="round"/></svg>' },
  ];
});
</script>

