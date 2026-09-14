import React, { useState, useEffect } from 'react';
import cinemaService from '../../service/cinemaService';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export default function AdminCinemas() {
  const { toast } = useToast();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCinema, setEditingCinema] = useState(null);
  const [formData, setFormData] = useState({ name: '', address: '' });

  const fetchCinemas = async () => {
    setLoading(true);
    try {
      const res = await cinemaService.getAll();
      setList(Array.isArray(res) ? res : []);
    } catch (err) {
      console.error(err);
      setList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCinemas();
  }, []);

  const handleOpenAdd = () => {
    setEditingCinema(null);
    setFormData({ name: '', address: '' });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (c) => {
    setEditingCinema(c);
    setFormData({ name: c.name, address: c.address });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingCinema) {
        await cinemaService.edit({ id: editingCinema.id, ...formData });
        toast.success('Cập nhật rạp thành công!');
      } else {
        await cinemaService.add(formData);
        toast.success('Thêm rạp thành công!');
      }
      setIsModalOpen(false);
      fetchCinemas();
    } catch (err) {
      toast.error(err.message || 'Thao tác thất bại');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">Quản Lý Rạp Chiếu</h1>
          <p className="text-xs text-slate-400">Danh sách các cụm rạp chiếu phim trong hệ thống</p>
        </div>
        <button
          onClick={handleOpenAdd}
          className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl text-sm transition-all shadow-lg shadow-red-900/30"
        >
          + Thêm Rạp
        </button>
      </div>

      {loading ? (
        <p className="text-slate-400 text-sm">Đang tải danh sách rạp...</p>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-800 text-slate-200 text-xs uppercase font-bold">
              <tr>
                <th className="p-4">ID Rạp</th>
                <th className="p-4">Tên Cụm Rạp</th>
                <th className="p-4">Địa Chỉ</th>
                <th className="p-4 text-right">Hành Động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {list.length === 0 ? (
                <tr>
                  <td colSpan="4" className="p-6 text-center text-slate-500">
                    Chưa có rạp chiếu nào
                  </td>
                </tr>
              ) : (
                list.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-800/50 transition-colors">
                    <td className="p-4 font-mono font-bold text-red-400">#{c.id}</td>
                    <td className="p-4 font-bold text-white text-base">{c.name}</td>
                    <td className="p-4 text-slate-300">📍 {c.address}</td>
                    <td className="p-4 text-right">
                      <button
                        onClick={() => handleOpenEdit(c)}
                        className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-xs font-semibold border border-slate-700 transition-all"
                      >
                        Chỉnh Sửa
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingCinema ? 'Chỉnh Sửa Rạp Chiếu' : 'Thêm Rạp Chiếu Mới'}>
        <form onSubmit={handleSubmit} className="space-y-4 text-sm text-slate-200">
          <div>
            <label className="block text-xs font-semibold mb-1">Tên Rạp Chiếu</label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white focus:outline-none focus:border-red-500"
              placeholder="VD: CinePrime Landmark 81"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold mb-1">Địa Chỉ Rạp</label>
            <input
              type="text"
              required
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white focus:outline-none focus:border-red-500"
              placeholder="VD: 720A Điện Biên Phủ, P. 22, Bình Thạnh, TP.HCM"
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
              Lưu Rạp
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
