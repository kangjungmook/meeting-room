import { onMounted, onUnmounted, unref } from 'vue';

/**
 * @param {Object} opts
 * @param {import('vue').Ref<boolean>|boolean} opts.enabled
 * @param {Array<import('vue').Ref<HTMLElement|null>|HTMLElement|null>} opts.inside
 * @param {(event: PointerEvent|MouseEvent) => void} opts.onOutside
 * @param {'pointerdown'|'mousedown'|'click'} [opts.eventName]
 */
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

