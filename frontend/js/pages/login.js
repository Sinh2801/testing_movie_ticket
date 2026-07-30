import { renderHeader } from '../components/header.js';
import { renderFooter } from '../components/footer.js';
import { authService } from '../services/authService.js';

document.addEventListener('DOMContentLoaded', async () => {
  await renderHeader();
  renderFooter();

  // If already logged in, redirect home
  if (authService.isLoggedIn()) {
    window.location.href = 'index.html';
    return;
  }

  const loginForm = document.getElementById('login-form');
  const alertBox = document.getElementById('alert-box');

  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value.trim();
      const submitBtn = document.getElementById('submit-btn');

      if (!email || !password) {
        showAlert('Vui lòng điền đầy đủ Email và Mật khẩu', 'error');
        return;
      }

      try {
        setLoading(submitBtn, true);
        hideAlert();

        const response = await authService.login(email, password);

        if (response.success) {
          showAlert('Đăng nhập thành công! Đang chuyển hướng...', 'success');
          setTimeout(() => {
            window.location.href = 'index.html';
          }, 1000);
        } else {
          showAlert(response.message || 'Email hoặc mật khẩu không chính xác', 'error');
        }
      } catch (err) {
        showAlert(err.message || 'Có lỗi xảy ra trong quá trình đăng nhập', 'error');
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
      btn.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin mr-2"></i> Đang xử lý...`;
    } else {
      btn.disabled = false;
      btn.innerHTML = `<span>Đăng Nhập</span> <i class="fa-solid fa-arrow-right"></i>`;
    }
  }
});
