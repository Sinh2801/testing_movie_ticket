/**
 * Centralized API Configuration & Client Module
 * Aligned with Spring Boot Backend (com.movieticketbooking)
 */

export const API_CONFIG = {
  // Configured to point directly to your Spring Boot REST API
  BASE_URL: 'http://localhost:8080/api',
  TIMEOUT: 10000,
  // Set to true to automatically fallback to Mock Engine if Spring Boot server is unreachable
  ENABLE_MOCK: true
};

/**
 * Named API Endpoints Registry (Matches Spring Controllers)
 */
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: { method: 'POST', path: '/auth/login' },
    REGISTER: { method: 'POST', path: '/auth/register' },
    ME: { method: 'GET', path: '/auth/me' }
  },
  MOVIES: {
    FILTER: { method: 'GET', path: '/movies/filter' },
    GET_LATEST: { method: 'GET', path: '/movies/filter' },
    SEARCH: { method: 'GET', path: '/movies/filter' },
    GET_BY_GENRE: { method: 'GET', path: '/movies/filter' },
    GET_DETAIL: { method: 'GET', path: '/movies/movie/:id' },
    ADD_MOVIE: { method: 'POST', path: '/movies/add' },
    UPDATE_MOVIE: { method: 'POST', path: '/movies/edit' }
  },
  GENRES: {
    GET_ALL: { method: 'GET', path: '/genres/all' },
    GET_BY_ID: { method: 'GET', path: '/genres/genre/:id' },
    ADD_GENRE: { method: 'POST', path: '/genres/add' },
    UPDATE_GENRE: { method: 'PUT', path: '/genres/edit' }
  },
  CINEMAS: {
    GET_ALL: { method: 'GET', path: '/cinemas/all' },
    GET_BY_ID: { method: 'GET', path: '/cinemas/cinema/:id' },
    ADD_CINEMA: { method: 'POST', path: '/cinemas/add' },
    UPDATE_CINEMA: { method: 'PUT', path: '/cinemas/edit' }
  },
  ROOMS: {
    GET_BY_CINEMA: { method: 'GET', path: '/rooms' },
    ADD_ROOM: { method: 'POST', path: '/rooms/add' },
    UPDATE_ROOM: { method: 'PUT', path: '/rooms/update' }
  },
  BOOKING: {
    GET_SHOWTIMES: { method: 'GET', path: '/movies/movie/:id/showtimes' },
    CREATE_BOOKING: { method: 'POST', path: '/bookings' },
    GET_USER_BOOKINGS: { method: 'GET', path: '/bookings/my-tickets' }
  }
};

/**
 * Helper to normalize backend DTOs (MovieResponse) to frontend UI attributes
 */
function normalizeMovie(m) {
  if (!m) return null;
  const statusMap = {
    'NOW_SHOWING': 'Đang chiếu',
    'COMING_SOON': 'Sắp chiếu',
    'STOPPED_SHOWING': 'Đã ngưng chiếu'
  };

  return {
    id: m.id || 'm1',
    title: m.name || m.title || 'Phim Chiếu Rạp',
    duration: typeof m.duration === 'number' ? `${m.duration} phút` : (m.duration || '120 phút'),
    country: m.country || 'Việt Nam',
    status: statusMap[m.movieStatus] || m.status || 'Đang chiếu',
    director: m.directorName || m.director || 'Chưa cập nhật',
    actors: m.actorsName || m.actors || 'Chưa cập nhật',
    genres: Array.isArray(m.genres) ? m.genres : [],
    genreName: Array.isArray(m.genres) ? m.genres.join(' / ') : (m.genreName || 'Điện Ảnh'),
    rating: m.rating || 8.8,
    poster: m.imageUrl || m.poster || 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=800&q=80',
    banner: m.imageUrl || m.banner || 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1600&q=80',
    description: m.description || 'Nội dung bộ phim đang được cập nhật.'
  };
}

