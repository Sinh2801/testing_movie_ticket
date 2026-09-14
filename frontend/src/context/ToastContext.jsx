import React, { createContext, useContext, useState, useCallback } from 'react';

// Configurable constants
export const MAX_TOASTS = 3; // Change this variable to adjust max toasts on screen
export const DEFAULT_DURATION = 5000; // 5 seconds auto-dismiss

const ToastContext = createContext(null);

// Web Audio API subtle notification sound generator
const playNotificationSound = (type = 'success') => {
  try {
    const AudioContextClass = window.AudioContext || window.webkitAudioContext;
    if (!AudioContextClass) return;
    const ctx = new AudioContextClass();
    
    // Check if audio context is allowed to play
    if (ctx.state === 'suspended') {
      ctx.resume();
    }

    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.connect(gain);
    gain.connect(ctx.destination);

    const now = ctx.currentTime;
    if (type === 'error') {
      // Gentle warning buzz
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(320, now);
      osc.frequency.exponentialRampToValueAtTime(180, now + 0.18);
      gain.gain.setValueAtTime(0.12, now);
      gain.gain.exponentialRampToValueAtTime(0.01, now + 0.18);
      osc.start(now);
      osc.stop(now + 0.18);
    } else {
      // Pleasant double chime (D5 -> A5)
      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, now); // D5
      osc.frequency.setValueAtTime(880, now + 0.08); // A5
      gain.gain.setValueAtTime(0.08, now);
      gain.gain.exponentialRampToValueAtTime(0.01, now + 0.22);
      osc.start(now);
      osc.stop(now + 0.22);
    }
  } catch (e) {
    // Ignore audio policy errors if user hasn't interacted yet
  }
};

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const addToast = useCallback((message, type = 'info', title = null) => {
    const id = Date.now() + Math.random().toString(36).substring(2, 9);
    playNotificationSound(type);

    setToasts((prev) => {
      const newToast = { id, message, type, title, duration: DEFAULT_DURATION };
      // Limit array size to MAX_TOASTS (keep newest)
      const updated = [...prev, newToast];
      if (updated.length > MAX_TOASTS) {
        return updated.slice(updated.length - MAX_TOASTS);
      }
      return updated;
    });

    return id;
  }, []);

  const toast = {
    success: (msg, title = 'Thành công') => addToast(msg, 'success', title),
    error: (msg, title = 'Lỗi') => addToast(msg, 'error', title),
    warning: (msg, title = 'Cảnh báo') => addToast(msg, 'warning', title),
    info: (msg, title = 'Thông báo') => addToast(msg, 'info', title),
    remove: removeToast
  };

  return (
    <ToastContext.Provider value={{ toasts, toast, removeToast }}>
      {children}
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}
