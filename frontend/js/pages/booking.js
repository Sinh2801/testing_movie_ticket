import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { movieService } from '../services/movieService.js';
import { ShowtimeService } from '../services/showtimeService.js';
import { BookingService } from '../services/bookingService.js';
import { authService } from '../services/authService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  const urlParams = new URLSearchParams(window.location.search);
  const movieId = parseInt(urlParams.get('id')) || 1;

  const movie = await movieService.getMovieDetail(movieId);
  let showtimes = [];
  try {
    const res = await ShowtimeService.getShowtimesByMovieId(movieId);
    showtimes = res.data || [];
  } catch (err) {
    console.warn('Could not load showtimes from API, using fallback:', err);
  }

  // Fallback mock showtimes if database has none yet
  if (!showtimes || showtimes.length === 0) {
    showtimes = [
      { id: 1, startTime: '2026-08-05T19:00:00', roomName: 'Phòng 01 (IMAX 3D)', cinemaName: 'CinePrime Landmark 81', price: 90000 },
      { id: 2, startTime: '2026-08-05T21:30:00', roomName: 'Phòng 02 (2D Atmos)', cinemaName: 'CinePrime Landmark 81', price: 90000 }
    ];
  }

  let selectedShowtime = showtimes[0];
  let selectedSeats = []; // Stores objects { id, seatName, price }

  const movieTitleEl = document.getElementById('booking-movie-title');
  const movieMetaEl = document.getElementById('booking-movie-meta');
  const showtimesContainer = document.getElementById('showtimes-container');
  const seatGrid = document.getElementById('seat-grid');
  const selectedSeatsList = document.getElementById('selected-seats-list');
  const seatCountEl = document.getElementById('seat-count');
  const totalPriceEl = document.getElementById('total-price');
  const confirmBtn = document.getElementById('confirm-booking-btn');

  if (movieTitleEl && movie) movieTitleEl.textContent = movie.title;
  if (movieMetaEl && movie) movieMetaEl.textContent = `${movie.duration} • ${movie.country} • ${movie.genreName || ''}`;

  renderShowtimes();
  await loadAndRenderSeats();

  function renderShowtimes() {
    if (!showtimesContainer) return;
    showtimesContainer.innerHTML = showtimes.map((st) => {
      const isSelected = selectedShowtime && selectedShowtime.id === st.id;
      const startTimeStr = st.startTime ? new Date(st.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' - ' + new Date(st.startTime).toLocaleDateString('vi-VN') : '19:00';

      return `
        <button 
          type="button" 
          data-id="${st.id}"
          class="showtime-btn px-4 py-3 rounded-2xl border text-center transition-all ${isSelected ? 'bg-rose-600 border-rose-500 text-white font-bold shadow-lg shadow-rose-600/30' : 'bg-slate-900 border-slate-800 text-slate-300 hover:border-slate-600'}"
        >
          <div class="text-xs font-bold text-rose-300">${st.cinemaName || 'CinePrime'}</div>
          <div class="text-sm font-black mt-1">${startTimeStr}</div>
          <div class="text-[11px] opacity-80 mt-0.5">${st.roomName || 'Phòng chiếu'}</div>
          <div class="text-[11px] text-amber-400 mt-1 font-bold">${(st.price || 90000).toLocaleString('vi-VN')} VNĐ</div>
        </button>
      `;
    }).join('');

    document.querySelectorAll('.showtime-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = parseInt(btn.getAttribute('data-id'));
        selectedShowtime = showtimes.find(s => s.id === id);
        selectedSeats = [];
        renderShowtimes();
        await loadAndRenderSeats();
        updateSummary();
      });
    });
  }

  async function loadAndRenderSeats() {
    if (!seatGrid || !selectedShowtime) return;
    seatGrid.innerHTML = `<div class="text-xs text-slate-400 py-6"><i class="fa-solid fa-spinner fa-spin mr-2"></i> Đang tải sơ đồ ghế từ máy chủ...</div>`;

    let apiSeats = [];
    try {
      const res = await ShowtimeService.getShowtimeSeats(selectedShowtime.id);
      apiSeats = res.data || [];
    } catch (err) {
      console.warn('Could not load seats from API, generating fallback map:', err);
    }

    // Fallback seat generation if API returned empty
    if (!apiSeats || apiSeats.length === 0) {
      const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
      let fakeId = 1;
      rows.forEach(r => {
        for (let c = 1; c <= 8; c++) {
          apiSeats.push({
            id: fakeId++,
            seatName: `${r}${c}`,
            status: (r === 'B' && c === 4) || (r === 'C' && c === 5) ? 'BOOKED' : 'AVAILABLE'
          });
        }
      });
    }

    // Group seats by row character (A, B, C...)
    const rowMap = {};
    apiSeats.forEach(s => {
      const rowName = s.seatName.charAt(0);
      if (!rowMap[rowName]) rowMap[rowName] = [];
      rowMap[rowName].push(s);
    });

    let html = '';
    Object.keys(rowMap).sort().forEach(row => {
      html += `<div class="flex items-center justify-center gap-2">`;
      html += `<span class="w-6 text-center text-xs font-bold text-slate-500">${row}</span>`;

      rowMap[row].forEach(seat => {
        const isOccupied = seat.status === 'BOOKED' || seat.status === 'SOLD';
        const isSelected = selectedSeats.some(s => s.id === seat.id);

        let seatClass = 'available';
        if (isOccupied) seatClass = 'occupied';
        else if (isSelected) seatClass = 'selected';

        html += `
          <button 
            type="button"
            data-id="${seat.id}"
            data-name="${seat.seatName}"
            class="seat ${seatClass}"
            ${isOccupied ? 'disabled' : ''}
          >
            ${seat.seatName.substring(1)}
          </button>
        `;
      });

      html += `<span class="w-6 text-center text-xs font-bold text-slate-500">${row}</span>`;
      html += `</div>`;
    });

    seatGrid.innerHTML = html;

    // Attach Seat Click Event Listeners
    document.querySelectorAll('.seat.available, .seat.selected').forEach(seatBtn => {
      seatBtn.addEventListener('click', () => {
        const id = parseInt(seatBtn.getAttribute('data-id'));
        const seatName = seatBtn.getAttribute('data-name');

        if (selectedSeats.some(s => s.id === id)) {
          selectedSeats = selectedSeats.filter(s => s.id !== id);
        } else {
          if (selectedSeats.length >= 8) {
            alert('Bạn chỉ có thể chọn tối đa 8 ghế cho mỗi lần đặt vé!');
            return;
          }
          selectedSeats.push({ id, seatName });
        }

        loadAndRenderSeats();
        updateSummary();
      });
    });
  }

  function updateSummary() {
    if (!selectedSeatsList || !totalPriceEl || !confirmBtn || !seatCountEl) return;

    if (selectedSeats.length === 0) {
      selectedSeatsList.textContent = 'Chưa chọn ghế';
      seatCountEl.textContent = '0 vé';
      totalPriceEl.textContent = '0 đ';
      confirmBtn.disabled = true;
      confirmBtn.classList.add('opacity-50', 'cursor-not-allowed');
    } else {
      selectedSeatsList.textContent = selectedSeats.map(s => s.seatName).join(', ');
      seatCountEl.textContent = `${selectedSeats.length} vé`;
      const total = selectedSeats.length * (selectedShowtime ? selectedShowtime.price : 90000);
      totalPriceEl.textContent = `${total.toLocaleString('vi-VN')} VNĐ`;
      confirmBtn.disabled = false;
      confirmBtn.classList.remove('opacity-50', 'cursor-not-allowed');
    }
  }

  // Handle Booking Flow confirmation click
  if (confirmBtn) {
    confirmBtn.addEventListener('click', () => {
      if (!authService.isLoggedIn()) {
        alert('Vui lòng Đăng nhập tài khoản để thực hiện đặt vé!');
        window.location.href = `login.html?redirect=booking.html?id=${movieId}`;
        return;
      }

      if (selectedSeats.length === 0) return;

      openCheckoutConfirmModal();
    });
  }

  function openCheckoutConfirmModal() {
    const modal = document.getElementById('booking-modal');
    const modalContent = document.getElementById('modal-content');
    if (!modal || !modalContent) return;

    const user = authService.getCurrentUser();
    const totalPrice = selectedSeats.length * (selectedShowtime.price || 90000);

    modalContent.innerHTML = `
      <div class="space-y-6">
        <div class="flex items-center justify-between border-b border-slate-800 pb-4">
          <h3 class="text-xl font-bold text-white flex items-center gap-2">
            <i class="fa-solid fa-credit-card text-rose-500"></i> Xác Nhận Đơn Hàng & Thanh Toán
          </h3>
          <button type="button" id="close-checkout-modal" class="text-slate-400 hover:text-white text-lg">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <!-- Ticket Overview Card -->
        <div class="bg-slate-950 p-4 rounded-2xl border border-slate-800 space-y-3 text-xs">
          <div class="flex items-center gap-3 border-b border-slate-800 pb-3">
            <img src="${movie.poster || movie.imageUrl}" class="w-12 h-16 object-cover rounded-lg">
            <div>
              <h4 class="font-bold text-white text-sm">${movie.title}</h4>
              <p class="text-slate-400 mt-0.5">${selectedShowtime.cinemaName || 'CinePrime'} - ${selectedShowtime.roomName || 'Phòng chiếu'}</p>
              <p class="text-rose-400 font-mono font-semibold mt-0.5">${new Date(selectedShowtime.startTime).toLocaleString('vi-VN')}</p>
            </div>
          </div>

          <div class="flex justify-between py-1">
            <span class="text-slate-400">Tài khoản đặt vé:</span>
            <span class="font-bold text-white">${user.username || user.email}</span>
          </div>

          <div class="flex justify-between py-1">
            <span class="text-slate-400">Danh sách ghế:</span>
            <span class="font-bold text-rose-400">${selectedSeats.map(s => s.seatName).join(', ')}</span>
          </div>

          <div class="flex justify-between py-1 border-t border-slate-800 pt-2">
            <span class="text-sm font-bold text-slate-300">Tổng thanh toán:</span>
            <span class="text-lg font-black text-amber-400">${totalPrice.toLocaleString('vi-VN')} VNĐ</span>
          </div>
        </div>

        <!-- Payment Method Selection -->
        <div class="space-y-2">
          <label class="text-xs font-bold text-slate-300">Phương Thức Thanh Toán</label>
          <div class="p-3 bg-slate-950 rounded-2xl border-2 border-rose-500 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-xl bg-pink-600/20 flex items-center justify-center text-pink-500 font-black text-sm">
                M
              </div>
              <div>
                <div class="font-bold text-white text-xs">Ví Điên Tử MoMo (Mô Phỏng Sandbox)</div>
                <div class="text-[10px] text-slate-400">Thanh toán tức thì & nhận vé qua Email</div>
              </div>
            </div>
            <i class="fa-solid fa-circle-check text-rose-500 text-base"></i>
          </div>
        </div>

        <!-- Submit Button -->
        <button 
          type="button" 
          id="btn-process-momo"
          class="w-full py-3.5 bg-gradient-to-r from-pink-600 to-rose-600 hover:from-pink-500 hover:to-rose-500 text-white font-extrabold text-xs rounded-xl shadow-lg shadow-pink-600/30 transition-all flex items-center justify-center gap-2"
        >
          <i class="fa-solid fa-lock"></i>
          <span>Thanh Toán Ngay Bằng MoMo</span>
        </button>
      </div>
    `;

    modal.classList.remove('hidden');

    document.getElementById('close-checkout-modal')?.addEventListener('click', () => {
      modal.classList.add('hidden');
    });

    // Handle MoMo Gateway Redirection
    document.getElementById('btn-process-momo')?.addEventListener('click', async () => {
      const btn = document.getElementById('btn-process-momo');
      try {
        btn.disabled = true;
        btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin mr-2"></i> Đang tạo đơn vé & chuyển MoMo...`;

        // 1. Create Ticket in Backend
        const seatIds = selectedSeats.map(s => s.id);
        const ticketRes = await BookingService.createTicket(selectedShowtime.id, seatIds);

        if (!ticketRes.data && !ticketRes.id) {
          throw new Error(ticketRes.message || 'Không thể tạo đơn vé');
        }

        const ticketId = ticketRes.data ? ticketRes.data.id : ticketRes.id;

        // 2. Create MoMo Payment Request
        const payRes = await BookingService.createMoMoPayment(ticketId);

        if (payRes.data && payRes.data.paymentUrl) {
          window.location.href = payRes.data.paymentUrl;
        } else {
          // Direct fallback redirection to payment success page
          window.location.href = `/payment-success.html?ticketId=${ticketId}&transactionId=MOMO-SIM-999`;
        }
      } catch (err) {
        alert('Lỗi đặt vé: ' + err.message);
        btn.disabled = false;
        btn.innerHTML = `<i class="fa-solid fa-lock"></i> <span>Thanh Toán Ngay Bằng MoMo</span>`;
      }
    });
  }
});
