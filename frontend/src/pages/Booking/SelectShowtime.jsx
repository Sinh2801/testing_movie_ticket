import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import showtimeService from '../../service/showtimeService';
import movieService from '../../service/movieService';

export default function SelectShowtime() {
  const { movieId } = useParams();
  const navigate = useNavigate();

  const [movie, setMovie] = useState(null);
  const [showtimes, setShowtimes] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [movieData, showtimesData] = await Promise.all([
          movieService.getById(movieId),
          showtimeService.getByMovieId(movieId),
        ]);
        setMovie(movieData);
        setShowtimes(showtimesData);
      } catch (err) {
        console.error('Lỗi lấy danh sách suất chiếu:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [movieId]);

  if (loading) {
    return <div className="text-center py-20 text-slate-400">Đang tải suất chiếu phim...</div>;
  }

  const formatTime = (timeString) => {
    if (!timeString) return '';
    const date = new Date(timeString);
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  };

  const formatDate = (timeString) => {
    if (!timeString) return '';
    const date = new Date(timeString);
    return date.toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-12">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl md:text-3xl font-extrabold text-white">
          Chọn Suất Chiếu Phim: <span className="text-red-500">{movie?.name}</span>
        </h1>
        <p className="text-slate-400 text-sm mt-1">Vui lòng chọn suất chiếu phù hợp với lịch trình của bạn</p>
      </div>

      {showtimes.length === 0 ? (
        <div className="text-center py-16 bg-slate-800/40 border border-slate-700/60 rounded-2xl">
          <p className="text-slate-300 font-semibold">Hiện chưa có suất chiếu nào được lên lịch cho bộ phim này.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {showtimes.map((st) => (
            <div
              key={st.id}
              className="bg-slate-800 border border-slate-700 hover:border-red-500/60 rounded-2xl p-6 transition-all flex flex-col md:flex-row justify-between items-start md:items-center gap-4 shadow-lg"
            >
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-xl font-bold text-red-400">{formatTime(st.startTime)}</span>
                  <span className="text-xs bg-slate-700 px-2 py-0.5 rounded text-slate-300">
                    {formatDate(st.startTime)}
                  </span>
                </div>
                <p className="text-sm font-semibold text-slate-200">{st.cinemaName || 'CinePrime Landmark 81'}</p>
                <p className="text-xs text-slate-400">Phòng: {st.roomName || 'Phòng 01'} • Giá vé: {st.price?.toLocaleString()} VNĐ</p>
              </div>

              <button
                onClick={() => navigate(`/booking/seats/${st.id}`)}
                className="w-full md:w-auto px-6 py-2.5 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl text-sm transition-all shadow-md active:scale-95"
              >
                Chọn ghế →
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
