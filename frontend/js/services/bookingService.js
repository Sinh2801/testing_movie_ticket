import { apiCall, API_ENDPOINTS } from '../config/api.js';

export const bookingService = {
  /**
   * Lấy danh sách suất chiếu của phim
   */
  async getShowtimes(movieId) {
    const res = await apiCall(API_ENDPOINTS.BOOKING.GET_SHOWTIMES, {
      params: { id: movieId }
    });
    return res.showtimes || [];
  },

  /**
   * Tạo đơn đặt vé mới
   */
  async createBooking(bookingData) {
    const res = await apiCall(API_ENDPOINTS.BOOKING.CREATE_BOOKING, {
      body: bookingData
    });
    return res;
  },

  /**
   * Lấy lịch sử đặt vé của người dùng
   */
  async getUserBookings() {
    const res = await apiCall(API_ENDPOINTS.BOOKING.GET_USER_BOOKINGS);
    return res.data || [];
  }
};
