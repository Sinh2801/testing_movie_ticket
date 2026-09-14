import React, { useState, useEffect, useCallback } from 'react';
import paymentService from '../../service/paymentService';
import ConfirmModal from '../../components/common/ConfirmModal';
import { useToast } from '../../context/ToastContext';

// Mapping trạng thái thanh toán và style badge
const PAYMENT_STATUS_MAP = {
  PENDING: {
    label: 'CHỜ XÁC NHẬN',
    badgeClass: 'bg-amber-950/80 text-amber-400 border-amber-700/60',
    icon: '⏳',
  },
  SUCCESS: {
    label: 'THÀNH CÔNG',
    badgeClass: 'bg-emerald-950/80 text-emerald-400 border-emerald-700/60',
    icon: '✓',
  },
  FAILED: {
    label: 'THẤT BẠI',
    badgeClass: 'bg-red-950/80 text-red-400 border-red-700/60',
    icon: '✕',
  },
  CANCELLED: {
    label: 'ĐÃ HỦY',
    badgeClass: 'bg-slate-800 text-slate-400 border-slate-700',
    icon: '⊘',
  },
};

// Mapping phương thức thanh toán
const PAYMENT_METHOD_MAP = {
  LOCAL: { label: 'Trực tiếp', icon: '🏦' },
  MOMO: { label: 'Ví MoMo', icon: '📱' },
  VNPAY: { label: 'Cổng VNPAY', icon: '💳' },
};

