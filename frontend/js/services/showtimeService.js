// Showtime Service for API integration
const ShowtimeService = {
    async createShowtime(data) {
        const token = localStorage.getItem('cine_token');
        const response = await fetch(`${CONFIG.API_BASE_URL}/showtimes/add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });
        return await response.json();
    },

    async filterShowtimes(cinemaId = null, date = null, movieName = null) {
        let url = `${CONFIG.API_BASE_URL}/showtimes/filter?`;
        if (cinemaId) url += `cinemaId=${cinemaId}&`;
        if (date) url += `date=${date}&`;
        if (movieName) url += `movieName=${encodeURIComponent(movieName)}&`;

        const response = await fetch(url);
        return await response.json();
    },

    async getShowtimesByMovieId(movieId) {
        const response = await fetch(`${CONFIG.API_BASE_URL}/showtimes/movie/${movieId}`);
        return await response.json();
    },

    async getShowtimeDetail(id) {
        const response = await fetch(`${CONFIG.API_BASE_URL}/showtimes/${id}`);
        return await response.json();
    },

    async getShowtimeSeats(showtimeId) {
        const response = await fetch(`${CONFIG.API_BASE_URL}/showtimes/${showtimeId}/seats`);
        return await response.json();
    }
};
