-- 1. Thêm dữ liệu mẫu Thể loại (genres)
INSERT INTO genres (id, name) VALUES 
(1, 'Hành Động'),
(2, 'Viễn Tưởng'),
(3, 'Tình Cảm'),
(4, 'Kinh Dị'),
(5, 'Hài Hước'),
(6, 'Hoạt Hình'),
(7, 'Tâm Lý');

-- 2. Thêm dữ liệu mẫu Phim (movies)
INSERT INTO movies (id, name, director_name, actors_name, description, duration, rating, country, image_url, movie_status, show_at, created_at) VALUES 
(1, 'Avatar: Dòng Nước', 'James Cameron', 'Sam Worthington, Zoe Saldaña', 'Lấy bối cảnh hơn một thập kỷ sau các sự kiện của phần phim đầu tiên.', 192, 8.8, 'Mỹ', 'https://images.unsplash.com/photo-1536440136628-849c177e76a1', 'NOW_SHOWING', '2026-08-01 10:00:00', NOW()),
(2, 'Oppenheimer', 'Christopher Nolan', 'Cillian Murphy, Emily Blunt', 'Câu chuyện về nhà vật lý lý thuyết J. Robert Oppenheimer.', 180, 8.9, 'Mỹ', 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1', 'NOW_SHOWING', '2026-08-02 14:00:00', NOW()),
(3, 'Kẻ Trộm Mặt Trăng 4', 'Chris Renaud', 'Steve Carell, Kristen Wiig', 'Gru và các Minion trở lại trong cuộc phiêu lưu mới.', 94, 8.2, 'Mỹ', 'https://images.unsplash.com/photo-1563089145-599997674d42', 'COMING_SOON', '2026-08-15 09:00:00', NOW());

-- 3. Gán thể loại cho phim (movie_genres)
INSERT INTO movie_genres (movie_id, genre_id) VALUES 
(1, 1), (1, 2), -- Avatar: Hành Động + Viễn Tưởng
(2, 7),         -- Oppenheimer: Tâm Lý
(3, 5), (3, 6); -- Kẻ Trộm Mặt Trăng 4: Hài Hước + Hoạt Hình

-- 4. Thêm dữ liệu mẫu Rạp chiếu (cinemas)
INSERT INTO cinemas (id, name, address) VALUES 
(1, 'CinePrime Vincom Landmark 81', '720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP.HCM'),
(2, 'CinePrime Paragon Quận 7', '03 Nguyễn Lương Bằng, Tân Phú, Quận 7, TP.HCM');

-- 5. Thêm dữ liệu mẫu Phòng chiếu (rooms)
INSERT INTO rooms (id, name, number_of_columns, number_of_rows, description, cinema_id) VALUES 
(1, 'Phòng 01 (IMAX 3D)', 8, 6, 'Phòng chiếu chuẩn IMAX cao cấp màn hình cực lớn', 1),
(2, 'Phòng 02 (2D Atmos)', 8, 6, 'Hệ thống âm thanh vòm Dolby Atmos chân thực', 1),
(3, 'Phòng VIP 01', 6, 4, 'Phòng chiếu ghế bọc da sang trọng đẳng cấp', 2);

-- 6. Thêm dữ liệu Ghế đầy đủ (seats) cho Phòng 01 (6 hàng x 8 cột = 48 ghế)
INSERT INTO seats (seat_name, type, room_id) VALUES 
('A1', 'STANDARD', 1), ('A2', 'STANDARD', 1), ('A3', 'STANDARD', 1), ('A4', 'STANDARD', 1), ('A5', 'STANDARD', 1), ('A6', 'STANDARD', 1), ('A7', 'STANDARD', 1), ('A8', 'STANDARD', 1),
('B1', 'STANDARD', 1), ('B2', 'STANDARD', 1), ('B3', 'STANDARD', 1), ('B4', 'STANDARD', 1), ('B5', 'STANDARD', 1), ('B6', 'STANDARD', 1), ('B7', 'STANDARD', 1), ('B8', 'STANDARD', 1),
('C1', 'VIP', 1),      ('C2', 'VIP', 1),      ('C3', 'VIP', 1),      ('C4', 'VIP', 1),      ('C5', 'VIP', 1),      ('C6', 'VIP', 1),      ('C7', 'VIP', 1),      ('C8', 'VIP', 1),
('D1', 'VIP', 1),      ('D2', 'VIP', 1),      ('D3', 'VIP', 1),      ('D4', 'VIP', 1),      ('D5', 'VIP', 1),      ('D6', 'VIP', 1),      ('D7', 'VIP', 1),      ('D8', 'VIP', 1),
('E1', 'VIP', 1),      ('E2', 'VIP', 1),      ('E3', 'VIP', 1),      ('E4', 'VIP', 1),      ('E5', 'VIP', 1),      ('E6', 'VIP', 1),      ('E7', 'VIP', 1),      ('E8', 'VIP', 1),
('F1', 'COUPLE', 1),   ('F2', 'COUPLE', 1),   ('F3', 'COUPLE', 1),   ('F4', 'COUPLE', 1),   ('F5', 'COUPLE', 1),   ('F6', 'COUPLE', 1),   ('F7', 'COUPLE', 1),   ('F8', 'COUPLE', 1);

-- 7. Thêm dữ liệu Ghế đầy đủ (seats) cho Phòng 02 (6 hàng x 8 cột = 48 ghế)
INSERT INTO seats (seat_name, type, room_id) VALUES 
('A1', 'STANDARD', 2), ('A2', 'STANDARD', 2), ('A3', 'STANDARD', 2), ('A4', 'STANDARD', 2), ('A5', 'STANDARD', 2), ('A6', 'STANDARD', 2), ('A7', 'STANDARD', 2), ('A8', 'STANDARD', 2),
('B1', 'STANDARD', 2), ('B2', 'STANDARD', 2), ('B3', 'STANDARD', 2), ('B4', 'STANDARD', 2), ('B5', 'STANDARD', 2), ('B6', 'STANDARD', 2), ('B7', 'STANDARD', 2), ('B8', 'STANDARD', 2),
('C1', 'VIP', 2),      ('C2', 'VIP', 2),      ('C3', 'VIP', 2),      ('C4', 'VIP', 2),      ('C5', 'VIP', 2),      ('C6', 'VIP', 2),      ('C7', 'VIP', 2),      ('C8', 'VIP', 2),
('D1', 'VIP', 2),      ('D2', 'VIP', 2),      ('D3', 'VIP', 2),      ('D4', 'VIP', 2),      ('D5', 'VIP', 2),      ('D6', 'VIP', 2),      ('D7', 'VIP', 2),      ('D8', 'VIP', 2),
('E1', 'VIP', 2),      ('E2', 'VIP', 2),      ('E3', 'VIP', 2),      ('E4', 'VIP', 2),      ('E5', 'VIP', 2),      ('E6', 'VIP', 2),      ('E7', 'VIP', 2),      ('E8', 'VIP', 2),
('F1', 'COUPLE', 2),   ('F2', 'COUPLE', 2),   ('F3', 'COUPLE', 2),   ('F4', 'COUPLE', 2),   ('F5', 'COUPLE', 2),   ('F6', 'COUPLE', 2),   ('F7', 'COUPLE', 2),   ('F8', 'COUPLE', 2);

-- 8. Thêm dữ liệu Ghế đầy đủ (seats) cho Phòng 03 (4 hàng x 6 cột = 24 ghế)
INSERT INTO seats (seat_name, type, room_id) VALUES 
('A1', 'VIP', 3), ('A2', 'VIP', 3), ('A3', 'VIP', 3), ('A4', 'VIP', 3), ('A5', 'VIP', 3), ('A6', 'VIP', 3),
('B1', 'VIP', 3), ('B2', 'VIP', 3), ('B3', 'VIP', 3), ('B4', 'VIP', 3), ('B5', 'VIP', 3), ('B6', 'VIP', 3),
('C1', 'VIP', 3), ('C2', 'VIP', 3), ('C3', 'VIP', 3), ('C4', 'VIP', 3), ('C5', 'VIP', 3), ('C6', 'VIP', 3),
('D1', 'COUPLE', 3), ('D2', 'COUPLE', 3), ('D3', 'COUPLE', 3), ('D4', 'COUPLE', 3), ('D5', 'COUPLE', 3), ('D6', 'COUPLE', 3);

-- 9. Thêm dữ liệu Suất chiếu mẫu (showtimes)
INSERT INTO showtimes (id, price, start_time, created_at, cinema_id, room_id, movie_id) VALUES 
(1, 90000.00, '2026-08-06 18:30:00', NOW(), 1, 1, 1),
(2, 90000.00, '2026-08-06 21:00:00', NOW(), 1, 2, 1),
(3, 110000.00, '2026-08-06 19:30:00', NOW(), 1, 1, 2),
(4, 120000.00, '2026-08-06 20:00:00', NOW(), 2, 3, 2);

-- 10. Thêm dữ liệu Trạng thái ghế cho Suất chiếu 1 (showtime_seats)
INSERT INTO showtime_seats (showtime_id, seat_id, status, created_at, updated_at)
SELECT 1, s.id, 'AVAILABLE', NOW(), NOW() FROM seats s WHERE s.room_id = 1;

-- 11. Thêm dữ liệu Trạng thái ghế cho Suất chiếu 2 (showtime_seats)
INSERT INTO showtime_seats (showtime_id, seat_id, status, created_at, updated_at)
SELECT 2, s.id, 'AVAILABLE', NOW(), NOW() FROM seats s WHERE s.room_id = 2;

-- 12. Thêm dữ liệu Trạng thái ghế cho Suất chiếu 3 (showtime_seats)
INSERT INTO showtime_seats (showtime_id, seat_id, status, created_at, updated_at)
SELECT 3, s.id, 'AVAILABLE', NOW(), NOW() FROM seats s WHERE s.room_id = 1;

-- 13. Thêm dữ liệu Trạng thái ghế cho Suất chiếu 4 (showtime_seats)
INSERT INTO showtime_seats (showtime_id, seat_id, status, created_at, updated_at)
SELECT 4, s.id, 'AVAILABLE', NOW(), NOW() FROM seats s WHERE s.room_id = 3;