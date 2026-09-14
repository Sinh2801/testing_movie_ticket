import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

const genreService = {
  getAll: async () => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.GENRES.GET_ALL);
      return res.data?.data || [];
    } catch (error) {
      console.error('Lỗi khi lấy danh sách thể loại:', error);
      throw error;
    }
  },

  getById: async (id) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.GENRES.GET_BY_ID(id));
      return res.data?.data || null;
    } catch (error) {
      console.error(`Lỗi khi lấy thông tin thể loại ID ${id}:`, error);
      throw error;
    }
  },

  add: async (data) => {
    try {
      const res = await axiosClient.post(API_ENDPOINTS.GENRES.ADD, data);
      return res.data?.data || res.data;
    } catch (error) {
      console.error('Lỗi khi thêm thể loại:', error);
      throw error;
    }
  },

  edit: async (id, data) => {
    try {
      const res = await axiosClient.put(API_ENDPOINTS.GENRES.EDIT(id), data);
      return res.data?.data || res.data;
    } catch (error) {
      console.error(`Lỗi khi sửa thể loại ID ${id}:`, error);
      throw error;
    }
  },
};

export default genreService;