/**
 * Comprehensive Mock Database matching Spring Boot Response structures
 */
const MOCK_DATA = {
  genres: [
    { id: 0, name: 'Tất cả thể loại' },
    { id: 1, name: 'Hành Động' },
    { id: 2, name: 'Viễn Tưởng' },
    { id: 3, name: 'Tình Cảm' },
    { id: 4, name: 'Kinh Dị' },
    { id: 5, name: 'Hài Hước' },
    { id: 6, name: 'Hoạt Hình' },
    { id: 7, name: 'Tâm Lý' }
  ],
  movies: [
    {
      id: 1,
      name: 'Avatar: Dòng Nước (The Way of Water)',
      duration: 192,
      country: 'Mỹ',
      movieStatus: 'NOW_SHOWING',
      directorName: 'James Cameron',
      actorsName: 'Sam Worthington, Zoe Saldaña, Sigourney Weaver, Stephen Lang',
      genres: ['Viễn Tưởng', 'Hành Động'],
      imageUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=800&q=80',
      description: 'Lấy bối cảnh hơn một thập kỷ sau các sự kiện của phần phim đầu tiên, bộ phim kể câu chuyện về gia đình Sully.'
    },
    {
      id: 2,
      name: 'Oppenheimer',
      duration: 180,
      country: 'Mỹ / Anh',
      movieStatus: 'NOW_SHOWING',
      directorName: 'Christopher Nolan',
      actorsName: 'Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.',
      genres: ['Tâm Lý', 'Lịch Sử'],
      imageUrl: 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=800&q=80',
      description: 'Câu chuyện về nhà vật lý lý thuyết J. Robert Oppenheimer, người đứng đầu Dự án Manhattan thời Chiến tranh thế giới thứ hai.'
    },
    {
      id: 3,
      name: 'Chiến Binh Báo Đen: Wakanda Bất Diệt',
      duration: 161,
      country: 'Mỹ',
      movieStatus: 'NOW_SHOWING',
      directorName: 'Ryan Coogler',
      actorsName: 'Letitia Wright, Lupita Nyong\'o, Danai Gurira, Winston Duke',
      genres: ['Hành Động', 'Viễn Tưởng'],
      imageUrl: 'https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=800&q=80',
      description: 'Nữ hoàng Ramonda và Shuri chiến đấu để bảo vệ quốc gia Wakanda khỏi các thế lực can thiệp sau cái chết của Vua T\'Challa.'
    },
    {
      id: 4,
      name: 'Hành Tinh Khỉ: Vương Quốc Mới',
      duration: 145,
      country: 'Mỹ',
      movieStatus: 'NOW_SHOWING',
      directorName: 'Wes Ball',
      actorsName: 'Owen Teague, Freya Allan, Kevin Durand, Peter Macon',
      genres: ['Viễn Tưởng', 'Hành Động'],
      imageUrl: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80',
      description: 'Nhiều năm sau triều đại của Caesar, một con khỉ trẻ tuổi bước vào hành trình định hình tương lai loài khỉ.'
    },
    {
      id: 5,
      name: 'Dune: Hành Tinh Cát - Phần 2',
      duration: 166,
      country: 'Mỹ / Canada',
      movieStatus: 'NOW_SHOWING',
      directorName: 'Denis Villeneuve',
      actorsName: 'Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem',
      genres: ['Viễn Tưởng', 'Hành Động'],
      imageUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80',
      description: 'Paul Atreides hợp lực với Chani và người Fremen để trả thù những kẻ âm mưu hủy hoại gia đình anh.'
    },
    {
      id: 6,
      name: 'Chuyến Tàu Băng Giá (Bullet Train)',
      duration: 126,
      country: 'Mỹ / Nhật Bản',
      movieStatus: 'NOW_SHOWING',
      directorName: 'David Leitch',
      actorsName: 'Brad Pitt, Joey King, Aaron Taylor-Johnson',
      genres: ['Hành Động', 'Hài Hước'],
      imageUrl: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80',
      description: 'Năm sát thủ cùng lên một chiếc tàu siêu tốc di chuyển từ Tokyo đến Morioka.'
    },
    {
      id: 7,
      name: 'Chú Thuật Hồi Chiến: Hỏa Ngục (Jujutsu Kaisen 0)',
      duration: 105,
      country: 'Nhật Bản',
      movieStatus: 'NOW_SHOWING',
      directorName: 'Sunghoo Park',
      actorsName: 'Megumi Ogata, Kana Hanazawa, Yuichi Nakamura',
      genres: ['Hoạt Hình', 'Hành Động'],
      imageUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80',
      description: 'Yuta Okkotsu đăng ký học tại Chú Thuật Cao Đẳng Tokyo dưới sự dẫn dắt của Satoru Gojo.'
    },
    {
      id: 8,
      name: 'Quật Mộ Trùng Phùng (Exhuma)',
      duration: 134,
      country: 'Hàn Quốc',
      movieStatus: 'NOW_SHOWING',
      directorName: 'Jang Jae-hyun',
      actorsName: 'Choi Min-sik, Kim Go-eun, Lee Do-hyun',
      genres: ['Kinh Dị', 'Bí Ẩn'],
      imageUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80',
      description: 'Sau khi gặp phải hàng loạt sự kiện siêu nhiên kỳ quái, hai pháp sư trẻ giải cứu em bé mới sinh.'
    },
    {
      id: 9,
      name: 'Top Gun: Maverick (Phi Công Siêu Đẳng)',
      duration: 130,
      country: 'Mỹ',
      movieStatus: 'NOW_SHOWING',
      directorName: 'Joseph Kosinski',
      actorsName: 'Tom Cruise, Miles Teller, Jennifer Connelly',
      genres: ['Hành Động', 'Tâm Lý'],
      imageUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=800&q=80',
      description: 'Pete "Maverick" Mitchell phải huấn luyện một đội phi công trẻ cho một nhiệm vụ nguy hiểm.'
    },
    {
      id: 10,
      name: 'Kẻ Trộm Mặt Trăng 4 (Despicable Me 4)',
      duration: 94,
      country: 'Mỹ',
      movieStatus: 'COMING_SOON',
      directorName: 'Chris Renaud',
      actorsName: 'Steve Carell, Kristen Wiig, Will Ferrell',
      genres: ['Hoạt Hình', 'Hài Hước'],
      imageUrl: 'https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=800&q=80',
      description: 'Gru và các Minion trở lại trong cuộc phiêu lưu mới đầy tiếng cười.'
    }
  ]
};

