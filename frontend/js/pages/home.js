import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { movieService } from '../services/movieService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  // Elements
  const heroContainer = document.getElementById('hero-banner-container');
  const movieListContainer = document.getElementById('movie-list-container');
  const sectionTitle = document.getElementById('section-title');
  const resultInfo = document.getElementById('result-info');

  const filterNameInput = document.getElementById('filter-name');
  const filterGenreSelect = document.getElementById('filter-genre');
  const filterCountrySelect = document.getElementById('filter-country');
  const resetFilterBtn = document.getElementById('reset-filter-btn');

  // 1. Fetch 3 Coming Soon Movies for Hero Banner
  const comingSoonMovies = await movieService.getComingSoonMovies(3);
  renderHeroBanner(comingSoonMovies, heroContainer);

  // 2. Populate Genre Options in Filter Dropdown
  const genres = await movieService.getGenres();
  if (filterGenreSelect && Array.isArray(genres)) {
    genres.forEach(g => {
      const option = document.createElement('option');
      option.value = g.id;
      option.textContent = g.name;
      filterGenreSelect.appendChild(option);
    });
  }

  // Check URL Query params if any
  const urlParams = new URLSearchParams(window.location.search);
  const searchFromUrl = urlParams.get('search') || '';
  const genreFromUrl = urlParams.get('genre') || '0';

  if (searchFromUrl && filterNameInput) filterNameInput.value = searchFromUrl;
  if (genreFromUrl !== '0' && filterGenreSelect) filterGenreSelect.value = genreFromUrl;

  // Function to execute movie filtering
  async function runFilter() {
    const nameVal = filterNameInput ? filterNameInput.value.trim() : '';
    const genreVal = filterGenreSelect ? filterGenreSelect.value : '0';
    const countryVal = filterCountrySelect ? filterCountrySelect.value : '';

    const isFiltered = nameVal || genreVal !== '0' || countryVal;

    const filteredMovies = await movieService.filterMovies({
      name: nameVal,
      genreId: genreVal,
      country: countryVal,
      movieStatus: 'NOW_SHOWING',
      size: isFiltered ? 20 : 10,
      page: 1
    });

    if (isFiltered) {
      if (sectionTitle) sectionTitle.textContent = `Kết Quả Lọc Phim (${filteredMovies.length})`;
      if (resultInfo) resultInfo.textContent = `Hiển thị phim khớp với tiêu chí tìm kiếm của bạn`;
    } else {
      if (sectionTitle) sectionTitle.textContent = `Top 10 Phim Đang Chiếu Rạp`;
      if (resultInfo) resultInfo.textContent = `Danh sách 10 phim mới nhất đang gây bão phòng vé`;
    }

    renderMoviesGrid(filteredMovies, movieListContainer);
  }

  // Event Listeners for Live Filter
  if (filterNameInput) {
    let timeout = null;
    filterNameInput.addEventListener('input', () => {
      clearTimeout(timeout);
      timeout = setTimeout(runFilter, 300);
    });
  }

  if (filterGenreSelect) {
    filterGenreSelect.addEventListener('change', runFilter);
  }

  if (filterCountrySelect) {
    filterCountrySelect.addEventListener('change', runFilter);
  }

  if (resetFilterBtn) {
    resetFilterBtn.addEventListener('click', () => {
      if (filterNameInput) filterNameInput.value = '';
      if (filterGenreSelect) filterGenreSelect.value = '0';
      if (filterCountrySelect) filterCountrySelect.value = '';
      runFilter();
    });
  }

  // Initial load
  await runFilter();
});

/**
 * Render 3 Coming Soon Movies in Hero Section
 */
