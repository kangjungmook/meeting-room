import { nextTick, onMounted, onUnmounted, ref, unref, watch } from 'vue';

function clamp(n, min, max) {
  return Math.min(Math.max(n, min), max);
}

/**
 * Anchored floating panel position helper.
 * Default matches existing patterns:
 * - admin menu: fixed panel, positioned by top + right
 * - date picker: fixed panel, positioned by top + left + width + maxHeight
 */
export function useAnchoredMenu({
  open,
  anchorEl,
  panelEl,
  mode = 'left', // 'left' | 'right'
  minWidth = 280,
  gap = 6,
  viewportPadding = 8,
  defaultHeight = 360,
  minAvailableHeight = 220,
} = {}) {
  const style = ref({});

  const calc = () => {
    const anchor = unref(anchorEl);
    if (!anchor) return;
    const rect = anchor.getBoundingClientRect();
    const width = Math.max(rect.width, minWidth);
    const availableHeight = Math.max(minAvailableHeight, window.innerHeight - viewportPadding * 2);
    const renderedHeight = unref(panelEl)?.offsetHeight ?? defaultHeight;
    const panelHeight = Math.min(renderedHeight, availableHeight);
    const spaceBelow = window.innerHeight - rect.bottom - viewportPadding;
    const spaceAbove = rect.top - viewportPadding;

    const top = (spaceBelow >= panelHeight || spaceBelow >= spaceAbove)
      ? rect.bottom + gap
      : rect.top - panelHeight - gap;

    if (mode === 'right') {
      // right aligned: use `right` distance from viewport right edge
      style.value = {
        top: `${clamp(top, viewportPadding, window.innerHeight - viewportPadding)}px`,
        right: `${Math.max(0, window.innerWidth - rect.right)}px`,
      };
      return;
    }

    // left aligned
    const left = clamp(rect.left, viewportPadding, Math.max(viewportPadding, window.innerWidth - width - viewportPadding));
    style.value = {
      top: `${clamp(top, viewportPadding, window.innerHeight - viewportPadding)}px`,
      left: `${left}px`,
      width: `${width}px`,
      maxHeight: `${availableHeight}px`,
    };
  };

  const updateIfOpen = () => {
    if (unref(open)) calc();
  };

  watch(() => unref(open), async (isOpen) => {
    if (!isOpen) return;
    await nextTick();
    calc();
  });

  onMounted(() => window.addEventListener('resize', updateIfOpen));
  onUnmounted(() => window.removeEventListener('resize', updateIfOpen));

  return { style, calc };
}

