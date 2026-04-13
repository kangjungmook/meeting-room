import { ref, computed } from 'vue';
import api from '../../api';
import { notifSetting, fetchAll } from './useAdminState';
import { showAdminToast } from './useAdminToast';

export const broadcast = ref({ title: '', body: '' });

export const notifMinutes = computed(() =>
  notifSetting.value.minutesBefore
    .split(',').map(s => s.trim()).filter(s => s !== '').map(Number)
);

export const addMinute = (v) => {
  const n = parseInt(v);
  if (!n || n < 1 || n > 120) return false;
  const parts = notifSetting.value.minutesBefore.split(',').map(s => s.trim()).filter(Boolean);
  if (parts.includes(String(n))) return false;
  parts.push(String(n));
  notifSetting.value.minutesBefore = parts.sort((a, b) => Number(a) - Number(b)).join(',');
  return true;
};

export const removeMinute = (idx) => {
  const parts = notifSetting.value.minutesBefore.split(',').map(s => s.trim()).filter(Boolean);
  parts.splice(idx, 1);
  notifSetting.value.minutesBefore = parts.join(',');
};

export const saveNotifSetting  = async () => { await api.put('/admin/notification-setting', notifSetting.value); showAdminToast('저장되었습니다.'); };
export const saveSystemSetting = async () => { await api.put('/admin/notification-setting', notifSetting.value); showAdminToast('시스템 설정이 저장되었습니다.'); };
export const sendBroadcast = async () => {
  await api.post('/admin/notification/broadcast', broadcast.value);
  broadcast.value = { title: '', body: '' };
  showAdminToast('전송되었습니다.');
};
