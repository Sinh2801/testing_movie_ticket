import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';

const axiosClient = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Helper kiểm tra JWT token đã hết hạn chưa
function isTokenExpired(token) {
  if (!token) return true;
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return true;
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const { exp } = JSON.parse(jsonPayload);
    if (!exp) return false;
    // Buffer 10 giây trước khi token hết hạn hoàn toàn
    return Date.now() >= exp * 1000 - 10000;
  } catch {
    return true;
  }
}

// Tự động kiểm tra token hết hạn và refresh token trước khi gửi request
axiosClient.interceptors.request.use(
  async (config) => {
    // Không kiểm tra cho các endpoint auth (login, register, refresh)
    if (config.url?.includes('/auth/')) {
      return config;
    }

    let token = localStorage.getItem('token');
    const refreshToken = localStorage.getItem('refreshToken');

    if (token && isTokenExpired(token)) {
      if (refreshToken) {
        try {
          // Thử làm mới token bằng Refresh Token
          const res = await axios.post(`${baseURL}/auth/refresh`, { refreshToken });
          const newAuthData = res.data?.data;
          if (newAuthData?.token) {
            token = newAuthData.token;
            localStorage.setItem('token', token);
            if (newAuthData.refreshToken) {
              localStorage.setItem('refreshToken', newAuthData.refreshToken);
            }
          }
        } catch (refreshErr) {
          console.warn('Refresh token expired or invalid, logging out...', refreshErr);
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          if (window.location.pathname !== '/login') {
            window.location.href = '/login';
          }
          return Promise.reject(refreshErr);
        }
      } else {
        // Hết hạn và không có refresh token -> logout
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }
    }

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Tự động Đăng xuất khi gặp 401 & Chuyển hướng sang /error khi lỗi kết nối Máy Chủ (ERR_NETWORK / 5xx)
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');

      const isAuthRequest = error.config?.url?.includes('/auth/');
      if (!isAuthRequest && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    } else if (
      !error.response ||
      error.code === 'ERR_NETWORK' ||
      error.code === 'ECONNABORTED' ||
      (error.response && error.response.status >= 500)
    ) {
      if (window.location.pathname !== '/error') {
        window.location.href = '/error';
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;

