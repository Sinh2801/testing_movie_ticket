/**
 * Centralized API Configuration & Client Module
 * All API calls across the web application use this module.
 */

export const API_CONFIG = {
  // Configurable base URL for your real backend server
  BASE_URL: 'https://api.cinebooking.example.com/v1',
  TIMEOUT: 10000,
  // Set to true to automatically return realistic mock data if backend server is unreachable or offline
  ENABLE_MOCK: true
};

/**
 * Named API Endpoints Registry
 */
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: { method: 'POST', path: '/auth/login' },
    REGISTER: { method: 'POST', path: '/auth/register' },
    ME: { method: 'GET', path: '/auth/me' },
    UPDATE_PROFILE: { method: 'PUT', path: '/auth/profile' }
  },
  MOVIES: {
    GET_LATEST: { method: 'GET', path: '/movies/latest' },
    GET_GENRES: { method: 'GET', path: '/movies/genres' },
    SEARCH: { method: 'GET', path: '/movies/search' },
    GET_BY_GENRE: { method: 'GET', path: '/movies/genre/:genre' },
    GET_DETAIL: { method: 'GET', path: '/movies/:id' }
  },
  BOOKING: {
    GET_SHOWTIMES: { method: 'GET', path: '/movies/:id/showtimes' },
    GET_SEATS: { method: 'GET', path: '/showtimes/:showtimeId/seats' },
    CREATE_BOOKING: { method: 'POST', path: '/bookings' },
    GET_USER_BOOKINGS: { method: 'GET', path: '/bookings/my-tickets' }
  }
};

/**
 * Comprehensive Mock Database for seamless offline demo & development
 */
