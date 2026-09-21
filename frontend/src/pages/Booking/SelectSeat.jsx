import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import showtimeService from '../../service/showtimeService';
import ticketService from '../../service/ticketService';
import paymentService from '../../service/paymentService';
import { useToast } from '../../context/ToastContext';

// Biến cấu hình thời gian tự động cập nhật sơ đồ ghế (tính bằng miligiây)
export const SEAT_POLLING_INTERVAL = 5000;

export default function SelectSeat() {
  const { showtimeId } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();

  const storageKey = `selected_seats_${showtimeId}`;

  const [showtime, setShowtime] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState(() => {
    try {
      const saved = sessionStorage.getItem(storageKey);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });
  const [loading, setLoading] = useState(true);
  const [bookingLoading, setBookingLoading] = useState(false);

  useEffect(() => {
    try {
      if (selectedSeatIds.length > 0) {
        sessionStorage.setItem(storageKey, JSON.stringify(selectedSeatIds));
      } else {
        sessionStorage.removeItem(storageKey);
      }
    } catch (e) {
      console.warn('Cannot write to sessionStorage:', e);
    }
  }, [selectedSeatIds, storageKey]);

  // Hàm ngầm fetch lại danh sách ghế và tự động phát hiện xung đột chọn trùng ghế
  const fetchLatestSeats = async (isBackground = false) => {
    try {
      const seatsData = await showtimeService.getSeats(showtimeId);
      if (!Array.isArray(seatsData)) return;

      if (isBackground) {
        // Kiểm tra xem trong các ghế đang chọn, có ghế nào vừa bị người khác đặt không
        setSelectedSeatIds((prevSelectedIds) => {
          if (prevSelectedIds.length === 0) return prevSelectedIds;

          const conflictingSeats = seatsData.filter((seat) => {
            const isNowBooked = seat.status === 'BOOKED' || seat.status === 'RESERVED';
            return prevSelectedIds.includes(seat.id) && isNowBooked;
          });

          if (conflictingSeats.length > 0) {
            const conflictingNames = conflictingSeats.map((s) => s.seatName || s.id).join(', ');
            toast.warning(`⚠️ Ghế (${conflictingNames}) vừa được người khác đặt! Đã tự động bỏ chọn.`);

            const conflictingIds = new Set(conflictingSeats.map((s) => s.id));
            return prevSelectedIds.filter((id) => !conflictingIds.has(id));
          }

          return prevSelectedIds;
        });
      }

      setSeats(seatsData);
    } catch (err) {
      console.error('Lỗi tự động cập nhật sơ đồ ghế:', err);
    }
  };

  // 1. Initial Load: Fetch Suất chiếu và Ghế ban đầu
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [showtimeData, seatsData] = await Promise.all([
          showtimeService.getById(showtimeId),
          showtimeService.getSeats(showtimeId),
        ]);
        setShowtime(showtimeData);
        setSeats(Array.isArray(seatsData) ? seatsData : []);
      } catch (err) {
        console.error('Lỗi lấy thông tin ghế:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [showtimeId]);

  // 2. Polling Effect: Tự động refresh ghế mỗi 5s & tạm ngưng khi người dùng rời tab (Tab Inactive)
  useEffect(() => {
    let intervalId = null;

    const startPolling = () => {
      if (!intervalId) {
        intervalId = setInterval(() => {
          if (document.visibilityState === 'visible') {
            fetchLatestSeats(true);
          }
        }, SEAT_POLLING_INTERVAL);
      }
    };

    const stopPolling = () => {
      if (intervalId) {
        clearInterval(intervalId);
        intervalId = null;
      }
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        fetchLatestSeats(true); // Fetch ngay lập tức khi quay trở lại tab
        startPolling();
      } else {
        stopPolling(); // Tạm dừng polling khi rời tab để tiết kiệm tài nguyên
      }
    };

    startPolling();
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      stopPolling();
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [showtimeId]);

  const toggleSelectSeat = (seat) => {
    if (seat.status === 'BOOKED' || seat.status === 'RESERVED') return;

    if (selectedSeatIds.includes(seat.id)) {
      setSelectedSeatIds(selectedSeatIds.filter((id) => id !== seat.id));
    } else {
      setSelectedSeatIds([...selectedSeatIds, seat.id]);
    }
  };

  const selectedSeats = seats.filter((s) => selectedSeatIds.includes(s.id));
  const totalPrice = selectedSeats.length * (showtime?.price || 90000);

  const handleBookTicket = async () => {
    if (selectedSeatIds.length === 0) {
      toast.warning('Vui lòng chọn ít nhất 1 ghế ngồi!');
      return;
    }

    try {
      setBookingLoading(true);

      // 1. Tạo vé trong hệ thống (/tickets/book)
      const ticketResponse = await ticketService.bookTicket({
        showtimeId: parseInt(showtimeId, 10),
        showtimeSeatIds: selectedSeatIds,
      });

      if (ticketResponse && ticketResponse.id) {
        let paymentResponse = null;

        // 2. Ngay sau khi nhận ticketResponse thành công, tự động gọi tới /payments/local/create với ticketId nhận được
        try {
          paymentResponse = await paymentService.createLocalPayment({
            ticketId: ticketResponse.id,
          });
        } catch (paymentErr) {
          console.error('Lỗi khi khởi tạo giao dịch thanh toán:', paymentErr);
        }

        // Clear saved seats in sessionStorage after successful ticket creation
        sessionStorage.removeItem(storageKey);

        // 3. Chuyển hướng tới trang Checkout với đầy đủ thông tin vé & thanh toán
        navigate('/booking/checkout', {
          state: {
            ticket: ticketResponse,
            payment: paymentResponse,
            showtime,
          },
        });
      } else {
        toast.error('Tạo vé thất bại, không nhận được phản hồi từ hệ thống!');
      }
    } catch (err) {
      console.error('Lỗi đặt vé:', err);
      toast.error(err.message || 'Đặt vé thất bại, vui lòng thử lại!');
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return <div className="text-center py-20 text-slate-400">Đang tải sơ đồ ghế ngồi...</div>;
  }

  return (
    <div className="max-w-7xl mx-auto space-y-8 pb-12">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-slate-800 pb-4 gap-2">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white">
            Sơ Đồ Chọn Ghế Phim: <span className="text-red-500">{showtime?.movieName}</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">Vui lòng chọn các ghế còn trống bên dưới</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* BÊN TRÁI: SƠ ĐỒ PHÒNG CHIẾU */}
        <div className="lg:col-span-2 bg-slate-950 border border-slate-800 rounded-2xl p-6 flex flex-col items-center space-y-8">
          
          {/* MÀN HÌNH */}
          <div className="w-full max-w-lg text-center space-y-2">
            <div className="w-full h-3 bg-gradient-to-r from-slate-700 via-red-500 to-slate-700 rounded-full shadow-lg shadow-red-900/40" />
            <p className="text-xs uppercase font-bold text-slate-500 tracking-widest">Màn Hình Projection</p>
          </div>

          {/* SƠ ĐỒ GHẾ GRID */}
          <div className="grid grid-cols-8 gap-3 max-w-md mx-auto pt-4">
            {seats.map((seat) => {
              const isBooked = seat.status === 'BOOKED' || seat.status === 'RESERVED';
              const isSelected = selectedSeatIds.includes(seat.id);

              return (
                <button
                  key={seat.id}
                  disabled={isBooked}
                  onClick={() => toggleSelectSeat(seat)}
                  className={`w-10 h-10 rounded-xl font-bold text-xs flex items-center justify-center transition-all shadow ${
                    isBooked
                      ? 'bg-slate-800 text-slate-600 border border-slate-800 cursor-not-allowed'
                      : isSelected
                      ? 'bg-red-600 text-white border-2 border-white scale-110 shadow-red-900/60'
                      : seat.type === 'VIP'
                      ? 'bg-amber-900/60 text-amber-300 border border-amber-700 hover:bg-amber-800'
                      : 'bg-slate-800 text-slate-200 border border-slate-700 hover:bg-slate-700'
                  }`}
                  title={isBooked ? 'Ghế đã có người đặt' : `Ghế ${seat.seatName || seat.id}`}
                >
                  {seat.seatName || seat.id}
                </button>
              );
            })}
          </div>

          {/* CHÚ THÍCH TRẠNG THÁI GHẾ */}
          <div className="flex flex-wrap items-center justify-center gap-6 text-xs text-slate-400 pt-4 border-t border-slate-800 w-full">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded bg-slate-800 border border-slate-700" />
              <span>Ghế Thường</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded bg-amber-900/60 border border-amber-700" />
              <span>Ghế VIP</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded bg-red-600 border border-white" />
              <span>Đang chọn</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded bg-slate-800 border border-slate-800" />
              <span>Đã đặt / Đang giữ tạm</span>
            </div>
          </div>

        </div>

        {/* BÊN PHẢI: TÓM TẮT VÉ & NÚT ĐẶT VÉ / QUAY LẠI */}
        <div className="bg-slate-800 border border-slate-700 rounded-2xl p-6 flex flex-col justify-between space-y-6 shadow-xl h-fit">
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-white border-b border-slate-700 pb-3">
              Tóm Tắt Đặt Vé
            </h2>

            <div className="space-y-2 text-sm text-slate-300">
              <p><strong>Phim:</strong> {showtime?.movieName}</p>
              <p><strong>Rạp chiếu:</strong> {showtime?.cinemaName || 'CinePrime Landmark 81'}</p>
              <p><strong>Phòng:</strong> {showtime?.roomName || 'Phòng 01'}</p>
              <p><strong>Giờ chiếu:</strong> {showtime?.startTime ? new Date(showtime.startTime).toLocaleString('vi-VN') : ''}</p>
              <p>
                <strong>Ghế đã chọn:</strong>{' '}
                {selectedSeats.length > 0 ? (
                  <span className="font-bold text-red-400">{selectedSeats.map((s) => s.seatName || s.id).join(', ')}</span>
                ) : (
                  <span className="text-slate-500">Chưa chọn ghế</span>
                )}
              </p>
            </div>

            <div className="pt-4 border-t border-slate-700 flex justify-between items-center">
              <span className="text-sm font-semibold text-slate-300">Tổng tiền vé:</span>
              <span className="text-2xl font-extrabold text-amber-400">
                {totalPrice.toLocaleString()} VNĐ
              </span>
            </div>
          </div>

          <div className="space-y-3 pt-2">
            <button
              onClick={handleBookTicket}
              disabled={bookingLoading || selectedSeatIds.length === 0}
              className="w-full py-3.5 bg-red-600 hover:bg-red-700 disabled:bg-slate-700 disabled:text-slate-500 text-white font-bold rounded-xl text-sm transition-all shadow-lg shadow-red-900/30"
            >
              {bookingLoading ? 'Đang xử lý tạo vé...' : 'Đặt Vé Ngay'}
            </button>
            <button
              onClick={() => navigate(-1)}
              className="w-full py-2.5 bg-slate-700 hover:bg-slate-600 text-slate-200 font-semibold rounded-xl text-xs transition-colors border border-slate-600"
            >
              ← Quay lại
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
