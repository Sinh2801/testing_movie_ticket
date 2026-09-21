import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

const authService = {
  /**
   * Đăng nhập tài khoản
   * @param {Object} credentials { email, password }
   * @returns {Promise<Object>} Trả về { message, data: { token, name, email } }
   */
  login: async (credentials) => {
    try {
      const response = await axiosClient.post(API_ENDPOINTS.AUTH.LOGIN, credentials);
      return response.data; // ApiResponse từ Spring Boot
    } catch (error) {
      throw error.response?.data || { message: 'Đăng nhập thất bại, lỗi kết nối mạng' };
    }
  },

  /**
   * Đăng ký tài khoản mới
   * @param {Object} userData { name, email, phoneNumber, password, confirmPassword }
   * @returns {Promise<Object>} Trả về { message, data: { token, name, email } }
   */
  register: async (userData) => {
    try {
      const response = await axiosClient.post(API_ENDPOINTS.AUTH.REGISTER, userData);
      return response.data; // ApiResponse từ Spring Boot
    } catch (error) {
      throw error.response?.data || { message: 'Đăng ký thất bại, lỗi kết nối mạng' };
    }
  },

  /**
   * Yêu cầu gửi mã OTP 6 chữ số quên mật khẩu
   * @param {Object} data { email }
   */
  forgotPassword: async (data) => {
    try {
      const response = await axiosClient.post(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, data);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Gửi yêu cầu OTP thất bại' };
    }
  },

  /**
   * Xác thực mã OTP 6 chữ số và đặt lại mật khẩu
   * @param {Object} data { email, otp, newPassword }
   */
  resetPassword: async (data) => {
    try {
      const response = await axiosClient.post(API_ENDPOINTS.AUTH.RESET_PASSWORD, data);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Đặt lại mật khẩu thất bại' };
    }
  },
};

export default authService;
