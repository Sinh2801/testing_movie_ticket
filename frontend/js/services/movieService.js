import { apiCall, API_ENDPOINTS } from '../config/api.js';

export const movieService = {
  /**
   * Lấy danh sách 10 phim mới nhất đang chiếu (movieStatus: NOW_SHOWING)
   */
  async getLatestMovies() {
    const res = await apiCall(API_ENDPOINTS.MOVIES.FILTER, {
      query: {
        size: 10,
        page: 1,
        name: '',
        genreId: 0,
        movieStatus: 'NOW_SHOWING',
        country: ''
      }
    });
    return res.data || [];
  },

  /**
   * Lấy danh sách 3 phim mới nhất sắp chiếu (movieStatus: COMING_SOON) làm Hero Banner
   */
  async getComingSoonMovies(size = 3) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.FILTER, {
      query: {
        size: size,
        page: 1,
        name: '',
        genreId: 0,
        movieStatus: 'COMING_SOON',
        country: ''
      }
    });
    return res.data || [];
  },

  /**
   * Lọc và tìm kiếm phim linh hoạt (tên, thể loại, quốc gia, trạng thái)
   */
  async filterMovies({ name = '', genreId = 0, movieStatus = 'NOW_SHOWING', country = '', page = 1, size = 10 } = {}) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.FILTER, {
      query: {
        size: size,
        page: page,
        name: name,
        genreId: parseInt(genreId) || 0,
        movieStatus: movieStatus,
        country: country
      }
    });
    return res.data || [];
  },

  /**
   * Lấy danh sách thể loại phim (gọi endpoint /genres/all của Spring Boot)
   */
  async getGenres() {
    const res = await apiCall(API_ENDPOINTS.GENRES.GET_ALL);
    return res.data || [];
  },

  /**
   * Tìm kiếm phim theo tên (gọi endpoint /movies/filter của Spring Boot)
   */
  async searchMovies(keyword) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.FILTER, {
      query: {
        size: 20,
        page: 1,
        name: keyword,
        genreId: 0,
        movieStatus: 'NOW_SHOWING',
        country: ''
      }
    });
    return res.data || [];
  },

  /**
   * Lọc danh sách phim theo thể loại (gọi endpoint /movies/filter của Spring Boot)
   */
  async getMoviesByGenre(genreId) {
    const numericGenreId = parseInt(genreId) || 0;
    const res = await apiCall(API_ENDPOINTS.MOVIES.FILTER, {
      query: {
        size: 20,
        page: 1,
        name: '',
        genreId: numericGenreId,
        movieStatus: 'NOW_SHOWING',
        country: ''
      }
    });
    return res.data || [];
  },

  /**
   * Lấy chi tiết thông tin 1 phim theo ID (gọi endpoint /movies/movie/{id} của Spring Boot)
   */
  async getMovieDetail(movieId) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.GET_DETAIL, {
      params: { id: movieId }
    });
    return res.data;
  },

  /**
   * Thêm phim mới (POST /api/movies/add)
   */
  async addMovie(movieData) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.ADD_MOVIE, {
      body: movieData
    });
    return res;
  },

  /**
   * Cập nhật phim (POST /api/movies/edit)
   */
  async updateMovie(movieData) {
    const res = await apiCall(API_ENDPOINTS.MOVIES.UPDATE_MOVIE, {
      body: movieData
    });
    return res;
  },

  /**
   * Thêm thể loại mới (POST /api/genres/add)
   */
  async addGenre(name) {
    const res = await apiCall(API_ENDPOINTS.GENRES.ADD_GENRE, {
      body: { name }
    });
    return res;
  },

  /**
   * Cập nhật thể loại (PUT /api/genres/edit)
   */
  async updateGenre(id, name) {
    const res = await apiCall(API_ENDPOINTS.GENRES.UPDATE_GENRE, {
      body: { id: parseInt(id), name }
    });
    return res;
  }
};
