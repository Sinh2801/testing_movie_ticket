import React, { useEffect, useState } from 'react';
import { useToast } from '../../context/ToastContext';

function ToastItem({ toast, onRemove }) {
  const [progress, setProgress] = useState(100);
  const [isExiting, setIsExiting] = useState(false);

  const duration = toast.duration || 5000;

  useEffect(() => {
    const startTime = Date.now();
    const interval = setInterval(() => {
      const elapsed = Date.now() - startTime;
      const remaining = Math.max(0, 100 - (elapsed / duration) * 100);
      setProgress(remaining);

      if (elapsed >= duration) {
        clearInterval(interval);
        triggerClose();
      }
    }, 50);

    return () => clearInterval(interval);
  }, [duration]);

  const triggerClose = () => {
    setIsExiting(true);
    setTimeout(() => {
      onRemove(toast.id);
    }, 250); // Match animation duration
  };

  // Color mappings based on toast type
  const config = {
    success: {
      bgColor: 'bg-slate-900/95 border-emerald-500/80 text-emerald-100 shadow-emerald-900/20',
      iconBg: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
      barColor: 'bg-emerald-500',
      defaultTitle: 'Thành công',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
        </svg>
      )
    },
    error: {
      bgColor: 'bg-slate-900/95 border-rose-500/80 text-rose-100 shadow-rose-900/20',
      iconBg: 'bg-rose-500/20 text-rose-400 border-rose-500/30',
      barColor: 'bg-rose-500',
      defaultTitle: 'Lỗi',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      )
    },
    warning: {
      bgColor: 'bg-slate-900/95 border-amber-500/80 text-amber-100 shadow-amber-900/20',
      iconBg: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
      barColor: 'bg-amber-500',
      defaultTitle: 'Cảnh báo',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      )
    },
    info: {
      bgColor: 'bg-slate-900/95 border-sky-500/80 text-sky-100 shadow-sky-900/20',
      iconBg: 'bg-sky-500/20 text-sky-400 border-sky-500/30',
      barColor: 'bg-sky-500',
      defaultTitle: 'Thông báo',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      )
    }
  };

  const style = config[toast.type] || config.info;

  return (
    <div
      className={`pointer-events-auto relative overflow-hidden rounded-xl border backdrop-blur-md p-4 shadow-xl transition-all duration-300 ease-out transform ${
        isExiting
          ? 'translate-x-full opacity-0 scale-95'
          : 'translate-x-0 opacity-100 scale-100 animate-slide-in-right'
      } ${style.bgColor}`}
    >
      <div className="flex items-start gap-3">
        {/* Type Icon */}
        <div className={`p-2 rounded-lg border ${style.iconBg} flex-shrink-0 mt-0.5`}>
          {style.icon}
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0 pr-2">
          <h4 className="text-sm font-semibold tracking-wide text-slate-100">
            {toast.title || style.defaultTitle}
          </h4>
          <p className="text-xs font-normal text-slate-300 mt-0.5 leading-relaxed break-words">
            {toast.message}
          </p>
        </div>

        {/* Close Button */}
        <button
          onClick={triggerClose}
          className="text-slate-400 hover:text-slate-100 hover:bg-slate-800/60 p-1 rounded-md transition-colors"
          title="Đóng thông báo"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      {/* Countdown Progress Bar */}
      <div className="absolute bottom-0 left-0 right-0 h-1 bg-slate-800/80">
        <div
          className={`h-full transition-all duration-75 ease-linear ${style.barColor}`}
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
}

export default function ToastContainer() {
  const { toasts, removeToast } = useToast();

  if (!toasts || toasts.length === 0) return null;

  return (
    <div className="fixed top-5 right-5 z-[9999] flex flex-col gap-3 max-w-sm w-full px-4 sm:px-0 pointer-events-none">
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} onRemove={removeToast} />
      ))}
    </div>
  );
}
