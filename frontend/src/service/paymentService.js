import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

export const paymentService = {
  /**
   * Lấy danh sách thanh toán phân trang có tìm kiếm realtime theo paymentCode
   * @param {number} page - Số trang (bắt đầu từ 1)
   * @param {number} size - Kích thước trang (mặc định 20)
   * @param {string} paymentCode - Từ khóa tìm kiếm theo mã thanh toán
   */
  getPagedPayments: async (page = 1, size = 20, paymentCode = '') => {
    const params = { 
      page, 
      size,
      paymentCode: paymentCode ? paymentCode.trim() : '' 
    };
    const res = await axiosClient.get(API_ENDPOINTS.PAYMENTS.FILTER, { params });
    return res.data?.data || { content: [], totalPages: 0, totalElements: 0, page: 1, size: 20 };
  },

  /**
   * Admin xác nhận thanh toán local (tiền mặt / chuyển khoản)
   * @param {number} ticketId - ID của vé thanh toán
   */
  confirmLocalPayment: async (ticketId) => {
    const res = await axiosClient.post(API_ENDPOINTS.PAYMENTS.LOCAL_CONFIRM, null, {
      params: { ticketId },
    });
    return res.data;
  },

  /**
   * Tạo giao dịch thanh toán local
   * @param {object} requestData - { ticketId }
   */
  createLocalPayment: async (requestData) => {
    const res = await axiosClient.post(API_ENDPOINTS.PAYMENTS.LOCAL_CREATE, requestData);
    return res.data?.data || null;
  },

  /**
   * Khởi tạo Stripe PaymentIntent
   * @param {object} requestData - { ticketId }
   */
  createStripePaymentIntent: async (requestData) => {
    const res = await axiosClient.post(API_ENDPOINTS.PAYMENTS.STRIPE_CREATE_INTENT, requestData);
    return res.data?.data || null;
  },

  /**
   * Xác nhận thanh toán Stripe sau khi thẻ thanh toán thành công
   * @param {number} ticketId
   * @param {string} paymentIntentId
   */
  confirmStripePayment: async (ticketId, paymentIntentId) => {
    const res = await axiosClient.post(API_ENDPOINTS.PAYMENTS.STRIPE_CONFIRM, null, {
      params: { ticketId, paymentIntentId },
    });
    return res.data;
  },
};

export default paymentService;
