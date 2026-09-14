import React, { useState } from 'react';
import axiosClient from '../../config/axiosClient';

export default function ConnectionError() {
  const [isChecking, setIsChecking] = useState(false);
  const [retryMessage, setRetryMessage] = useState('');
  const [showDetails, setShowDetails] = useState(false);

  const handleRetry = async () => {
    setIsChecking(true);
    setRetryMessage('');
    try {
      // Gọi thử 1 endpoint công khai bất kỳ để test kết nối máy chủ
      await axiosClient.get('/movies/now-showing', { params: { page: 1, size: 1 } });
      setRetryMessage('✅ Đã khôi phục kết nối! Đang quay về trang chủ...');
      setTimeout(() => {
        window.location.href = '/home';
      }, 1200);
    } catch (error) {
      setRetryMessage('❌ Chưa thể kết nối lại. Vui lòng kiểm tra lại internet hoặc thử lại sau ít phút!');
    } finally {
      setIsChecking(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex items-center justify-center p-4 relative overflow-hidden font-sans">
      {/* Dynamic Background Effects */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-red-600/10 rounded-full blur-3xl pointer-events-none animate-pulse"></div>
      <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-amber-500/10 rounded-full blur-3xl pointer-events-none"></div>

      {/* Main Glassmorphism Card */}
      <div className="max-w-lg w-full bg-slate-900/80 backdrop-blur-xl border border-slate-800 rounded-3xl p-8 shadow-2xl relative z-10 text-center space-y-6">
        
        {/* Animated Server Disconnect Icon */}
        <div className="mx-auto w-24 h-24 rounded-full bg-red-950/60 border border-red-800/60 flex items-center justify-center shadow-lg shadow-red-900/20 relative group">
          <div className="absolute inset-0 rounded-full bg-red-600/20 animate-ping opacity-30"></div>
          <svg
            className="w-12 h-12 text-red-500 transition-transform duration-300 group-hover:scale-110"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth="1.75"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M18.364 5.636a9 9 0 010 12.728m0 0l-2.829-2.829m2.829 2.829L21 21M15.536 8.464a5 5 0 010 7.072m0 0l-2.829-2.829m-4.243 2.829a5 5 0 010-7.072m-2.829 9.9a9 9 0 010-12.728M3 3l18 18"
            />
          </svg>
        </div>

        {/* Header Text */}
        <div className="space-y-2">
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
            Không Thể Kết Nối
          </h1>
          <p className="text-sm text-slate-400 leading-relaxed">
            Rất tiếc, kết nối bị gián đoạn hoặc hệ thống đang bảo trì. Vui lòng kiểm tra lại mạng và thử lại sau ít phút!
          </p>
        </div>

        {/* Status Alert Badge */}
        {retryMessage && (
          <div
            className={`p-3.5 rounded-xl text-xs font-semibold border transition-all animate-fadeIn ${
              retryMessage.includes('✅')
                ? 'bg-emerald-950/80 border-emerald-800 text-emerald-300'
                : 'bg-red-950/80 border-red-800 text-red-300'
            }`}
          >
            {retryMessage}
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
          <button
            onClick={handleRetry}
            disabled={isChecking}
            className="w-full sm:w-auto px-6 py-3 bg-red-600 hover:bg-red-700 active:bg-red-800 text-white font-bold rounded-xl text-sm transition-all shadow-lg shadow-red-900/30 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isChecking ? (
              <>
                <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                </svg>
                Đang thử lại...
              </>
            ) : (
              <>
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                Thử kết nối lại
              </>
            )}
          </button>

          <a
            href="/home"
            className="w-full sm:w-auto px-6 py-3 bg-slate-800 hover:bg-slate-700 active:bg-slate-750 text-slate-200 font-bold rounded-xl text-sm transition-all border border-slate-700 flex items-center justify-center gap-2"
          >
            <svg className="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            Về Trang Chủ
          </a>
        </div>

        {/* Technical Details Toggle */}
        <div className="pt-4 border-t border-slate-800/80">
          <button
            onClick={() => setShowDetails(!showDetails)}
            className="text-xs text-slate-500 hover:text-slate-400 transition-colors inline-flex items-center gap-1 font-medium"
          >
            <span>{showDetails ? 'Ẩn thông tin kỹ thuật' : 'Chi tiết mã lỗi kỹ thuật'}</span>
            <svg
              className={`w-3.5 h-3.5 transform transition-transform duration-200 ${showDetails ? 'rotate-180' : ''}`}
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          {showDetails && (
            <div className="mt-3 p-3 bg-slate-950/80 rounded-xl border border-slate-800 text-left text-xs font-mono text-slate-400 space-y-1 animate-fadeIn">
              <p><span className="text-red-400">Error Code:</span> ERR_NETWORK / 5xx</p>
              <p><span className="text-slate-500">API Endpoint:</span> {import.meta.env.VITE_API_URL || 'http://localhost:8081/api'}</p>
              <p><span className="text-slate-500">Timestamp:</span> {new Date().toISOString()}</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
