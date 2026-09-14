import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/layout/Header';
import Footer from './components/layout/Footer';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';

// Movies pages
import SearchMovies from './pages/Movie/SearchMovies';
import NowShowingMovies from './pages/Movie/NowShowingMovies';
import ComingSoonMovies from './pages/Movie/ComingSoonMovies';
import MovieDetail from './pages/Movie/MovieDetail';

// Booking pages
import SelectShowtime from './pages/Booking/SelectShowtime';
import SelectSeat from './pages/Booking/SelectSeat';
import CheckoutPayment from './pages/Booking/CheckoutPayment';

// Profile page
import UserProfile from './pages/Profile/UserProfile';

// Admin pages
import AdminLayout from './pages/Admin/AdminLayout';
import AdminDashboard from './pages/Admin/AdminDashboard';
import AdminMovies from './pages/Admin/AdminMovies';
import AdminTickets from './pages/Admin/AdminTickets';
import AdminPayments from './pages/Admin/AdminPayments';
import AdminShowtimes from './pages/Admin/AdminShowtimes';
import AdminGenres from './pages/Admin/AdminGenres';
import AdminCinemas from './pages/Admin/AdminCinemas';
import AdminRooms from './pages/Admin/AdminRooms';
import AdminUsers from './pages/Admin/AdminUsers';

import ConnectionError from './pages/Error/ConnectionError';

import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import ToastContainer from './components/common/ToastContainer';
import ErrorBoundary from './components/common/ErrorBoundary';

function MainLayout({ children }) {
  return (
    <div className="flex flex-col min-h-screen bg-slate-900 text-slate-100">
      <Header />
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 lg:px-8 py-6">
        {children}
      </main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <ToastProvider>
          <ToastContainer />
          <BrowserRouter>
          <Routes>
            {/* STANDALONE ERROR PAGE */}
            <Route path="/error" element={<ConnectionError />} />

            {/* USER APP ROUTES (HAVE HEADER & FOOTER) */}
            <Route
              path="/*"
              element={
                <MainLayout>
                  <Routes>
                    <Route path="/" element={<Navigate to="/home" replace />} />
                    <Route path="/home" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    {/* Movies */}
                    <Route path="/movies/search" element={<SearchMovies />} />
                    <Route path="/movies/now-showing" element={<NowShowingMovies />} />
                    <Route path="/movies/coming-soon" element={<ComingSoonMovies />} />
                    <Route path="/movies/:id" element={<MovieDetail />} />

                    {/* Booking */}
                    <Route path="/booking/showtimes/:movieId" element={<SelectShowtime />} />
                    <Route path="/booking/seats/:showtimeId" element={<SelectSeat />} />
                    <Route path="/booking/checkout" element={<CheckoutPayment />} />

                    {/* Profile */}
                    <Route path="/profile" element={<UserProfile />} />

                    <Route path="*" element={<Navigate to="/home" replace />} />
                  </Routes>
                </MainLayout>
              }
            />

            {/* ADMIN DASHBOARD ROUTES (SEPARATE ADMIN LAYOUT) */}
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<AdminDashboard />} />
              <Route path="movies" element={<AdminMovies />} />
              <Route path="tickets" element={<AdminTickets />} />
              <Route path="payments" element={<AdminPayments />} />
              <Route path="showtimes" element={<AdminShowtimes />} />
              <Route path="genres" element={<AdminGenres />} />
              <Route path="cinemas" element={<AdminCinemas />} />
              <Route path="rooms" element={<AdminRooms />} />
              <Route path="users" element={<AdminUsers />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </ToastProvider>
    </AuthProvider>
  </ErrorBoundary>
  );
}
