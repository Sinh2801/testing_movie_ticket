import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import authService from '../../service/authService';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { saveAuthData } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!email || !password) {
      setErrorMessage('Vui lòng điền đầy đủ Email và Mật khẩu!');
      return;
    }

    try {
      setLoading(true);
      // Gửi LoginRequest tới backend Spring Boot
      const response = await authService.login({ email, password });
      
      // response từ Spring Boot: { message: "Login success", data: { token, name, email } }
      if (response && response.data) {
        saveAuthData(response.data); // Lưu token và user vào localStorage
        navigate('/home');
      }
    } catch (err) {
      console.error('Lỗi đăng nhập:', err);
      setErrorMessage(err.data || err.message || 'Đăng nhập thất bại!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[75vh] flex items-center justify-center py-12 px-4">
      <div className="w-full max-w-md bg-slate-800/90 border border-slate-700/80 rounded-2xl shadow-2xl p-8 backdrop-blur">
        
        {/* HEADER */}
        <div className="text-center space-y-2 mb-6">
          <div className="inline-flex w-12 h-12 rounded-xl bg-red-600 items-center justify-center shadow-lg shadow-red-900/30 mb-2">
            <svg className="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M18 3v2h-2V3H8v2H6V3H4v18h2v-2h2v2h8v-2h2v2h2V3h-2zM8 17H6v-2h2v2zm0-4H6v-2h2v2zm0-4H6V7h2v2zm10 8h-2v-2h2v2zm0-4h-2v-2h2v2zm0-4h-2V7h2v2z"/>
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-white">Đăng Nhập MyCinema</h2>
          <p className="text-slate-400 text-sm">Nhập thông tin tài khoản của bạn để tiếp tục</p>
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

        {/* FORM */}
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* EMAIL */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Địa chỉ Email <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <input
                type="email"
                required
                placeholder="vd: user@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-3 text-slate-100 text-sm focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500 placeholder-slate-500"
              />
            </div>
          </div>

          {/* PASSWORD */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-medium text-slate-300">
                Mật khẩu <span className="text-red-500">*</span>
              </label>
              <Link to="/forgot-password" className="text-xs text-red-400 hover:underline">Quên mật khẩu?</Link>
            </div>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                required
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-3 text-slate-100 text-sm focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500 placeholder-slate-500 pr-10"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-3.5 text-slate-400 hover:text-slate-200"
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

          {/* SUBMIT BUTTON */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-red-600 hover:bg-red-700 disabled:bg-red-800 text-white font-bold py-3 rounded-xl transition-all shadow-lg shadow-red-900/30 hover:scale-[1.01] active:scale-[0.99] text-sm mt-2 flex items-center justify-center gap-2"
          >
            {loading ? (
              <span>Đang xử lý...</span>
            ) : (
              <span>Đăng Nhập</span>
            )}
          </button>
        </form>

        {/* FOOTER LINK */}
        <div className="mt-6 text-center text-xs text-slate-400">
          Chưa có tài khoản?{' '}
          <Link to="/register" className="text-red-400 font-semibold hover:underline">
            Đăng ký ngay
          </Link>
        </div>

      </div>
    </div>
  );
}
