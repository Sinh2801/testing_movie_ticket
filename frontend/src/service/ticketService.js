import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

const ticketService = {
  // Đặt vé mới
  bookTicket: async (requestData) => {
    try {
      const res = await axiosClient.post(API_ENDPOINTS.TICKETS.BOOK, requestData);
      return res.data?.data || null;
    } catch (error) {
      console.error('Lỗi khi thực hiện đặt vé:', error);
      throw error;
    }
  },

  // Hủy vé xem phim
  cancelTicket: async (ticketId) => {
    try {
      const res = await axiosClient.post(API_ENDPOINTS.TICKETS.CANCEL(ticketId));
      return res.data;
    } catch (error) {
      console.error(`Lỗi khi hủy vé ID ${ticketId}:`, error);
      throw error;
    }
  },

  // Lấy lịch sử vé của User đang đăng nhập
  getHistory: async () => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.TICKETS.HISTORY);
      return res.data?.data || [];
    } catch (error) {
      console.error('Lỗi khi lấy lịch sử đặt vé:', error);
      throw error;
    }
  },

  // Lấy chi tiết vé theo UID
  getByUid: async (uid) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.TICKETS.GET_BY_UID(uid));
      return res.data?.data || null;
    } catch (error) {
      console.error(`Lỗi khi lấy thông tin vé UID ${uid}:`, error);
      throw error;
    }
  },

  // Lọc và tìm kiếm vé cho Admin
  filter: async (page = 1, size = 10, uid = '') => {
    try {
      const params = {
        page: page,
        size: size,
        uid: uid,
      };
      const res = await axiosClient.get(API_ENDPOINTS.TICKETS.FILTER, { params });
      return res.data?.data || { content: [], totalPages: 0, totalElements: 0 };
    } catch (error) {
      console.error('Lỗi khi lọc danh sách vé:', error);
      return { content: [], totalPages: 0, totalElements: 0 };
    }
  },
};

export default ticketService;
