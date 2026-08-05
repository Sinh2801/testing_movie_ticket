import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { movieService } from '../services/movieService.js';
import { cinemaService } from '../services/cinemaService.js';
import { ShowtimeService } from '../services/showtimeService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  // Tab State Management
  const tabBtns = document.querySelectorAll('.admin-tab-btn');
  const tabContents = document.querySelectorAll('.admin-tab-content');

  tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.getAttribute('data-tab');

      tabBtns.forEach(b => {
        b.classList.remove('bg-rose-600', 'text-white', 'shadow-lg', 'shadow-rose-600/30');
        b.classList.add('text-slate-400', 'hover:text-white', 'hover:bg-slate-900');
      });

      btn.classList.add('bg-rose-600', 'text-white', 'shadow-lg', 'shadow-rose-600/30');
      btn.classList.remove('text-slate-400', 'hover:text-white', 'hover:bg-slate-900');

      tabContents.forEach(content => {
        if (content.id === `tab-${targetTab}`) {
          content.classList.remove('hidden');
        } else {
          content.classList.add('hidden');
        }
      });
    });
  });

  // Modal Close Handlers
  document.querySelectorAll('.close-modal-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('#movie-modal, #genre-modal, #cinema-modal, #room-modal, #showtime-modal').forEach(m => m.classList.add('hidden'));
    });
  });

  // Data Loading Trigger
  await loadAllAdminData();

  // Refresh Button
  const refreshBtn = document.getElementById('refresh-admin-btn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', async () => {
      await loadAllAdminData();
      showAdminAlert('Đã tải lại toàn bộ dữ liệu từ server!', 'success');
    });
  }

  // --- 1. MOVIE CRUD LOGIC ---
  const openAddMovieBtn = document.getElementById('open-add-movie-btn');
  const movieModal = document.getElementById('movie-modal');
  const movieForm = document.getElementById('movie-form');

  if (openAddMovieBtn && movieModal) {
    openAddMovieBtn.addEventListener('click', () => {
      document.getElementById('movie-modal-title').textContent = 'Thêm Phim Mới';
      document.getElementById('movie-form-id').value = '';
      movieForm.reset();
      movieModal.classList.remove('hidden');
    });
  }

  if (movieForm) {
    movieForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('movie-form-id').value;
      const movieData = {
        name: document.getElementById('movie-name').value.trim(),
        directorName: document.getElementById('movie-director').value.trim(),
        actorsName: document.getElementById('movie-actors').value.trim(),
        duration: parseInt(document.getElementById('movie-duration').value) || 120,
        country: document.getElementById('movie-country').value.trim(),
        movieStatus: document.getElementById('movie-status').value || 'NOW_SHOWING',
        imageUrl: document.getElementById('movie-image-url').value.trim(),
        description: document.getElementById('movie-description').value.trim(),
        showAt: new Date().toISOString().slice(0, 19)
      };

      try {
        let res;
        if (id) {
          movieData.id = parseInt(id);
          res = await movieService.updateMovie(movieData);
        } else {
          movieData.genres = [1];
          res = await movieService.addMovie(movieData);
        }

        if (res.success) {
          movieModal.classList.add('hidden');
          showAdminAlert(id ? 'Cập nhật phim thành công!' : 'Thêm phim mới thành công!', 'success');
          await loadMoviesTable();
        } else {
          showAdminAlert(res.message || 'Lỗi xử lý phim', 'error');
        }
      } catch (err) {
        showAdminAlert(err.message || 'Lỗi gọi API Phim', 'error');
      }
    });
  }

  // --- 2. GENRE CRUD LOGIC ---
  const openAddGenreBtn = document.getElementById('open-add-genre-btn');
  const genreModal = document.getElementById('genre-modal');
  const genreForm = document.getElementById('genre-form');

  if (openAddGenreBtn && genreModal) {
    openAddGenreBtn.addEventListener('click', () => {
      document.getElementById('genre-modal-title').textContent = 'Thêm Thể Loại Mới';
      document.getElementById('genre-form-id').value = '';
      genreForm.reset();
      genreModal.classList.remove('hidden');
    });
  }

  if (genreForm) {
    genreForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('genre-form-id').value;
      const name = document.getElementById('genre-name').value.trim();

      try {
        let res;
        if (id) {
          res = await movieService.updateGenre(id, name);
        } else {
          res = await movieService.addGenre(name);
        }

        if (res.success) {
          genreModal.classList.add('hidden');
          showAdminAlert(id ? 'Cập nhật thể loại thành công!' : 'Thêm thể loại thành công!', 'success');
          await loadGenresTable();
        } else {
          showAdminAlert(res.message || 'Lỗi xử lý thể loại', 'error');
        }
      } catch (err) {
        showAdminAlert(err.message || 'Lỗi gọi API Thể loại', 'error');
      }
    });
  }

  // --- 3. CINEMA CRUD LOGIC ---
  const openAddCinemaBtn = document.getElementById('open-add-cinema-btn');
  const cinemaModal = document.getElementById('cinema-modal');
  const cinemaForm = document.getElementById('cinema-form');

  if (openAddCinemaBtn && cinemaModal) {
    openAddCinemaBtn.addEventListener('click', () => {
      document.getElementById('cinema-modal-title').textContent = 'Thêm Rạp Chiếu Mới';
      document.getElementById('cinema-form-id').value = '';
      cinemaForm.reset();
      cinemaModal.classList.remove('hidden');
    });
  }

  if (cinemaForm) {
    cinemaForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('cinema-form-id').value;
      const data = {
        name: document.getElementById('cinema-name').value.trim(),
        address: document.getElementById('cinema-address').value.trim()
      };

      try {
        let res;
        if (id) {
          data.id = parseInt(id);
          res = await cinemaService.updateCinema(data);
        } else {
          res = await cinemaService.createCinema(data);
        }

        if (res.success) {
          cinemaModal.classList.add('hidden');
          showAdminAlert(id ? 'Cập nhật rạp thành công!' : 'Thêm rạp chiếu thành công!', 'success');
          await loadCinemasTable();
        } else {
          showAdminAlert(res.message || 'Lỗi xử lý rạp chiếu', 'error');
        }
      } catch (err) {
        showAdminAlert(err.message || 'Lỗi gọi API Rạp', 'error');
      }
    });
  }

  // --- 4. ROOM CRUD LOGIC ---
  const openAddRoomBtn = document.getElementById('open-add-room-btn');
  const roomModal = document.getElementById('room-modal');
  const roomForm = document.getElementById('room-form');
  const roomCinemaSelect = document.getElementById('admin-room-cinema-select');

  if (openAddRoomBtn && roomModal) {
    openAddRoomBtn.addEventListener('click', () => {
      document.getElementById('room-modal-title').textContent = 'Thêm Phòng Chiếu Mới';
      document.getElementById('room-form-id').value = '';
      roomForm.reset();
      roomModal.classList.remove('hidden');
    });
  }

  if (roomCinemaSelect) {
    roomCinemaSelect.addEventListener('change', async () => {
      await loadRoomsTable(roomCinemaSelect.value);
    });
  }

  if (roomForm) {
    roomForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('room-form-id').value;
      const cinemaId = parseInt(roomCinemaSelect.value) || 1;

      const data = {
        name: document.getElementById('room-name').value.trim(),
        numberOfRows: parseInt(document.getElementById('room-rows').value) || 6,
        numberOfColumns: parseInt(document.getElementById('room-cols').value) || 8,
        description: document.getElementById('room-description').value.trim(),
        cinemaId: cinemaId
      };

      try {
        let res;
        if (id) {
          data.id = parseInt(id);
          res = await cinemaService.updateRoom({ id: parseInt(id), name: data.name, description: data.description });
        } else {
          res = await cinemaService.createRoom(data);
        }

        if (res.success) {
          roomModal.classList.add('hidden');
          showAdminAlert(id ? 'Cập nhật phòng chiếu thành công!' : 'Thêm phòng chiếu thành công!', 'success');
          await loadRoomsTable(cinemaId);
        } else {
          showAdminAlert(res.message || 'Lỗi xử lý phòng chiếu', 'error');
        }
      } catch (err) {
        showAdminAlert(err.message || 'Lỗi gọi API Phòng chiếu', 'error');
      }
    });
  }

  // --- 5. SHOWTIME CRUD & FILTER LOGIC ---
  const openAddShowtimeBtn = document.getElementById('open-add-showtime-btn');
  const showtimeModal = document.getElementById('showtime-modal');
  const showtimeForm = document.getElementById('showtime-form');
  const showtimeCinemaSelect = document.getElementById('showtime-cinema-select');

  if (openAddShowtimeBtn && showtimeModal) {
    openAddShowtimeBtn.addEventListener('click', async () => {
      await populateShowtimeFormDropdowns();
      // Set default start time to tomorrow at 19:00
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(19, 0, 0, 0);
      document.getElementById('showtime-start-time').value = tomorrow.toISOString().slice(0, 16);
      showtimeModal.classList.remove('hidden');
    });
  }

  if (showtimeCinemaSelect) {
    showtimeCinemaSelect.addEventListener('change', async () => {
      await populateRoomsForCinema(showtimeCinemaSelect.value);
    });
  }

  if (showtimeForm) {
    showtimeForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const data = {
        cinemaId: parseInt(document.getElementById('showtime-cinema-select').value),
        roomId: parseInt(document.getElementById('showtime-room-select').value),
        movieId: parseInt(document.getElementById('showtime-movie-select').value),
        price: parseFloat(document.getElementById('showtime-price').value),
        startTime: document.getElementById('showtime-start-time').value
      };

      try {
        const res = await ShowtimeService.createShowtime(data);
        if (res.status === 200 || res.status === 201 || res.data) {
          showtimeModal.classList.add('hidden');
          showAdminAlert('Tạo suất chiếu thành công! Đã tự động khởi tạo sơ đồ ghế.', 'success');
          await loadShowtimesTable();
        } else {
          showAdminAlert(res.message || 'Tạo suất chiếu thất bại', 'error');
        }
      } catch (err) {
        showAdminAlert(err.message || 'Lỗi gọi API Tạo suất chiếu', 'error');
      }
    });
  }

  // Showtime Filters Handlers
  const filterCinema = document.getElementById('filter-showtime-cinema');
  const filterDate = document.getElementById('filter-showtime-date');
  const filterSearch = document.getElementById('filter-showtime-search');
  const btnResetFilter = document.getElementById('btn-reset-showtime-filter');

  if (filterCinema) filterCinema.addEventListener('change', loadShowtimesTable);
  if (filterDate) filterDate.addEventListener('change', loadShowtimesTable);
  if (filterSearch) filterSearch.addEventListener('input', debounce(loadShowtimesTable, 300));
  if (btnResetFilter) {
    btnResetFilter.addEventListener('click', () => {
      if (filterCinema) filterCinema.value = '';
      if (filterDate) filterDate.value = '';
      if (filterSearch) filterSearch.value = '';
      loadShowtimesTable();
    });
  }
});

