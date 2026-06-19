import { ref, computed } from 'vue';
import dayjs from 'dayjs';
import 'dayjs/locale/ko';

dayjs.locale('ko');

// ── 실시간 시계 (1초 갱신) ─────────────────────────────────────
export const liveNow  = ref(dayjs());
export const liveTime = computed(() => liveNow.value.format('HH:mm'));
export const liveDate = computed(() => liveNow.value.format('YYYY년 M월 D일 (ddd)'));

setInterval(() => { liveNow.value = dayjs(); }, 1000);
