import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000,
});

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
      document.body.innerHTML = `
        <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;background:#f8fafc;font-family:sans-serif;">
          <div style="background:white;border:1px solid #e2e8f0;border-radius:16px;padding:40px 48px;text-align:center;max-width:400px;box-shadow:0 4px 24px #0001;">
            <div style="width:56px;height:56px;background:#fef2f2;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 20px;">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M12 9v4M12 17h.01M4 20h16L12 4 4 20z" stroke="#ef4444" stroke-width="2" stroke-linecap="round"/></svg>
            </div>
            <h1 style="font-size:20px;font-weight:800;color:#1e293b;margin:0 0 8px;">시스템 점검 중</h1>
            <p style="font-size:14px;color:#64748b;margin:0;">${msg}</p>
          </div>
        </div>`;
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
        const res = await axios.post('/api/auth/refresh', { refreshToken }, { timeout: 10000 });
        const newToken = res.data.token;
        localStorage.setItem('token', newToken);
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