import React, { useState, useEffect } from 'react';
import genreService from '../../service/genreService';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';

export default function AdminGenres() {
  const { toast } = useToast();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState(null);
  const [name, setName] = useState('');

  const fetchGenres = async () => {
    setLoading(true);
    try {
      const res = await genreService.getAll();
      setList(Array.isArray(res) ? res : []);
    } catch (err) {
      console.error(err);
      setList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGenres();
  }, []);

  const handleOpenAdd = () => {
    setEditingGenre(null);
    setName('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (genre) => {
    setEditingGenre(genre);
    setName(genre.name);
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingGenre) {
        await genreService.edit(editingGenre.id, { name });
        toast.success('Cập nhật thể loại thành công!');
      } else {
        await genreService.add({ name });
        toast.success('Thêm thể loại thành công!');
      }
      setIsModalOpen(false);
      fetchGenres();
    } catch (err) {
      toast.error(err.message || 'Thao tác thất bại');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">Quản Lý Thể Loại</h1>
          <p className="text-xs text-slate-400">Danh mục các thể loại phim điện ảnh trong hệ thống</p>
        </div>
        <button
          onClick={handleOpenAdd}
          className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl text-sm transition-all shadow-lg shadow-red-900/30"
        >
          + Thêm Thể Loại
        </button>
      </div>

      {loading ? (
        <p className="text-slate-400 text-sm">Đang tải danh mục thể loại...</p>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-800 text-slate-200 text-xs uppercase font-bold">
              <tr>
                <th className="p-4">ID Thể Loại</th>
                <th className="p-4">Tên Thể Loại Phim</th>
                <th className="p-4 text-right">Hành Động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {list.length === 0 ? (
                <tr>
                  <td colSpan="3" className="p-6 text-center text-slate-500">
                    Chưa có thể loại phim nào
                  </td>
                </tr>
              ) : (
                list.map((g) => (
                  <tr key={g.id} className="hover:bg-slate-800/50 transition-colors">
                    <td className="p-4 font-mono font-bold text-red-400">#{g.id}</td>
                    <td className="p-4 font-bold text-white text-base">{g.name}</td>
                    <td className="p-4 text-right">
                      <button
                        onClick={() => handleOpenEdit(g)}
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

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingGenre ? 'Chỉnh Sửa Thể Loại' : 'Thêm Thể Loại Mới'}>
        <form onSubmit={handleSubmit} className="space-y-4 text-sm text-slate-200">
          <div>
            <label className="block text-xs font-semibold mb-1">Tên Thể Loại</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white focus:outline-none focus:border-red-500"
              placeholder="VD: Hành Động, Viễn Tưởng..."
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
              Lưu
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
