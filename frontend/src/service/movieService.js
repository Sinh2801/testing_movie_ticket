import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

const movieService = {
  // Lấy chi tiết phim
  getById: async (id) => {
    try {
      const resp = await axiosClient.get(API_ENDPOINTS.MOVIES.GET_BY_ID(id));
      return resp.data?.data || null;
    } catch (error) {
      console.error('Lỗi khi lấy thông tin chi tiết phim:', error);
      throw error;
    }
  },

  // Phim đang chiếu
  getNowShowing: async (page = 1, size = 10) => {
    try {
      const resp = await axiosClient.get(API_ENDPOINTS.MOVIES.NOW_SHOWING, {
        params: { page, size }
      });
      return resp.data?.data?.content || [];
    } catch (error) {
      console.error('Lỗi khi lấy danh sách phim đang chiếu:', error);
      return [];
    }
  },

  // Phim sắp chiếu
  getComingSoon: async (page = 1, size = 10) => {
    try {
      const resp = await axiosClient.get(API_ENDPOINTS.MOVIES.COMING_SOON, {
        params: { page, size }
      });
      const data = resp.data?.data;
      if (data && Array.isArray(data.content)) {
        return data.content;
      }
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error('Lỗi khi lấy danh sách phim sắp chiếu:', error);
      return [];
    }
  },

  // Lọc / Tìm kiếm phim cho Admin và trang Search
  filter: async (name = '', page = 1, size = 10) => {
    try {
      const res = await axiosClient.get(API_ENDPOINTS.MOVIES.FILTER, { params: { name, page, size } });
      return res.data?.data || { content: [], totalPages: 0, totalElements: 0 };
    } catch (error) {
      console.error('Lỗi khi tìm kiếm/lọc danh sách phim:', error);
      return { content: [], totalPages: 0, totalElements: 0 };
    }
  },

  // Thêm phim mới
  add: async (movieData) => {
    try {
      const res = await axiosClient.post(API_ENDPOINTS.MOVIES.ADD, movieData);
      return res.data?.data || res.data;
    } catch (error) {
      console.error('Lỗi khi thêm phim:', error);
      throw error;
    }
  },

  // Cập nhật thông tin phim
  edit: async (id, movieData) => {
    try {
      const res = await axiosClient.put(API_ENDPOINTS.MOVIES.EDIT(id), movieData);
      return res.data?.data || res.data;
    } catch (error) {
      console.error(`Lỗi khi sửa phim ID ${id}:`, error);
      throw error;
    }
  },
};

export default movieService;