import React, { useState, useEffect } from 'react';
import movieService from '../../service/movieService';
import ticketService from '../../service/ticketService';
import showtimeService from '../../service/showtimeService';
import genreService from '../../service/genreService';

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    totalMovies: 0,
    totalTickets: 0,
    totalShowtimes: 0,
    totalGenres: 0,
  });

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [moviesRes, ticketsRes, showtimesRes, genresRes] = await Promise.all([
          movieService.filter('', 1, 1),
          ticketService.filter('', 1, 1),
          showtimeService.filter(1, 1),
          genreService.getAll(),
        ]);
        setStats({
          totalMovies: moviesRes?.totalElements || 0,
          totalTickets: ticketsRes?.totalElements || 0,
          totalShowtimes: showtimesRes?.totalElements || 0,
          totalGenres: Array.isArray(genresRes) ? genresRes.length : 0,
        });
      } catch (err) {
        console.error('Lỗi lấy thống kê dashboard:', err);
      }
    };
    fetchStats();
  }, []);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold text-white">Admin Dashboard</h1>
        <p className="text-slate-400 text-sm mt-1">Tổng quan các thông số hệ thống MyCinema</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-2 shadow-xl">
          <p className="text-xs text-slate-400 font-semibold uppercase">Tổng Phim</p>
          <p className="text-3xl font-extrabold text-red-500">{stats.totalMovies}</p>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-2 shadow-xl">
          <p className="text-xs text-slate-400 font-semibold uppercase">Tổng Vé Đã Đặt</p>
          <p className="text-3xl font-extrabold text-amber-400">{stats.totalTickets}</p>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-2 shadow-xl">
          <p className="text-xs text-slate-400 font-semibold uppercase">Suất Chiếu</p>
          <p className="text-3xl font-extrabold text-emerald-400">{stats.totalShowtimes}</p>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-2 shadow-xl">
          <p className="text-xs text-slate-400 font-semibold uppercase">Thể Loại</p>
          <p className="text-3xl font-extrabold text-blue-400">{stats.totalGenres}</p>
        </div>
      </div>
    </div>
  );
}
