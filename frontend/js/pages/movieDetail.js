import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { movieService } from '../services/movieService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  const urlParams = new URLSearchParams(window.location.search);
  const movieId = urlParams.get('id') || 1;

  const detailContainer = document.getElementById('movie-detail-container');
  if (!detailContainer) return;

  try {
    const movie = await movieService.getMovieDetail(movieId);

    if (!movie) {
      detailContainer.innerHTML = `
        <div class="text-center py-20">
          <h2 class="text-2xl font-bold text-slate-300">Không tìm thấy thông tin phim</h2>
          <a href="index.html" class="mt-4 inline-block px-5 py-2.5 bg-rose-600 text-white rounded-xl">Về trang chủ</a>
        </div>
      `;
      return;
    }

    // Render full movie details
    detailContainer.innerHTML = `
      <div class="relative rounded-3xl overflow-hidden bg-slate-900/90 border border-slate-800 shadow-2xl mb-12">
        
        <!-- Backdrop Header -->
        <div class="relative h-96 w-full overflow-hidden">
          <img src="${movie.banner}" alt="${movie.title}" class="w-full h-full object-cover filter brightness-75 blur-[2px] scale-105">
          <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/80 to-transparent"></div>
          <div class="absolute inset-0 bg-gradient-to-r from-slate-900 via-slate-900/70 to-transparent"></div>
        </div>

        <!-- Main Info Container -->
        <div class="relative -mt-48 px-6 md:px-12 pb-12">
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            
            <!-- Poster Col (4 cols) -->
            <div class="lg:col-span-4 shrink-0">
              <div class="relative aspect-[2/3] w-full rounded-2xl overflow-hidden shadow-2xl shadow-rose-950/40 border-2 border-slate-700/80 group">
                <img src="${movie.poster}" alt="${movie.title}" class="w-full h-full object-cover">
              </div>
            </div>

            <!-- Content Col (8 cols) -->
            <div class="lg:col-span-8 space-y-6 pt-4">
              
              <!-- Badges & Status -->
              <div class="flex items-center gap-3 flex-wrap">
                <span class="bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 text-xs font-bold px-3.5 py-1.5 rounded-full flex items-center gap-1.5">
                  <i class="fa-solid fa-circle text-[8px] animate-pulse"></i> ${movie.status}
                </span>
                <span class="bg-rose-600/20 text-rose-400 border border-rose-500/30 text-xs font-bold px-3.5 py-1.5 rounded-full">
                  ${movie.genreName || 'Điện Ảnh'}
                </span>
              </div>

              <!-- Title -->
              <h1 class="text-3xl sm:text-4xl md:text-5xl font-black text-white leading-tight tracking-wide">
                ${movie.title}
              </h1>

              <!-- Quick Info Grid -->
              <div class="grid grid-cols-2 sm:grid-cols-3 gap-4 p-5 bg-slate-950/70 rounded-2xl border border-slate-800">
                <div>
                  <p class="text-xs text-slate-400 font-medium">Thời lượng</p>
                  <p class="text-sm font-bold text-slate-200 mt-1 flex items-center gap-2">
                    <i class="fa-regular fa-clock text-rose-500"></i> ${movie.duration}
                  </p>
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-medium">Quốc gia</p>
                  <p class="text-sm font-bold text-slate-200 mt-1 flex items-center gap-2">
                    <i class="fa-solid fa-globe text-rose-500"></i> ${movie.country}
                  </p>
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-medium">Trạng thái</p>
                  <p class="text-sm font-bold text-emerald-400 mt-1 flex items-center gap-2">
                    <i class="fa-solid fa-film text-emerald-500"></i> ${movie.status}
                  </p>
                </div>
              </div>

              <!-- Detailed Info Table (Director & Actors) -->
              <div class="space-y-4 pt-2">
                <div class="flex items-start gap-4">
                  <span class="w-28 shrink-0 text-sm font-semibold text-slate-400">Đạo diễn:</span>
                  <span class="text-sm font-bold text-rose-300">${movie.director}</span>
                </div>

                <div class="flex items-start gap-4">
                  <span class="w-28 shrink-0 text-sm font-semibold text-slate-400">Diễn viên:</span>
                  <span class="text-sm text-slate-200 leading-relaxed">${movie.actors}</span>
                </div>

                <div class="flex items-start gap-4 pt-2">
                  <span class="w-28 shrink-0 text-sm font-semibold text-slate-400">Nội dung phim:</span>
                  <p class="text-sm text-slate-300 leading-relaxed">${movie.description}</p>
                </div>
              </div>

              <!-- Action Call to Action Button: Đặt vé -->
              <div class="pt-6 border-t border-slate-800/80 flex items-center gap-4 flex-wrap">
                <a href="booking.html?id=${movie.id}" class="px-8 py-4 bg-gradient-to-r from-rose-600 via-pink-600 to-rose-600 hover:from-rose-500 hover:to-pink-500 text-white font-extrabold text-base rounded-2xl shadow-xl shadow-rose-600/40 hover:shadow-rose-600/60 hover:scale-[1.02] transition-all duration-300 flex items-center gap-3">
                  <i class="fa-solid fa-ticket text-xl"></i>
                  <span>ĐẶT VÉ XEM PHIM NGAY</span>
                </a>
                <a href="index.html" class="px-6 py-4 bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-sm rounded-2xl border border-slate-700 transition-all flex items-center gap-2">
                  <i class="fa-solid fa-arrow-left"></i> Quay lại
                </a>
              </div>

            </div>

          </div>
        </div>

      </div>
    `;
  } catch (err) {
    detailContainer.innerHTML = `<div class="text-rose-400 text-center py-10">Lỗi khi tải chi tiết phim: ${err.message}</div>`;
  }
});
