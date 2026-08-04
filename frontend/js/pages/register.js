import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { authService } from '../services/authService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  if (authService.isLoggedIn()) {
    window.location.href = 'index.html';
    return;
  }

  const registerForm = document.getElementById('register-form');
  const alertBox = document.getElementById('alert-box');

  if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const username = document.getElementById('username').value.trim();
      const email = document.getElementById('email').value.trim();
      const phoneNumber = document.getElementById('phone').value.trim();
      const password = document.getElementById('password').value.trim();
      const confirmPassword = document.getElementById('confirm-password')?.value.trim() || password;
      const submitBtn = document.getElementById('submit-btn');

      if (!username || !email || !password || !phoneNumber || !confirmPassword) {
        showAlert('Vui lòng điền đầy đủ tất cả thông tin đăng ký', 'error');
        return;
      }

      if (password.length < 6) {
        showAlert('Mật khẩu phải chứa ít nhất 6 ký tự', 'error');
        return;
      }

      if (password !== confirmPassword) {
        showAlert('Mật khẩu xác nhận không khớp', 'error');
        return;
      }

      try {
        setLoading(submitBtn, true);
        hideAlert();

        const response = await authService.register(username, email, phoneNumber, password, confirmPassword);

        if (response.success) {
          showAlert('Tạo tài khoản thành công! Đang chuyển hướng...', 'success');
          setTimeout(() => {
            window.location.href = 'index.html';
          }, 1000);
        } else {
          showAlert(response.message || 'Đăng ký không thành công', 'error');
        }
      } catch (err) {
        showAlert(err.message || 'Có lỗi xảy ra trong quá trình đăng ký', 'error');
      } finally {
        setLoading(submitBtn, false);
      }
    });
  }

  function showAlert(message, type = 'error') {
    if (!alertBox) return;
    alertBox.classList.remove('hidden', 'bg-rose-500/10', 'border-rose-500/30', 'text-rose-400', 'bg-emerald-500/10', 'border-emerald-500/30', 'text-emerald-400');
    
    if (type === 'error') {
      alertBox.classList.add('bg-rose-500/10', 'border-rose-500/30', 'text-rose-400');
      alertBox.innerHTML = `<i class="fa-solid fa-circle-exclamation mr-2"></i> ${message}`;
    } else {
      alertBox.classList.add('bg-emerald-500/10', 'border-emerald-500/30', 'text-emerald-400');
      alertBox.innerHTML = `<i class="fa-solid fa-circle-check mr-2"></i> ${message}`;
    }
  }

  function hideAlert() {
    if (alertBox) alertBox.classList.add('hidden');
  }

  function setLoading(btn, isLoading) {
    if (!btn) return;
    if (isLoading) {
      btn.disabled = true;
      btn.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin mr-2"></i> Đang đăng ký...`;
    } else {
      btn.disabled = false;
      btn.innerHTML = `<span>Tạo Tài Khoản</span> <i class="fa-solid fa-user-plus"></i>`;
    }
  }
});
