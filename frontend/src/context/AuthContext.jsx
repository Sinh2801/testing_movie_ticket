import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  // Lấy dữ liệu khởi tạo từ localStorage
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const [token, setToken] = useState(() => {
    return localStorage.getItem('token') || null;
  });

  // Đồng bộ Auth state với localStorage khi login/register thành công
  const saveAuthData = (authResponse) => {
    // authResponse từ Backend Spring Boot: { token, name, email, role }
    const { token: jwtToken, name, email, role } = authResponse;

    const userData = {
      name,
      email,
      avatar: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTUgzjz5sJNK5nOc9SE-dhaGlLnPZlSsEhj-7Ry3ZSElDAbbNLqJXWZr5s&s=10',
      role: role || 'USER',
    };

    // Lưu vào localStorage
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(userData));

    // Cập nhật State
    setToken(jwtToken);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, saveAuthData, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
