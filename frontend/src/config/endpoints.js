/**
 * Centralized API Endpoints Configuration for Frontend
 * Đã tổng hợp chuẩn từ file backend/test_api.txt
 */

export const API_BASE_URL = 'http://localhost:8081/api';

export const API_ENDPOINTS = {
  // [AUTHENTICATION]
  AUTH: {
    REGISTER: '/auth/register',
    LOGIN: '/auth/login',
    FORGOT_PASSWORD: '/auth/forgot-password',
    RESET_PASSWORD: '/auth/reset-password',
  },

  // [GENRES MANAGEMENT]
  GENRES: {
    GET_ALL: '/genres/all',
    GET_BY_ID: (id) => `/genres/gerne/${id}`,
    ADD: '/genres/add',
    EDIT: (id) => `/genres/edit/${id}`,
  },

  // [CINEMAS MANAGEMENT]
  CINEMAS: {
    GET_ALL: '/cinemas/all',
    GET_BY_ID: (id) => `/cinemas/cinema/${id}`,
    ADD: '/cinemas/add',
    EDIT: '/cinemas/edit',
  },

  // [ROOMS MANAGEMENT]
  ROOMS: {
    GET_BY_CINEMA_ID: (cinemaId) => `/rooms/cinema/${cinemaId}`,
    ADD: '/rooms/add',
    EDIT: '/rooms/edit',
  },

  // [MOVIES MANAGEMENT]
  MOVIES: {
    FILTER: '/movies/filter',
    NOW_SHOWING: '/movies/now-showing',
    COMING_SOON: '/movies/coming-soon',
    GET_BY_ID: (id) => `/movies/movie/${id}`,
    ADD: '/movies/add',
    EDIT: (id) => `/movies/edit/${id}`,
  },

  // [SHOWTIMES MANAGEMENT]
  SHOWTIMES: {
    ADD: '/showtimes/add',
    GET_BY_MOVIE_ID: (movieId) => `/showtimes/movie/${movieId}`,
    GET_BY_ID: (id) => `/showtimes/${id}`,
    GET_SEATS: (showtimeId) => `/showtimes/${showtimeId}/seats`,
    FILTER: '/showtimes/filter',
  },

  // [TICKET BOOKING & CANCELLATION]
  TICKETS: {
    BOOK: '/tickets/book',
    CANCEL: (ticketId) => `/tickets/${ticketId}/cancel`,
    HISTORY: '/tickets/history',
    GET_BY_ID: (id) => `/tickets/${id}`,
    GET_BY_UID: (uid) => `/tickets/uid/${uid}`,
    FILTER: '/tickets/filter',
  },

  // [PAYMENT & EMAIL]
  PAYMENTS: {
    FILTER: '/payments/filter',
    LOCAL_CREATE: '/payments/local/create',
    LOCAL_CONFIRM: '/payments/local/confirm',
    STRIPE_CREATE_INTENT: '/payments/stripe/create-payment-intent',
    STRIPE_CONFIRM: '/payments/stripe/confirm',
    MOMO_CREATE: '/payments/momo/create',
    MOMO_CALLBACK: '/payments/momo/callback',
  },
};

export default API_ENDPOINTS;
