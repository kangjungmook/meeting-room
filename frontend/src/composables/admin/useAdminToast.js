import { ref } from 'vue';

export const adminToast = ref({ show: false, msg: '', type: 'success' });

let _timer;
export const showAdminToast = (msg, type = 'success') => {
  adminToast.value = { show: true, msg, type };
  clearTimeout(_timer);
  _timer = setTimeout(() => { adminToast.value.show = false; }, 3500);
};
