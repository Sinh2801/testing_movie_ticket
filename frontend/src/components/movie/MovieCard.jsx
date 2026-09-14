import React from 'react';
import { useNavigate } from 'react-router-dom';

export default function MovieCard({ movie }) {
  const navigate = useNavigate();

  // Format thể loại từ mảng List<String> hoặc chuỗi
  const formatGenres = (genres) => {
    if (Array.isArray(genres)) return genres.join(', ');
    return genres || 'Đang cập nhật';
  };

  const handleBooking = (e) => {
    e.stopPropagation();
    if (movie?.id) {
      navigate(`/booking/showtimes/${movie.id}`);
    }
  };

  const handleDetail = (e) => {
    e.stopPropagation();
    if (movie?.id) {
      navigate(`/movies/${movie.id}`);
    }
  };

  return (
    <div
      onClick={handleDetail}
      className="group bg-slate-800 border border-slate-700/80 rounded-xl overflow-hidden shadow-lg hover:shadow-red-950/40 hover:border-slate-600 transition-all duration-300 flex flex-col h-full hover:-translate-y-1 cursor-pointer"
    >
      {/* POSTER IMAGE CONTAINER */}
      <div className="relative aspect-[2/3] w-full overflow-hidden bg-slate-900">
        <img
          src={movie?.imageUrl || movie?.posterUrl || 'https://via.placeholder.com/300x450?text=No+Poster'}
          alt={movie?.name || movie?.title}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          loading="lazy"
        />

        {/* AGE RATING BADGE */}
        <div className="absolute top-3 left-3 bg-red-600/90 text-white font-extrabold text-[11px] px-2 py-0.5 rounded shadow">
          {movie?.ageRating || 'P'}
        </div>

        {/* RATING BADGE */}
        <div className="absolute top-3 right-3 bg-slate-950/80 backdrop-blur text-amber-400 font-bold text-xs px-2 py-1 rounded-md border border-slate-700/60 flex items-center gap-1">
          <span>★</span>
          <span>{movie?.rating || 9.0}</span>
        </div>

        {/* HOVER OVERLAY BUTTON */}
        <div className="absolute inset-0 bg-slate-950/70 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex flex-col items-center justify-center gap-3 p-4">
          <button
            onClick={handleBooking}
            className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-2.5 rounded-lg transition-colors shadow-lg shadow-red-900/40 text-sm"
          >
            Đặt vé ngay
          </button>
          <button
            onClick={handleDetail}
            className="w-full bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium py-2 rounded-lg transition-colors border border-slate-600 text-xs"
          >
            Xem chi tiết
          </button>
        </div>
      </div>

      {/* MOVIE INFO */}
      <div className="p-4 flex flex-col flex-1 justify-between gap-2">
        <div>
          <h3 className="text-base font-bold text-white group-hover:text-red-400 transition-colors line-clamp-1">
            {movie?.name || movie?.title}
          </h3>
          <p className="text-xs text-slate-400 mt-1 line-clamp-1">{formatGenres(movie?.genres || movie?.genre)}</p>
        </div>

        <div className="flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-slate-700/50">
          <span className="flex items-center gap-1">
            <svg className="w-3.5 h-3.5 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            {movie?.duration || 120} phút
          </span>
          <span onClick={handleBooking} className="text-red-500 font-semibold group-hover:underline">
            Đặt vé →
          </span>
        </div>
      </div>
    </div>
  );
}

