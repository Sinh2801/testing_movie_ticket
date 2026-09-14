import React, { useState, useEffect } from 'react';
import HeroCarousel from '../../components/movie/HeroCarousel';
import MovieCard from '../../components/movie/MovieCard';
import movieService from '../../service/movieService';

export default function Home() {
  const [nowShowingMovies, setNowShowingMovies] = useState([]);
  const [comingSoonMovies, setComingSoonMovies] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMovies = async () => {
      try {
        setLoading(true);
        // Gọi API từ backend
        const [nowShowingData, comingSoonData] = await Promise.all([
          movieService.getNowShowing(1, 10),
          movieService.getComingSoon(1, 3)
        ]);

        setNowShowingMovies(Array.isArray(nowShowingData) ? nowShowingData : []);
        setComingSoonMovies(Array.isArray(comingSoonData) ? comingSoonData : []);
      } catch (error) {
        console.error('Lỗi lấy danh sách phim:', error);
        setNowShowingMovies([]);
        setComingSoonMovies([]);
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
  }, []);

  return (
    <div className="space-y-10 pb-12">
      {/* SECTION 1: SLIDER 3 PHIM SẮP CHIẾU */}
      <HeroCarousel comingSoonMovies={comingSoonMovies} />

      {/* SECTION 2: 10 PHIM ĐANG CHIẾU */}
      <section className="space-y-6">
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <div className="flex items-center gap-3">
            <div className="w-2 h-7 bg-red-600 rounded-full" />
            <h2 className="text-2xl md:text-3xl font-extrabold text-white tracking-wide">
              Phim Đang Chiếu
            </h2>
          </div>

          <a href="#" className="text-sm font-semibold text-slate-400 hover:text-red-500 transition-colors flex items-center gap-1">
            <span>Xem tất cả</span>
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" />
            </svg>
          </a>
        </div>

        {/* GRID 10 PHIM */}
        {loading ? (
          <div className="text-center py-12 text-slate-400 text-sm">Đang tải danh sách phim...</div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5">
            {nowShowingMovies.map((movie) => (
              <MovieCard key={movie.id || movie.title} movie={movie} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