const MOCK_DATA = {
  genres: [
    { id: 'all', name: 'Tất cả thể loại' },
    { id: 'action', name: 'Hành Động' },
    { id: 'sci-fi', name: 'Viễn Tưởng' },
    { id: 'romance', name: 'Tình Cảm' },
    { id: 'horror', name: 'Kinh Dị' },
    { id: 'comedy', name: 'Hài Hước' },
    { id: 'animation', name: 'Hoạt Hình' },
    { id: 'drama', name: 'Tâm Lý' }
  ],
  movies: [
    {
      id: 'm1',
      title: 'Avatar: Dòng Dòng Nước (The Way of Water)',
      duration: '192 phút',
      country: 'Mỹ',
      status: 'Đang chiếu',
      director: 'James Cameron',
      actors: 'Sam Worthington, Zoe Saldaña, Sigourney Weaver, Stephen Lang',
      genres: ['sci-fi', 'action', 'drama'],
      genreName: 'Viễn Tưởng / Hành Động',
      rating: 8.9,
      poster: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1600&q=80',
      description: 'Lấy bối cảnh hơn một thập kỷ sau các sự kiện của phần phim đầu tiên, bộ phim kể câu chuyện về gia đình Sully, những rắc rối theo sau họ, những nỗ lực họ thực hiện để giữ an toàn cho nhau, những trận chiến họ phải chiến đấu để sinh tồn và những bi kịch họ phải chịu đựng.'
    },
    {
      id: 'm2',
      title: 'Oppenheimer',
      duration: '180 phút',
      country: 'Mỹ / Anh',
      status: 'Đang chiếu',
      director: 'Christopher Nolan',
      actors: 'Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.',
      genres: ['drama', 'history'],
      genreName: 'Tâm Lý / Lịch Sử',
      rating: 9.1,
      poster: 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?auto=format&fit=crop&w=1600&q=80',
      description: 'Câu chuyện về nhà vật lý lý thuyết J. Robert Oppenheimer, người đứng đầu Phòng thí nghiệm Los Alamos trong Dự án Manhattan thời Chiến tranh thế giới thứ hai, và vai trò của ông trong việc phát triển bom nguyên tử.'
    },
    {
      id: 'm3',
      title: 'Chiến Binh Báo Đen: Wakanda Bất Diệt',
      duration: '161 phút',
      country: 'Mỹ',
      status: 'Đang chiếu',
      director: 'Ryan Coogler',
      actors: 'Letitia Wright, Lupita Nyong\'o, Danai Gurira, Winston Duke',
      genres: ['action', 'sci-fi'],
      genreName: 'Hành Động / Viễn Tưởng',
      rating: 8.5,
      poster: 'https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1600&q=80',
      description: 'Nữ hoàng Ramonda, Shuri, M\'Baku, Okoye và Dora Milaje chiến đấu để bảo vệ quốc gia của họ khỏi sự can thiệp của các thế lực thế giới sau cái chết của Vua T\'Challa.'
    },
    {
      id: 'm4',
      title: 'Hành Tinh Khỉ: Vương Quốc Mới',
      duration: '145 phút',
      country: 'Mỹ',
      status: 'Đang chiếu',
      director: 'Wes Ball',
      actors: 'Owen Teague, Freya Allan, Kevin Durand, Peter Macon',
      genres: ['sci-fi', 'action'],
      genreName: 'Viễn Tưởng / Phiêu Lưu',
      rating: 8.3,
      poster: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1600&q=80',
      description: 'Nhiều năm sau triều đại của Caesar, một con khỉ trẻ tuổi bước vào một hành trình dẫn dắt anh ta chấm dứt quá khứ và đưa ra những lựa chọn định hình tương lai cho cả loài khỉ và con người.'
    },
    {
      id: 'm5',
      title: 'Dune: Hành Tinh Cát - Phần 2',
      duration: '166 phút',
      country: 'Mỹ / Canada',
      status: 'Đang chiếu',
      director: 'Denis Villeneuve',
      actors: 'Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem',
      genres: ['sci-fi', 'action'],
      genreName: 'Viễn Tưởng / Phiêu Lưu',
      rating: 9.3,
      poster: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1600&q=80',
      description: 'Paul Atreides hợp lực với Chani và người Fremen để trả thù những kẻ âm mưu hủy hoại gia đình anh, đối mặt với sự lựa chọn giữa tình yêu của đời mình và số phận của vũ trụ.'
    },
    {
      id: 'm6',
      title: 'Chuyến Tàu Băng Giá (Bullet Train)',
      duration: '126 phút',
      country: 'Mỹ / Nhật Bản',
      status: 'Đang chiếu',
      director: 'David Leitch',
      actors: 'Brad Pitt, Joey King, Aaron Taylor-Johnson, Brian Tyree Henry',
      genres: ['action', 'comedy'],
      genreName: 'Hành Động / Hài Hước',
      rating: 8.0,
      poster: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?auto=format&fit=crop&w=1600&q=80',
      description: 'Năm sát thủ cùng lên một chiếc tàu siêu tốc di chuyển từ Tokyo đến Morioka và nhận ra những nhiệm vụ của họ không hề độc lập mà có sự kết nối bất ngờ.'
    },
    {
      id: 'm7',
      title: 'Chú Thuật Hồi Chiến: Hỏa Ngục (Jujutsu Kaisen 0)',
      duration: '105 phút',
      country: 'Nhật Bản',
      status: 'Đang chiếu',
      director: 'Sunghoo Park',
      actors: 'Megumi Ogata, Kana Hanazawa, Mikako Komatsu, Yuichi Nakamura',
      genres: ['animation', 'action', 'fantasy'],
      genreName: 'Hoạt Hình / Giả Tưởng',
      rating: 8.7,
      poster: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=1600&q=80',
      description: 'Yuta Okkotsu, một học sinh trung học bị nguyền rủa bởi hồn ma của người bạn thơ ấu Rika, đăng ký học tại Chú Thuật Cao Đẳng Tokyo dưới sự dẫn dắt của Satoru Gojo.'
    },
    {
      id: 'm8',
      title: 'Quật Mộ Trùng Phùng (Exhuma)',
      duration: '134 phút',
      country: 'Hàn Quốc',
      status: 'Đang chiếu',
      director: 'Jang Jae-hyun',
      actors: 'Choi Min-sik, Kim Go-eun, Yoo Hai-jin, Lee Do-hyun',
      genres: ['horror', 'drama'],
      genreName: 'Kinh Dị / Bí Ẩn',
      rating: 8.8,
      poster: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=1600&q=80',
      description: 'Sau khi gặp phải hàng loạt sự kiện siêu nhiên kỳ quái, một gia đình giàu có ở Los Angeles đã thuê hai pháp sư trẻ để giải cứu em bé mới sinh của họ.'
    },
    {
      id: 'm9',
      title: 'Top Gun: Maverick (Phi Công Siêu Đẳng)',
      duration: '130 phút',
      country: 'Mỹ',
      status: 'Đang chiếu',
      director: 'Joseph Kosinski',
      actors: 'Tom Cruise, Miles Teller, Jennifer Connelly, Jon Hamm',
      genres: ['action', 'drama'],
      genreName: 'Hành Động / Kịch Tính',
      rating: 9.0,
      poster: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1600&q=80',
      description: 'Sau hơn 30 năm phục vụ với tư cách là một trong những phi công hàng đầu của Hải quân, Pete "Maverick" Mitchell phải huấn luyện một đội phi công trẻ cho một nhiệm vụ nguy hiểm.'
    },
    {
      id: 'm10',
      title: 'Kẻ Trộm Mặt Trăng 4 (Despicable Me 4)',
      duration: '94 phút',
      country: 'Mỹ',
      status: 'Sắp chiếu',
      director: 'Chris Renaud',
      actors: 'Steve Carell, Kristen Wiig, Will Ferrell, Sofia Vergara',
      genres: ['animation', 'comedy'],
      genreName: 'Hoạt Hình / Hài Hước',
      rating: 8.2,
      poster: 'https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=800&q=80',
      banner: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=1600&q=80',
      description: 'Gru và các Minion trở lại trong cuộc phiêu lưu mới đầy tiếng cười khi gia đình nhỏ chào đón thành viên Gru Jr., đồng thời đối mặt với kẻ thù mới nguy hiểm.'
    }
  ]
};

