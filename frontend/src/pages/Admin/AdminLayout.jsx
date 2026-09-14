import React from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function AdminLayout() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { label: 'Dashboard', path: '/admin' },
    { label: 'Quản lý Phim', path: '/admin/movies' },
    { label: 'Quản lý Vé', path: '/admin/tickets' },
    { label: 'Quản lý Thanh toán', path: '/admin/payments' },
    { label: 'Quản lý Suất Chiếu', path: '/admin/showtimes' },
    { label: 'Quản lý Thể Loại', path: '/admin/genres' },
    { label: 'Quản lý Rạp', path: '/admin/cinemas' },
    { label: 'Quản lý Phòng', path: '/admin/rooms' },
    { label: 'Quản lý Tài Khoản', path: '/admin/users' },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col md:flex-row">
      {/* SIDEBAR ADMIN TOP / LEFT */}
      <aside className="w-full md:w-64 bg-slate-900 border-r border-slate-800 p-5 flex flex-col justify-between shrink-0">
        <div className="space-y-6">
          <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
            <div className="w-9 h-9 rounded-xl bg-red-600 flex items-center justify-center font-extrabold text-white text-lg">
              A
            </div>
            <div>
              <h2 className="font-extrabold text-white text-base">MyCinema Admin</h2>
              <p className="text-xs text-slate-400">Trang Quản Trị Hệ Thống</p>
            </div>
          </div>

          <nav className="space-y-1">
            {navItems.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`block px-4 py-2.5 rounded-xl text-sm font-semibold transition-colors ${
                    isActive
                      ? 'bg-red-600 text-white shadow-lg shadow-red-900/30'
                      : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                  }`}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </div>

        {/* NÚT QUAY VỀ TRANG CHỦ & ĐĂNG XUẤT */}
        <div className="pt-6 border-t border-slate-800 space-y-2">
          <button
            onClick={() => navigate('/home')}
            className="w-full py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold rounded-xl text-xs transition-colors border border-slate-700 flex items-center justify-center gap-2"
          >
            <span>🏠 Quay về Trang Chủ</span>
          </button>
          <button
            onClick={handleLogout}
            className="w-full py-2.5 bg-red-950/80 hover:bg-red-900 text-red-300 font-semibold rounded-xl text-xs transition-colors border border-red-800 flex items-center justify-center gap-2"
          >
            <span>🚪 Đăng Xuất</span>
          </button>
        </div>
      </aside>

      {/* MAIN CONTENT AREA */}
      <main className="flex-1 p-6 md:p-10 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
