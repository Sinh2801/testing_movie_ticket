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
};

export default authService;
