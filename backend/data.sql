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

-- 6. Thêm dữ liệu mẫu Ghế ngồi (seats) cho Phòng 01 (hàng A, B, C...)
INSERT INTO seats (seat_name, type, room_id) VALUES 
('A1', 'STANDARD', 1), ('A2', 'STANDARD', 1), ('A3', 'STANDARD', 1), ('A4', 'STANDARD', 1),
('B1', 'VIP', 1),      ('B2', 'VIP', 1),      ('B3', 'VIP', 1),      ('B4', 'VIP', 1),
('C1', 'COUPLE', 1),   ('C2', 'COUPLE', 1);

-- bây giờ 