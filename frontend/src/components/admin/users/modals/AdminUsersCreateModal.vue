<template>
  <BaseModal :show="show" @close="$emit('close')">
    <template #header>
      <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
        <h3 class="text-[17px] font-bold text-slate-800">유저 생성</h3>
        <button
          type="button"
          class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 transition-all"
          @click="$emit('close')"
        >
          <AppIcon name="close" :size="16" />
        </button>
      </div>
    </template>

      <form class="px-6 py-5 flex flex-col gap-4" @submit.prevent="$emit('submit')">
        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-700">사번</label>
          <input
            :value="form.employeeId"
            type="text"
            required
            placeholder="예) EMP001"
            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400"
            @input="$emit('update:form', { ...form, employeeId: $event.target.value })"
          />
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-700">이름</label>
          <input
            :value="form.name"
            type="text"
            required
            placeholder="예) 홍길동"
            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400"
            @input="$emit('update:form', { ...form, name: $event.target.value })"
          />
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-700">비밀번호</label>
          <input
            :value="form.password"
            type="password"
            required
            placeholder="4자 이상"
            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400"
            @input="$emit('update:form', { ...form, password: $event.target.value })"
          />
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">역할</label>
            <select
              :value="form.role"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer"
              @change="$emit('update:form', { ...form, role: $event.target.value })"
            >
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">초기 상태</label>
            <select
              :value="form.status"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer"
              @change="$emit('update:form', { ...form, status: $event.target.value })"
            >
              <option value="APPROVED">승인됨</option>
              <option value="PENDING">대기중</option>
            </select>
          </div>
        </div>

        <p v-if="error" class="text-[12px] font-semibold text-red-500 -mt-1">{{ error }}</p>

        <div class="flex gap-3 pt-1">
          <button
            type="button"
            class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all"
            @click="$emit('close')"
          >취소</button>
          <button
            type="submit"
            class="flex-[2] py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[14px] font-bold transition-all"
          >생성</button>
        </div>
      </form>
  </BaseModal>
</template>

<script setup>
import AppIcon from '../../../icons/AppIcon.vue';
import BaseModal from '../../../ui/BaseModal.vue';

defineProps({
  show: { type: Boolean, required: true },
  form: { type: Object, required: true },
  error: { type: String, default: '' },
});

defineEmits(['close', 'submit', 'update:form']);
</script>

