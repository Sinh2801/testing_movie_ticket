import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { movieService } from '../services/movieService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  const urlParams = new URLSearchParams(window.location.search);
  const searchQuery = urlParams.get('search') || '';
  const genreQuery = urlParams.get('genre') || 'all';

  const movieListContainer = document.getElementById('movie-list-container');
  const sectionTitle = document.getElementById('section-title');
  const resultInfo = document.getElementById('result-info');

  let movies = [];

  // Fetch logic based on search or genre
  if (searchQuery) {
    movies = await movieService.searchMovies(searchQuery);
    if (sectionTitle) sectionTitle.textContent = `Kết quả tìm kiếm cho: "${searchQuery}"`;
    if (resultInfo) resultInfo.textContent = `Tìm thấy ${movies.length} bộ phim phù hợp`;
  } else if (genreQuery && genreQuery !== 'all') {
    movies = await movieService.getMoviesByGenre(genreQuery);
    const genres = await movieService.getGenres();
    const genreName = genres.find(g => g.id === genreQuery)?.name || genreQuery;
    if (sectionTitle) sectionTitle.textContent = `Phim Thể Loại: ${genreName}`;
    if (resultInfo) resultInfo.textContent = `Hiển thị ${movies.length} phim thuộc thể loại này`;
  } else {
    // Default: 10 phim mới nhất
    movies = await movieService.getLatestMovies();
    if (sectionTitle) sectionTitle.textContent = `Top 10 Phim Mới Nhất Chiếu Rạp`;
    if (resultInfo) resultInfo.textContent = `Danh sách 10 phim đang gây bão phòng vé tuần này`;
  }

  // Render Hero Banner with first movie
  renderHeroBanner(movies[0]);

  // Render Movies Grid
  renderMoviesGrid(movies, movieListContainer);
});

function renderHeroBanner(featuredMovie) {
  const heroContainer = document.getElementById('hero-banner-container');
  if (!heroContainer || !featuredMovie) return;

  heroContainer.innerHTML = `
    <div class="relative w-full h-[520px] rounded-3xl overflow-hidden shadow-2xl shadow-rose-950/20 border border-slate-800">
      <!-- Backdrop Image -->
      <img src="${featuredMovie.banner}" alt="${featuredMovie.title}" class="w-full h-full object-cover object-center filter brightness-90 scale-105 transform hover:scale-100 transition-transform duration-700">
      
      <!-- Gradient Overlays -->
      <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/60 to-transparent"></div>
      <div class="absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-950/70 to-transparent"></div>

      <!-- Banner Content -->
      <div class="absolute bottom-0 left-0 right-0 p-8 md:p-12 max-w-3xl space-y-4">
        
        <div class="flex items-center gap-3 flex-wrap">
          <span class="bg-rose-600 text-white text-xs font-extrabold uppercase px-3 py-1 rounded-full shadow-lg shadow-rose-600/40">
            <i class="fa-solid fa-fire mr-1"></i> Phim Nổi Bật
          </span>
          <span class="bg-amber-500/20 text-amber-400 border border-amber-500/30 text-xs font-bold px-3 py-1 rounded-full flex items-center gap-1">
            <i class="fa-solid fa-star text-amber-400"></i> ${featuredMovie.rating} / 10
          </span>
          <span class="text-xs text-slate-300 bg-slate-800/80 px-3 py-1 rounded-full border border-slate-700">
            <i class="fa-regular fa-clock mr-1"></i> ${featuredMovie.duration}
          </span>
        </div>

        <h1 class="text-3xl md:text-5xl font-black text-white leading-tight tracking-wide drop-shadow-md">
          ${featuredMovie.title}
        </h1>

        <p class="text-slate-300 text-sm md:text-base line-clamp-2 md:line-clamp-3 leading-relaxed max-w-2xl font-normal">
          ${featuredMovie.description}
        </p>

        <div class="pt-4 flex items-center gap-4 flex-wrap">
          <a href="booking.html?id=${featuredMovie.id}" class="px-6 py-3.5 bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-500 hover:to-pink-500 text-white font-bold rounded-2xl shadow-xl shadow-rose-600/30 hover:shadow-rose-600/50 hover:-translate-y-0.5 transition-all duration-300 flex items-center gap-2">
            <i class="fa-solid fa-ticket text-lg"></i>
            <span>Đặt Vé Ngay</span>
          </a>
          <a href="movie-detail.html?id=${featuredMovie.id}" class="px-6 py-3.5 bg-slate-800/90 hover:bg-slate-700 text-slate-200 font-semibold rounded-2xl border border-slate-700/80 hover:border-slate-500 transition-all duration-300 flex items-center gap-2">
            <i class="fa-solid fa-circle-info"></i>
            <span>Xem Chi Tiết</span>
          </a>
        </div>

      </div>
    </div>
  `;
}

