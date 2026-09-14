import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

const roomService = {
  getByCinemaId: async (cinemaId) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.ROOMS.GET_BY_CINEMA_ID(cinemaId));
      return res.data?.data || [];
    } catch (error) {
      console.error(`Lỗi khi lấy danh sách phòng chiếu theo rạp ID ${cinemaId}:`, error);
      throw error;
    }
  },

  add: async (data) => {
    try {
      const res = await axiosClient.post(API_ENDPOINTS.ROOMS.ADD, data);
      return res.data?.data || res.data;
    } catch (error) {
      console.error('Lỗi khi thêm phòng chiếu:', error);
      throw error;
    }
  },

  edit: async (data) => {
    try {
      const res = await axiosClient.put(API_ENDPOINTS.ROOMS.EDIT, data);
      return res.data?.data || res.data;
    } catch (error) {
      console.error('Lỗi khi sửa phòng chiếu:', error);
      throw error;
    }
  },
};

export default roomService;
