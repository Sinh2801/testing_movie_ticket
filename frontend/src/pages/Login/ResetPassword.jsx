import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import authService from '../../service/authService';

export default function ResetPassword() {
  const location = useLocation();
  const navigate = useNavigate();

  // Nhận email từ trang ForgotPassword chuyển sang nếu có
  const initialEmail = location.state?.email || '';

  const [email, setEmail] = useState(initialEmail);
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!email || !otp || !newPassword) {
      setErrorMessage('Vui lòng điền đầy đủ Email, Mã OTP và Mật khẩu mới!');
      return;
    }

    if (otp.trim().length !== 6) {
      setErrorMessage('Mã OTP phải có đúng 6 chữ số!');
      return;
    }

    if (newPassword !== confirmPassword) {
      setErrorMessage('Mật khẩu xác nhận không trùng khớp!');
      return;
    }

    try {
      setLoading(true);
      const response = await authService.resetPassword({
        email: email.trim(),
        otp: otp.trim(),
        newPassword: newPassword
      });

      setSuccessMessage(response.message || 'Đặt lại mật khẩu thành công!');
      
      // Tự động chuyển hướng về trang Đăng nhập sau 2.5 giây
      setTimeout(() => {
        navigate('/login');
      }, 2500);

    } catch (err) {
      console.error('Lỗi đặt lại mật khẩu:', err);
      setErrorMessage(err.message || err.data || 'Đặt lại mật khẩu thất bại. Vui lòng kiểm tra mã OTP!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[75vh] flex items-center justify-center py-12 px-4">
      <div className="w-full max-w-md bg-slate-800/90 border border-slate-700/80 rounded-2xl shadow-2xl p-8 backdrop-blur">
        
        {/* HEADER */}
        <div className="text-center space-y-2 mb-6">
          <div className="inline-flex w-12 h-12 rounded-xl bg-emerald-500/20 border border-emerald-500/30 items-center justify-center shadow-lg shadow-emerald-900/20 mb-2">
            <svg className="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-white">Xác Thực & Đổi Mật Khẩu</h2>
          <p className="text-slate-400 text-sm">Nhập mã OTP 6 chữ số đã gửi về email để đặt mật khẩu mới</p>
        </div>

        {/* THÔNG BÁO LỖI NẾU CÓ */}
        {errorMessage && (
          <div className="mb-4 p-3 bg-red-950/80 border border-red-800 text-red-300 text-xs rounded-xl flex items-center gap-2">
            <svg className="w-4 h-4 text-red-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{errorMessage}</span>
          </div>
        )}

        {/* THÔNG BÁO THÀNH CÔNG */}
        {successMessage && (
          <div className="mb-4 p-3.5 bg-emerald-950/90 border border-emerald-700/80 text-emerald-300 text-xs rounded-xl flex items-center gap-2.5">
            <svg className="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{successMessage} Đang chuyển sang trang Đăng nhập...</span>
          </div>
        )}

        {/* FORM */}
        <form onSubmit={handleSubmit} className="space-y-4">
          
          {/* EMAIL */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Địa chỉ Email <span className="text-emerald-400">*</span>
            </label>
            <input
              type="email"
              required
              placeholder="vd: user@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-slate-100 text-sm focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 placeholder-slate-500"
            />
          </div>

          {/* OTP 6 CHỮ SỐ */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1 flex justify-between">
              <span>Mã OTP (6 chữ số) <span className="text-emerald-400">*</span></span>
              <span className="text-slate-400 text-[11px]">Hạn dùng: 5 phút</span>
            </label>
            <input
              type="text"
              maxLength="6"
              required
              placeholder="123456"
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))} // Chỉ cho nhập số
              className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-3 text-center text-xl font-mono tracking-[0.4em] font-bold text-amber-400 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 placeholder-slate-600"
            />
          </div>

          {/* MẬT KHẨU MỚI */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Mật khẩu mới <span className="text-emerald-400">*</span>
            </label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                required
                placeholder="••••••••"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-slate-100 text-sm focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 placeholder-slate-500 pr-10"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-3 text-slate-400 hover:text-slate-200"
              >
                {showPassword ? (
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858-5.908a10.025 10.025 0 014.122-.913c4.478 0 8.268 2.943 9.542 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21M3 3l18 18" />
                  </svg>
                ) : (
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                )}
              </button>
            </div>
          </div>

          {/* XÁC NHẬN MẬT KHẨU MỚI */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Xác nhận mật khẩu mới <span className="text-emerald-400">*</span>
            </label>
            <input
              type={showPassword ? 'text' : 'password'}
              required
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-slate-100 text-sm focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 placeholder-slate-500"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-600 hover:to-teal-700 text-white font-bold py-3 rounded-xl transition-all shadow-lg shadow-emerald-900/30 text-sm flex items-center justify-center gap-2 mt-2"
          >
            {loading ? (
              <span>Đang đổi mật khẩu...</span>
            ) : (
              <span>Xác Nhận & Đổi Mật Khẩu</span>
            )}
          </button>
        </form>

        {/* FOOTER LINK */}
        <div className="mt-6 text-center text-xs text-slate-400">
          Chưa nhận được mã OTP?{' '}
          <Link to="/forgot-password" className="text-emerald-400 font-semibold hover:underline">
            Gửi lại mã OTP
          </Link>
        </div>

      </div>
    </div>
  );
}
