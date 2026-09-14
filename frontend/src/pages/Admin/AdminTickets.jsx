import React, { useState, useEffect } from 'react';
import ticketService from '../../service/ticketService';
import ConfirmModal from '../../components/common/ConfirmModal';
import { useToast } from '../../context/ToastContext';

// Helper Mapping Trạng Thái Vé sang Tiếng Việt
export const TICKET_STATUS_MAP = {
  PENDING: { label: 'CHỜ THANH TOÁN', class: 'bg-amber-950 text-amber-400 border-amber-800' },
  CONFIRMED: { label: 'ĐÃ XÁC NHẬN', class: 'bg-emerald-950 text-emerald-400 border-emerald-800' },
  CHECKIN: { label: 'ĐÃ CHECK-IN', class: 'bg-blue-950 text-blue-400 border-blue-800' },
  CHECKEDIN: { label: 'ĐÃ CHECK-IN', class: 'bg-blue-950 text-blue-400 border-blue-800' },
  CANCELLED: { label: 'ĐÃ HỦY VÉ', class: 'bg-red-950 text-red-400 border-red-800' },
};

export default function AdminTickets() {
  const { toast } = useToast();
  const [uid, setUid] = useState('');
  const [page, setPage] = useState(1);
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [loading, setLoading] = useState(false);
  const [confirmCancelTarget, setConfirmCancelTarget] = useState(null);
  const [cancelLoading, setCancelLoading] = useState(false);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const res = await ticketService.filter(page, 10, uid);
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
      setData({ content: [], totalPages: 0 });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, [uid, page]);

  const handleExecuteCancel = async () => {
    if (!confirmCancelTarget) return;
    setCancelLoading(true);
    try {
      await ticketService.cancelTicket(confirmCancelTarget.id);
      toast.success(`Hủy vé #${confirmCancelTarget.id} thành công!`);
      fetchTickets();
    } catch (err) {
      toast.error(err.message || 'Không thể hủy vé');
    } finally {
      setCancelLoading(false);
      setConfirmCancelTarget(null);
    }
  };

  const ticketList = Array.isArray(data?.content) ? data.content : [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">Quản Lý Vé</h1>
          <p className="text-xs text-slate-400">Danh sách vé đặt xem phim của khách hàng</p>
        </div>
        <input
          type="text"
          placeholder="Tìm kiếm theo mã UID vé..."
          value={uid}
          onChange={(e) => { setUid(e.target.value); setPage(1); }}
          className="bg-slate-900 border border-slate-700 rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-red-500 w-full sm:w-72"
        />
      </div>

      {loading ? (
        <p className="text-slate-400 text-sm">Đang tải danh sách vé...</p>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-800 text-slate-200 text-xs uppercase font-bold">
              <tr>
                <th className="p-4">ID</th>
                <th className="p-4">Mã UID</th>
                <th className="p-4">Tổng Tiền</th>
                <th className="p-4">Trạng Thái</th>
                <th className="p-4">Ngày Đặt</th>
                <th className="p-4 text-right">Hành Động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {ticketList.length === 0 ? (
                <tr>
                  <td colSpan="6" className="p-6 text-center text-slate-500">
                    Không tìm thấy vé phù hợp
                  </td>
                </tr>
              ) : (
                ticketList.map((t) => {
                  const statusInfo = TICKET_STATUS_MAP[t.status] || { label: t.status, class: 'bg-slate-800 text-slate-300 border-slate-700' };
                  return (
                    <tr key={t.id} className="hover:bg-slate-800/50 transition-colors">
                      <td className="p-4 font-bold text-slate-400">#{t.id}</td>
                      <td className="p-4 font-mono font-bold text-red-400">{t.uid || `VE-${t.id}`}</td>
                      <td className="p-4 font-bold text-amber-400">{t.totalPrice?.toLocaleString()} VNĐ</td>
                      <td className="p-4">
                        <span className={`px-2.5 py-1 rounded text-xs font-bold border ${statusInfo.class}`}>
                          {statusInfo.label}
                        </span>
                      </td>
                      <td className="p-4">{t.createdAt ? new Date(t.createdAt).toLocaleString('vi-VN') : ''}</td>
                      <td className="p-4 text-right">
                        <button
                          disabled={t.status === 'CHECKIN' || t.status === 'CANCELLED'}
                          onClick={() => setConfirmCancelTarget(t)}
                          className="px-3 py-1 bg-red-950 hover:bg-red-900 border border-red-800 text-red-300 rounded text-xs font-semibold disabled:opacity-40 transition-all"
                        >
                          Hủy Vé
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>

          {data.totalPages > 1 && (
            <div className="flex justify-between items-center p-4 border-t border-slate-800 text-xs">
              <button
                disabled={page <= 1}
                onClick={() => setPage(page - 1)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded disabled:opacity-40"
              >
                Trang trước
              </button>
              <span>Trang {page} / {data.totalPages}</span>
              <button
                disabled={page >= data.totalPages}
                onClick={() => setPage(page + 1)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded disabled:opacity-40"
              >
                Trang sau
              </button>
            </div>
          )}
        </div>
      )}

      {/* CONFIRMATION MODAL HỦY VÉ */}
      <ConfirmModal
        isOpen={!!confirmCancelTarget}
        onClose={() => setConfirmCancelTarget(null)}
        onConfirm={handleExecuteCancel}
        title="Xác nhận hủy vé"
        message={`Bạn có chắc chắn muốn hủy vé ID #${confirmCancelTarget?.id} (Mã UID: ${confirmCancelTarget?.uid || 'N/A'}) không? Ghế ngồi của vé này sẽ được giải phóng.`}
        confirmText="Xác Nhận Hủy Vé"
        cancelText="Bỏ Qua"
        isDanger={true}
        loading={cancelLoading}
      />
    </div>
  );
}
