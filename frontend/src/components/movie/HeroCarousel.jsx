import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export default function HeroCarousel({ comingSoonMovies = [] }) {
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);

  const movies = comingSoonMovies.length > 0 ? comingSoonMovies : [];

  // Tự động chuyển slide sau 5 giây
  useEffect(() => {
    if (movies.length === 0) return;
    const timer = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % movies.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [movies.length]);

  // Lắng nghe sự kiện bàn phím (Mũi tên Trái / Phải)
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (movies.length === 0) return;
      if (e.key === 'ArrowLeft') {
        handlePrev();
      } else if (e.key === 'ArrowRight') {
        handleNext();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [movies.length]);

  if (movies.length === 0) {
    return (
      <div className="w-full h-[300px] bg-slate-950 rounded-2xl flex items-center justify-center text-slate-400">
        Đang tải danh sách phim sắp chiếu...
      </div>
    );
  }

  const handlePrev = () => {
    setCurrentIndex((prev) => (prev === 0 ? movies.length - 1 : prev - 1));
  };

  const handleNext = () => {
    setCurrentIndex((prev) => (prev + 1) % movies.length);
  };

  const currentMovie = movies[currentIndex] || movies[0];

  const handleDetail = () => {
    if (currentMovie?.id) {
      navigate(`/movies/${currentMovie.id}`);
    }
  };

  // Format danh sách thể loại từ List<String> của Backend MovieResponse
  const formatGenres = (genres) => {
    if (Array.isArray(genres)) return genres.join(', ');
    return genres || 'Đang cập nhật';
  };

  // Format ngày khởi chiếu
  const formatDate = (dateString) => {
    if (!dateString) return 'Sắp ra mắt';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('vi-VN');
    } catch {
      return dateString;
    }
  };

  return (
    <section className="relative w-full h-[480px] md:h-[560px] bg-slate-950 overflow-hidden rounded-2xl border border-slate-800 shadow-2xl my-6">
      {/* BACKGROUND IMAGE BANNERS */}
      {movies.map((movie, index) => (
        <div
          key={movie.id || index}
          className={`absolute inset-0 transition-opacity duration-1000 ease-in-out ${
            index === currentIndex ? 'opacity-100 z-10' : 'opacity-0 z-0 pointer-events-none'
          }`}
        >
          <img
            src={movie.imageUrl}
            alt={movie.name}
            className="w-full h-full object-cover object-center"
          />
          {/* Dark Overlay Gradient */}
          <div className="absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-950/80 to-transparent" />
          <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-transparent" />
        </div>
      ))}

      {/* BANNER CONTENT */}
      <div className="relative z-20 h-full max-w-7xl mx-auto px-12 md:px-20 flex flex-col justify-center">
        <div className="max-w-2xl space-y-4">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-600/90 text-white text-xs font-bold uppercase tracking-wider shadow-lg">
            <span>PHIM SẮP CHIẾU NỔI BẬT</span>
          </div>

          <h1 className="text-3xl md:text-5xl font-extrabold text-white leading-tight drop-shadow-md cursor-pointer hover:text-red-400 transition-colors" onClick={handleDetail}>
            {currentMovie?.name}
          </h1>

          <div className="flex flex-wrap items-center gap-4 text-sm text-slate-300">
            <span className="bg-slate-800/80 px-2.5 py-1 rounded border border-slate-700 font-semibold text-amber-400">
              ★ {currentMovie?.rating || 9.0}
            </span>
            <span>{formatGenres(currentMovie?.genres)}</span>
            <span>•</span>
            <span>{currentMovie?.duration} phút</span>
            <span>•</span>
            <span className="text-red-400 font-medium">Khởi chiếu: {formatDate(currentMovie?.showAt)}</span>
          </div>

          <p className="text-slate-300 text-sm md:text-base line-clamp-3 leading-relaxed">
            {currentMovie?.description}
          </p>

          <div className="pt-2 flex items-center gap-4">
            <button onClick={handleDetail} className="bg-red-600 hover:bg-red-700 text-white font-bold px-6 py-3 rounded-xl transition-all shadow-lg shadow-red-900/40 hover:scale-105 active:scale-95 flex items-center gap-2">
              <svg className="w-5 h-5 fill-current" viewBox="0 0 24 24">
                <path d="M8 5v14l11-7z" />
              </svg>
              <span>Xem Chi Tiết</span>
            </button>
          </div>
        </div>
      </div>

      {/* ARROW BUTTONS - THU NHỎ SIZE VÀ ĐỐI ỨNG CÂN ĐỐI */}
      <button
        onClick={handlePrev}
        className="absolute left-3 top-1/2 -translate-y-1/2 z-30 w-9 h-9 rounded-full bg-slate-900/70 border border-slate-700 hover:bg-red-600 hover:border-red-600 text-white flex items-center justify-center backdrop-blur transition-all shadow-lg focus:outline-none"
        title="Phim trước (Mũi tên Trái)"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M15 19l-7-7 7-7" />
        </svg>
      </button>

      <button
        onClick={handleNext}
        className="absolute right-3 top-1/2 -translate-y-1/2 z-30 w-9 h-9 rounded-full bg-slate-900/70 border border-slate-700 hover:bg-red-600 hover:border-red-600 text-white flex items-center justify-center backdrop-blur transition-all shadow-lg focus:outline-none"
        title="Phim tiếp theo (Mũi tên Phải)"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M9 5l7 7-7 7" />
        </svg>
      </button>

      {/* DOTS INDICATOR */}
      <div className="absolute bottom-6 right-8 z-30 flex items-center gap-2">
        {movies.map((_, idx) => (
          <button
            key={idx}
            onClick={() => setCurrentIndex(idx)}
            className={`h-2.5 rounded-full transition-all duration-300 ${
              idx === currentIndex ? 'w-8 bg-red-600' : 'w-2.5 bg-slate-600 hover:bg-slate-400'
            }`}
          />
        ))}
      </div>
    </section>
  );
}
