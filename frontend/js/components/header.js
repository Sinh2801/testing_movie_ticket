import { authService } from '../services/authService.js';
import { movieService } from '../services/movieService.js';

export async function renderHeader() {
  const headerContainer = document.getElementById('main-header');
  if (!headerContainer) return;

  const currentUser = authService.getCurrentUser();
  const isLoggedIn = authService.isLoggedIn();
  const genres = await movieService.getGenres();

  // URL search query check
  const urlParams = new URLSearchParams(window.location.search);
  const currentSearch = urlParams.get('search') || '';
  const currentGenre = urlParams.get('genre') || 'all';

  headerContainer.innerHTML = `
    <nav class="bg-slate-900/90 backdrop-blur-md border-b border-slate-800/80 sticky top-0 z-50 transition-all duration-300">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-20 gap-4">
          
          <!-- Logo -->
          <a href="index.html" class="flex items-center gap-3 group shrink-0">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-500 via-rose-500 to-purple-600 p-0.5 shadow-lg shadow-rose-500/20 group-hover:scale-105 transition-transform duration-300">
              <div class="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
                <i class="fa-solid fa-clapperboard text-rose-500 text-lg group-hover:rotate-12 transition-transform duration-300"></i>
              </div>
            </div>
            <div class="flex flex-col">
              <span class="text-xl font-extrabold tracking-wider bg-gradient-to-r from-white via-slate-100 to-rose-400 bg-clip-text text-transparent">
                CINE<span class="text-rose-500">PRIME</span>
              </span>
              <span class="text-[10px] tracking-widest uppercase text-slate-400 font-medium">Đặt Vé Xem Phim</span>
            </div>
          </a>

          <!-- Middle Section: Search bar & Genre selector -->
          <div class="flex-1 max-w-2xl flex items-center gap-3">
            
            <!-- Genre Dropdown -->
            <div class="relative min-w-[140px] hidden md:block">
              <select id="header-genre-select" class="w-full bg-slate-800/80 text-slate-200 text-sm font-medium rounded-xl border border-slate-700/60 px-3 py-2.5 outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 cursor-pointer appearance-none pr-8 transition-colors">
                ${genres.map(g => `<option value="${g.id}" ${currentGenre === g.id ? 'selected' : ''}>${g.name}</option>`).join('')}
              </select>
              <div class="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400 text-xs">
                <i class="fa-solid fa-chevron-down"></i>
              </div>
            </div>

            <!-- Search Bar -->
            <div class="relative flex-1">
              <form id="header-search-form" class="relative">
                <input 
                  type="text" 
                  id="header-search-input" 
                  placeholder="Tìm kiếm phim theo tên..." 
                  value="${escapeHtml(currentSearch)}"
                  class="w-full bg-slate-800/80 text-white placeholder-slate-400 text-sm rounded-xl border border-slate-700/60 pl-10 pr-10 py-2.5 outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-all duration-300"
                />
                <button type="submit" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-rose-400 transition-colors">
                  <i class="fa-solid fa-magnifying-glass"></i>
                </button>
                ${currentSearch ? `
                  <button type="button" id="header-clear-search" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white text-xs">
                    <i class="fa-solid fa-xmark"></i>
                  </button>
                ` : ''}
              </form>
            </div>

          </div>

          <!-- Right Section: Auth Action / Avatar Dropdown -->
          <div class="flex items-center gap-3 shrink-0">
            ${isLoggedIn ? `
              <!-- Logged In State: Avatar + Name + Dropdown -->
              <div class="relative" id="user-dropdown-wrapper">
                <button id="avatar-btn" class="flex items-center gap-3 p-1.5 rounded-full hover:bg-slate-800/80 border border-slate-700/50 hover:border-rose-500/50 transition-all duration-300 focus:outline-none">
                  <img src="${currentUser.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + currentUser.username}" alt="${currentUser.username}" class="w-9 h-9 rounded-full object-cover border-2 border-rose-500/80 shadow-md">
                  <span class="hidden sm:block text-sm font-semibold text-slate-200 max-w-[120px] truncate">${currentUser.username}</span>
                  <i class="fa-solid fa-chevron-down text-slate-400 text-xs mr-2 transition-transform duration-300" id="avatar-arrow"></i>
                </button>

                <!-- Dropdown Menu -->
                <div id="avatar-dropdown" class="hidden absolute right-0 mt-3 w-56 bg-slate-900/95 backdrop-blur-xl border border-slate-800 rounded-2xl shadow-2xl shadow-black/80 py-2 z-50 animate-in fade-in slide-in-from-top-2 duration-200">
                  <div class="px-4 py-3 border-b border-slate-800">
                    <p class="text-xs text-slate-400 uppercase tracking-wider font-semibold">Tài khoản</p>
                    <p class="text-sm font-bold text-white truncate mt-0.5">${currentUser.username}</p>
                    <p class="text-xs text-slate-400 truncate mt-0.5">${currentUser.email}</p>
                  </div>
                  
                  <div class="py-1">
                    <a href="profile.html" class="flex items-center gap-3 px-4 py-2.5 text-sm text-slate-200 hover:bg-slate-800/80 hover:text-rose-400 transition-colors">
                      <i class="fa-regular fa-user w-4 text-center"></i>
                      <span>Trang thông tin cá nhân</span>
                    </a>
                    <a href="admin.html" class="flex items-center gap-3 px-4 py-2.5 text-sm text-slate-200 hover:bg-slate-800/80 hover:text-rose-400 transition-colors">
                      <i class="fa-solid fa-sliders w-4 text-center text-rose-500"></i>
                      <span>Trang Quản Trị (Admin)</span>
                    </a>
                  </div>

                  <div class="border-t border-slate-800 pt-1">
                    <button id="logout-btn" class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-rose-400 hover:bg-rose-500/10 transition-colors text-left">
                      <i class="fa-solid fa-right-from-bracket w-4 text-center"></i>
                      <span>Đăng xuất</span>
                    </button>
                  </div>
                </div>
              </div>
            ` : `
              <!-- Not Logged In State: Login & Register Buttons -->
              <div class="flex items-center gap-2">
                <a href="login.html" class="px-4 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800 rounded-xl transition-all">
                  Đăng nhập
                </a>
                <a href="register.html" class="px-4 py-2 text-sm font-semibold text-white bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-500 hover:to-pink-500 rounded-xl shadow-lg shadow-rose-600/30 hover:shadow-rose-600/50 hover:scale-105 transition-all duration-300">
                  Đăng ký
                </a>
              </div>
            `}
          </div>

        </div>
      </div>
    </nav>
  `;

  // Attach Event Listeners
  attachHeaderEvents();
}

