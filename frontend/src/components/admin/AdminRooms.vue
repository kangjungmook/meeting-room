<template>
  <div>
    <AdminRoomsTable
      v-if="!isMobile"
      :rooms="allRooms"
      @toggle="toggleRoom"
      @edit="openModal"
      @delete="deleteRoom"
    />
    <AdminRoomsCards
      v-else
      :rooms="allRooms"
      @toggle="toggleRoom"
      @edit="openModal"
      @delete="deleteRoom"
    />

    <!-- 회의실 추가/수정 모달 -->
    <div v-if="modal.show" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white w-full max-w-sm rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
          <h3 class="text-[17px] font-bold text-slate-800">{{ modal.isEdit ? '회의실 수정' : '회의실 추가' }}</h3>
          <button @click="modal.show = false" class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 transition-all">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 3l10 10M13 3L3 13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <form @submit.prevent="submitModal" class="px-6 py-5 flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">회의실 이름 <span class="text-red-400">*</span></label>
            <input v-model="modal.form.name" type="text" required placeholder="예) A 회의실"
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">수용 인원</label>
            <input v-model.number="modal.form.capacity" type="number" min="1" required
              class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-[13px] font-bold text-slate-700">색상 코드</label>
            <div class="flex items-center gap-3">
              <input v-model="modal.form.colorCode" type="color"
                class="w-12 h-10 rounded-lg cursor-pointer border border-slate-200 p-0.5" />
              <input v-model="modal.form.colorCode" type="text" placeholder="#6366f1"
                class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[14px] font-mono text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400" />
            </div>
          </div>
          <div class="flex items-center justify-between px-4 py-3 bg-slate-50 rounded-xl border border-slate-200">
            <span class="text-[14px] font-semibold text-slate-700">활성화</span>
            <ToggleSwitch v-model="modal.form.isActive" inactive-class="bg-slate-300" />
          </div>
          <div class="flex gap-3 pt-1">
            <button type="button" @click="modal.show = false"
              class="flex-1 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-[14px] font-bold transition-all">취소</button>
            <button type="submit"
              class="flex-[2] py-2.5 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white text-[14px] font-bold transition-all">
              {{ modal.isEdit ? '수정 완료' : '추가' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useAdmin } from '../../composables/useAdmin';
import AdminRoomsTable from './rooms/AdminRoomsTable.vue';
import AdminRoomsCards from './rooms/AdminRoomsCards.vue';
import ToggleSwitch from '../ui/ToggleSwitch.vue';

const { allRooms, createRoom, updateRoom, deleteRoom, toggleRoom } = useAdmin();
const props = defineProps({ isMobile: Boolean });

const modal = reactive({
  show: false, isEdit: false,
  form: { id: null, name: '', capacity: 1, colorCode: '#6366f1', isActive: true },
});

const openModal = (room = null) => {
  if (room) {
    modal.isEdit = true;
    modal.form = { id: room.id, name: room.name, capacity: room.capacity, colorCode: room.colorCode, isActive: room.isActive };
  } else {
    modal.isEdit = false;
    modal.form = { id: null, name: '', capacity: 1, colorCode: '#6366f1', isActive: true };
  }
  modal.show = true;
};

const submitModal = async () => {
  try {
    if (modal.isEdit) await updateRoom(modal.form.id, modal.form);
    else await createRoom(modal.form);
    modal.show = false;
  } catch { alert('오류가 발생했습니다.'); }
};

defineExpose({ openModal });
</script>
