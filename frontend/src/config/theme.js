/**
 * Design System Tokens & Style Configuration for MyCinema
 * File lưu định nghĩa màu sắc, kích thước, typography và spacing chuẩn dùng cho dự án.
 */

export const THEME_CONFIG = {
  colors: {
    primary: '#E50914',         // Màu đỏ thương hiệu rạp phim (Cinema Red)
    primaryHover: '#B80710',    // Màu đỏ khi hover
    accent: '#F5C518',          // Màu vàng đánh giá sao & điểm nhấn (Gold)
    background: '#0F172A',      // Màu nền tối chính (Slate 900)
    cardBg: '#1E293B',          // Màu nền thẻ/card (Slate 800)
    cardBorder: '#334155',      // Đường viền thẻ (Slate 700)
    textPrimary: '#F8FAFC',     // Chữ chính (Slate 50)
    textSecondary: '#94A3B8',   // Chữ phụ (Slate 400)
    success: '#22C55E',         // Màu thông báo thành công
    error: '#EF4444',           // Màu thông báo lỗi
  },
  font: {
    family: "'Inter', sans-serif",
    sizes: {
      xs: '0.75rem',    // 12px
      sm: '0.875rem',   // 14px
      base: '1rem',     // 16px
      lg: '1.125rem',   // 18px
      xl: '1.25rem',    // 20px
      '2xl': '1.5rem',   // 24px
      '3xl': '1.875rem', // 30px
      '4xl': '2.25rem',  // 36px
    },
    weights: {
      regular: 400,
      medium: 500,
      semibold: 600,
      bold: 700,
      extrabold: 800,
    }
  },
  borderRadius: {
    sm: '0.375rem',   // 6px
    md: '0.5rem',      // 8px
    lg: '0.75rem',     // 12px
    xl: '1rem',        // 16px
    full: '9999px',
  },
  shadows: {
    card: '0 10px 15px -3px rgba(0, 0, 0, 0.3), 0 4px 6px -2px rgba(0, 0, 0, 0.15)',
    glowRed: '0 0 20px rgba(229, 9, 20, 0.4)',
  }
};

export const ROUTES = {
  HOME: '/home',
  LOGIN: '/login',
  REGISTER: '/register',
  PROFILE: '/profile',
  ADMIN: '/admin',
};