// Loaders
async function loadAllAdminData() {
  await loadMoviesTable();
  await loadGenresTable();
  await loadCinemasTable();
  await loadShowtimeFilters();
  await loadShowtimesTable();
}

async function loadMoviesTable() {
  const tableBody = document.getElementById('admin-movie-table');
  if (!tableBody) return;

  const movies = await movieService.getLatestMovies();

  if (movies.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="6" class="text-center py-6 text-slate-500">Chưa có dữ liệu phim</td></tr>`;
    return;
  }

  tableBody.innerHTML = movies.map(m => `
    <tr class="hover:bg-slate-800/40 transition-colors">
      <td class="px-6 py-4 font-mono font-bold text-rose-400">#${m.id}</td>
      <td class="px-6 py-4">
        <div class="flex items-center gap-3">
          <img src="${m.poster}" alt="${m.title}" class="w-10 h-14 object-cover rounded-lg border border-slate-700 shrink-0">
          <div>
            <div class="font-bold text-white max-w-xs truncate">${m.title}</div>
            <div class="text-xs text-rose-400 mt-0.5">${m.genreName || 'Điện ảnh'}</div>
          </div>
        </div>
      </td>
      <td class="px-6 py-4 text-xs">
        <div class="font-semibold text-slate-200">Đạo diễn: ${m.director}</div>
        <div class="text-slate-400 truncate max-w-xs mt-0.5">Diễn viên: ${m.actors}</div>
      </td>
      <td class="px-6 py-4 text-xs">
        <div class="font-semibold text-slate-200">${m.duration}</div>
        <div class="text-slate-400">${m.country}</div>
      </td>
      <td class="px-6 py-4 text-xs">
        <span class="inline-block px-2.5 py-1 rounded-full text-[11px] font-bold ${m.status === 'Đang chiếu' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'}">
          ${m.status}
        </span>
      </td>
      <td class="px-6 py-4 text-right">
        <button onclick="editMovie(${m.id}, '${escapeQuote(m.title)}', '${escapeQuote(m.director)}', '${escapeQuote(m.actors)}', '${m.duration.replace(/\D/g,'')}', '${escapeQuote(m.country)}', '${m.poster}')" class="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-rose-400 text-xs font-semibold rounded-lg border border-slate-700 transition-colors">
          <i class="fa-regular fa-pen-to-square mr-1"></i> Sửa
        </button>
      </td>
    </tr>
  `).join('');
}

async function loadGenresTable() {
  const tableBody = document.getElementById('admin-genre-table');
  if (!tableBody) return;

  const genres = await movieService.getGenres();

  tableBody.innerHTML = genres.map(g => `
    <tr class="hover:bg-slate-800/40 transition-colors">
      <td class="px-6 py-4 font-mono font-bold text-rose-400">#${g.id}</td>
      <td class="px-6 py-4 font-bold text-white">${g.name}</td>
      <td class="px-6 py-4 text-right">
        <button onclick="editGenre(${g.id}, '${escapeQuote(g.name)}')" class="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-rose-400 text-xs font-semibold rounded-lg border border-slate-700 transition-colors">
          <i class="fa-regular fa-pen-to-square mr-1"></i> Sửa
        </button>
      </td>
    </tr>
  `).join('');
}

async function loadCinemasTable() {
  const tableBody = document.getElementById('admin-cinema-table');
  const roomCinemaSelect = document.getElementById('admin-room-cinema-select');
  if (!tableBody) return;

  let cinemas = await cinemaService.getAllCinemas();
  if (!cinemas || cinemas.length === 0) {
    cinemas = [
      { id: 1, name: 'CinePrime Vincom Landmark 81', address: '720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP.HCM' },
      { id: 2, name: 'CinePrime Paragon Quận 7', address: '03 Nguyễn Lương Bằng, Tân Phú, Quận 7, TP.HCM' }
    ];
  }

  if (roomCinemaSelect) {
    roomCinemaSelect.innerHTML = cinemas.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    await loadRoomsTable(cinemas[0]?.id || 1);
  }

  tableBody.innerHTML = cinemas.map(c => `
    <tr class="hover:bg-slate-800/40 transition-colors">
      <td class="px-6 py-4 font-mono font-bold text-rose-400">#${c.id}</td>
      <td class="px-6 py-4 font-bold text-white">${c.name}</td>
      <td class="px-6 py-4 text-xs text-slate-300">${c.address}</td>
      <td class="px-6 py-4 text-right">
        <button onclick="editCinema(${c.id}, '${escapeQuote(c.name)}', '${escapeQuote(c.address)}')" class="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-rose-400 text-xs font-semibold rounded-lg border border-slate-700 transition-colors">
          <i class="fa-regular fa-pen-to-square mr-1"></i> Sửa
        </button>
      </td>
    </tr>
  `).join('');
}

async function loadRoomsTable(cinemaId) {
  const tableBody = document.getElementById('admin-room-table');
  if (!tableBody) return;

  let rooms = await cinemaService.getRoomsByCinemaId(cinemaId);
  if (!rooms || rooms.length === 0) {
    rooms = [
      { id: 101, name: 'Phòng 01 (IMAX 3D)', numberOfRows: 6, numberOfColumns: 8, description: 'Phòng chiếu chuẩn IMAX cao cấp' },
      { id: 102, name: 'Phòng 02 (2D Atmos)', numberOfRows: 6, numberOfColumns: 8, description: 'Âm thanh vòm Dolby Atmos' }
    ];
  }

  tableBody.innerHTML = rooms.map(r => `
    <tr class="hover:bg-slate-800/40 transition-colors">
      <td class="px-6 py-4 font-mono font-bold text-rose-400">#${r.id}</td>
      <td class="px-6 py-4 font-bold text-white">${r.name}</td>
      <td class="px-6 py-4 text-xs text-slate-300 font-semibold">${r.numberOfRows || 6} hàng x ${r.numberOfColumns || 8} cột</td>
      <td class="px-6 py-4 text-xs text-slate-400">${r.description || 'Chưa có mô tả'}</td>
      <td class="px-6 py-4 text-right">
        <button onclick="editRoom(${r.id}, '${escapeQuote(r.name)}', '${escapeQuote(r.description || '')}')" class="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-rose-400 text-xs font-semibold rounded-lg border border-slate-700 transition-colors">
          <i class="fa-regular fa-pen-to-square mr-1"></i> Sửa
        </button>
      </td>
    </tr>
  `).join('');
}

// Showtimes table & filters logic
async function loadShowtimeFilters() {
  const filterCinema = document.getElementById('filter-showtime-cinema');
  if (!filterCinema) return;

  let cinemas = await cinemaService.getAllCinemas();
  if (cinemas && cinemas.length > 0) {
    filterCinema.innerHTML = `<option value="">Tất cả cụm rạp</option>` +
      cinemas.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  }
}

async function loadShowtimesTable() {
  const tableBody = document.getElementById('admin-showtime-table');
  if (!tableBody) return;

  const cinemaId = document.getElementById('filter-showtime-cinema')?.value || null;
  const date = document.getElementById('filter-showtime-date')?.value || null;
  const movieName = document.getElementById('filter-showtime-search')?.value.trim() || null;

  try {
    const res = await ShowtimeService.filterShowtimes(cinemaId, date, movieName);
    const showtimes = res.data || [];

    if (showtimes.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="6" class="text-center py-6 text-slate-500">Không tìm thấy suất chiếu phù hợp</td></tr>`;
      return;
    }

    tableBody.innerHTML = showtimes.map(st => `
      <tr class="hover:bg-slate-800/40 transition-colors">
        <td class="px-6 py-4 font-mono font-bold text-rose-400">#${st.id}</td>
        <td class="px-6 py-4 font-bold text-white">
          <div class="flex items-center gap-3">
            <img src="${st.movieImageUrl || 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=300&q=80'}" class="w-8 h-10 object-cover rounded shadow">
            <div>
              <div>${st.movieName}</div>
              <div class="text-xs text-slate-400 font-normal">${st.duration || 120} phút</div>
            </div>
          </div>
        </td>
        <td class="px-6 py-4 text-xs">
          <div class="font-semibold text-slate-200">${st.cinemaName}</div>
          <div class="text-rose-400 font-bold">${st.roomName}</div>
        </td>
        <td class="px-6 py-4 font-bold text-emerald-400">${(st.price || 0).toLocaleString('vi-VN')} VNĐ</td>
        <td class="px-6 py-4 text-xs font-mono text-amber-300 font-bold">
          ${st.startTime ? new Date(st.startTime).toLocaleString('vi-VN') : ''}
        </td>
        <td class="px-6 py-4 text-right">
          <span class="px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            Sẵn sàng
          </span>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tableBody.innerHTML = `<tr><td colspan="6" class="text-center py-6 text-rose-500">Lỗi khi tải suất chiếu: ${err.message}</td></tr>`;
  }
}

async function populateShowtimeFormDropdowns() {
  const movieSelect = document.getElementById('showtime-movie-select');
  const cinemaSelect = document.getElementById('showtime-cinema-select');

  const movies = await movieService.getLatestMovies();
  movieSelect.innerHTML = movies.map(m => `<option value="${m.id}">${m.title}</option>`).join('');

  let cinemas = await cinemaService.getAllCinemas();
  if (!cinemas || cinemas.length === 0) {
    cinemas = [
      { id: 1, name: 'CinePrime Vincom Landmark 81' },
      { id: 2, name: 'CinePrime Paragon Quận 7' }
    ];
  }
  cinemaSelect.innerHTML = cinemas.map(c => `<option value="${c.id}">${c.name}</option>`).join('');

  await populateRoomsForCinema(cinemas[0].id);
}

async function populateRoomsForCinema(cinemaId) {
  const roomSelect = document.getElementById('showtime-room-select');
  let rooms = await cinemaService.getRoomsByCinemaId(cinemaId);
  if (!rooms || rooms.length === 0) {
    rooms = [{ id: 1, name: 'Phòng 01 (IMAX 3D)' }, { id: 2, name: 'Phòng 02 (2D Atmos)' }];
  }
  roomSelect.innerHTML = rooms.map(r => `<option value="${r.id}">${r.name}</option>`).join('');
}

// Helpers
function showAdminAlert(msg, type = 'success') {
  const alertBox = document.getElementById('admin-alert');
  if (!alertBox) return;

  alertBox.classList.remove('hidden', 'bg-rose-500/10', 'border-rose-500/30', 'text-rose-400', 'bg-emerald-500/10', 'border-emerald-500/30', 'text-emerald-400');
  if (type === 'error') {
    alertBox.classList.add('bg-rose-500/10', 'border-rose-500/30', 'text-rose-400');
    alertBox.innerHTML = `<i class="fa-solid fa-circle-exclamation mr-2"></i> ${msg}`;
  } else {
    alertBox.classList.add('bg-emerald-500/10', 'border-emerald-500/30', 'text-emerald-400');
    alertBox.innerHTML = `<i class="fa-solid fa-circle-check mr-2"></i> ${msg}`;
  }

  setTimeout(() => alertBox.classList.add('hidden'), 4000);
}

function escapeQuote(str) {
  return (str || '').replace(/'/g, "\\'");
}

function debounce(func, wait) {
  let timeout;
  return function(...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}
