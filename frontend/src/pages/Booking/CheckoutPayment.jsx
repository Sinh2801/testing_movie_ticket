import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import paymentService from '../../service/paymentService';
import { useToast } from '../../context/ToastContext';

export default function CheckoutPayment() {
  const location = useLocation();
  const navigate = useNavigate();
  const { toast } = useToast();

  const ticket = location.state?.ticket;
  const payment = location.state?.payment;
  const showtime = location.state?.showtime;

  const [paymentMethod, setPaymentMethod] = useState('STRIPE');
  const [stripeLoading, setStripeLoading] = useState(false);
  const [stripeProcessing, setStripeProcessing] = useState(false);
  const [stripeData, setStripeData] = useState(null);

  const stripeRef = useRef(null);
  const cardElementRef = useRef(null);
  const cardContainerRef = useRef(null);

  // Tải script Stripe.js khi trang load
  useEffect(() => {
    if (!document.getElementById('stripe-js')) {
      const script = document.createElement('script');
      script.id = 'stripe-js';
      script.src = 'https://js.stripe.com/v3/';
      script.async = true;
      document.head.appendChild(script);
    }
  }, []);

  // Tự động khởi tạo Stripe Intent khi có vé hợp lệ
  useEffect(() => {
    if (ticket?.id && !stripeData && !stripeLoading) {
      handleInitStripeIntent();
    }
  }, [ticket]);

  const handleInitStripeIntent = async () => {
    try {
      setStripeLoading(true);
      const response = await paymentService.createStripePaymentIntent({ ticketId: ticket.id });
      if (response && response.clientSecret) {
        setStripeData(response);
      } else {
        toast.error('Không thể khởi tạo thanh toán Stripe!');
      }
    } catch (err) {
      console.error('Stripe Intent error:', err);
      const errorMessage = err.message || err.response?.data?.message || 'Lỗi khởi tạo cổng thanh toán Stripe!';
      toast.error(errorMessage);
    } finally {
      setStripeLoading(false);
    }
  };

  // Mount Stripe Card Element
  useEffect(() => {
    if (paymentMethod === 'STRIPE' && stripeData && !stripeLoading) {
      const mountCard = () => {
        if (!window.Stripe) {
          setTimeout(mountCard, 200);
          return;
        }
        try {
          if (!stripeRef.current) {
            stripeRef.current = window.Stripe(stripeData.publishableKey);
          }
          if (cardContainerRef.current && !cardElementRef.current) {
            const elements = stripeRef.current.elements();
            const style = {
              base: {
                color: '#f8fafc',
                fontFamily: 'Inter, system-ui, sans-serif',
                fontSize: '16px',
                lineHeight: '24px',
                '::placeholder': { color: '#64748b' },
                iconColor: '#38bdf8',
              },
              invalid: {
                color: '#f87171',
                iconColor: '#f87171',
              },
            };
            cardElementRef.current = elements.create('card', { style, hidePostalCode: true });
            cardElementRef.current.mount(cardContainerRef.current);
          }
        } catch (e) {
          console.error('Mount Stripe Card error:', e);
        }
      };

      mountCard();
    }
  }, [paymentMethod, stripeData, stripeLoading]);

  if (!ticket) {
    return (
      <div className="text-center py-20 space-y-4">
        <p className="text-red-400 font-semibold">Không tìm thấy thông tin giao dịch thanh toán vé!</p>
        <button onClick={() => navigate('/home')} className="px-5 py-2 bg-slate-800 text-white rounded-lg text-sm">
          Quay về Trang Chủ
        </button>
      </div>
    );
  }

  const transferContent = payment?.paymentCode || ticket.uid || `VE-${ticket.id}`;
  const amount = payment?.amount || ticket.totalPrice || 0;
  const qrCodeUrl = `https://img.vietqr.io/image/BIDV-9999999999-compact2.png?amount=${amount}&addInfo=${transferContent}&accountName=MYCINEMA%20ENTERTAINMENT`;

  const handlePayWithStripe = async (e) => {
    e.preventDefault();
    if (!stripeRef.current || !cardElementRef.current || !stripeData?.clientSecret) {
      toast.warning('Cổng thanh toán Stripe chưa sẵn sàng!');
      return;
    }

    try {
      setStripeProcessing(true);
      const result = await stripeRef.current.confirmCardPayment(stripeData.clientSecret, {
        payment_method: {
          card: cardElementRef.current,
        },
      });

      if (result.error) {
        toast.error(`Thanh toán thất bại: ${result.error.message}`);
      } else if (result.paymentIntent && result.paymentIntent.status === 'succeeded') {
        await paymentService.confirmStripePayment(ticket.id, result.paymentIntent.id);
        toast.success('🎉 Thanh toán thành công!');
        navigate('/profile');
      }
    } catch (err) {
      console.error('Payment execution error:', err);
      toast.error(err.message || 'Lỗi trong quá trình xử lý thanh toán!');
    } finally {
      setStripeProcessing(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-16">
      {/* HEADER HÓA ĐƠN */}
      <div className="text-center space-y-1">
        <h1 className="text-3xl font-bold text-white">Thanh Toán Đơn Hàng</h1>
        <p className="text-slate-400 text-sm">Chọn phương thức thanh toán để hoàn tất vé</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        
        {/* KHU VỰC BÊN TRÁI: FORM THANH TOÁN (7 COLS) */}
        <div className="lg:col-span-7 space-y-6">
          
          {/* SELECTION TABS */}
          <div className="grid grid-cols-2 bg-slate-900 p-1 rounded-xl border border-slate-800 gap-1">
            <button
              type="button"
              onClick={() => setPaymentMethod('STRIPE')}
              className={`py-3 px-4 text-xs md:text-sm font-bold rounded-lg transition-all ${
                paymentMethod === 'STRIPE'
                  ? 'bg-blue-600 text-white shadow'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Thẻ Stripe (Visa/Mastercard)
            </button>

            <button
              type="button"
              onClick={() => setPaymentMethod('VIETQR')}
              className={`py-3 px-4 text-xs md:text-sm font-bold rounded-lg transition-all ${
                paymentMethod === 'VIETQR'
                  ? 'bg-emerald-600 text-white shadow'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              VietQR / Chuyển Khoản
            </button>
          </div>

          {/* TAB 1: STRIPE CARD SIMPLE FORM */}
          {paymentMethod === 'STRIPE' && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-6 shadow-xl">
              <h2 className="text-base font-bold text-white">Thông Tin Thẻ</h2>

              <form onSubmit={handlePayWithStripe} className="space-y-5">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-2">
                    Số thẻ, ngày hết hạn & CVC
                  </label>
                  <div className="bg-slate-950 border border-slate-700 p-3.5 rounded-xl shadow-inner focus-within:border-blue-500">
                    {stripeLoading ? (
                      <div className="text-center py-2 text-slate-400 text-xs">Đang tải Stripe...</div>
                    ) : (
                      <div ref={cardContainerRef} id="stripe-card-element" className="min-h-[38px]" />
                    )}
                  </div>
                </div>

                {/* GỢI Ý THẺ TEST ĐƠN GIẢN */}
                <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-400">
                  Thẻ test Stripe: <span className="font-mono text-amber-400 font-bold">4242 4242 4242 4242</span> | Hạn dùng: <span className="font-mono text-slate-200">12/30</span> | CVC: <span className="font-mono text-slate-200">123</span>
                </div>

                {/* NÚT THANH TOÁN DUY NHẤT VỚI CHỮ "Thanh toán" */}
                <button
                  type="submit"
                  disabled={stripeProcessing || stripeLoading}
                  className="w-full py-3.5 bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white font-bold rounded-xl text-base transition-all shadow-lg flex items-center justify-center gap-2"
                >
                  {stripeProcessing ? 'Đang xử lý...' : 'Thanh toán'}
                </button>
              </form>
            </div>
          )}

          {/* TAB 2: VIETQR CONTENT */}
          {paymentMethod === 'VIETQR' && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-6 shadow-xl">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <h2 className="text-base font-bold text-white">Quét Mã VietQR</h2>
                <span className="text-lg font-bold text-amber-400">{amount.toLocaleString()} VNĐ</span>
              </div>

              <div className="flex flex-col items-center space-y-3">
                <div className="bg-white p-3 rounded-2xl">
                  <img src={qrCodeUrl} alt="Mã QR VietQR" className="w-44 h-44 object-contain" />
                </div>
                <div className="text-center text-xs text-slate-300 space-y-1">
                  <p>Ngân Hàng: <strong className="text-amber-400">BIDV</strong></p>
                  <p>Số TK: <strong className="font-mono text-white">9999999999</strong></p>
                  <p>Nội dung: <strong className="text-red-400 font-mono">{transferContent}</strong></p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => navigate('/profile')}
                className="w-full py-3 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-xl text-sm transition-all shadow"
              >
                Tôi Đã Chuyển Khoản
              </button>
            </div>
          )}

        </div>

        {/* KHU VỰC BÊN PHẢI: CHI TIẾT ĐƠN VÉ (5 COLS) */}
        <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-5 shadow-xl">
          <h2 className="text-base font-bold text-white border-b border-slate-800 pb-3">
            Thông Tin Đơn Vé #{ticket.id}
          </h2>

          <div className="space-y-4">
            <div className="flex gap-4 items-center">
              {showtime?.movieImageUrl && (
                <img
                  src={showtime.movieImageUrl}
                  alt={showtime.movieName}
                  className="w-14 h-18 object-cover rounded-lg border border-slate-700 shrink-0"
                />
              )}
              <div className="space-y-1">
                <h3 className="font-bold text-white text-sm line-clamp-1">
                  {showtime?.movieName || ticket.movieName || 'Phim Chiếu Rạp'}
                </h3>
                <p className="text-xs text-slate-400">🎬 {showtime?.cinemaName || 'Rạp chiếu'}</p>
                <p className="text-xs text-slate-400">🚪 {showtime?.roomName || 'Phòng chiếu'}</p>
              </div>
            </div>

            <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-800 space-y-2 text-xs text-slate-300">
              <div className="flex justify-between">
                <span className="text-slate-500">Ghế đã chọn:</span>
                <span className="font-bold text-red-400">{ticket.seatNames ? ticket.seatNames.join(', ') : 'Đã chọn'}</span>
              </div>
            </div>

            <div className="pt-2 border-t border-slate-800 flex justify-between items-center">
              <span className="text-sm font-semibold text-slate-400">Tổng thanh toán:</span>
              <span className="text-xl font-bold text-amber-400">
                {amount.toLocaleString()} VNĐ
              </span>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}

