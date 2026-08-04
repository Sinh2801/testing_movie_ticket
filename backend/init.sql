-- Tạo Database nếu chưa có
CREATE DATABASE IF NOT EXISTS movie_ticket_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE movie_ticket_booking;

-- 1. Bảng users (Người dùng / Quản trị viên)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER' -- Enum: USER, ADMIN
);

-- 2. Bảng genres (Thể loại phim)
CREATE TABLE IF NOT EXISTS genres (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- 3. Bảng movies (Phim)
CREATE TABLE IF NOT EXISTS movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    director_name VARCHAR(255),
    actors_name VARCHAR(255),
    description TEXT,
    duration INT,
    rating DOUBLE DEFAULT 0.0,
    country VARCHAR(255),
    image_url VARCHAR(500),
    movie_status VARCHAR(50), -- Enum: COMING_SOON, NOW_SHOWING, STOPPED_SHOWING
    show_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng movie_genres (Bảng trung gian N-N giữa Movies và Genres)
CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id INT NOT NULL,
    genre_id SMALLINT NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_mg_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE,
    CONSTRAINT fk_mg_genre FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE CASCADE
);

-- 5. Bảng cinemas (Rạp chiếu phim)
CREATE TABLE IF NOT EXISTS cinemas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL
);

-- 6. Bảng rooms (Phòng chiếu)
CREATE TABLE IF NOT EXISTS rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    number_of_columns INT NOT NULL,
    number_of_rows INT NOT NULL,
    description TEXT,
    cinema_id INT,
    CONSTRAINT fk_rooms_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE CASCADE
);

-- 7. Bảng seats (Ghế ngồi sơ đồ tĩnh của phòng)
CREATE TABLE IF NOT EXISTS seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seat_name VARCHAR(50) NOT NULL,
    type VARCHAR(50) DEFAULT 'STANDARD', -- Enum: STANDARD, COUPLE, VIP
    room_id INT,
    CONSTRAINT fk_seats_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
);

-- 8. Bảng showtimes (Suất chiếu phim)
CREATE TABLE IF NOT EXISTS showtimes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    price DECIMAL(10, 2) NOT NULL,
    start_time DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    cinema_id INT,
    room_id INT,
    movie_id INT,
    CONSTRAINT fk_showtimes_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE CASCADE,
    CONSTRAINT fk_showtimes_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE,
    CONSTRAINT fk_showtimes_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE
);

-- 9. Bảng showtime_seats (Ghế theo từng suất chiếu - Trạng thái trống/đã đặt)
CREATE TABLE IF NOT EXISTS showtime_seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50) DEFAULT 'AVAILABLE', -- Enum: AVAILABLE, BOOKED, RESERVED
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    seat_id INT,
    showtime_id INT,
    CONSTRAINT fk_sts_seat FOREIGN KEY (seat_id) REFERENCES seats (id) ON DELETE CASCADE,
    CONSTRAINT fk_sts_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE CASCADE
);

-- 10. Bảng tickets (Vé xem phim / Đơn đặt vé)
CREATE TABLE IF NOT EXISTS tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(255),
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50), -- Enum: PENDING, CONFIRMED, CANCELLED
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT,
    showtime_id INT,
    CONSTRAINT fk_tickets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_tickets_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE CASCADE
);

-- 11. Bảng ticket_seats (Chi tiết ghế của từng vé đặt)
CREATE TABLE IF NOT EXISTS ticket_seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id INT,
    showtime_seat_id INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    cancelled_at DATETIME,
    CONSTRAINT fk_ts_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_ts_showtime_seat FOREIGN KEY (showtime_seat_id) REFERENCES showtime_seats (id) ON DELETE CASCADE
);

-- 12. Bảng payments (Thanh toán giao dịch)
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(255),
    ticket_id INT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    method VARCHAR(50), -- Enum: VNPAY, MOMO, CREDIT_CARD, BANK_TRANSFER, CASH
    status VARCHAR(50), -- Enum: PENDING, SUCCESS, FAILED, REFUNDED
    transaction_id VARCHAR(255),
    response_code VARCHAR(100),
    paid_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE
);