function renderHeroBanner(movies, container) {
  if (!container) return;

  if (!movies || movies.length === 0) {
    container.innerHTML = `
      <div class="p-6 bg-slate-900 rounded-2xl border border-slate-800 text-center text-slate-400 text-xs">
        Đang cập nhật danh sách phim sắp chiếu...
      </div>
    `;
    return;
  }

  container.innerHTML = `
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      ${movies.slice(0, 3).map((m, index) => `
        <div class="relative group rounded-2xl overflow-hidden border border-slate-800 bg-slate-900 shadow-lg hover:border-rose-500/50 transition-all duration-300">
          <!-- Background Image -->
          <div class="aspect-[16/9] w-full overflow-hidden bg-slate-950">
            <img src="${m.banner || m.poster}" alt="${m.title}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
          </div>

          <!-- Overlay Badge -->
          <div class="absolute top-3 left-3 flex items-center gap-2">
            <span class="bg-rose-600 text-white text-[10px] font-black px-2.5 py-1 rounded-md shadow-md uppercase">
              Sắp chiếu #${index + 1}
            </span>
          </div>

          <!-- Card Content -->
          <div class="p-4 space-y-2">
            <div class="flex items-center justify-between text-[11px] text-slate-400">
              <span class="text-rose-400 font-semibold">${m.genreName || 'Điện Ảnh'}</span>
              <span><i class="fa-regular fa-clock mr-1"></i> ${m.duration}</span>
            </div>

            <h3 class="text-base font-bold text-white group-hover:text-rose-400 transition-colors line-clamp-1">
              <a href="movie-detail.html?id=${m.id}">${m.title}</a>
            </h3>

            <p class="text-slate-400 text-xs line-clamp-2 leading-relaxed">
              ${m.description}
            </p>

            <div class="pt-2 flex items-center gap-2">
              <a href="movie-detail.html?id=${m.id}" class="w-full py-2 bg-slate-800 hover:bg-slate-700 text-white font-semibold text-xs rounded-xl border border-slate-700 text-center transition-all">
                <i class="fa-solid fa-circle-info mr-1"></i> Xem Chi Tiết
              </a>
            </div>
          </div>
        </div>
      `).join('')}
    </div>
  `;
}

/**
 * Render Movies Grid (10 Now Showing or Filter Result)
 */
function renderMoviesGrid(movies, container) {
  if (!container) return;

  if (!movies || movies.length === 0) {
    container.innerHTML = `
      <div class="col-span-full text-center py-12 bg-slate-900/60 rounded-2xl border border-slate-800">
        <i class="fa-solid fa-film text-4xl text-slate-600 mb-3"></i>
        <h3 class="text-lg font-bold text-slate-300 mb-1">Không tìm thấy bộ phim nào</h3>
        <p class="text-slate-500 text-xs">Hãy thử thay đổi tên phim, thể loại hoặc quốc gia cần tìm nhé!</p>
      </div>
    `;
    return;
  }

  container.innerHTML = movies.map((movie, index) => `
    <div class="group relative bg-slate-900/90 rounded-2xl overflow-hidden border border-slate-800 hover:border-rose-500/50 transition-all duration-300 hover:-translate-y-1.5 hover:shadow-xl flex flex-col h-full">
      
      <!-- Top Badge / Rank -->
      <div class="absolute top-2.5 left-2.5 z-10 flex items-center gap-1.5">
        <span class="bg-rose-600 text-white text-[10px] font-black px-2 py-0.5 rounded shadow">
          #${index + 1}
        </span>
        <span class="bg-slate-950/80 backdrop-blur-xs text-amber-400 border border-amber-500/30 text-[10px] font-bold px-2 py-0.5 rounded">
          <i class="fa-solid fa-star text-[9px] mr-0.5"></i>${movie.rating || 8.8}
        </span>
      </div>

      <!-- Poster Image Wrapper -->
      <div class="relative aspect-[2/3] w-full overflow-hidden bg-slate-950">
        <img src="${movie.poster}" alt="${movie.title}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
        
        <!-- Hover Overlay -->
        <div class="absolute inset-0 bg-slate-950/80 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex flex-col items-center justify-center p-3 gap-2 text-center">
          <a href="movie-detail.html?id=${movie.id}" class="w-full py-2 bg-slate-800 hover:bg-slate-700 text-white font-semibold text-xs rounded-xl border border-slate-600 transition-all flex items-center justify-center gap-1.5">
            <i class="fa-solid fa-circle-info"></i> Chi Tiết
          </a>
          <a href="booking.html?id=${movie.id}" class="w-full py-2 bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs rounded-xl shadow-md transition-all flex items-center justify-center gap-1.5">
            <i class="fa-solid fa-ticket"></i> Đặt Vé
          </a>
        </div>
      </div>

      <!-- Movie Details Footer inside Card -->
      <div class="p-3.5 flex flex-col flex-1 justify-between space-y-2">
        <div>
          <span class="text-[11px] font-medium text-rose-400 truncate block mb-0.5">${movie.genreName || 'Điện Ảnh'}</span>
          <h3 class="text-sm font-bold text-white group-hover:text-rose-400 transition-colors line-clamp-1">
            <a href="movie-detail.html?id=${movie.id}">${movie.title}</a>
          </h3>
        </div>

        <div class="flex items-center justify-between text-[11px] text-slate-400 pt-2 border-t border-slate-800">
          <span><i class="fa-regular fa-clock text-slate-500 mr-1"></i>${movie.duration}</span>
          <span><i class="fa-solid fa-globe text-slate-500 mr-1"></i>${movie.country}</span>
        </div>
      </div>

    </div>
  `).join('');
}
