export function renderFooter() {
  const footerContainer = document.getElementById('main-footer');
  if (!footerContainer) return;

  footerContainer.innerHTML = `
    <footer class="bg-slate-950 border-t border-slate-800/80 text-slate-400 py-12">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          
          <!-- Col 1: Brand Info -->
          <div class="space-y-4">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-xl bg-gradient-to-tr from-amber-500 via-rose-500 to-purple-600 p-0.5 shadow-lg shadow-rose-500/20">
                <div class="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
                  <i class="fa-solid fa-clapperboard text-rose-500 text-base"></i>
                </div>
              </div>
              <span class="text-xl font-extrabold tracking-wider bg-gradient-to-r from-white via-slate-100 to-rose-400 bg-clip-text text-transparent">
                CINE<span class="text-rose-500">PRIME</span>
              </span>
            </div>
            <p class="text-sm text-slate-400 leading-relaxed">
              Hệ thống đặt vé xem phim trực tuyến hàng đầu Việt Nam. Trải nghiệm điện ảnh đỉnh cao với chất lượng âm thanh hình ảnh tuyệt vời nhất.
            </p>
            <div class="flex items-center gap-3 text-slate-400 pt-2">
              <a href="#" class="w-9 h-9 rounded-lg bg-slate-900 flex items-center justify-center hover:text-rose-400 hover:bg-slate-800 transition-colors">
                <i class="fa-brands fa-facebook-f"></i>
              </a>
              <a href="#" class="w-9 h-9 rounded-lg bg-slate-900 flex items-center justify-center hover:text-rose-400 hover:bg-slate-800 transition-colors">
                <i class="fa-brands fa-youtube"></i>
              </a>
              <a href="#" class="w-9 h-9 rounded-lg bg-slate-900 flex items-center justify-center hover:text-rose-400 hover:bg-slate-800 transition-colors">
                <i class="fa-brands fa-tiktok"></i>
              </a>
            </div>
          </div>

          <!-- Col 2: Quick Links -->
          <div>
            <h4 class="text-white font-semibold text-sm uppercase tracking-wider mb-4 border-l-2 border-rose-500 pl-3">Quy định & Điểu khoản</h4>
            <ul class="space-y-2.5 text-sm">
              <li><a href="#" class="hover:text-rose-400 transition-colors">Điều khoản chung</a></li>
              <li><a href="#" class="hover:text-rose-400 transition-colors">Điều khoản giao dịch</a></li>
              <li><a href="#" class="hover:text-rose-400 transition-colors">Chính sách thanh toán</a></li>
              <li><a href="#" class="hover:text-rose-400 transition-colors">Chính sách bảo mật</a></li>
            </ul>
          </div>

          <!-- Col 3: Cinema Systems -->
          <div>
            <h4 class="text-white font-semibold text-sm uppercase tracking-wider mb-4 border-l-2 border-rose-500 pl-3">Hệ thống Rạp</h4>
            <ul class="space-y-2.5 text-sm">
              <li><a href="#" class="hover:text-rose-400 transition-colors">CinePrime Vincom Landmark 81</a></li>
              <li><a href="#" class="hover:text-rose-400 transition-colors">CinePrime Paragon Quận 7</a></li>
              <li><a href="#" class="hover:text-rose-400 transition-colors">CinePrime Bà Triệu Hà Nội</a></li>
              <li><a href="#" class="hover:text-rose-400 transition-colors">CinePrime Ninh Kiều Cần Thơ</a></li>
            </ul>
          </div>

          <!-- Col 4: Contact & Apps -->
          <div>
            <h4 class="text-white font-semibold text-sm uppercase tracking-wider mb-4 border-l-2 border-rose-500 pl-3">Chăm sóc khách hàng</h4>
            <div class="space-y-3 text-sm">
              <p class="flex items-center gap-3">
                <i class="fa-solid fa-phone text-rose-500"></i>
                <span>Hotline: 1900 1234 (8:00 - 22:00)</span>
              </p>
              <p class="flex items-center gap-3">
                <i class="fa-solid fa-envelope text-rose-500"></i>
                <span>Email: hotro@cineprime.vn</span>
              </p>
              <div class="pt-2">
                <span class="inline-block bg-emerald-500/10 text-emerald-400 text-xs font-semibold px-3 py-1 rounded-full border border-emerald-500/20">
                  <i class="fa-solid fa-shield-halved mr-1"></i> Đã chứng nhận BCT
                </span>
              </div>
            </div>
          </div>

        </div>

        <div class="border-t border-slate-900 pt-6 text-center text-xs text-slate-400">
          <p>© 2026 CinePrime. Tất cả quyền được bảo lưu. Thiết kế và phát triển cho Đồ án Web Đặt vé Xem phim.</p>
        </div>
      </div>
    </footer>
  `;
}
