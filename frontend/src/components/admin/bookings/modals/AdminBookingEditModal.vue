<template>
  <BaseModal :show="show" :max-width-class="'max-w-md'" @close="$emit('close')">
    <template #header>
      <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
        <h3 class="text-[17px] font-bold text-slate-800">예약 수정</h3>
        <button
          type="button"
          @click="$emit('close')"
          class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 transition-all"
        >
          <AppIcon name="close" :size="16" />
        </button>
      </div>
    </template>

    <form @submit.prevent="$emit('submit')" class="px-6 py-5 flex flex-col gap-4">
      <div class="flex flex-col gap-1.5">
        <label class="text-[13px] font-bold text-slate-700">제목</label>
        <input
          v-model="form.title"
          type="text"
          required
          class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent"
        />
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-[13px] font-bold text-slate-700">회의실</label>
        <select
          v-model="form.roomId"
          required
          class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400 cursor-pointer"
        >
          <option v-for="r in allRooms" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-[13px] font-bold text-slate-700">날짜</label>
        <DatePicker v-model="form.date" placeholder="날짜 선택" />
      </div>
      <div class="grid grid-cols-2 gap-3">
        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-700">시작 시간</label>
          <input
            v-model="form.startClock"
            type="time"
            required
            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2.5 text-[14px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-700">종료 시간</label>
          <input
            v-model="form.endClock"
            type="time"
            required
            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2.5 text-[14px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-[13px] font-bold text-slate-700">예약자</label>
        <input
          v-model="form.organizer"
          type="text"
          class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400"
        />
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-[13px] font-bold text-slate-700">참석자 ID (쉼표 구분 숫자)</label>
        <input
          v-model="form.attendeeIdsStr"
          type="text"
          placeholder="예) 2,3,5"
          class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400"
        />
      </div>
      <p v-if="error" class="text-[12px] font-semibold text-red-500 -mt-1">{{ error }}</p>
      <div class="flex gap-3 pt-1">
        <button
          type="button"
          @click="$emit('close')"
          class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all"
        >취소</button>
        <button
          type="submit"
          :disabled="isSubmitting"
          class="flex-[2] py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 disabled:opacity-50 disabled:cursor-not-allowed text-white text-[14px] font-bold transition-all"
        >
          {{ isSubmitting ? '수정 중...' : '수정 완료' }}
        </button>
      </div>
    </form>
  </BaseModal>
</template>

<script setup>
import BaseModal from '../../../ui/BaseModal.vue';
import AppIcon from '../../../icons/AppIcon.vue';
import DatePicker from '../../../ui/DatePicker.vue';

defineProps({
  show: { type: Boolean, required: true },
  form: { type: Object, required: true },
  error: { type: String, default: '' },
  isSubmitting: { type: Boolean, default: false },
  allRooms: { type: Array, required: true },
});

defineEmits(['close', 'submit']);
</script>