export default function AdminPayments() {
  const { toast } = useToast();
  const [paymentCode, setPaymentCode] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize] = useState(20);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [confirmingId, setConfirmingId] = useState(null);

  // State quản lý Modal chi tiết & Modal xác nhận
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [confirmTarget, setConfirmTarget] = useState(null);

  // Hàm load danh sách thanh toán từ Backend
  const fetchPayments = useCallback(async () => {
    setLoading(true);
    try {
      const res = await paymentService.getPagedPayments(page, pageSize, paymentCode);
      if (Array.isArray(res)) {
        setData({ content: res, totalPages: 1, totalElements: res.length });
      } else if (res && Array.isArray(res.content)) {
        setData({
          content: res.content,
          totalPages: res.totalPages || 1,
          totalElements: res.totalElements || res.content.length,
        });
      } else {
        setData({ content: [], totalPages: 0, totalElements: 0 });
      }
    } catch (err) {
      console.error('Lỗi khi tải danh sách thanh toán:', err);
      setData({ content: [], totalPages: 0, totalElements: 0 });
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, paymentCode]);

  useEffect(() => {
    fetchPayments();
  }, [fetchPayments]);

  const handleSearchChange = (e) => {
    setPaymentCode(e.target.value);
    setPage(1);
  };

  const handleOpenDetail = (payment) => {
    setSelectedPayment(payment);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedPayment(null);
  };

  const handleRequestConfirm = (payment) => {
    setConfirmTarget(payment);
  };

  const handleExecuteConfirm = async () => {
    if (!confirmTarget) return;
    const payment = confirmTarget;
    const targetTicketId = payment.ticketId || payment.id;

    setConfirmingId(payment.id);
    try {
      await paymentService.confirmLocalPayment(targetTicketId);
      toast.success(`Xác nhận thanh toán cho vé #${targetTicketId} thành công!`);
      fetchPayments();
      if (selectedPayment && selectedPayment.id === payment.id) {
        setSelectedPayment((prev) => ({
          ...prev,
          status: 'SUCCESS',
          paidAt: new Date().toISOString(),
        }));
      }
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data?.message || err.message || 'Xác nhận thanh toán thất bại!');
    } finally {
      setConfirmingId(null);
      setConfirmTarget(null);
    }
  };

  const paymentList = Array.isArray(data?.content) ? data.content : [];

  return (
    <div className="space-y-6">
      {/* HEADER PAGE */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white flex items-center gap-2">
            <span>💳</span> Quản Lý Thanh Toán
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Theo dõi, tra cứu và xác nhận giao dịch thanh toán của khách hàng
          </p>
        </div>

        {/* SEARCH REALTIME INPUT */}
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            placeholder="Tìm kiếm realtime theo mã thanh toán..."
            value={paymentCode}
            onChange={handleSearchChange}
            className="w-full bg-slate-900 border border-slate-700 focus:border-red-500 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:ring-1 focus:ring-red-500/50 transition-all pl-10"
          />
          <span className="absolute left-3.5 top-3 text-slate-400 text-sm">🔍</span>
          {paymentCode && (
            <button
              onClick={() => { setPaymentCode(''); setPage(1); }}
              className="absolute right-3 top-2.5 text-slate-400 hover:text-white text-sm"
              title="Xóa tìm kiếm"
            >
              ✕
            </button>
          )}
        </div>
      </div>

      {/* TABLE LIST PAYMENTS */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-2xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-800/80 text-slate-200 text-xs uppercase font-bold tracking-wider">
              <tr>
                <th className="p-4">ID</th>
                <th className="p-4">Mã Thanh Toán</th>
                <th className="p-4">Mã Giao Dịch</th>
                <th className="p-4">Số Tiền</th>
                <th className="p-4">Phương Thức</th>
                <th className="p-4">Trạng Thái</th>
                <th className="p-4">Ngày Tạo</th>
                <th className="p-4 text-center">Hành Động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                <tr>
                  <td colSpan="8" className="p-8 text-center text-slate-400">
                    <div className="inline-flex items-center gap-2">
                      <svg className="animate-spin h-5 w-5 text-red-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      <span>Đang tải danh sách thanh toán...</span>
                    </div>
                  </td>
                </tr>
              ) : paymentList.length === 0 ? (
                <tr>
                  <td colSpan="8" className="p-8 text-center text-slate-500">
                    {paymentCode
                      ? `Không tìm thấy giao dịch nào khớp với mã "${paymentCode}"`
                      : 'Chưa có dữ liệu thanh toán'}
                  </td>
                </tr>
              ) : (
                paymentList.map((item) => {
                  const statusObj = PAYMENT_STATUS_MAP[item.status] || {
                    label: item.status,
                    badgeClass: 'bg-slate-800 text-slate-300 border-slate-700',
                    icon: '•',
                  };
                  const methodObj = PAYMENT_METHOD_MAP[item.method] || {
                    label: item.method,
                    icon: '💵',
                  };

                  return (
                    <tr key={item.id} className="hover:bg-slate-800/40 transition-colors">
                      <td className="p-4 font-bold text-slate-400">#{item.id}</td>
                      <td className="p-4 font-mono font-bold text-red-400">
                        {item.paymentCode || '-'}
                      </td>
                      <td className="p-4 font-mono text-xs text-slate-300">
                        {item.transactionId || '-'}
                      </td>
                      <td className="p-4 font-bold text-amber-400">
                        {item.amount != null ? item.amount.toLocaleString('vi-VN') : '0'} VNĐ
                      </td>
                      <td className="p-4 text-xs font-semibold">
                        <span className="inline-flex items-center gap-1.5 bg-slate-800/80 px-2.5 py-1 rounded-lg border border-slate-700/50">
                          <span>{methodObj.icon}</span>
                          <span>{methodObj.label}</span>
                        </span>
                      </td>
                      <td className="p-4">
                        <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold border ${statusObj.badgeClass}`}>
                          <span>{statusObj.icon}</span>
                          <span>{statusObj.label}</span>
                        </span>
                      </td>
                      <td className="p-4 text-xs text-slate-400">
                        {item.createdAt ? new Date(item.createdAt).toLocaleString('vi-VN') : '-'}
                      </td>
                      <td className="p-4 text-center space-x-2">
                        {/* NÚT CHI TIẾT */}
                        <button
                          onClick={() => handleOpenDetail(item)}
                          className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-xs font-semibold border border-slate-700 transition-all hover:border-slate-500"
                        >
                          👁️ Chi Tiết
                        </button>

                        {/* NÚT XÁC NHẬN (NẾU STATUS LÀ PENDING) */}
                        {item.status === 'PENDING' && (
                          <button
                            disabled={confirmingId === item.id}
                            onClick={() => handleRequestConfirm(item)}
                            className="px-3 py-1.5 bg-emerald-950 hover:bg-emerald-900 border border-emerald-700 text-emerald-300 rounded-lg text-xs font-bold transition-all disabled:opacity-50 shadow-sm shadow-emerald-900/40"
                          >
                            {confirmingId === item.id ? 'Đang xác nhận...' : '✓ Xác Nhận'}
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* PHÂN TRANG (20 DỮ LIỆU/TRANG) */}
        {data.totalPages > 0 && (
          <div className="flex flex-col sm:flex-row justify-between items-center p-4 border-t border-slate-800 gap-3 text-xs text-slate-400">
            <div>
              Hiển thị <span className="font-bold text-white">{paymentList.length}</span> / <span className="font-bold text-white">{data.totalElements}</span> giao dịch (20 dữ liệu/trang)
            </div>
            <div className="flex items-center gap-2">
              <button
                disabled={page <= 1 || loading}
                onClick={() => setPage((p) => Math.max(p - 1, 1))}
                className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg disabled:opacity-40 disabled:hover:bg-slate-800 transition-colors font-semibold border border-slate-700"
              >
                ◀ Trang trước
              </button>
              <span className="px-3 font-semibold text-slate-200">
                Trang <span className="text-red-400 font-bold">{page}</span> / {data.totalPages || 1}
              </span>
              <button
                disabled={page >= data.totalPages || loading}
                onClick={() => setPage((p) => p + 1)}
                className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg disabled:opacity-40 disabled:hover:bg-slate-800 transition-colors font-semibold border border-slate-700"
              >
                Trang sau ▶
              </button>
            </div>
          </div>
        )}
      </div>

      {/* MODAL CHI TIẾT THANH TOÁN (PAYMENT DETAIL MODAL) */}
      {isModalOpen && selectedPayment && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fadeIn">
          <div className="bg-slate-900 border border-slate-800 w-full max-w-lg rounded-2xl p-6 shadow-2xl space-y-6 relative text-slate-200">
            {/* Header Modal */}
            <div className="flex justify-between items-center border-b border-slate-800 pb-4">
              <div>
                <h3 className="text-lg font-extrabold text-white flex items-center gap-2">
                  <span>📄</span> Chi Tiết Thanh Toán #{selectedPayment.id}
                </h3>
                <p className="text-xs text-slate-400">Thông tin toàn bộ theo PaymentDetailResponse</p>
              </div>
              <button
                onClick={handleCloseModal}
                className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
              >
                ✕
              </button>
            </div>

            {/* Grid Nội dung Chi Tiết */}
            <div className="space-y-3.5 text-sm">
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">ID Thanh Toán:</span>
                <span className="font-bold text-white">#{selectedPayment.id}</span>
              </div>

              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">Mã Thanh Toán (paymentCode):</span>
                <span className="font-mono font-bold text-red-400">{selectedPayment.paymentCode || 'N/A'}</span>
              </div>

              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">ID Vé liên quan (ticketId):</span>
                <span className="font-mono font-bold text-cyan-400">#{selectedPayment.ticketId || selectedPayment.id}</span>
              </div>

              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">Mã Giao Dịch (transactionId):</span>
                <span className="font-mono text-slate-300">{selectedPayment.transactionId || 'Chưa cập nhật'}</span>
              </div>

              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">Số Tiền (amount):</span>
                <span className="font-extrabold text-amber-400 text-base">
                  {selectedPayment.amount != null ? selectedPayment.amount.toLocaleString('vi-VN') : '0'} VNĐ
                </span>
              </div>

              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">Phương Thức (method):</span>
                <span className="font-semibold text-slate-200">
                  {PAYMENT_METHOD_MAP[selectedPayment.method]?.label || selectedPayment.method}
                </span>
              </div>

              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">Trạng Thái (status):</span>
                <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold border ${PAYMENT_STATUS_MAP[selectedPayment.status]?.badgeClass || 'bg-slate-800 text-slate-300'}`}>
                  {PAYMENT_STATUS_MAP[selectedPayment.status]?.label || selectedPayment.status}
                </span>
              </div>

              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">Thời Gian Thanh Toán (paidAt):</span>
                <span className="text-slate-300 font-medium">
                  {selectedPayment.paidAt ? new Date(selectedPayment.paidAt).toLocaleString('vi-VN') : 'Chưa thanh toán'}
                </span>
              </div>

              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <span className="text-slate-400">Ngày Tạo (createdAt):</span>
                <span className="text-slate-300 font-medium">
                  {selectedPayment.createdAt ? new Date(selectedPayment.createdAt).toLocaleString('vi-VN') : 'N/A'}
                </span>
              </div>

              <div className="flex justify-between py-2">
                <span className="text-slate-400">Cập Nhật Gần Nhất (updatedAt):</span>
                <span className="text-slate-300 font-medium">
                  {selectedPayment.updatedAt ? new Date(selectedPayment.updatedAt).toLocaleString('vi-VN') : 'N/A'}
                </span>
              </div>
            </div>

            {/* Footer Modal Actions */}
            <div className="pt-4 border-t border-slate-800 flex justify-end gap-3">
              {selectedPayment.status === 'PENDING' && (
                <button
                  disabled={confirmingId === selectedPayment.id}
                  onClick={() => handleRequestConfirm(selectedPayment)}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold transition-all shadow-lg shadow-emerald-900/40"
                >
                  {confirmingId === selectedPayment.id ? 'Đang xác nhận...' : '✓ Xác Nhận Thanh Toán'}
                </button>
              )}
              <button
                onClick={handleCloseModal}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold border border-slate-700 transition-colors"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* CONFIRMATION MODAL */}
      <ConfirmModal
        isOpen={!!confirmTarget}
        onClose={() => setConfirmTarget(null)}
        onConfirm={handleExecuteConfirm}
        title="Xác nhận hoàn tất thanh toán"
        message={`Xác nhận hoàn tất thanh toán cho Mã thanh toán "${confirmTarget?.paymentCode}" (ID Vé #${confirmTarget?.ticketId || confirmTarget?.id})?`}
        confirmText="Xác Nhận Thanh Toán"
        cancelText="Hủy Bỏ"
        isDanger={false}
        loading={!!confirmingId}
      />
    </div>
  );
}
