import api from './api.js';

const unwrap = (response) => response.data.data;

export const studentService = {
  getAll: () => api.get('/students').then(unwrap),
  search: (keyword) => api.get('/students/search', { params: { keyword } }).then(unwrap),
  getById: (id) => api.get(`/students/${encodeURIComponent(id)}`).then(unwrap),
  create: (student) => api.post('/students', student).then(unwrap),
  update: (id, student) => api.put(`/students/${encodeURIComponent(id)}`, student).then(unwrap),
  remove: (id) => api.delete(`/students/${encodeURIComponent(id)}`),
};
