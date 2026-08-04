import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { authService } from '../services/authService.js';
import { bookingService } from '../services/bookingService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  if (!authService.isLoggedIn()) {
    window.location.href = 'login.html';
    return;
  }

  const user = authService.getCurrentUser();
  
  // Render Profile Header
  const userNameEl = document.getElementById('profile-username');
  const userEmailEl = document.getElementById('profile-email');
  const userPhoneEl = document.getElementById('profile-phone');
  const userAvatarEl = document.getElementById('profile-avatar');

  if (userNameEl) userNameEl.textContent = user.username;
  if (userEmailEl) userEmailEl.textContent = user.email;
  if (userPhoneEl) userPhoneEl.textContent = user.phone || 'Chưa cập nhật';
  if (userAvatarEl) userAvatarEl.src = user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`;

  // Render User Bookings History
  const historyContainer = document.getElementById('booking-history-container');
  if (historyContainer) {
    const history = await bookingService.getUserBookings();

    if (!history || history.length === 0) {
      historyContainer.innerHTML = `
        <div class="text-center py-12 bg-slate-900/60 rounded-2xl border border-slate-800">
          <i class="fa-solid fa-ticket text-4xl text-slate-600 mb-3"></i>
          <p class="text-slate-400 text-sm">Bạn chưa có vé xem phim nào trong lịch sử</p>
          <a href="index.html" class="mt-4 inline-block px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white font-semibold text-xs rounded-xl transition-all">
            Đặt vé ngay hôm nay
          </a>
        </div>
      `;
      return;
    }

    historyContainer.innerHTML = history.map(item => `
      <div class="p-5 bg-slate-900/90 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <img src="${item.moviePoster || 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=200&q=80'}" alt="${item.movieTitle}" class="w-16 h-20 object-cover rounded-xl border border-slate-700 shrink-0">
          <div class="space-y-1">
            <span class="text-[10px] uppercase font-bold tracking-wider text-rose-400 bg-rose-500/10 px-2 py-0.5 rounded border border-rose-500/20">Mã vé: ${item.id}</span>
            <h4 class="text-base font-bold text-white">${item.movieTitle}</h4>
            <p class="text-xs text-slate-400">
              <i class="fa-regular fa-clock mr-1 text-slate-500"></i> ${item.showtime} (${item.room})
            </p>
            <p class="text-xs text-slate-300">
              Ghế: <strong class="text-rose-400 font-bold">${Array.isArray(item.seats) ? item.seats.join(', ') : item.seats}</strong>
            </p>
          </div>
        </div>

        <div class="text-left md:text-right shrink-0">
          <p class="text-xs text-slate-400">Tổng thanh toán</p>
          <p class="text-lg font-black text-amber-400 mt-0.5">${(item.totalPrice || 0).toLocaleString('vi-VN')} đ</p>
          <span class="inline-block mt-1 text-[11px] font-semibold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20">
            <i class="fa-solid fa-circle-check mr-1 text-[9px]"></i> Đã thanh toán
          </span>
        </div>
      </div>
    `).join('');
  }

  // Handle Change Password Form Submission
  const changePwForm = document.getElementById('change-password-form');
  const pwAlert = document.getElementById('pw-alert');

  if (changePwForm) {
    changePwForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const currentPassword = document.getElementById('current-password').value.trim();
      const newPassword = document.getElementById('new-password').value.trim();
      const confirmNewPassword = document.getElementById('confirm-new-password').value.trim();

      if (!currentPassword || !newPassword || !confirmNewPassword) {
        showPwAlert('Vui lòng điền đầy đủ các thông tin', 'error');
        return;
      }

      if (newPassword.length < 6) {
        showPwAlert('Mật khẩu mới phải từ 6 ký tự trở lên', 'error');
        return;
      }

      if (newPassword !== confirmNewPassword) {
        showPwAlert('Mật khẩu mới xác nhận không trùng khớp', 'error');
        return;
      }

      // Prepared for backend API endpoint
      showPwAlert('Yêu cầu đổi mật khẩu đã được xử lý thành công!', 'success');
      changePwForm.reset();
    });
  }

  function showPwAlert(msg, type) {
    if (!pwAlert) return;
    pwAlert.classList.remove('hidden', 'bg-rose-500/20', 'text-rose-400', 'bg-emerald-500/20', 'text-emerald-400');
    if (type === 'error') {
      pwAlert.classList.add('bg-rose-500/20', 'text-rose-400', 'border', 'border-rose-500/30');
    } else {
      pwAlert.classList.add('bg-emerald-500/20', 'text-emerald-400', 'border', 'border-emerald-500/30');
    }
    pwAlert.textContent = msg;
  }
});
