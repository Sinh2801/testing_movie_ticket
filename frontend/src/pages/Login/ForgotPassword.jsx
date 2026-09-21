import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../../service/authService';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!email) {
      setErrorMessage('Vui lòng nhập địa chỉ Email!');
      return;
    }

    try {
      setLoading(true);
      const response = await authService.forgotPassword({ email });
      setSuccessMessage(response.message || 'Mã OTP 6 chữ số đã được gửi tới email của bạn!');
      
      // Tự động chuyển hướng sang trang nhập OTP sau 2.5 giây
      setTimeout(() => {
        navigate('/reset-password', { state: { email } });
      }, 2500);
    } catch (err) {
      console.error('Lỗi yêu cầu OTP:', err);
      setErrorMessage(err.message || err.data || 'Không thể gửi mã OTP. Vui lòng thử lại!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[75vh] flex items-center justify-center py-12 px-4">
      <div className="w-full max-w-md bg-slate-800/90 border border-slate-700/80 rounded-2xl shadow-2xl p-8 backdrop-blur">
        
        {/* HEADER */}
        <div className="text-center space-y-2 mb-6">
          <div className="inline-flex w-12 h-12 rounded-xl bg-amber-500/20 border border-amber-500/30 items-center justify-center shadow-lg shadow-amber-900/20 mb-2">
            <svg className="w-6 h-6 text-amber-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-white">Quên Mật Khẩu?</h2>
          <p className="text-slate-400 text-sm">Nhập email tài khoản của bạn để nhận mã OTP 6 chữ số xác thực</p>
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
            <span>{successMessage} Đang chuyển hướng...</span>
          </div>
        )}

        {/* FORM */}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Địa chỉ Email tài khoản <span className="text-amber-400">*</span>
            </label>
            <input
              type="email"
              required
              placeholder="vd: user@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-3 text-slate-100 text-sm focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500 placeholder-slate-500"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700 text-white font-bold py-3 rounded-xl transition-all shadow-lg shadow-amber-900/30 text-sm flex items-center justify-center gap-2"
          >
            {loading ? (
              <span>Đang gửi OTP...</span>
            ) : (
              <span>Gửi Mã OTP 6 Chữ Số</span>
            )}
          </button>
        </form>

        {/* FOOTER LINK */}
        <div className="mt-6 text-center text-xs text-slate-400 flex justify-between items-center px-1">
          <Link to="/login" className="text-slate-400 hover:text-slate-200 hover:underline">
            ← Quay lại Đăng nhập
          </Link>
          <Link to="/reset-password" className="text-amber-400 font-medium hover:underline">
            Đã có mã OTP?
          </Link>
        </div>

      </div>
    </div>
  );
}
