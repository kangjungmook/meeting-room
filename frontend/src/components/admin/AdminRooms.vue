<template>
  <div>
    <!-- 데스크탑 테이블 -->
    <div v-if="!isMobile" class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-100">
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">회의실명</th>
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">수용인원</th>
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">색상</th>
              <th class="text-left px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">상태</th>
              <th class="text-right px-6 py-3 text-[11px] font-bold text-slate-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="allRooms.length === 0">
              <td colspan="5" class="py-16 text-center">
                <div class="flex flex-col items-center gap-2">
                  <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="text-slate-200"><rect x="4" y="8" width="32" height="24" rx="4" stroke="currentColor" stroke-width="2"/><path d="M12 8V6M28 8V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <p class="text-[14px] font-semibold text-slate-400">회의실이 없습니다</p>
                </div>
              </td>
            </tr>
            <tr v-for="room in allRooms" :key="room.id"
              class="border-b border-slate-50 hover:bg-indigo-50/20 transition-colors group">
              <td class="px-6 py-3.5">
                <div class="flex items-center gap-3">
                  <span class="w-8 h-8 rounded-xl flex-shrink-0 border border-slate-200/80 shadow-sm" :style="{background: room.colorCode}"></span>
                  <span class="text-[15px] font-bold text-slate-800">{{ room.name }}</span>
                </div>
              </td>
              <td class="px-6 py-3.5">
                <span class="flex items-center gap-1 text-[14px] text-slate-600">
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="text-slate-400"><circle cx="7" cy="5.5" r="2.5" stroke="currentColor" stroke-width="1.4"/><path d="M2 12c0-2.8 2.2-5 5-5s5 2.2 5 5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                  {{ room.capacity }}인
                </span>
              </td>
              <td class="px-6 py-3.5">
                <div class="flex items-center gap-2">
                  <span class="w-5 h-5 rounded-md border border-slate-200/80 shadow-sm" :style="{background: room.colorCode}"></span>
                  <span class="text-[12px] text-slate-500 font-mono">{{ room.colorCode }}</span>
                </div>
              </td>
              <td class="px-6 py-3.5">
                <button @click="toggleRoom(room)"
                  :class="['flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[12px] font-bold transition-all',
                    room.isActive ? 'bg-emerald-50 text-emerald-600 hover:bg-emerald-100 border border-emerald-200/60'
                                  : 'bg-slate-100 text-slate-500 hover:bg-slate-200 border border-slate-200/60']">
                  <span :class="['w-1.5 h-1.5 rounded-full', room.isActive ? 'bg-emerald-500' : 'bg-slate-400']"></span>
                  {{ room.isActive ? '활성' : '비활성' }}
                </button>
              </td>
              <td class="px-6 py-3.5">
                <div class="flex items-center justify-end gap-2">
                  <a :href="`/kiosk/${room.id}`" target="_blank" class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-emerald-500 hover:bg-emerald-50 transition-all">키오스크</a>
                  <button @click="openModal(room)" class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-slate-700 hover:bg-slate-100 transition-all">수정</button>
                  <button @click="deleteRoom(room)" class="px-3 py-1.5 rounded-lg text-[13px] font-semibold text-red-400 hover:bg-red-50 transition-all">삭제</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 모바일 카드 -->
    <div v-else class="flex flex-col gap-3">
      <div v-if="allRooms.length === 0" class="bg-white rounded-2xl border border-slate-200 py-12 text-center text-slate-500 text-[15px] font-semibold">회의실이 없습니다</div>
      <div v-for="room in allRooms" :key="room.id" class="bg-white rounded-2xl border border-slate-200 p-4">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-3">
            <span class="w-9 h-9 rounded-xl flex-shrink-0 border border-slate-200" :style="{background: room.colorCode}"></span>
            <div>
              <p class="text-[15px] font-bold text-slate-800">{{ room.name }}</p>
              <p class="text-[12px] text-slate-600">{{ room.capacity }}인 · {{ room.colorCode }}</p>
            </div>
          </div>
          <button @click="toggleRoom(room)" :class="['px-3 py-1 rounded-lg text-[12px] font-bold transition-all', room.isActive ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-700']">
            {{ room.isActive ? '활성' : '비활성' }}
          </button>
        </div>
        <div class="flex gap-2 pt-3 border-t border-slate-50">
          <a :href="`/kiosk/${room.id}`" target="_blank" class="flex-1 py-2 text-center rounded-xl text-[13px] font-semibold text-emerald-500 bg-emerald-50 hover:bg-emerald-100 transition-all">키오스크</a>
          <button @click="openModal(room)" class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-slate-700 bg-slate-50 hover:bg-slate-100 transition-all">수정</button>
          <button @click="deleteRoom(room)" class="flex-1 py-2 rounded-xl text-[13px] font-semibold text-red-400 bg-red-50 hover:bg-red-100 transition-all">삭제</button>
        </div>
      </div>
    </div>

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
            <button type="button" @click="modal.form.isActive = !modal.form.isActive"
              :class="['relative w-11 h-6 rounded-full transition-colors duration-200 focus:outline-none', modal.form.isActive ? 'bg-indigo-500' : 'bg-slate-300']">
              <span :class="['absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-md transition-all duration-200', modal.form.isActive ? 'left-[22px]' : 'left-0.5']"></span>
            </button>
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
