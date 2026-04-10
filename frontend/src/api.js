import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000,
});

export async function refreshAccessToken(refreshToken, timeout = 10000) {
  const res = await axios.post('/api/auth/refresh', { refreshToken }, { timeout });
  return res.data; // { token, refreshToken? }
}

// 요청마다 토큰 자동 첨부
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// 401/403 → refreshToken으로 자동 재발급 후 재시도
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token);
  });
  failedQueue = [];
};

api.interceptors.response.use(
  res => res,
  async err => {
    // 503 점검 모드
    if (err.response?.status === 503) {
      const msg = err.response?.data?.message || '현재 시스템 점검 중입니다.';
      // API 레이어에서 DOM을 직접 덮어쓰지 않고, 라우팅으로 점검 화면을 보여준다.
      try {
        sessionStorage.setItem('maintenanceMessage', msg);
      } catch {}
      if (window.location.pathname !== '/maintenance') {
        window.location.replace('/maintenance');
      }
      return Promise.reject(err);
    }
    const originalRequest = err.config;
    // 타임아웃 또는 네트워크 에러는 재시도 없이 즉시 반환
    if (err.code === 'ECONNABORTED' || !err.response) return Promise.reject(err);
    // 로그인 요청의 403은 거절된 계정 응답이므로 인터셉터가 처리하지 않음
    if (originalRequest.url?.includes('/auth/')) return Promise.reject(err);
    if ((err.response?.status === 401 || err.response?.status === 403) && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        localStorage.removeItem('token');
        window.location.href = '/';
        return Promise.reject(err);
      }
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        });
      }
      originalRequest._retry = true;
      isRefreshing = true;
      try {
        const data = await refreshAccessToken(refreshToken, 10000);
        const newToken = data.token;
        localStorage.setItem('token', newToken);
        if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
        processQueue(null, newToken);
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return api(originalRequest);
      } catch (refreshErr) {
        processQueue(refreshErr, null);
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        window.location.href = '/';
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }
    return Promise.reject(err);
  }
);

export default api;