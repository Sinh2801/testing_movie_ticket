import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import movieService from '../../service/movieService';
import { useToast } from '../../context/ToastContext';

export default function MovieDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDetail = async () => {
      setLoading(true);
      try {
        const data = await movieService.getById(id);
        setMovie(data);
      } catch (err) {
        console.error('Lỗi khi lấy chi tiết phim:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  if (loading) {
    return <div className="text-center py-20 text-slate-400">Đang tải thông tin chi tiết phim...</div>;
  }

  if (!movie) {
    return <div className="text-center py-20 text-red-400">Không tìm thấy phim yêu cầu!</div>;
  }

  // Kiểm tra nếu phim chưa chiếu (movieStatus === 'COMING_SOON') thì disable nút đặt vé
  const isComingSoon = movie.movieStatus === 'COMING_SOON';

  const formatGenres = (genres) => {
    if (Array.isArray(genres)) return genres.join(', ');
    return genres || 'Chưa cập nhật';
  };

  const handleBooking = () => {
    if (isComingSoon) {
      toast.info('Vé của phim này hiện chưa được mở bán! Vui lòng quay lại sau.');
      return;
    }
    navigate(`/booking/showtimes/${movie.id}`);
  };

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-12">
      {/* BANNER HEADER */}
      <div className="relative rounded-2xl overflow-hidden border border-slate-800 bg-slate-950 p-6 md:p-10 flex flex-col md:flex-row gap-8 shadow-2xl">
        <div className="w-full md:w-1/3 aspect-[2/3] max-w-[280px] rounded-xl overflow-hidden border border-slate-700 shadow-xl shrink-0 mx-auto md:mx-0">
          <img
            src={movie.imageUrl}
            alt={movie.name}
            className="w-full h-full object-cover"
          />
        </div>

        <div className="flex-1 space-y-4">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-600/90 text-white text-xs font-bold uppercase">
            <span>{movie.movieStatus === 'NOW_SHOWING' ? 'Phim Đang Chiếu' : 'Phim Sắp Chiếu'}</span>
          </div>

          <h1 className="text-3xl md:text-5xl font-extrabold text-white leading-tight">
            {movie.name}
          </h1>

          <div className="flex flex-wrap items-center gap-4 text-sm text-slate-300">
            <span className="bg-amber-400/10 text-amber-400 font-bold px-2.5 py-1 rounded border border-amber-400/30">
              ★ {movie.rating || 9.0}
            </span>
            <span>⏱ {movie.duration} phút</span>
            <span>•</span>
            <span>🌍 {movie.country}</span>
          </div>

          <div className="space-y-2 text-sm text-slate-300 pt-2 border-t border-slate-800">
            <p><strong>Thể loại:</strong> {formatGenres(movie.genres)}</p>
            <p><strong>Đạo diễn:</strong> {movie.directorName || 'Đang cập nhật'}</p>
            <p><strong>Diễn viên:</strong> {movie.actorsName || 'Đang cập nhật'}</p>
          </div>

          <p className="text-slate-300 text-sm md:text-base leading-relaxed pt-2">
            {movie.description}
          </p>

          {/* NÚT ĐẶT VÉ NGAY */}
          <div className="pt-4">
            <button
              onClick={handleBooking}
              disabled={isComingSoon}
              className={`w-full sm:w-auto px-8 py-3.5 rounded-xl font-bold text-base transition-all shadow-lg flex items-center justify-center gap-2 ${
                isComingSoon
                  ? 'bg-amber-600/90 hover:bg-amber-700 text-white shadow-amber-900/40 hover:scale-105 active:scale-95'
                  : 'bg-red-600 hover:bg-red-700 text-white shadow-red-900/40 hover:scale-105 active:scale-95'
              }`}
              title={isComingSoon ? 'Phim sắp chiếu chưa mở bán vé' : 'Bấm để chọn suất chiếu và đặt vé'}
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 002 2h14a2 2 0 002-2V7a2 2 0 00-2-2H5z" />
              </svg>
              <span>{isComingSoon ? 'Vé chưa mở bán' : 'Đặt Vé Ngay'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
