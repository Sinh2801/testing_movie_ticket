import axiosClient from '../config/axiosClient';
import API_ENDPOINTS from '../config/endpoints';

export const genreService = {
  getAll: async () => {
    const res = await axiosClient.get(API_ENDPOINTS.GENRES.GET_ALL);
    return res.data?.data || [];
  },
};

export const cinemaService = {
  getAll: async () => {
    const res = await axiosClient.get(API_ENDPOINTS.CINEMAS.GET_ALL);
    return res.data?.data || [];
  },
  getById: async (id) => {
    const res = await axiosClient.get(API_ENDPOINTS.CINEMAS.GET_BY_ID(id));
    return res.data?.data || null;
  },
};

export const roomService = {
  getByCinemaId: async (cinemaId) => {
    const res = await axiosClient.get(API_ENDPOINTS.ROOMS.GET_BY_CINEMA_ID(cinemaId));
    return res.data?.data || [];
  },
};

export const showtimeService = {
  getByMovieId: async (movieId) => {
    const res = await axiosClient.get(API_ENDPOINTS.SHOWTIMES.GET_BY_MOVIE_ID(movieId));
    return res.data?.data || [];
  },
  getById: async (id) => {
    const res = await axiosClient.get(API_ENDPOINTS.SHOWTIMES.GET_BY_ID(id));
    return res.data?.data || null;
  },
  getSeats: async (showtimeId) => {
    const res = await axiosClient.get(API_ENDPOINTS.SHOWTIMES.GET_SEATS(showtimeId));
    return res.data?.data || [];
  },
  filter: async (page = 1, size = 10) => {
    const res = await axiosClient.get(API_ENDPOINTS.SHOWTIMES.FILTER, { params: { page, size } });
    return res.data?.data || { content: [], totalPages: 0 };
  },
};

export const ticketService = {
  bookTicket: async (requestData) => {
    // requestData: { showtimeId, seatIds }
    const res = await axiosClient.post(API_ENDPOINTS.TICKETS.BOOK, requestData);
    return res.data?.data || null;
  },
  cancelTicket: async (ticketId) => {
    const res = await axiosClient.post(API_ENDPOINTS.TICKETS.CANCEL(ticketId));
    return res.data;
  },
  getHistory: async () => {
    const res = await axiosClient.get(API_ENDPOINTS.TICKETS.HISTORY);
    return res.data?.data || [];
  },
  getByUid: async (uid) => {
    const res = await axiosClient.get(API_ENDPOINTS.TICKETS.GET_BY_UID(uid));
    return res.data?.data || null;
  },
  filter: async (uid = '', page = 1, size = 10) => {
    const res = await axiosClient.get(API_ENDPOINTS.TICKETS.FILTER, { params: { uid, page, size } });
    return res.data?.data || { content: [], totalPages: 0 };
  },
};

export const adminMovieService = {
  filter: async (name = '', page = 1, size = 10) => {
    const res = await axiosClient.get(API_ENDPOINTS.MOVIES.FILTER, { params: { name, page, size } });
    return res.data?.data || { content: [], totalPages: 0 };
  },
};
