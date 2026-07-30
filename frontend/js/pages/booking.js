import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { movieService } from '../services/movieService.js';
import { bookingService } from '../services/bookingService.js';
import { authService } from '../services/authService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  const urlParams = new URLSearchParams(window.location.search);
  const movieId = urlParams.get('id') || 'm1';

  const movie = await movieService.getMovieDetail(movieId);
  const showtimes = await bookingService.getShowtimes(movieId);

  let selectedShowtime = showtimes[0] || null;
  let selectedSeats = [];

  const movieTitleEl = document.getElementById('booking-movie-title');
  const movieMetaEl = document.getElementById('booking-movie-meta');
  const showtimesContainer = document.getElementById('showtimes-container');
  const seatGrid = document.getElementById('seat-grid');
  const selectedSeatsList = document.getElementById('selected-seats-list');
  const totalPriceEl = document.getElementById('total-price');
  const confirmBtn = document.getElementById('confirm-booking-btn');

  if (movieTitleEl && movie) movieTitleEl.textContent = movie.title;
  if (movieMetaEl && movie) movieMetaEl.textContent = `${movie.duration} • ${movie.country} • ${movie.genreName || ''}`;

  // Render Showtimes
  renderShowtimes();
  // Render Seat Layout
  renderSeatLayout();

  function renderShowtimes() {
    if (!showtimesContainer) return;
    showtimesContainer.innerHTML = showtimes.map((st, i) => `
      <button 
        type="button" 
        data-id="${st.id}"
        class="showtime-btn px-4 py-3 rounded-2xl border text-center transition-all ${st.id === selectedShowtime.id ? 'bg-rose-600 border-rose-500 text-white font-bold shadow-lg shadow-rose-600/30' : 'bg-slate-900 border-slate-800 text-slate-300 hover:border-slate-600'}"
      >
        <div class="text-sm font-bold">${st.time}</div>
        <div class="text-[11px] opacity-80 mt-0.5">${st.room}</div>
        <div class="text-[11px] text-amber-400 mt-1 font-semibold">${st.price.toLocaleString('vi-VN')} đ</div>
      </button>
    `).join('');

    document.querySelectorAll('.showtime-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-id');
        selectedShowtime = showtimes.find(s => s.id === id);
        selectedSeats = [];
        renderShowtimes();
        renderSeatLayout();
        updateSummary();
      });
    });
  }

  function renderSeatLayout() {
    if (!seatGrid) return;
    const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
    const cols = 8;
    const occupiedSeats = ['A3', 'B4', 'C5', 'D2', 'E6'];

    let html = '';

    rows.forEach(row => {
      html += `<div class="flex items-center justify-center gap-2">`;
      html += `<span class="w-6 text-center text-xs font-bold text-slate-500">${row}</span>`;
      
      for (let col = 1; col <= cols; col++) {
        const seatId = `${row}${col}`;
        const isOccupied = occupiedSeats.includes(seatId);
        const isSelected = selectedSeats.includes(seatId);

        let seatClass = 'available';
        if (isOccupied) seatClass = 'occupied';
        else if (isSelected) seatClass = 'selected';

        html += `
          <button 
            type="button"
            data-seat="${seatId}"
            class="seat ${seatClass}"
            ${isOccupied ? 'disabled' : ''}
          >
            ${col}
          </button>
        `;
      }
      html += `<span class="w-6 text-center text-xs font-bold text-slate-500">${row}</span>`;
      html += `</div>`;
    });

    seatGrid.innerHTML = html;

    // Attach Seat Click Listeners
    document.querySelectorAll('.seat.available, .seat.selected').forEach(seatBtn => {
      seatBtn.addEventListener('click', () => {
        const seatId = seatBtn.getAttribute('data-seat');
        if (selectedSeats.includes(seatId)) {
          selectedSeats = selectedSeats.filter(s => s !== seatId);
        } else {
          if (selectedSeats.length >= 8) {
            alert('Bạn chỉ có thể chọn tối đa 8 ghế cho 1 lần đặt vé');
            return;
          }
          selectedSeats.push(seatId);
        }
        renderSeatLayout();
        updateSummary();
      });
    });
  }

  function updateSummary() {
    if (!selectedSeatsList || !totalPriceEl || !confirmBtn) return;

    if (selectedSeats.length === 0) {
      selectedSeatsList.textContent = 'Chưa chọn ghế nào';
      totalPriceEl.textContent = '0 đ';
      confirmBtn.disabled = true;
      confirmBtn.classList.add('opacity-50', 'cursor-not-allowed');
    } else {
      selectedSeatsList.textContent = selectedSeats.join(', ');
      const total = selectedSeats.length * (selectedShowtime ? selectedShowtime.price : 90000);
      totalPriceEl.textContent = `${total.toLocaleString('vi-VN')} đ`;
      confirmBtn.disabled = false;
      confirmBtn.classList.remove('opacity-50', 'cursor-not-allowed');
    }
  }

  // Handle Booking Confirmation
  if (confirmBtn) {
    confirmBtn.addEventListener('click', async () => {
      if (!authService.isLoggedIn()) {
        alert('Vui lòng Đăng nhập tài khoản để hoàn tất thanh toán đặt vé');
        window.location.href = `login.html?redirect=booking.html?id=${movieId}`;
        return;
      }

      if (selectedSeats.length === 0) return;

      const user = authService.getCurrentUser();
      const bookingData = {
        movieId: movie.id,
        movieTitle: movie.title,
        moviePoster: movie.poster,
        showtime: selectedShowtime.time,
        room: selectedShowtime.room,
        seats: selectedSeats,
        totalPrice: selectedSeats.length * selectedShowtime.price,
        userEmail: user.email,
        username: user.username
      };

      try {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin mr-2"></i> Đang xử lý...`;

        const res = await bookingService.createBooking(bookingData);

        if (res.success) {
          showBookingSuccessModal(res.booking);
        }
      } catch (err) {
        alert('Lỗi đặt vé: ' + err.message);
      } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = `<span>Xác Nhận Thanh Toán</span> <i class="fa-solid fa-check-circle"></i>`;
      }
    });
  }

  function showBookingSuccessModal(booking) {
    const modal = document.getElementById('booking-modal');
    const modalContent = document.getElementById('modal-content');

    if (!modal || !modalContent) return;

    modalContent.innerHTML = `
      <div class="text-center space-y-4">
        <div class="w-16 h-16 bg-emerald-500/20 text-emerald-400 rounded-full flex items-center justify-center mx-auto border border-emerald-500/40 text-3xl">
          <i class="fa-solid fa-circle-check"></i>
        </div>

        <h3 class="text-2xl font-black text-white">Đặt Vé Thành Công!</h3>
        <p class="text-xs text-slate-400">Mã vé của bạn: <span class="text-rose-400 font-mono font-bold">${booking.id}</span></p>

        <div class="p-4 bg-slate-950/80 rounded-2xl border border-slate-800 text-left text-xs space-y-2">
          <p><span class="text-slate-400">Phim:</span> <strong class="text-white">${booking.movieTitle}</strong></p>
          <p><span class="text-slate-400">Suất chiếu:</span> <strong class="text-white">${booking.showtime} (${booking.room})</strong></p>
          <p><span class="text-slate-400">Ghế đã chọn:</span> <strong class="text-rose-400 font-bold">${booking.seats.join(', ')}</strong></p>
          <p><span class="text-slate-400">Tổng tiền:</span> <strong class="text-amber-400 font-bold text-sm">${booking.totalPrice.toLocaleString('vi-VN')} đ</strong></p>
        </div>

        <div class="pt-4 flex items-center justify-center gap-3">
          <a href="profile.html" class="px-5 py-2.5 bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs rounded-xl transition-all">
            Xem vé đã đặt
          </a>
          <a href="index.html" class="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs rounded-xl transition-all">
            Về trang chủ
          </a>
        </div>
      </div>
    `;

    modal.classList.remove('hidden');
  }
});
