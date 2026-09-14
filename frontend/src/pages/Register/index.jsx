import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import authService from '../../service/authService';
import { useToast } from '../../context/ToastContext';

export default function Register() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { saveAuthData } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    const newErrors = {};

    if (!formData.name.trim()) newErrors.name = 'Vui lòng nhập họ và tên';
    if (!formData.email.trim()) newErrors.email = 'Vui lòng nhập email';
    if (!formData.phoneNumber.trim()) newErrors.phoneNumber = 'Vui lòng nhập số điện thoại';
    if (!formData.password) newErrors.password = 'Vui lòng nhập mật khẩu';
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Mật khẩu xác nhận không khớp!';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      setLoading(true);
      // Gửi RegisterRequest tới Backend Spring Boot
      const response = await authService.register({
        name: formData.name,
        email: formData.email,
        phoneNumber: formData.phoneNumber,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
      });

      // response từ Spring Boot: { message: "Register success", data: { token, name, email } }
      if (response && response.data) {
        saveAuthData(response.data); // Lưu token và thông tin user vào localStorage
        toast.success('Đăng ký tài khoản thành công!');
        navigate('/home');
      }
    } catch (err) {
      console.error('Lỗi đăng ký:', err);
      setErrorMessage(err.data || err.message || 'Đăng ký thất bại!');
      toast.error(err.data || err.message || 'Đăng ký thất bại!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center py-12 px-4">
      <div className="w-full max-w-lg bg-slate-800/90 border border-slate-700/80 rounded-2xl shadow-2xl p-8 backdrop-blur">
        
        {/* HEADER */}
        <div className="text-center space-y-2 mb-6">
          <h2 className="text-2xl font-bold text-white">Đăng Ký Tài Khoản Mới</h2>
          <p className="text-slate-400 text-sm">Tạo tài khoản để trải nghiệm dịch vụ đặt vé phim số 1</p>
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
        <form onSubmit={handleSubmit} className="space-y-4">
          
          {/* HỌ TÊN */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Họ và tên <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="name"
              placeholder="Nguyễn Văn A"
              value={formData.name}
              onChange={handleChange}
              className={`w-full bg-slate-900 border ${
                errors.name ? 'border-red-500' : 'border-slate-700'
              } rounded-xl px-4 py-2.5 text-slate-100 text-sm focus:outline-none focus:border-red-500 placeholder-slate-500`}
            />
            {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
          </div>

          {/* EMAIL & SỐ ĐIỆN THOẠI */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Địa chỉ Email <span className="text-red-500">*</span>
              </label>
              <input
                type="email"
                name="email"
                placeholder="user@example.com"
                value={formData.email}
                onChange={handleChange}
                className={`w-full bg-slate-900 border ${
                  errors.email ? 'border-red-500' : 'border-slate-700'
                } rounded-xl px-4 py-2.5 text-slate-100 text-sm focus:outline-none focus:border-red-500 placeholder-slate-500`}
              />
              {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Số điện thoại <span className="text-red-500">*</span>
              </label>
              <input
                type="tel"
                name="phoneNumber"
                placeholder="0912345678"
                value={formData.phoneNumber}
                onChange={handleChange}
                className={`w-full bg-slate-900 border ${
                  errors.phoneNumber ? 'border-red-500' : 'border-slate-700'
                } rounded-xl px-4 py-2.5 text-slate-100 text-sm focus:outline-none focus:border-red-500 placeholder-slate-500`}
              />
              {errors.phoneNumber && <p className="text-red-500 text-xs mt-1">{errors.phoneNumber}</p>}
            </div>
          </div>

          {/* MẬT KHẨU & CONFIRM PASSWORD */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Mật khẩu <span className="text-red-500">*</span>
              </label>
              <input
                type="password"
                name="password"
                placeholder="••••••••"
                value={formData.password}
                onChange={handleChange}
                className={`w-full bg-slate-900 border ${
                  errors.password ? 'border-red-500' : 'border-slate-700'
                } rounded-xl px-4 py-2.5 text-slate-100 text-sm focus:outline-none focus:border-red-500 placeholder-slate-500`}
              />
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Xác nhận mật khẩu <span className="text-red-500">*</span>
              </label>
              <input
                type="password"
                name="confirmPassword"
                placeholder="••••••••"
                value={formData.confirmPassword}
                onChange={handleChange}
                className={`w-full bg-slate-900 border ${
                  errors.confirmPassword ? 'border-red-500' : 'border-slate-700'
                } rounded-xl px-4 py-2.5 text-slate-100 text-sm focus:outline-none focus:border-red-500 placeholder-slate-500`}
              />
              {errors.confirmPassword && (
                <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>
              )}
            </div>
          </div>

          {/* SUBMIT BUTTON */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-red-600 hover:bg-red-700 disabled:bg-red-800 text-white font-bold py-3 rounded-xl transition-all shadow-lg shadow-red-900/30 hover:scale-[1.01] active:scale-[0.99] text-sm mt-3 flex items-center justify-center gap-2"
          >
            {loading ? (
              <span>Đang xử lý đăng ký...</span>
            ) : (
              <span>Đăng Ký Tài Khoản</span>
            )}
          </button>
        </form>

        {/* FOOTER LINK */}
        <div className="mt-6 text-center text-xs text-slate-400">
          Đã có tài khoản?{' '}
          <Link to="/login" className="text-red-400 font-semibold hover:underline">
            Đăng nhập ngay
          </Link>
        </div>

      </div>
    </div>
  );
}
