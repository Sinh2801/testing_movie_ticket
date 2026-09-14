import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import MovieCard from '../../components/movie/MovieCard';
import movieService from '../../service/movieService';

export default function ComingSoonMovies() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = parseInt(searchParams.get('page') || '1', 10);

  const [movies, setMovies] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchMovies = async () => {
      setLoading(true);
      try {
        const response = await movieService.getComingSoon(currentPage, 20);
        // Response backend dạng PagedResponse hoặc mảng
        if (response?.content) {
          setMovies(response.content);
          setTotalPages(response.totalPages || 0);
        } else if (Array.isArray(response)) {
          setMovies(response);
          setTotalPages(1);
        }
      } catch (err) {
        console.error('Lỗi lấy danh sách phim sắp chiếu:', err);
        setMovies([]);
      } finally {
        setLoading(false);
      }
    };
    fetchMovies();
  }, [currentPage]);

  const handlePageChange = (newPage) => {
    setSearchParams({ page: newPage });
  };

  return (
    <div className="space-y-8 pb-12">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl md:text-3xl font-extrabold text-white">
          Phim Sắp Chiếu
        </h1>
        <p className="text-slate-400 text-sm mt-1">Danh sách các siêu phẩm điện ảnh chuẩn bị khởi chiếu tại MyCinema</p>
      </div>

      {loading ? (
        <div className="text-center py-20 text-slate-400">Đang tải danh sách phim...</div>
      ) : movies.length === 0 ? (
        <div className="text-center py-20 text-slate-400">Hiện chưa có phim sắp chiếu nào.</div>
      ) : (
        <>
          {/* GRID 20 PHIM SẮP CHIẾU */}
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5">
            {movies.map((movie) => (
              <MovieCard key={movie.id} movie={movie} />
            ))}
          </div>

          {/* PHÂN TRANG */}
          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 pt-6">
              <button
                disabled={currentPage <= 1}
                onClick={() => handlePageChange(currentPage - 1)}
                className="px-4 py-2 bg-slate-800 border border-slate-700 text-slate-300 rounded-lg text-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-slate-700"
              >
                Trang trước
              </button>
              <span className="text-sm font-semibold text-slate-300 px-3">
                Trang {currentPage} / {totalPages}
              </span>
              <button
                disabled={currentPage >= totalPages}
                onClick={() => handlePageChange(currentPage + 1)}
                className="px-4 py-2 bg-slate-800 border border-slate-700 text-slate-300 rounded-lg text-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-slate-700"
              >
                Trang sau
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