/**
 * Universal API Dispatcher Helper
 */
export async function apiCall(endpoint, { params = {}, query = {}, body = null, headers = {} } = {}) {
  let path = endpoint.path;
  Object.keys(params).forEach(key => {
    path = path.replace(`:${key}`, encodeURIComponent(params[key]));
  });

  const queryString = new URLSearchParams(query).toString();
  const fullUrl = `${API_CONFIG.BASE_URL}${path}${queryString ? `?${queryString}` : ''}`;

  const token = localStorage.getItem('cine_token');
  const requestHeaders = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...headers
  };

  try {
    const response = await fetch(fullUrl, {
      method: endpoint.method,
      headers: requestHeaders,
      body: body ? JSON.stringify(body) : null
    });

    if (response.ok) {
      const json = await response.json();
      
      // Handle Spring Boot ApiResponse wrapper: { message: "...", data: ... }
      const rawData = json.data !== undefined ? json.data : json;

      // Handle PagedResponse wrapper: { content: [...], pageNumber: 1, ... }
      if (rawData && rawData.content && Array.isArray(rawData.content)) {
        return {
          success: true,
          message: json.message || 'Success',
          data: rawData.content.map(normalizeMovie)
        };
      }

      // Handle Single Movie object
      if (rawData && (rawData.name || rawData.title)) {
        return {
          success: true,
          message: json.message || 'Success',
          data: normalizeMovie(rawData)
        };
      }

      // Handle List of items (e.g. Genres or Cinemas)
      if (Array.isArray(rawData)) {
        return {
          success: true,
          message: json.message || 'Success',
          data: rawData
        };
      }

      return { success: true, message: json.message || 'Success', data: rawData };
    }
  } catch (error) {
    if (!API_CONFIG.ENABLE_MOCK) {
      throw error;
    }
    console.warn(`[API Notice] Request to Spring Boot backend failed (${fullUrl}). Using Mock Engine.`, error);
  }

  // --- MOCK ENGINE FALLBACK (Aligned with Spring Boot DTOs) ---
  await new Promise(res => setTimeout(res, 150));

  // Auth Mocks
  if (endpoint.path === API_ENDPOINTS.AUTH.LOGIN.path) {
    const { email, password } = body || {};
    const mockUser = {
      id: 1,
      username: (email || 'user').split('@')[0],
      email: email || 'user@example.com',
      phoneNumber: '0988776655',
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${email}`
    };
    return { success: true, token: 'mock_jwt_token_123', user: mockUser };
  }

  if (endpoint.path === API_ENDPOINTS.AUTH.REGISTER.path) {
    const { username, email, phone } = body || {};
    const mockUser = {
      id: 2,
      username: username || 'newuser',
      email: email || 'new@example.com',
      phoneNumber: phone || '0912345678',
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`
    };
    return { success: true, token: 'mock_jwt_token_456', user: mockUser };
  }

  // Movies Filter & Search Mocks
  if (endpoint.path === API_ENDPOINTS.MOVIES.FILTER.path) {
    let list = [...MOCK_DATA.movies];
    if (query.name) {
      list = list.filter(m => m.name.toLowerCase().includes(query.name.toLowerCase()));
    }
    if (query.movieStatus) {
      list = list.filter(m => m.movieStatus === query.movieStatus);
    }
    return { success: true, data: list.map(normalizeMovie) };
  }

  // Movie Detail Mock
  if (endpoint.path === API_ENDPOINTS.MOVIES.GET_DETAIL.path) {
    const targetId = parseInt(params.id) || 1;
    const movie = MOCK_DATA.movies.find(m => m.id === targetId) || MOCK_DATA.movies[0];
    return { success: true, data: normalizeMovie(movie) };
  }

  // Genres Mock
  if (endpoint.path === API_ENDPOINTS.GENRES.GET_ALL.path) {
    return { success: true, data: MOCK_DATA.genres };
  }

  // Showtimes & Booking Mocks
  if (endpoint.path === API_ENDPOINTS.BOOKING.GET_SHOWTIMES.path) {
    return {
      success: true,
      showtimes: [
        { id: 'st1', time: '14:30', room: 'Phòng 01 (2D Atmos)', price: 90000 },
        { id: 'st2', time: '17:00', room: 'Phòng 03 (IMAX 3D)', price: 120000 },
        { id: 'st3', time: '19:45', room: 'Phòng 02 (2D Vietsub)', price: 95000 },
        { id: 'st4', time: '22:15', room: 'Phòng VIP Premium', price: 150000 }
      ]
    };
  }

  if (endpoint.path === API_ENDPOINTS.BOOKING.CREATE_BOOKING.path) {
    const booking = {
      id: 'BK-' + Math.floor(100000 + Math.random() * 900000),
      ...body,
      createdAt: new Date().toISOString()
    };
    const history = JSON.parse(localStorage.getItem('cine_history') || '[]');
    history.unshift(booking);
    localStorage.setItem('cine_history', JSON.stringify(history));
    return { success: true, booking };
  }

  if (endpoint.path === API_ENDPOINTS.BOOKING.GET_USER_BOOKINGS.path) {
    const history = JSON.parse(localStorage.getItem('cine_history') || '[]');
    return { success: true, data: history };
  }

  return { success: true, data: [] };
}
