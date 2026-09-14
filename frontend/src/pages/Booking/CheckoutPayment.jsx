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

  const [paymentMethod, setPaymentMethod] = useState('VIETQR'); // 'VIETQR' | 'STRIPE'
  const [stripeLoading, setStripeLoading] = useState(false);
  const [stripeProcessing, setStripeProcessing] = useState(false);
  const [stripeData, setStripeData] = useState(null); // { clientSecret, publishableKey, amount, paymentIntentId }

  const stripeRef = useRef(null);
  const cardElementRef = useRef(null);
  const cardContainerRef = useRef(null);

  useEffect(() => {
    // Tải script Stripe.js nếu chưa có trong DOM
    if (!document.getElementById('stripe-js')) {
      const script = document.createElement('script');
      script.id = 'stripe-js';
      script.src = 'https://js.stripe.com/v3/';
      script.async = true;
      document.head.appendChild(script);
    }
  }, []);

  // Tự động mount Stripe Card Element sau khi DOM div container đã render xong
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
                color: '#ffffff',
                fontFamily: 'Inter, sans-serif',
                fontSize: '16px',
                '::placeholder': { color: '#94a3b8' },
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

  // Khởi tạo Stripe Intent khi chọn phương thức Stripe
  const handleSelectStripeTab = async () => {
    setPaymentMethod('STRIPE');
    if (stripeData) return; // Đã tạo intent trước đó

    try {
      setStripeLoading(true);
      const response = await paymentService.createStripePaymentIntent({ ticketId: ticket.id });
      if (response && response.clientSecret) {
        setStripeData(response);
      } else {
        toast.error('Không thể tạo cổng thanh toán Stripe!');
      }
    } catch (err) {
      console.error('Stripe Intent error:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Lỗi khởi tạo cổng thanh toán Stripe!';
      toast.error(errorMessage);
    } finally {
      setStripeLoading(false);
    }
  };

  const handlePayWithStripe = async () => {
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
        // Xác nhận thành công với Backend
        await paymentService.confirmStripePayment(ticket.id, result.paymentIntent.id);
        toast.success('🎉 Thanh toán thẻ Stripe thành công!');
        navigate('/profile');
      }
    } catch (err) {
      console.error('Payment execution error:', err);
      toast.error(err.message || 'Lỗi trong quá trình xử lý thanh toán thẻ!');
    } finally {
      setStripeProcessing(false);
    }
  };

  const handleDonePayment = () => {
    navigate('/profile');
  };

  return (
    <div className="max-w-3xl mx-auto space-y-8 pb-12">
      <div className="text-center space-y-2">
        <h1 className="text-3xl font-extrabold text-white">Cổng Thanh Toán Vé Phim</h1>
        <p className="text-slate-400 text-sm">Vui lòng chọn phương thức thanh toán thuận tiện nhất cho bạn</p>
      </div>

      {/* TABS CHỌN PHƯƠNG THỨC THANH TOÁN */}
      <div className="flex bg-slate-900 p-1.5 rounded-2xl border border-slate-800 gap-2">
        <button
          onClick={() => setPaymentMethod('VIETQR')}
          className={`flex-1 py-3 text-sm font-bold rounded-xl transition-all flex items-center justify-center gap-2 ${
            paymentMethod === 'VIETQR'
              ? 'bg-slate-800 text-amber-400 shadow-md border border-slate-700'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <span>📱</span> Chuyển Khoản VietQR / Ngân Hàng
        </button>
        <button
          onClick={handleSelectStripeTab}
          className={`flex-1 py-3 text-sm font-bold rounded-xl transition-all flex items-center justify-center gap-2 ${
            paymentMethod === 'STRIPE'
              ? 'bg-slate-800 text-blue-400 shadow-md border border-slate-700'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <span>💳</span> Thẻ Quốc Tế Stripe (Visa / Mastercard)
        </button>
      </div>

      {/* CẢNH BÁO THỜI GIAN */}
      <div className="bg-red-950/90 border border-red-800 p-4 rounded-xl flex items-center gap-3 text-red-200 text-xs md:text-sm font-semibold shadow-lg">
        <svg className="w-6 h-6 text-red-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span>LƯU Ý: Ghế của quý khách đang được giữ tạm thời. Vui lòng hoàn tất thanh toán để nhận vé!</span>
      </div>

      {/* GIAO DIỆN THANH TOÁN 1: VIETQR */}
      {paymentMethod === 'VIETQR' && (
        <div className="space-y-6">
          <div className="bg-slate-800 border border-slate-700 rounded-2xl p-6 md:p-8 grid grid-cols-1 md:grid-cols-2 gap-8 shadow-2xl items-center">
            <div className="flex flex-col items-center space-y-3 bg-white p-4 rounded-xl">
              <img
                src={qrCodeUrl}
                alt="Mã QR Thanh Toán BIDV"
                className="w-full max-w-[220px] aspect-square object-contain"
              />
              <p className="text-xs text-slate-800 font-bold">Quét QR bằng Ngân hàng / MOMO</p>
            </div>

            <div className="space-y-4 text-sm text-slate-200">
              <h2 className="text-lg font-bold text-white border-b border-slate-700 pb-2">
                Thông Tin Chuyển Khoản
              </h2>

              <div className="space-y-2">
                <p><strong>Ngân hàng:</strong> <span className="text-amber-400 font-bold">BIDV (NH Đầu tư & Phát triển VN)</span></p>
                <p><strong>Tên tài khoản:</strong> MYCINEMA ENTERTAINMENT</p>
                <p><strong>Số tài khoản:</strong> <span className="font-mono text-red-400 font-extrabold text-base">9999999999</span></p>
                <p><strong>Số tiền:</strong> <span className="text-xl font-extrabold text-amber-400">{amount.toLocaleString()} VNĐ</span></p>
                <p>
                  <strong>Nội dung:</strong>{' '}
                  <span className="bg-slate-900 px-2 py-1 rounded text-red-400 font-mono font-bold">{transferContent}</span>
                </p>
              </div>

              <div className="pt-4 border-t border-slate-700 space-y-1 text-xs text-slate-400">
                <p>• Phim: {showtime?.movieName}</p>
                <p>• Mã Vé ID: #{ticket.id}</p>
              </div>
            </div>
          </div>

          <div className="text-center">
            <button
              onClick={handleDonePayment}
              className="w-full sm:w-auto px-10 py-3.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-base rounded-xl transition-all shadow-lg shadow-emerald-900/30 hover:scale-105 active:scale-95"
            >
              ✓ Đã Chuyển Khoản xong
            </button>
          </div>
        </div>
      )}

      {/* GIAO DIỆN THANH TOÁN 2: STRIPE CARD */}
      {paymentMethod === 'STRIPE' && (
        <div className="bg-slate-800 border border-slate-700 rounded-2xl p-6 md:p-8 space-y-6 shadow-2xl">
          <div className="flex items-center justify-between border-b border-slate-700 pb-4">
            <div>
              <h2 className="text-lg font-bold text-white">Thanh Toán Qua Thẻ Quốc Tế Stripe</h2>
              <p className="text-xs text-slate-400">Hỗ trợ Visa, Mastercard, JCB, American Express</p>
            </div>
            <span className="text-2xl font-extrabold text-blue-400">{amount.toLocaleString()} VNĐ</span>
          </div>

          {stripeLoading ? (
            <div className="text-center py-10 text-slate-400 space-y-2">
              <div className="inline-block w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin" />
              <p className="text-sm">Đang khởi tạo cổng thanh toán bảo mật Stripe...</p>
            </div>
          ) : (
            <div className="space-y-6">
              {/* CONTAINER CHO STRIPE CARD ELEMENT */}
              <div className="space-y-2">
                <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider">
                  Thông Tin Thẻ Thanh Toán
                </label>
                <div className="bg-slate-900 border border-slate-700 p-4 rounded-xl shadow-inner">
                  <div ref={cardContainerRef} id="stripe-card-element" className="min-h-[40px]" />
                </div>
              </div>

              <div className="bg-slate-900/60 p-4 rounded-xl text-xs text-slate-400 space-y-1">
                <p>💡 <strong>Thẻ thử nghiệm của Stripe (Sandbox):</strong></p>
                <p>• Số thẻ test: <span className="font-mono text-amber-400">4242 4242 4242 4242</span></p>
                <p>• Ngày hết hạn: Nhập tháng/năm bất kỳ trong tương lai (Ví dụ: <span className="font-mono text-amber-400">12/30</span>)</p>
                <p>• Mã CVC: <span className="font-mono text-amber-400">123</span></p>
              </div>

              <button
                onClick={handlePayWithStripe}
                disabled={stripeProcessing}
                className="w-full py-4 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-700 text-white font-extrabold rounded-xl text-base transition-all shadow-lg shadow-blue-900/40 hover:scale-[1.01] active:scale-[0.99]"
              >
                {stripeProcessing ? 'Đang xử lý thanh toán an toàn...' : `🔒 Thanh Toán ${amount.toLocaleString()} VNĐ qua Stripe`}
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
