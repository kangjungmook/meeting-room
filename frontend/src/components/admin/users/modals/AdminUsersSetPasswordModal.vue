  <template>
  <BaseModal :show="show" @close="$emit('close')">
    <template #header>
      <div class="flex items-center justify-between px-6 py-5 border-b border-slate-100">
        <div>
          <h3 class="text-[17px] font-bold text-slate-800">비밀번호 설정</h3>
          <p class="text-[13px] text-slate-600 mt-0.5">{{ userName }}</p>
        </div>
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
          <label class="text-[13px] font-bold text-slate-700">새 비밀번호</label>
          <input
            :value="password"
            type="password"
            required
            placeholder="4자 이상"
            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-400"
            @input="$emit('update:password', $event.target.value)"
          />
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-[13px] font-bold text-slate-700">비밀번호 확인</label>
          <input
            :value="confirm"
            type="password"
            required
            :class="[
              'w-full bg-slate-50 border rounded-xl px-4 py-2.5 text-[15px] font-semibold text-slate-700 outline-none focus:ring-2 transition-all',
              confirm && password !== confirm ? 'border-red-300 focus:ring-red-300' : 'border-slate-200 focus:ring-indigo-400',
            ]"
            @input="$emit('update:confirm', $event.target.value)"
          />
          <p v-if="confirm && password !== confirm" class="text-[12px] text-red-400 font-semibold">비밀번호가 일치하지 않습니다</p>
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
          >설정</button>
        </div>
      </form>
  </BaseModal>
</template>

<script setup>
import AppIcon from '../../../icons/AppIcon.vue';
import BaseModal from '../../../ui/BaseModal.vue';

defineProps({
  show: { type: Boolean, required: true },
  userName: { type: String, default: '' },
  password: { type: String, default: '' },
  confirm: { type: String, default: '' },
  error: { type: String, default: '' },
});

defineEmits(['close', 'submit', 'update:password', 'update:confirm']);
</script>

