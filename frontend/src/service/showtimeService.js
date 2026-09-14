import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

const showtimeService = {
  getByMovieId: async (movieId) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.SHOWTIMES.GET_BY_MOVIE_ID(movieId));
      return res.data?.data || [];
    } catch (error) {
      console.error(`Lỗi khi lấy danh sách suất chiếu cho phim ID ${movieId}:`, error);
      throw error;
    }
  },

  getById: async (id) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.SHOWTIMES.GET_BY_ID(id));
      return res.data?.data || null;
    } catch (error) {
      console.error(`Lỗi khi lấy thông tin suất chiếu ID ${id}:`, error);
      throw error;
    }
  },

  getSeats: async (showtimeId) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.SHOWTIMES.GET_SEATS(showtimeId));
      return res.data?.data || [];
    } catch (error) {
      console.error(`Lỗi khi lấy sơ đồ ghế cho suất chiếu ID ${showtimeId}:`, error);
      throw error;
    }
  },

  filter: async (page = 1, size = 10) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.SHOWTIMES.FILTER, { params: { page, size } });
      return res.data?.data || { content: [], totalPages: 0, totalElements: 0 };
    } catch (error) {
      console.error('Lỗi khi lọc danh sách suất chiếu:', error);
      return { content: [], totalPages: 0, totalElements: 0 };
    }
  },

  add: async (data) => {
    try {
      const res = await axiosClient.post(API_ENDPOINTS.SHOWTIMES.ADD, data);
      return res.data?.data || res.data;
    } catch (error) {
      console.error('Lỗi khi thêm suất chiếu:', error);
      throw error;
    }
  },
};

export default showtimeService;
