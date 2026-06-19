<template>
  <svg :width="size" :height="size" :viewBox="icon.viewBox" fill="none"
       :class="[spin && 'animate-spin', cls]"
       v-bind="$attrs">
    <component :is="'path'" v-for="(p, i) in icon.paths" :key="i" v-bind="p" />
    <component :is="'circle'" v-for="(c, i) in icon.circles" :key="'c'+i" v-bind="c" />
    <component :is="'rect'" v-for="(r, i) in icon.rects" :key="'r'+i" v-bind="r" />
  </svg>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  name:  { type: String, required: true },
  size:  { type: [Number, String], default: 16 },
  spin:  { type: Boolean, default: false },
  cls:   { type: String,  default: '' },
});

const ICONS = {

  // 닫기 (X)
  close: {
    viewBox: '0 0 16 16',
    paths: [{ d: 'M3 3l10 10M13 3L3 13', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }],
  },

  // 새로고침 / 초기화
  refresh: {
    viewBox: '0 0 18 18',
    paths: [
      { d: 'M15.5 9A6.5 6.5 0 1 1 9 2.5c2 0 3.8.9 5 2.3', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round' },
      { d: 'M14 2l.5 3-3-.5', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' },
    ],
  },

  // 캘린더
  calendar: {
    viewBox: '0 0 18 18',
    paths: [
      { d: 'M5.5 1.5v3M12.5 1.5v3M1.5 7h15', stroke: 'currentColor', 'stroke-width': '1.6', 'stroke-linecap': 'round' },
    ],
    rects: [
      { x: '1.5', y: '3.5', width: '15', height: '13', rx: '2', stroke: 'currentColor', 'stroke-width': '1.6' },
    ],
  },

  // 사용자 (1명)
  user: {
    viewBox: '0 0 15 15',
    circles: [{ cx: '7.5', cy: '5', r: '3', stroke: 'currentColor', 'stroke-width': '1.5' }],
    paths: [{ d: 'M1.5 14c0-3.3 2.7-6 6-6s6 2.7 6 6', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linecap': 'round' }],
  },

  // 사용자 (2명 / 참석자)
  users: {
    viewBox: '0 0 18 14',
    circles: [
      { cx: '6', cy: '4', r: '2.8', stroke: 'currentColor', 'stroke-width': '1.4' },
      { cx: '12', cy: '4', r: '2.8', stroke: 'currentColor', 'stroke-width': '1.4' },
    ],
    paths: [
      { d: 'M0.5 13c0-3 2.5-5.2 5.5-5.2', stroke: 'currentColor', 'stroke-width': '1.4', 'stroke-linecap': 'round' },
      { d: 'M17.5 13c0-3-2.5-5.2-5.5-5.2', stroke: 'currentColor', 'stroke-width': '1.4', 'stroke-linecap': 'round' },
    ],
  },

  // 검색 (돋보기)
  search: {
    viewBox: '0 0 16 16',
    circles: [{ cx: '7', cy: '7', r: '5', stroke: 'currentColor', 'stroke-width': '1.6' }],
    paths: [{ d: 'M11 11l3 3', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round' }],
  },

  // 햄버거 메뉴
  menu: {
    viewBox: '0 0 18 18',
    paths: [{ d: 'M2 4.5h14M2 9h14M2 13.5h14', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round' }],
  },

  // 체크 (성공)
  check: {
    viewBox: '0 0 16 16',
    paths: [{ d: 'M3 8l3.5 3.5 6.5-7', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  // 더하기 (+)
  plus: {
    viewBox: '0 0 16 16',
    paths: [{ d: 'M8 2v12M2 8h12', stroke: 'currentColor', 'stroke-width': '2.2', 'stroke-linecap': 'round' }],
  },

  // 로그아웃
  logout: {
    viewBox: '0 0 15 15',
    paths: [
      { d: 'M6 2H3a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h3', stroke: 'currentColor',  'stroke-width': '1.5', 'stroke-linecap': 'round' },
      { d: 'M10 10l3-3-3-3M13 7H6', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' },
    ],
  },

  // 설정 (기어)
  settings: {
    viewBox: '0 0 24 24',
    circles: [{ cx: '12', cy: '12', r: '3', stroke: 'currentColor', 'stroke-width': '1.8' }],
    paths: [{
      d: 'M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z',
      stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round',
    }],
  },

  // 수정 (연필)
  edit: {
    viewBox: '0 0 14 14',
    paths: [{ d: 'M9.5 2.5l2 2L4 12H2v-2L9.5 2.5z', stroke: 'currentColor', 'stroke-width': '1.4', 'stroke-linejoin': 'round' }],
  },

  // 삭제 (쓰레기통)
  trash: {
    viewBox: '0 0 14 14',
    paths: [{ d: 'M2 4h10M5 4V2.5h4V4M5.5 6.5v4M8.5 6.5v4M3.5 4l.5 8h6l.5-8', stroke: 'currentColor', 'stroke-width': '1.4', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  // 알림 (벨)
  bell: {
    viewBox: '0 0 15 15',
    paths: [
      { d: 'M7.5 1a4.5 4.5 0 0 1 4.5 4.5c0 2.5.5 3.5 1 4.5H2c.5-1 1-2 1-4.5A4.5 4.5 0 0 1 7.5 1z', stroke: 'currentColor', 'stroke-width': '1.4', 'stroke-linejoin': 'round' },
      { d: 'M6 13a1.5 1.5 0 0 0 3 0', stroke: 'currentColor', 'stroke-width': '1.4' },
    ],
  },

  // 눈 (비밀번호 보기)
  eye: {
    viewBox: '0 0 18 18',
    circles: [{ cx: '9', cy: '9', r: '2.5', stroke: 'currentColor', 'stroke-width': '1.5' }],
    paths: [{ d: 'M1 9s3-6 8-6 8 6 8 6-3 6-8 6-8-6-8-6z', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linejoin': 'round' }],
  },

  // 눈 (비밀번호 숨기기)
  'eye-off': {
    viewBox: '0 0 18 18',
    paths: [
      { d: 'M1 1l16 16', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linecap': 'round' },
      { d: 'M7.5 7.6A2.5 2.5 0 0 0 11.4 11M5 4.1C2.9 5.5 1 9 1 9s3 5.5 8 5.5c1.5 0 2.8-.5 3.9-1.2', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linecap': 'round' },
      { d: 'M17 9s-.8 1.8-2.5 3.2', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linecap': 'round' },
    ],
  },

  // 달 (다크모드)
  moon: {
    viewBox: '0 0 18 18',
    paths: [{ d: 'M15 10.5A6.5 6.5 0 0 1 7.5 3a6.5 6.5 0 1 0 7.5 7.5z', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linejoin': 'round' }],
  },

  // 태양 (라이트모드)
  sun: {
    viewBox: '0 0 18 18',
    circles: [{ cx: '9', cy: '9', r: '3', stroke: 'currentColor', 'stroke-width': '1.5' }],
    paths: [{ d: 'M9 1v2M9 15v2M1 9h2M15 9h2M3.2 3.2l1.4 1.4M13.4 13.4l1.4 1.4M14.8 3.2l-1.4 1.4M4.6 13.4l-1.4 1.4', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linecap': 'round' }],
  },

  // 위 화살표 (chevron-up)
  'chevron-up': {
    viewBox: '0 0 14 14',
    paths: [{ d: 'M2 9l5-5 5 5', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  // 아래 화살표 (chevron-down)
  'chevron-down': {
    viewBox: '0 0 14 14',
    paths: [{ d: 'M2 5l5 5 5-5', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  // 왼쪽 화살표 (chevron-left)
  'chevron-left': {
    viewBox: '0 0 14 14',
    paths: [{ d: 'M9 2L4 7l5 5', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  // 오른쪽 화살표 (chevron-right)
  'chevron-right': {
    viewBox: '0 0 14 14',
    paths: [{ d: 'M5 2l5 5-5 5', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  // 왼쪽 방향 (이전)
  'arrow-left': {
    viewBox: '0 0 18 18',
    paths: [{ d: 'M15 9H3M8 4L3 9l5 5', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  // 오른쪽 방향 (다음)
  'arrow-right': {
    viewBox: '0 0 18 18',
    paths: [{ d: 'M3 9h12M10 4l5 5-5 5', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  // 경고 / 에러 (원 안 느낌표)
  'alert-circle': {
    viewBox: '0 0 16 16',
    circles: [{ cx: '8', cy: '8', r: '6', stroke: 'currentColor', 'stroke-width': '1.5' }],
    paths: [{ d: 'M8 5v3.5M8 10.5h.01', stroke: 'currentColor', 'stroke-width': '1.6', 'stroke-linecap': 'round' }],
  },

  // 정보 (원 안 i)
  'info': {
    viewBox: '0 0 16 16',
    circles: [{ cx: '8', cy: '8', r: '6', stroke: 'currentColor', 'stroke-width': '1.5' }],
    paths: [{ d: 'M8 7v4M8 5.5h.01', stroke: 'currentColor', 'stroke-width': '1.6', 'stroke-linecap': 'round' }],
  },

  // 키오스크 / 모니터
  clock: {
    viewBox: '0 0 16 16',
    circles: [{ cx: '8', cy: '8', r: '6.5', stroke: 'currentColor', 'stroke-width': '1.5' }],
    paths: [{ d: 'M8 4.5v4l2.5 2', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }],
  },

  lock: {
    viewBox: '0 0 24 24',
    paths: [{ d: 'M12 2a5 5 0 0 1 5 5v2H7V7a5 5 0 0 1 5-5zM7 9h10a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2zm5 4a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3z', fill: 'currentColor' }],
  },

  stop: {
    viewBox: '0 0 14 14',
    rects: [{ x: '1', y: '1', width: '12', height: '12', rx: '2.5', fill: 'currentColor' }],
  },

  monitor: {
    viewBox: '0 0 18 18',
    rects: [{ x: '1.5', y: '2.5', width: '15', height: '11', rx: '2', stroke: 'currentColor', 'stroke-width': '1.6' }],
    paths: [{ d: 'M6 15.5h6M9 13.5v2', stroke: 'currentColor', 'stroke-width': '1.6', 'stroke-linecap': 'round' }],
  },

};

const icon = computed(() => ICONS[props.name] ?? {
  viewBox: '0 0 16 16',
  paths: [{ d: 'M2 2l12 12M14 2L2 14', stroke: 'currentColor', 'stroke-width': '1.5' }], // fallback: X
});
</script>
