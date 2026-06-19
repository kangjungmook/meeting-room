<template>
  <Transition
    enter-active-class="transition-all duration-300 ease-out"
    leave-active-class="transition-all duration-200 ease-in"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0">
    <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center px-4 bg-slate-900/60 backdrop-blur-sm">
      <div class="w-full max-w-sm bg-white rounded-2xl shadow-2xl overflow-hidden">

        <!-- 상단 컬러 바 -->
        <div class="h-1 w-full" :style="{ background: steps[current].color }"></div>

        <!-- 콘텐츠 -->
        <div class="px-7 pt-7 pb-5">

          <!-- 아이콘 -->
          <div class="w-14 h-14 rounded-2xl flex items-center justify-center mb-5"
               :style="{ background: steps[current].color + '18' }">
            <span class="text-3xl">{{ steps[current].icon }}</span>
          </div>

          <!-- 제목 / 설명 -->
          <p class="text-[18px] font-black text-gray-900 leading-snug mb-2">{{ steps[current].title }}</p>
          <p class="text-[14px] text-gray-500 leading-relaxed">{{ steps[current].desc }}</p>

        </div>

        <!-- 하단 -->
        <div class="px-7 pb-7 flex flex-col gap-4">

          <!-- 스텝 인디케이터 -->
          <div class="flex items-center justify-center gap-1.5">
            <span v-for="(_, i) in steps" :key="i"
                  class="rounded-full transition-all duration-300"
                  :class="i === current ? 'w-5 h-2' : 'w-2 h-2 bg-gray-200'"
                  :style="i === current ? { width: '20px', height: '8px', background: steps[current].color } : {}">
            </span>
          </div>

          <!-- 버튼 -->
          <div class="flex gap-2">
            <button v-if="current > 0" @click="current--"
                    class="flex-1 py-2.5 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-[14px] font-bold transition-colors">
              이전
            </button>
            <button v-if="current < steps.length - 1" @click="current++"
                    class="flex-[2] py-2.5 rounded-xl text-white text-[14px] font-bold transition-all"
                    :style="{ background: steps[current].color }">
              다음
            </button>
            <button v-else @click="close"
                    class="flex-[2] py-2.5 rounded-xl text-white text-[14px] font-bold transition-all"
                    :style="{ background: steps[current].color }">
              시작하기
            </button>
          </div>

          <!-- 건너뛰기 -->
          <button v-if="current < steps.length - 1" @click="close"
                  class="text-[12px] text-gray-400 hover:text-gray-600 transition-colors text-center">
            건너뛰기
          </button>

        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '../../api';

const show = ref(false);
const current = ref(0);

const steps = [
  {
    icon: '📅',
    color: '#4F46E5',
    title: '회의실 예약 시스템',
    desc: '회의실을 간편하게 예약하고 일정을 한눈에 확인할 수 있습니다.',
  },
  {
    icon: '🗓️',
    color: '#0EA5E9',
    title: '일 · 주 · 월 보기',
    desc: '상단 탭에서 일간 / 주간 / 월간 보기로 전환할 수 있습니다.',
  },
  {
    icon: '➕',
    color: '#10B981',
    title: '새 예약 만들기',
    desc: '좌측 사이드바의 "새 예약" 버튼을 눌러 회의실을 예약하세요.',
  },
  {
    icon: '🔔',
    color: '#F59E0B',
    title: '내 회의 확인',
    desc: '"내 회의" 메뉴에서 내가 등록한 예약 목록을 확인할 수 있습니다.',
  },
];

onMounted(async () => {
  try {
    const res = await api.get('/users/tutorial-done');
    if (!res.data.done) show.value = true;
  } catch {}
});

const close = async () => {
  show.value = false;
  try { await api.patch('/users/tutorial-done'); } catch {}
};
</script>
