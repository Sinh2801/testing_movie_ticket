import { apiCall, API_ENDPOINTS } from '../config/api.js';

export const movieService = {
  /**
   * Lấy danh sách 10 phim mới nhất
   */
  async getLatestMovies() {
    const res = await apiCall(API_ENDPOINTS.MOVIES.GET_LATEST);
    return res.data || [];
  },

  /**
   * Lấy danh sách thể loại phim
   */
  async getGenres() {
    const res = await apiCall(API_ENDPOINTS.MOVIES.GET_GENRES);
    return res.data || [];
  },

  /**
   * Tìm kiếm phim theo tên
   */
  async searchMovies(keyword) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.SEARCH, {
      query: { q: keyword }
    });
    return res.data || [];
  },

  /**
   * Lọc danh sách phim theo thể loại
   */
  async getMoviesByGenre(genreId) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.GET_BY_GENRE, {
      params: { genre: genreId }
    });
    return res.data || [];
  },

  /**
   * Lấy chi tiết thông tin 1 phim theo ID
   */
  async getMovieDetail(movieId) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.GET_DETAIL, {
      params: { id: movieId }
    });
    return res.data;
  }
};
