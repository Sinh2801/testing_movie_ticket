import { apiCall, API_ENDPOINTS } from '../config/api.js';

export const authService = {
  /**
   * Đăng nhập người dùng bằng email và mật khẩu
   */
  async login(email, password) {
    const res = await apiCall(API_ENDPOINTS.AUTH.LOGIN, {
      body: { email, password }
    });

    if (res.success && res.token) {
      localStorage.setItem('cine_token', res.token);
      localStorage.setItem('cine_user', JSON.stringify(res.user));
    }
    return res;
  },

  /**
   * Đăng ký tài khoản mới (RegisterRequest: username, email, phoneNumber, password, confirmPassword)
   */
  async register(username, email, phoneNumber, password, confirmPassword) {
    const res = await apiCall(API_ENDPOINTS.AUTH.REGISTER, {
      body: { username, email, phoneNumber, password, confirmPassword }
    });

    if (res.success && res.token) {
      localStorage.setItem('cine_token', res.token);
      localStorage.setItem('cine_user', JSON.stringify(res.user || { username: res.data?.username, email: res.data?.email }));
    }
    return res;
  },

  /**
   * Đăng xuất người dùng & xóa thông tin session
   */
  logout() {
    localStorage.removeItem('cine_token');
    localStorage.removeItem('cine_user');
    window.location.href = 'index.html';
  },

  /**
   * Lấy thông tin user hiện tại từ Storage hoặc API
   */
  getCurrentUser() {
    const userStr = localStorage.getItem('cine_user');
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * Kiểm tra người dùng đã đăng nhập chưa
   */
  isLoggedIn() {
    return !!localStorage.getItem('cine_token') && !!localStorage.getItem('cine_user');
  }
};
