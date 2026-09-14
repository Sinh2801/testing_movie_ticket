import React from 'react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-slate-950 border-t border-slate-800 text-slate-400 py-12 px-4 lg:px-8 mt-auto">
      <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8">
        
        {/* CỘT 1: GIỚI THIỆU */}
        <div className="space-y-4">
          <Link to="/home" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-red-600 flex items-center justify-center">
              <svg className="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
                <path d="M18 3v2h-2V3H8v2H6V3H4v18h2v-2h2v2h8v-2h2v2h2V3h-2zM8 17H6v-2h2v2zm0-4H6v-2h2v2zm0-4H6V7h2v2zm10 8h-2v-2h2v2zm0-4h-2v-2h2v2zm0-4h-2V7h2v2z"/>
              </svg>
            </div>
            <span className="text-xl font-extrabold text-white">
              My<span className="text-red-600">Cinema</span>
            </span>
          </Link>
          <p className="text-sm leading-relaxed">
            Hệ thống rạp chiếu phim hiện đại hàng đầu Việt Nam. Trải nghiệm điện ảnh chân thực với công nghệ âm thanh Dolby Atmos và màn hình IMAX đỉnh cao.
          </p>
        </div>

        {/* CỘT 2: QUY ĐỊNH & ĐIỀU KHOẢN */}
        <div>
          <h3 className="text-white font-semibold text-base mb-4">Quy Định & Điều Khoản</h3>
          <ul className="space-y-2.5 text-sm">
            <li><a href="#" className="hover:text-red-500 transition-colors">Điều khoản chung</a></li>
            <li><a href="#" className="hover:text-red-500 transition-colors">Điều khoản giao dịch</a></li>
            <li><a href="#" className="hover:text-red-500 transition-colors">Chính sách thanh toán</a></li>
            <li><a href="#" className="hover:text-red-500 transition-colors">Chính sách bảo mật</a></li>
          </ul>
        </div>

        {/* CỘT 3: HỆ THỐNG RẠP & TRỢ GIÚP */}
        <div>
          <h3 className="text-white font-semibold text-base mb-4">Trợ Giúp & Hỗ Trợ</h3>
          <ul className="space-y-2.5 text-sm">
            <li><a href="#" className="hover:text-red-500 transition-colors">Hệ thống rạp chiếu</a></li>
            <li><a href="#" className="hover:text-red-500 transition-colors">Hỏi đáp (FAQ)</a></li>
            <li><a href="#" className="hover:text-red-500 transition-colors">Liên hệ quảng cáo</a></li>
            <li><a href="#" className="hover:text-red-500 transition-colors">Tuyển dụng</a></li>
          </ul>
        </div>

        {/* CỘT 4: CHĂM SÓC KHÁCH HÀNG */}
        <div>
          <h3 className="text-white font-semibold text-base mb-4">Chăm Sóc Khách Hàng</h3>
          <div className="space-y-3 text-sm">
            <p className="flex items-center gap-2">
              <svg className="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 5a2 2 0 012-2h32a2 2 0 012 2v14a2 2 0 01-2 2H5a2 2 0 01-2-2V5z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 5l9 6 9-6" />
              </svg>
              <span>Hotline: 1900 6060 (8:00 - 22:00)</span>
            </p>
            <p className="flex items-center gap-2">
              <svg className="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
              </svg>
              <span>Email: cskh@mycinema.vn</span>
            </p>
          </div>
        </div>

      </div>

      <div className="max-w-7xl mx-auto border-t border-slate-800/80 mt-10 pt-6 text-center text-xs text-slate-500">
        <p>© 2026 MyCinema Inc. Tất cả quyền được bảo lưu. Thiết kế giao diện bởi Senior React Developer.</p>
      </div>
    </footer>
  );
}
