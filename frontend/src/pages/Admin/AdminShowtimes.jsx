import React, { useState, useEffect } from 'react';
import showtimeService from '../../service/showtimeService';
import movieService from '../../service/movieService';
import cinemaService from '../../service/cinemaService';
import roomService from '../../service/roomService';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export default function AdminShowtimes() {
  const { toast } = useToast();
  const [page, setPage] = useState(1);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(false);

  // Lists for Form Selection
  const [nowShowingMovies, setNowShowingMovies] = useState([]);
  const [cinemas, setCinemas] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loadingRooms, setLoadingRooms] = useState(false);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    movieId: '',
    cinemaId: '',
    roomId: '',
    price: 90000,
    startTime: '',
  });

  const fetchShowtimes = async () => {
    setLoading(true);
    try {
      const res = await showtimeService.filter(page, 10);
      if (res && Array.isArray(res.content)) {
        setData({
          content: res.content,
          totalPages: res.totalPages || 1,
          totalElements: res.totalElements != null ? res.totalElements : res.content.length,
        });
      } else if (Array.isArray(res)) {
        setData({ content: res, totalPages: 1, totalElements: res.length });
      } else {
        setData({ content: [], totalPages: 0, totalElements: 0 });
      }
    } catch (err) {
      console.error(err);
      setData({ content: [], totalPages: 0, totalElements: 0 });
    } finally {
      setLoading(false);
    }
  };

  // Lấy danh sách Phim đang chiếu & Danh sách Rạp khi mở Modal hoặc load trang
  useEffect(() => {
    fetchShowtimes();
  }, [page]);

  const handleOpenAddModal = async () => {
    setFormData({
      movieId: '',
      cinemaId: '',
      roomId: '',
      price: 90000,
      startTime: '',
    });
    setRooms([]);
    setIsModalOpen(true);

    try {
      // 1. Lấy danh sách Phim Đang Chiếu (NOW_SHOWING)
      const moviesRes = await movieService.getNowShowing(1, 100);
      setNowShowingMovies(Array.isArray(moviesRes) ? moviesRes : []);

      // 2. Lấy danh sách tất cả Cụm Rạp
      const cinemasRes = await cinemaService.getAll();
      setCinemas(Array.isArray(cinemasRes) ? cinemasRes : []);
    } catch (err) {
      console.error('Lỗi khi tải danh sách Phim/Rạp:', err);
    }
  };

  // Tự động tải danh sách Phòng khi Rạp được chọn
  const handleCinemaChange = async (e) => {
    const cinemaId = e.target.value;
    setFormData((prev) => ({ ...prev, cinemaId: cinemaId ? parseInt(cinemaId, 10) : '', roomId: '' }));
    setRooms([]);

    if (!cinemaId) return;

    setLoadingRooms(true);
    try {
      const roomList = await roomService.getByCinemaId(cinemaId);
      setRooms(Array.isArray(roomList) ? roomList : []);
    } catch (err) {
      console.error(`Lỗi khi lấy phòng chiếu cho Rạp #${cinemaId}:`, err);
      setRooms([]);
    } finally {
      setLoadingRooms(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.movieId) {
      toast.warning('Vui lòng chọn Phim đang chiếu!');
      return;
    }
    if (!formData.cinemaId) {
      toast.warning('Vui lòng chọn Cụm Rạp!');
      return;
    }
    if (!formData.roomId) {
      toast.warning('Vui lòng chọn Phòng chiếu!');
      return;
    }
    if (!formData.startTime) {
      toast.warning('Vui lòng chọn Thời gian bắt đầu chiếu!');
      return;
    }

    try {
      await showtimeService.add({
        movieId: parseInt(formData.movieId, 10),
        cinemaId: parseInt(formData.cinemaId, 10),
        roomId: parseInt(formData.roomId, 10),
        price: parseFloat(formData.price),
        startTime: formData.startTime,
      });
      toast.success('Thêm suất chiếu mới thành công!');
      setIsModalOpen(false);
      fetchShowtimes();
    } catch (err) {
      toast.error(err.message || 'Thêm suất chiếu thất bại');
    }
  };

  const showtimeList = Array.isArray(data?.content) ? data.content : [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">Quản Lý Suất Chiếu</h1>
          <p className="text-xs text-slate-400">Lịch chiếu các phim đang chiếu tại các rạp & phòng chiếu</p>
        </div>
        <button
          onClick={handleOpenAddModal}
          className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl text-sm transition-all shadow-lg shadow-red-900/30 flex items-center gap-2"
        >
          <span>+ Thêm Suất Chiếu Mới</span>
        </button>
      </div>

      {loading ? (
        <p className="text-slate-400 text-sm">Đang tải lịch suất chiếu...</p>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-800 text-slate-200 text-xs uppercase font-bold">
              <tr>
                <th className="p-4">ID</th>
                <th className="p-4">Tên Phim</th>
                <th className="p-4">Cụm Rạp Chiếu</th>
                <th className="p-4">Phòng Chiếu</th>
                <th className="p-4">Giá Vé</th>
                <th className="p-4">Thời Gian Bắt Đầu</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {showtimeList.length === 0 ? (
                <tr>
                  <td colSpan="6" className="p-6 text-center text-slate-500">
                    Chưa có suất chiếu nào trong hệ thống
                  </td>
                </tr>
              ) : (
                showtimeList.map((st) => (
                  <tr key={st.id} className="hover:bg-slate-800/50 transition-colors">
                    <td className="p-4 font-mono font-bold text-red-400">#{st.id}</td>
                    <td className="p-4 font-bold text-white text-base">{st.movieName || `Phim #${st.movieId}`}</td>
                    <td className="p-4 text-slate-300">📍 {st.cinemaName || `Rạp #${st.cinemaId || 'Chưa rõ'}`}</td>
                    <td className="p-4 font-semibold text-slate-300">🎬 {st.roomName || `Phòng #${st.roomId}`}</td>
                    <td className="p-4 font-bold text-amber-400">{st.price?.toLocaleString()} VNĐ</td>
                    <td className="p-4 font-medium text-slate-200">
                      {st.startTime ? new Date(st.startTime).toLocaleString('vi-VN') : ''}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {data.totalPages > 0 && (
            <div className="flex flex-col sm:flex-row justify-between items-center p-4 border-t border-slate-800 gap-3 text-xs text-slate-400">
              <div>
                Hiển thị <span className="font-bold text-white">{showtimeList.length}</span> / <span className="font-bold text-white">{data.totalElements || showtimeList.length}</span> suất chiếu
              </div>
              <div className="flex items-center gap-2">
                <button
                  disabled={page <= 1 || loading}
                  onClick={() => setPage((p) => Math.max(p - 1, 1))}
                  className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg disabled:opacity-40 transition-colors font-semibold border border-slate-700"
                >
                  ◀ Trang trước
                </button>
                <span className="px-3 font-semibold text-slate-200">
                  Trang <span className="text-red-400 font-bold">{page}</span> / {data.totalPages || 1}
                </span>
                <button
                  disabled={page >= data.totalPages || loading}
                  onClick={() => setPage((p) => p + 1)}
                  className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg disabled:opacity-40 transition-colors font-semibold border border-slate-700"
                >
                  Trang sau ▶
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* MODAL THÊM SUẤT CHIẾU VỚI QUY TRÌNH CHỌN TỪNG BƯỚC */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Thêm Suất Chiếu Mới">
        <form onSubmit={handleSubmit} className="space-y-4 text-sm text-slate-200">
          
          {/* BƯỚC 1: CHỌN PHIM ĐANG CHIẾU */}
          <div>
            <label className="block text-xs font-bold mb-1 text-slate-300">
              🎬 1. Chọn Phim (Chỉ Phim Đang Chiếu) <span className="text-red-500">*</span>
            </label>
            <select
              required
              value={formData.movieId}
              onChange={(e) => setFormData({ ...formData, movieId: e.target.value })}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white focus:outline-none focus:border-red-500 font-medium"
            >
              <option value="">-- Chọn phim đang chiếu --</option>
              {nowShowingMovies.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.name} ({m.duration ? `${m.duration} phút` : 'Chưa rõ thời lượng'})
                </option>
              ))}
            </select>
            {nowShowingMovies.length === 0 && (
              <p className="text-xs text-amber-400 mt-1">⚠️ Không tìm thấy phim nào ở trạng thái Đang Chiếu.</p>
            )}
          </div>

          {/* BƯỚC 2: CHỌN RẠP CHIẾU */}
          <div>
            <label className="block text-xs font-bold mb-1 text-slate-300">
              📍 2. Chọn Cụm Rạp Chiếu <span className="text-red-500">*</span>
            </label>
            <select
              required
              value={formData.cinemaId}
              onChange={handleCinemaChange}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white focus:outline-none focus:border-red-500 font-medium"
            >
              <option value="">-- Chọn cụm rạp --</option>
              {cinemas.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} {c.address ? `- ${c.address}` : ''}
                </option>
              ))}
            </select>
          </div>

          {/* BƯỚC 3: CHỌN PHÒNG CHIẾU THUỘC RẠP ĐÓ */}
          <div>
            <label className="block text-xs font-bold mb-1 text-slate-300">
              🚪 3. Chọn Phòng Chiếu <span className="text-red-500">*</span>
            </label>
            <select
              required
              disabled={!formData.cinemaId || loadingRooms}
              value={formData.roomId}
              onChange={(e) => setFormData({ ...formData, roomId: e.target.value })}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white focus:outline-none focus:border-red-500 disabled:opacity-50 disabled:cursor-not-allowed font-medium"
            >
              <option value="">
                {!formData.cinemaId
                  ? '-- Vui lòng chọn Cụm Rạp trước --'
                  : loadingRooms
                  ? 'Đang tải danh sách phòng...'
                  : '-- Chọn phòng chiếu --'}
              </option>
              {rooms.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.name || `Phòng số ${r.id}`}
                </option>
              ))}
            </select>
          </div>

          {/* BƯỚC 4: GIÁ VÉ & THỜI GIAN BẮT ĐẦU */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2 border-t border-slate-800/80">
            <div>
              <label className="block text-xs font-bold mb-1 text-slate-300">
                💵 4. Giá Vé (VNĐ) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                required
                min="0"
                step="5000"
                value={formData.price}
                onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white focus:outline-none focus:border-red-500 font-medium"
                placeholder="VD: 90000"
              />
            </div>
            <div>
              <label className="block text-xs font-bold mb-1 text-slate-300">
                ⏰ 5. Thời Gian Bắt Đầu <span className="text-red-500">*</span>
              </label>
              <input
                type="datetime-local"
                required
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white focus:outline-none focus:border-red-500 font-medium text-slate-200"
              />
            </div>
          </div>

          {/* MODAL FOOTER */}
          <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold"
            >
              Hủy Bỏ
            </button>
            <button
              type="submit"
              className="px-5 py-2 bg-red-600 hover:bg-red-700 text-white rounded-xl text-xs font-bold shadow-lg shadow-red-900/30 transition-all"
            >
              Lưu Suất Chiếu
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
