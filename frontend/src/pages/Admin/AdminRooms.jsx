import React, { useState, useEffect } from 'react';
import roomService from '../../service/roomService';
import cinemaService from '../../service/cinemaService';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export default function AdminRooms() {
  const { toast } = useToast();
  const [cinemas, setCinemas] = useState([]);
  const [selectedCinemaId, setSelectedCinemaId] = useState('');
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);

  // Modal State đầy đủ theo CreateRoomRequest DTO của Backend
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    cinemaId: '',
    name: '',
    numberOfRows: 6,
    numberOfColumns: 8,
    description: '',
  });

  // 1. Fetch danh sách Rạp chiếu
  useEffect(() => {
    const fetchCinemas = async () => {
      try {
        const res = await cinemaService.getAll();
        const list = Array.isArray(res) ? res : [];
        setCinemas(list);
        if (list.length > 0) {
          setSelectedCinemaId(list[0].id);
          setFormData((prev) => ({ ...prev, cinemaId: list[0].id }));
        }
      } catch (err) {
        console.error('Lỗi khi tải danh sách rạp:', err);
      }
    };
    fetchCinemas();
  }, []);

  // 2. Fetch danh sách phòng khi chọn rạp
  useEffect(() => {
    if (!selectedCinemaId) return;
    const fetchRooms = async () => {
      setLoading(true);
      try {
        const res = await roomService.getByCinemaId(selectedCinemaId);
        setRooms(Array.isArray(res) ? res : []);
      } catch (err) {
        console.error('Lỗi khi tải danh sách phòng:', err);
        setRooms([]);
      } finally {
        setLoading(false);
      }
    };
    fetchRooms();
  }, [selectedCinemaId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await roomService.add({
        cinemaId: parseInt(formData.cinemaId, 10),
        name: formData.name,
        numberOfRows: parseInt(formData.numberOfRows, 10),
        numberOfColumns: parseInt(formData.numberOfColumns, 10),
        description: formData.description,
      });
      toast.success('Thêm phòng chiếu & sinh sơ đồ ghế thành công!');
      setIsModalOpen(false);
      setFormData({
        cinemaId: selectedCinemaId,
        name: '',
        numberOfRows: 6,
        numberOfColumns: 8,
        description: '',
      });

      // Reload danh sách phòng
      const res = await roomService.getByCinemaId(selectedCinemaId);
      setRooms(Array.isArray(res) ? res : []);
    } catch (err) {
      toast.error(err.message || 'Thêm phòng chiếu thất bại');
    }
  };

  return (
    <div className="space-y-6">
      {/* HEADER & LỌC THEO RẠP */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">Quản Lý Phòng Chiếu</h1>
          <p className="text-xs text-slate-400">Danh sách các phòng chiếu và sơ đồ ghế tự động</p>
        </div>
        <div className="flex items-center gap-3 w-full sm:w-auto">
          <select
            value={selectedCinemaId}
            onChange={(e) => setSelectedCinemaId(e.target.value)}
            className="bg-slate-900 border border-slate-700 text-white text-sm rounded-xl px-4 py-2 focus:outline-none focus:border-red-500"
          >
            {cinemas.map((c) => (
              <option key={c.id} value={c.id}>
                📍 {c.name}
              </option>
            ))}
          </select>
          <button
            onClick={() => setIsModalOpen(true)}
            className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl text-sm transition-all shrink-0 shadow-lg shadow-red-900/30"
          >
            + Thêm Phòng
          </button>
        </div>
      </div>

      {/* HIỂN THỊ DANH SÁCH PHÒNG BẰNG BẢNG CHUYÊN NGHIỆP */}
      {loading ? (
        <p className="text-slate-400 text-sm">Đang tải danh sách phòng chiếu...</p>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-800 text-slate-200 text-xs uppercase font-bold">
              <tr>
                <th className="p-4">ID Phòng</th>
                <th className="p-4">Tên Phòng Chiếu</th>
                <th className="p-4">Thuộc Cụm Rạp</th>
                <th className="p-4">Sơ Đồ Ghế (Hàng x Cột)</th>
                <th className="p-4">Mô Tả</th>
                <th className="p-4 text-right">Trạng Thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {rooms.length === 0 ? (
                <tr>
                  <td colSpan="6" className="p-8 text-center text-slate-500">
                    Chưa có phòng chiếu nào tại rạp này
                  </td>
                </tr>
              ) : (
                rooms.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-800/50 transition-colors">
                    <td className="p-4 font-mono font-bold text-red-400">#{r.id}</td>
                    <td className="p-4 font-bold text-white text-base">{r.name}</td>
                    <td className="p-4 text-slate-300">
                      {cinemas.find((c) => String(c.id) === String(selectedCinemaId))?.name || `Cinema #${selectedCinemaId}`}
                    </td>
                    <td className="p-4 font-semibold text-amber-400">
                      {r.numberOfRows && r.numberOfColumns
                        ? `${r.numberOfRows} hàng x ${r.numberOfColumns} cột (${r.numberOfRows * r.numberOfColumns} ghế)`
                        : '48 Ghế (Standard & VIP)'}
                    </td>
                    <td className="p-4 text-slate-400 text-xs">{r.description || 'Phòng tiêu chuẩn'}</td>
                    <td className="p-4 text-right">
                      <span className="bg-emerald-950/90 text-emerald-400 border border-emerald-800 px-3 py-1 rounded-full text-xs font-bold">
                        Hoạt động
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* MODAL THÊM PHÒNG CHIẾU VỚI ĐẦY ĐỦ FIELDS */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Thêm Phòng Chiếu Mới">
        <form onSubmit={handleSubmit} className="space-y-4 text-sm text-slate-200">
          <div>
            <label className="block text-xs font-semibold mb-1">Chọn Rạp Chiếu</label>
            <select
              value={formData.cinemaId}
              onChange={(e) => setFormData({ ...formData, cinemaId: e.target.value })}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white focus:outline-none focus:border-red-500"
            >
              {cinemas.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold mb-1">Tên Phòng Chiếu</label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white focus:outline-none focus:border-red-500"
              placeholder="VD: Phòng 01 (IMAX)"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold mb-1">Số Hàng Ghế (Rows)</label>
              <input
                type="number"
                min="1"
                max="20"
                required
                value={formData.numberOfRows}
                onChange={(e) => setFormData({ ...formData, numberOfRows: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white focus:outline-none focus:border-red-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">Số Cột Ghế (Columns)</label>
              <input
                type="number"
                min="1"
                max="20"
                required
                value={formData.numberOfColumns}
                onChange={(e) => setFormData({ ...formData, numberOfColumns: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white focus:outline-none focus:border-red-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold mb-1">Mô Tả Phòng Chiếu</label>
            <textarea
              rows="2"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white focus:outline-none focus:border-red-500"
              placeholder="VD: Hệ thống âm thanh Dolby Atmos & Màn hình cong IMAX"
            />
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs font-semibold"
            >
              Hủy
            </button>
            <button
              type="submit"
              className="px-5 py-2 bg-red-600 hover:bg-red-700 text-white rounded-xl text-xs font-bold shadow"
            >
              Tạo Phòng
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
