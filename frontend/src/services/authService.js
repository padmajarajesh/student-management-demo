import api from './api.js';

// Every backend response looks like { success, message, data, errors?, timestamp }.
const unwrap = (response) => response.data.data;

export const authService = {
  register: (payload) => api.post('/auth/register', payload).then(unwrap),
  login: (credentials) => api.post('/auth/login', credentials).then(unwrap),
  logout: () => api.post('/auth/logout'),
  refresh: () => api.post('/auth/refresh').then(unwrap),
  getCurrentUser: () => api.get('/auth/me').then(unwrap),
};
