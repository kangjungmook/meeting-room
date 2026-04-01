import api from '../../api';
import { fetchAll } from './useAdminState';

// ── 회의실 CRUD ───────────────────────────────────────────────
export const createRoom = async (form)   => { await api.post('/admin/rooms', form);              fetchAll(); };
export const updateRoom = async (id, form) => { await api.put(`/admin/rooms/${id}`, form);       fetchAll(); };
export const deleteRoom = async (room)   => {
  if (!confirm(`"${room.name}" 회의실을 삭제할까요?`)) return;
  await api.delete(`/admin/rooms/${room.id}`);
  fetchAll();
};
export const toggleRoom = async (room)   => { await api.patch(`/admin/rooms/${room.id}/toggle`); fetchAll(); };
