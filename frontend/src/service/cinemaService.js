import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

const cinemaService = {
  getAll: async () => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.CINEMAS.GET_ALL);
      return res.data?.data || [];
    } catch (error) {
      console.error('Lỗi khi lấy danh sách rạp chiếu:', error);
      throw error;
    }
  },

  getById: async (id) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.CINEMAS.GET_BY_ID(id));
      return res.data?.data || null;
    } catch (error) {
      console.error(`Lỗi khi lấy thông tin rạp ID ${id}:`, error);
      throw error;
    }
  },

  add: async (data) => {
    try {
      const res = await axiosClient.post(API_ENDPOINTS.CINEMAS.ADD, data);
      return res.data?.data || res.data;
    } catch (error) {
      console.error('Lỗi khi thêm rạp chiếu:', error);
      throw error;
    }
  },

  edit: async (data) => {
    try {
      const res = await axiosClient.put(API_ENDPOINTS.CINEMAS.EDIT, data);
      return res.data?.data || res.data;
    } catch (error) {
      console.error('Lỗi khi sửa rạp chiếu:', error);
      throw error;
    }
  },
};

export default cinemaService;