function renderMoviesGrid(movies, container) {
  if (!container) return;

  if (!movies || movies.length === 0) {
    container.innerHTML = `
      <div class="col-span-full text-center py-16 bg-slate-900/50 rounded-3xl border border-slate-800">
        <i class="fa-solid fa-film text-5xl text-slate-600 mb-4"></i>
        <h3 class="text-xl font-bold text-slate-300 mb-2">Không tìm thấy bộ phim nào</h3>
        <p class="text-slate-500 text-sm mb-6">Thử lại với từ khóa khác hoặc chọn thể loại phim khác xem sao nhé!</p>
        <a href="index.html" class="inline-flex items-center gap-2 px-5 py-2.5 bg-rose-600 hover:bg-rose-500 text-white font-medium rounded-xl transition-all">
          <i class="fa-solid fa-arrow-rotate-left"></i> Xem 10 phim mới nhất
        </a>
      </div>
    `;
    return;
  }

  container.innerHTML = movies.map((movie, index) => `
    <div class="group relative bg-slate-900/90 rounded-2xl overflow-hidden border border-slate-800/80 hover:border-rose-500/50 transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl hover:shadow-rose-950/30 flex flex-col h-full">
      
      <!-- Top Badge / Rank -->
      <div class="absolute top-3 left-3 z-10 flex items-center gap-1.5">
        <span class="bg-rose-600/90 backdrop-blur-md text-white text-[11px] font-black px-2.5 py-1 rounded-lg shadow-md">
          #${index + 1}
        </span>
        <span class="bg-slate-950/80 backdrop-blur-md text-emerald-400 border border-emerald-500/30 text-[11px] font-bold px-2 py-1 rounded-lg">
          ${movie.status}
        </span>
      </div>

      <!-- Poster Image Wrapper -->
      <div class="relative aspect-[2/3] w-full overflow-hidden bg-slate-950">
        <img src="${movie.poster}" alt="${movie.title}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
        
        <!-- Hover Overlay -->
        <div class="absolute inset-0 bg-slate-950/80 opacity-0 group-hover:opacity-100 transition-opacity duration-300 backdrop-blur-xs flex flex-col items-center justify-center p-4 gap-3 text-center">
          <a href="movie-detail.html?id=${movie.id}" class="w-full py-2.5 bg-slate-800 hover:bg-slate-700 text-white font-semibold text-xs rounded-xl border border-slate-600 transition-all flex items-center justify-center gap-2">
            <i class="fa-solid fa-circle-info"></i> Xem Chi Tiết
          </a>
          <a href="booking.html?id=${movie.id}" class="w-full py-2.5 bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-500 hover:to-pink-500 text-white font-bold text-xs rounded-xl shadow-lg shadow-rose-600/40 transition-all flex items-center justify-center gap-2">
            <i class="fa-solid fa-ticket"></i> Đặt Vé Ngay
          </a>
        </div>
      </div>

      <!-- Movie Details Footer inside Card -->
      <div class="p-4 flex flex-col flex-1 justify-between space-y-3">
        <div>
          <div class="flex items-center justify-between gap-2 text-xs text-slate-400 mb-1">
            <span class="truncate font-medium text-rose-400">${movie.genreName || 'Phim Chiếu Rạp'}</span>
            <span class="flex items-center gap-1 font-bold text-amber-400 shrink-0">
              <i class="fa-solid fa-star text-[10px]"></i> ${movie.rating}
            </span>
          </div>
          <h3 class="text-base font-bold text-white group-hover:text-rose-400 transition-colors line-clamp-1">
            <a href="movie-detail.html?id=${movie.id}">${movie.title}</a>
          </h3>
        </div>

        <div class="flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-slate-800/80">
          <span class="flex items-center gap-1">
            <i class="fa-regular fa-clock text-slate-400"></i> ${movie.duration}
          </span>
          <span class="flex items-center gap-1">
            <i class="fa-solid fa-globe text-slate-400"></i> ${movie.country}
          </span>
        </div>
      </div>

    </div>
  `).join('');
}
