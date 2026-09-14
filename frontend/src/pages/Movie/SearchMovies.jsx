import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import MovieCard from '../../components/movie/MovieCard';
import movieService from '../../service/movieService';

export default function SearchMovies() {
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.get('q') || '';
  const currentPage = parseInt(searchParams.get('page') || '1', 10);

  const [movies, setMovies] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchSearch = async () => {
      setLoading(true);
      try {
        const result = await movieService.filter(query, currentPage, 20);
        setMovies(result.content || []);
        setTotalPages(result.totalPages || 0);
      } catch (err) {
        console.error('Lỗi tìm kiếm phim:', err);
        setMovies([]);
        setTotalPages(0);
      } finally {
        setLoading(false);
      }
    };
    fetchSearch();
  }, [query, currentPage]);

  const handlePageChange = (newPage) => {
    setSearchParams({ q: query, page: newPage });
  };

  return (
    <div className="space-y-8 pb-12">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl md:text-3xl font-extrabold text-white">
          Kết quả tìm kiếm cho: <span className="text-red-500">"{query}"</span>
        </h1>
      </div>

      {loading ? (
        <div className="text-center py-20 text-slate-400">Đang tìm kiếm phim...</div>
      ) : movies.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[50vh] text-center space-y-3">
          <div className="w-16 h-16 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-400 text-2xl">
            🔍
          </div>
          <h2 className="text-xl font-bold text-slate-200">Không tìm thấy phim phù hợp</h2>
          <p className="text-slate-400 text-sm max-w-sm">
            Rất tiếc, chúng tôi không tìm thấy bộ phim nào matching với từ khóa "{query}". Vui lòng thử từ khóa khác.
          </p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5">
            {movies.map((movie) => (
              <MovieCard key={movie.id} movie={movie} />
            ))}
          </div>

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
