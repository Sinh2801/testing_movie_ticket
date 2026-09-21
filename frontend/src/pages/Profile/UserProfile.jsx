import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import ticketService from '../../service/ticketService';
import { useToast } from '../../context/ToastContext';

export default function UserProfile() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState('profile'); // 'profile' | 'tickets'

  // Form Đổi mật khẩu
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  // Danh sách lịch sử vé
  const [tickets, setTickets] = useState([]);
  const [loadingTickets, setLoadingTickets] = useState(false);

  useEffect(() => {
    if (activeTab === 'tickets') {
      fetchTickets();
    }
  }, [activeTab]);

  const fetchTickets = async () => {
    setLoadingTickets(true);
    try {
      const data = await ticketService.getHistory();
      setTickets(data);
    } catch (err) {
      console.error('Lỗi lấy lịch sử vé:', err);
    } finally {
      setLoadingTickets(false);
    }
  };

  const handleCancelTicket = async (ticketId) => {
    if (!window.confirm('Bạn có chắc chắn muốn hủy vé này không?')) return;
    try {
      await ticketService.cancelTicket(ticketId);
      toast.success('Hủy vé thành công!');
      fetchTickets();
    } catch (err) {
      toast.error(err.message || 'Không thể hủy vé. Vui lòng kiểm tra lại thời gian (chỉ cho phép hủy trước 2 tiếng)!');
    }
  };

  const handleChangePassword = (e) => {
    e.preventDefault();
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      toast.warning('Mật khẩu mới không khớp!');
      return;
    }
    toast.success('Đổi mật khẩu thành công!');
    setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
  };

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-12">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl md:text-3xl font-extrabold text-white">Trang Cá Nhân & Lịch Sử Đặt Vé</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <div className="bg-slate-800 border border-slate-700 rounded-2xl p-4 space-y-2 shadow-xl h-fit">
          <button
            onClick={() => setActiveTab('profile')}
            className={`w-full text-left px-4 py-3 rounded-xl font-semibold text-sm transition-all flex items-start gap-3 ${
              activeTab === 'profile'
                ? 'bg-red-600 text-white shadow-md shadow-red-900/30'
                : 'text-slate-300 hover:bg-slate-700/60'
            }`}
          >
            <svg className="w-5 h-5 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            <div className="flex flex-col leading-snug">
              <span>Thông tin cá nhân</span>
              <span className="text-xs opacity-90">& Đổi mật khẩu</span>
            </div>
          </button>

          <button
            onClick={() => setActiveTab('tickets')}
            className={`w-full text-left px-4 py-3 rounded-xl font-semibold text-sm transition-all flex items-center gap-3 whitespace-nowrap ${
              activeTab === 'tickets'
                ? 'bg-red-600 text-white shadow-md shadow-red-900/30'
                : 'text-slate-300 hover:bg-slate-700/60'
            }`}
          >
            <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 002 2h14a2 2 0 002-2V7a2 2 0 00-2-2H5z" />
            </svg>
            <span>Lịch Sử Đặt Vé</span>
          </button>
        </div>

        <div className="lg:col-span-3">
          {activeTab === 'profile' && (
            <div className="bg-slate-800 border border-slate-700 rounded-2xl p-6 md:p-8 space-y-8 shadow-xl">
              <div className="space-y-4">
                <h2 className="text-xl font-bold text-white border-b border-slate-700 pb-3">Thông Tin Cá Nhân</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-slate-300">
                  <div className="bg-slate-900 border border-slate-700 rounded-xl p-4">
                    <span className="text-xs text-slate-400 block mb-1">Họ và tên</span>
                    <strong className="text-white text-base">{user?.name || 'Chưa cập nhật'}</strong>
                  </div>
                  <div className="bg-slate-900 border border-slate-700 rounded-xl p-4">
                    <span className="text-xs text-slate-400 block mb-1">Email đăng ký</span>
                    <strong className="text-white text-base">{user?.email || 'Chưa cập nhật'}</strong>
                  </div>
                </div>
              </div>

              <form onSubmit={handleChangePassword} className="space-y-5 pt-6 border-t border-slate-700">
                <h2 className="text-xl font-bold text-white">Đổi Mật Khẩu</h2>
                
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">Mật khẩu hiện tại</label>
                    <input
                      type="password"
                      required
                      placeholder="••••••••"
                      value={passwordForm.oldPassword}
                      onChange={(e) => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })}
                      className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-red-500 transition-colors"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">Mật khẩu mới</label>
                    <input
                      type="password"
                      required
                      placeholder="••••••••"
                      value={passwordForm.newPassword}
                      onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                      className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-red-500 transition-colors"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">Xác nhận mật khẩu mới</label>
                    <input
                      type="password"
                      required
                      placeholder="••••••••"
                      value={passwordForm.confirmPassword}
                      onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                      className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-red-500 transition-colors"
                    />
                  </div>
                </div>

                <div className="pt-2">
                  <button
                    type="submit"
                    className="px-6 py-2.5 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl text-sm transition-all shadow-md shadow-red-900/30"
                  >
                    Lưu Đổi Mật Khẩu
                  </button>
                </div>
              </form>
            </div>
          )}

          {activeTab === 'tickets' && (
            <div className="bg-slate-800 border border-slate-700 rounded-2xl p-6 md:p-8 space-y-6 shadow-xl">
              <h2 className="text-xl font-bold text-white border-b border-slate-700 pb-3">Lịch Sử Vé Đã Đặt</h2>

              {loadingTickets ? (
                <p className="text-center py-10 text-slate-400">Đang tải lịch sử vé...</p>
              ) : tickets.length === 0 ? (
                <p className="text-center py-10 text-slate-400">Bạn chưa có lịch sử đặt vé nào.</p>
              ) : (
                <div className="space-y-4">
                  {tickets.map((t) => {
                    const isPending = t.status === 'PENDING';
                    const isDisableCancel = t.status === 'CHECKIN' || t.status === 'CANCELLED';

                    return (
                      <div
                        key={t.id}
                        className="bg-slate-900 border border-slate-700 rounded-2xl p-5 flex flex-col md:flex-row justify-between items-start md:items-center gap-5 shadow-lg"
                      >
                        <div className="flex gap-4 items-start">
                          {t.movieImageUrl ? (
                            <img
                              src={t.movieImageUrl}
                              alt={t.movieName}
                              className="w-16 h-24 object-cover rounded-xl border border-slate-700 shadow shrink-0"
                            />
                          ) : (
                            <div className="w-16 h-24 bg-slate-800 rounded-xl border border-slate-700 flex items-center justify-center text-xs text-slate-500 font-bold shrink-0">
                              NO IMG
                            </div>
                          )}

                          <div className="space-y-1.5 text-xs text-slate-300">
                            <h3 className="font-extrabold text-white text-base">
                              {t.movieName || 'Phim Phim Chiếu Rạp'}
                            </h3>

                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1 text-slate-400">
                              <p>🎬 <strong>Rạp:</strong> <span className="text-slate-200">{t.cinemaName || 'N/A'}</span></p>
                              <p>🚪 <strong>Phòng:</strong> <span className="text-slate-200">{t.roomName || 'N/A'}</span></p>
                              <p>⏱️ <strong>Suất chiếu:</strong> <span className="text-slate-200">{t.startTime ? new Date(t.startTime).toLocaleString('vi-VN') : 'N/A'}</span></p>
                              <p>💺 <strong>Ghế:</strong> <span className="text-red-400 font-bold">{t.seatNames?.length > 0 ? t.seatNames.join(', ') : 'N/A'}</span></p>
                            </div>

                            <div className="flex items-center gap-3 pt-1">
                              <span className="font-mono text-slate-400">Mã vé: <strong className="text-slate-200">{t.uid || `VE-${t.id}`}</strong></span>
                              <span className="text-slate-600">|</span>
                              <span>Tổng: <strong className="text-amber-400 text-sm">{t.totalPrice?.toLocaleString()} VNĐ</strong></span>
                              <span className="text-slate-600">|</span>
                              <span className={`font-bold px-2 py-0.5 rounded text-[11px] ${
                                t.status === 'CONFIRMED' ? 'bg-emerald-950 text-emerald-400 border border-emerald-800/50' :
                                t.status === 'CANCELLED' ? 'bg-red-950 text-red-400 border border-red-800/50' :
                                'bg-amber-950 text-amber-400 border border-amber-800/50'
                              }`}>
                                {t.status === 'PENDING' ? 'Chờ Thanh Toán' : t.status === 'CONFIRMED' ? 'Đã Thanh Toán' : t.status}
                              </span>
                            </div>
                          </div>
                        </div>

                        <div className="flex md:flex-col gap-2 w-full md:w-auto shrink-0 pt-2 md:pt-0">
                          {isPending && (
                            <button
                              onClick={() => {
                                navigate('/booking/checkout', {
                                  state: {
                                    ticket: t,
                                    showtime: {
                                      id: t.showtimeId,
                                      movieName: t.movieName,
                                      movieImageUrl: t.movieImageUrl,
                                      cinemaName: t.cinemaName,
                                      roomName: t.roomName,
                                      startTime: t.startTime,
                                      price: t.totalPrice,
                                    }
                                  }
                                });
                              }}
                              className="flex-1 md:flex-initial px-4 py-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-extrabold rounded-xl text-xs transition-all shadow-md shadow-blue-900/30 flex items-center justify-center gap-1.5"
                            >
                              💳 Thanh Toán Ngay
                            </button>
                          )}

                          <button
                            disabled={isDisableCancel}
                            onClick={() => handleCancelTicket(t.id)}
                            className="flex-1 md:flex-initial px-4 py-2 bg-red-600/20 hover:bg-red-600 text-red-300 hover:text-white disabled:opacity-40 font-semibold rounded-xl text-xs transition-all border border-red-500/30 disabled:border-slate-800"
                          >
                            {isDisableCancel ? 'Không thể hủy' : 'Hủy Vé'}
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

        </div>

      </div>
    </div>
  );
}
