import { onMounted, onUnmounted, unref } from 'vue';

export function useClickOutside({ enabled, inside, onOutside, eventName = 'pointerdown' }) {
  const handler = (event) => {
    if (!unref(enabled)) return;
    const target = event.target;
    const list = inside.map(v => unref(v)).filter(Boolean);
    if (list.some(el => el.contains?.(target))) return;
    onOutside(event);
  };

  onMounted(() => document.addEventListener(eventName, handler));
  onUnmounted(() => document.removeEventListener(eventName, handler));
}