function attachHeaderEvents() {
  // Genre Select Event
  const genreSelect = document.getElementById('header-genre-select');
  if (genreSelect) {
    genreSelect.addEventListener('change', (e) => {
      const genre = e.target.value;
      if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/' || window.location.pathname === '') {
        const searchInput = document.getElementById('header-search-input');
        const searchVal = searchInput ? searchInput.value.trim() : '';
        const url = new URL(window.location.href);
        url.searchParams.set('genre', genre);
        if (searchVal) url.searchParams.set('search', searchVal);
        else url.searchParams.delete('search');
        window.location.href = url.toString();
      } else {
        window.location.href = `index.html?genre=${genre}`;
      }
    });
  }

  // Search Form Event
  const searchForm = document.getElementById('header-search-form');
  if (searchForm) {
    searchForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const input = document.getElementById('header-search-input');
      const keyword = input.value.trim();
      const genreSelect = document.getElementById('header-genre-select');
      const genre = genreSelect ? genreSelect.value : 'all';

      window.location.href = `index.html?search=${encodeURIComponent(keyword)}&genre=${genre}`;
    });
  }

  // Clear Search
  const clearBtn = document.getElementById('header-clear-search');
  if (clearBtn) {
    clearBtn.addEventListener('click', () => {
      window.location.href = 'index.html';
    });
  }

  // Avatar Dropdown Toggle
  const avatarBtn = document.getElementById('avatar-btn');
  const dropdown = document.getElementById('avatar-dropdown');
  const avatarArrow = document.getElementById('avatar-arrow');

  if (avatarBtn && dropdown) {
    avatarBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      dropdown.classList.toggle('hidden');
      if (avatarArrow) avatarArrow.classList.toggle('rotate-180');
    });

    document.addEventListener('click', (e) => {
      if (!dropdown.contains(e.target) && !avatarBtn.contains(e.target)) {
        dropdown.classList.add('hidden');
        if (avatarArrow) avatarArrow.classList.remove('rotate-180');
      }
    });
  }

  // Logout Action
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      authService.logout();
    });
  }
}

function escapeHtml(str) {
  return str.replace(/[&<>"']/g, (m) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }[m]));
}
