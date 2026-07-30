import { apiCall, API_ENDPOINTS } from '../config/api.js';

export const cinemaService = {
  /**
   * Lấy tất cả rạp chiếu (GET /api/cinemas/all)
   */
  async getAllCinemas() {
    const res = await apiCall(API_ENDPOINTS.CINEMAS.GET_ALL);
    return res.data || [];
  },

  /**
   * Lấy chi tiết rạp chiếu theo ID (GET /api/cinemas/cinema/{id})
   */
  async getCinemaById(id) {
    const res = await apiCall(API_ENDPOINTS.CINEMAS.GET_BY_ID, {
      params: { id }
    });
    return res.data;
  },

  /**
   * Thêm rạp chiếu mới (POST /api/cinemas/add)
   */
  async createCinema(data) {
    const res = await apiCall(API_ENDPOINTS.CINEMAS.ADD_CINEMA, {
      body: data
    });
    return res;
  },

  /**
   * Cập nhật rạp chiếu (PUT /api/cinemas/edit)
   */
  async updateCinema(data) {
    const res = await apiCall(API_ENDPOINTS.CINEMAS.UPDATE_CINEMA, {
      body: data
    });
    return res;
  },

  /**
   * Lấy danh sách phòng chiếu theo rạp (GET /api/rooms?cinemaId={id})
   */
  async getRoomsByCinemaId(cinemaId) {
    const res = await apiCall(API_ENDPOINTS.ROOMS.GET_BY_CINEMA, {
      query: { cinemaId }
    });
    return res.data || [];
  },

  /**
   * Thêm phòng chiếu mới (POST /api/rooms/add)
   */
  async createRoom(data) {
    const res = await apiCall(API_ENDPOINTS.ROOMS.ADD_ROOM, {
      body: data
    });
    return res;
  },

  /**
   * Cập nhật phòng chiếu (PUT /api/rooms/update)
   */
  async updateRoom(data) {
    const res = await apiCall(API_ENDPOINTS.ROOMS.UPDATE_ROOM, {
      body: data
    });
    return res;
  }
};