/**
 * Universal API Dispatcher Helper
 * @param {Object} endpoint - Endpoint object from API_ENDPOINTS (e.g. API_ENDPOINTS.MOVIES.GET_LATEST)
 * @param {Object} options - Options containing params, query, body, headers
 * @returns {Promise<Object>} Response data
 */
export async function apiCall(endpoint, { params = {}, query = {}, body = null, headers = {} } = {}) {
  // Construct URL by replacing dynamic parameters (e.g., :id, :genre)
  let path = endpoint.path;
  Object.keys(params).forEach(key => {
    path = path.replace(`:${key}`, encodeURIComponent(params[key]));
  });

  // Append query strings if present
  const queryString = new URLSearchParams(query).toString();
  const fullUrl = `${API_CONFIG.BASE_URL}${path}${queryString ? `?${queryString}` : ''}`;

  // Attach auth token if available
  const token = localStorage.getItem('cine_token');
  const requestHeaders = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...headers
  };

  try {
    if (!API_CONFIG.ENABLE_MOCK) {
      const response = await fetch(fullUrl, {
        method: endpoint.method,
        headers: requestHeaders,
        body: body ? JSON.stringify(body) : null
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Lỗi API HTTP (${response.status})`);
      }

      return await response.json();
    }
  } catch (error) {
    console.warn(`[API Notice] Request to real endpoint failed or mock mode enabled. Using Mock Engine. Endpoint: ${path}`, error);
  }

  // --- MOCK ENGINE FALLBACK ---
  await new Promise(res => setTimeout(res, 200)); // Simulate realistic network delay

  // Auth Mocks
  if (endpoint.path === API_ENDPOINTS.AUTH.LOGIN.path) {
    const { email, password } = body || {};
    if (!email || !password) throw new Error('Vui lòng nhập Email và Mật khẩu');
    const mockUser = {
      id: 'u_' + Date.now(),
      username: email.split('@')[0],
      email: email,
      phone: '0988776655',
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${email}`
    };
    return {
      success: true,
      token: 'mock_jwt_token_' + Date.now(),
      user: mockUser
    };
  }

  if (endpoint.path === API_ENDPOINTS.AUTH.REGISTER.path) {
    const { username, email, password, phone } = body || {};
    if (!username || !email || !password || !phone) {
      throw new Error('Vui lòng điền đầy đủ tất cả các trường đăng ký');
    }
    const mockUser = {
      id: 'u_' + Date.now(),
      username,
      email,
      phone,
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`
    };
    return {
      success: true,
      token: 'mock_jwt_token_' + Date.now(),
      user: mockUser
    };
  }

  if (endpoint.path === API_ENDPOINTS.AUTH.ME.path) {
    const userStr = localStorage.getItem('cine_user');
    if (!userStr) throw new Error('Chưa đăng nhập');
    return { success: true, user: JSON.parse(userStr) };
  }

  // Movie Mocks
  if (endpoint.path === API_ENDPOINTS.MOVIES.GET_LATEST.path) {
    return { success: true, data: MOCK_DATA.movies.slice(0, 10) };
  }

  if (endpoint.path === API_ENDPOINTS.MOVIES.GET_GENRES.path) {
    return { success: true, data: MOCK_DATA.genres };
  }

  if (endpoint.path === API_ENDPOINTS.MOVIES.GET_DETAIL.path) {
    const movie = MOCK_DATA.movies.find(m => m.id === params.id) || MOCK_DATA.movies[0];
    return { success: true, data: movie };
  }

  if (endpoint.path === API_ENDPOINTS.MOVIES.SEARCH.path) {
    const keyword = (query.q || '').toLowerCase();
    const filtered = MOCK_DATA.movies.filter(m => m.title.toLowerCase().includes(keyword));
    return { success: true, data: filtered };
  }

  if (endpoint.path === API_ENDPOINTS.MOVIES.GET_BY_GENRE.path) {
    const genre = params.genre;
    if (genre === 'all' || !genre) return { success: true, data: MOCK_DATA.movies };
    const filtered = MOCK_DATA.movies.filter(m => m.genres.includes(genre));
    return { success: true, data: filtered };
  }

  // Booking Mocks
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
