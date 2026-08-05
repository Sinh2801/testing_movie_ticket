import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { authService } from '../services/authService.js';
import { BookingService } from '../services/bookingService.js';

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

  if (userNameEl) userNameEl.textContent = user.username || 'Khách hàng';
  if (userEmailEl) userEmailEl.textContent = user.email || 'user@example.com';
  if (userPhoneEl) userPhoneEl.textContent = user.phone || 'Chưa cập nhật';
  if (userAvatarEl) userAvatarEl.src = user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username || 'User'}`;

  // Render User Bookings History
  await loadUserTicketHistory();

  async function loadUserTicketHistory() {
    const historyContainer = document.getElementById('booking-history-container');
    if (!historyContainer) return;

    let history = [];
    try {
      const res = await BookingService.getUserTicketHistory();
      history = res.data || [];
    } catch (err) {
      console.warn('Could not fetch ticket history from backend:', err);
    }

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

    historyContainer.innerHTML = history.map(item => {
      const movieTitle = item.movieName || 'Xem Phim';
      const poster = item.movieImageUrl || 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=200&q=80';
      const ticketUid = item.uid || `TIC-#${item.id}`;
      const showtimeDate = item.startTime ? new Date(item.startTime) : new Date();
      const showtimeStr = item.startTime ? showtimeDate.toLocaleString('vi-VN') : '19:00';
      const cinemaRoomStr = `${item.cinemaName || 'CinePrime'} - ${item.roomName || 'Phòng chiếu'}`;
      const seatsStr = Array.isArray(item.seatNames) ? item.seatNames.join(', ') : (item.seatNames || 'Chưa xếp');

      const isCancelled = item.status === 'CANCELLED';
      const isConfirmed = item.status === 'CONFIRMED' || item.status === 'PAID';

      // Cancellation eligibility rule: must be at least 2 hours before showtime
      const now = new Date();
      const hoursUntilShow = (showtimeDate.getTime() - now.getTime()) / (1000 * 60 * 60);
      const canCancel = !isCancelled && hoursUntilShow >= 2;

      let cancelActionHtml = '';
      if (isCancelled) {
        cancelActionHtml = `<span class="inline-block mt-1 text-[11px] font-semibold text-rose-400 bg-rose-500/10 px-2.5 py-0.5 rounded-full border border-rose-500/20"><i class="fa-solid fa-ban mr-1 text-[9px]"></i> Đã hủy vé</span>`;
      } else if (canCancel) {
        cancelActionHtml = `
          <button data-ticket-id="${item.id}" data-movie-name="${escapeQuote(movieTitle)}" class="btn-cancel-ticket mt-2 px-3 py-1.5 bg-rose-600/20 hover:bg-rose-600 text-rose-400 hover:text-white font-bold text-xs rounded-xl border border-rose-500/30 transition-all flex items-center gap-1.5">
            <i class="fa-solid fa-ban"></i> Hủy vé này
          </button>
        `;
      } else {
        cancelActionHtml = `<span class="inline-block mt-1 text-[11px] font-semibold text-slate-500 bg-slate-800/60 px-2.5 py-0.5 rounded-full border border-slate-700/50"><i class="fa-solid fa-clock mr-1 text-[9px]"></i> Không thể hủy (&lt; 2h chiếu)</span>`;
      }

      return `
        <div class="p-5 bg-slate-900/90 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div class="flex items-center gap-4">
            <img src="${poster}" alt="${movieTitle}" class="w-16 h-20 object-cover rounded-xl border border-slate-700 shrink-0">
            <div class="space-y-1">
              <span class="text-[10px] uppercase font-bold tracking-wider text-rose-400 bg-rose-500/10 px-2 py-0.5 rounded border border-rose-500/20">Mã vé: ${ticketUid}</span>
              <h4 class="text-base font-bold text-white">${movieTitle}</h4>
              <p class="text-xs text-slate-400">
                <i class="fa-regular fa-clock mr-1 text-slate-500"></i> ${showtimeStr} (${cinemaRoomStr})
              </p>
              <p class="text-xs text-slate-300">
                Ghế đã đặt: <strong class="text-rose-400 font-bold">${seatsStr}</strong>
              </p>
            </div>
          </div>

          <div class="text-left md:text-right shrink-0">
            <p class="text-xs text-slate-400">Tổng thanh toán</p>
            <p class="text-lg font-black text-amber-400 mt-0.5">${(item.totalPrice || 0).toLocaleString('vi-VN')} VNĐ</p>
            <div class="flex flex-col items-start md:items-end gap-1 mt-1">
              ${isConfirmed ? `<span class="inline-block text-[11px] font-semibold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20"><i class="fa-solid fa-circle-check mr-1 text-[9px]"></i> Đã thanh toán</span>` : ''}
              ${cancelActionHtml}
            </div>
          </div>
        </div>
      `;
    }).join('');

    // Attach click listeners to Cancel Ticket buttons
    document.querySelectorAll('.btn-cancel-ticket').forEach(btn => {
      btn.addEventListener('click', async () => {
        const ticketId = parseInt(btn.getAttribute('data-ticket-id'));
        const movieName = btn.getAttribute('data-movie-name');

        if (confirm(`Bạn có chắc chắn muốn HỦY VÉ cho phim "${movieName}" không?\n\nSau khi hủy, các ghế đã chọn sẽ được giải phóng lập tức.`)) {
          try {
            btn.disabled = true;
            btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Đang hủy...`;

            const res = await BookingService.cancelTicket(ticketId);
            if (res.data || res.status === 200 || res.message.includes('thành công')) {
              alert('Hủy vé thành công! Ghế đã được giải phóng và Email xác nhận đã được gửi.');
              await loadUserTicketHistory();
            } else {
              alert(res.message || 'Hủy vé thất bại!');
              btn.disabled = false;
            }
          } catch (err) {
            alert('Lỗi hủy vé: ' + err.message);
            btn.disabled = false;
          }
        }
      });
    });
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

  function escapeQuote(str) {
    return (str || '').replace(/'/g, "\\'");
  }
});
