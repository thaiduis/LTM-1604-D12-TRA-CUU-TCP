-- Tạo database cho ứng dụng từ điển Anh-Việt
CREATE DATABASE IF NOT EXISTS dictionary_db;
USE dictionary_db;

-- Tạo bảng từ điển với thông tin chi tiết
CREATE TABLE IF NOT EXISTS dictionary (
    id INT AUTO_INCREMENT PRIMARY KEY,
    english_word VARCHAR(255) NOT NULL,
    part_of_speech VARCHAR(50) NOT NULL, -- Từ loại (noun, verb, adjective, etc.)
    phonetic_spelling VARCHAR(100), -- Phiên âm
    vietnamese_meaning TEXT NOT NULL,
    detailed_definition TEXT NOT NULL, -- Giải thích chi tiết bằng tiếng Việt
    example_sentence TEXT, -- Câu ví dụ
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY word_pos_idx (english_word, part_of_speech) -- Cho phép cùng một từ có nhiều từ loại khác nhau
);

-- Tạo index để tăng tốc độ tìm kiếm
CREATE INDEX idx_english_word ON dictionary(english_word);

-- Thêm dữ liệu mẫu với thông tin chi tiết
INSERT INTO dictionary (english_word, part_of_speech, phonetic_spelling, vietnamese_meaning, detailed_definition, example_sentence) VALUES 
('hello', 'exclamation', '/həˈləʊ/', 'xin chào', 'Từ dùng để chào hỏi khi gặp ai đó hoặc bắt đầu cuộc nói chuyện', 'Hello, how are you today?'),
('book', 'noun', '/bʊk/', 'sách', 'Một tập hợp các trang được in và đóng thành quyển, thường chứa văn bản hoặc hình ảnh', 'I love reading books in my free time.'),
('book', 'verb', '/bʊk/', 'đặt chỗ, đặt vé', 'Hành động đăng ký trước để sử dụng dịch vụ hoặc mua vé', 'I need to book a flight ticket for tomorrow.'),
('run', 'verb', '/rʌn/', 'chạy', 'Di chuyển nhanh bằng cách dùng hai chân, với tốc độ nhanh hơn đi bộ', 'He runs five kilometers every morning.'),
('run', 'noun', '/rʌn/', 'cuộc chạy, lần chạy', 'Một lần hoặc một quãng đường chạy', 'I went for a quick run this morning.'),
('beautiful', 'adjective', '/ˈbjuːtɪfl/', 'đẹp', 'Có vẻ đẹp về mặt thẩm mỹ, làm hài lòng các giác quan hoặc tâm trí', 'She has a beautiful smile.'),
('love', 'noun', '/lʌv/', 'tình yêu', 'Cảm xúc mãnh liệt và sâu sắc của tình cảm và sự gắn bó', 'Their love for each other is very strong.'),
('love', 'verb', '/lʌv/', 'yêu', 'Cảm thấy tình yêu hoặc tình cảm sâu sắc với ai đó hoặc điều gì đó', 'I love spending time with my family.'),
('fast', 'adjective', '/fɑːst/', 'nhanh', 'Di chuyển hoặc có khả năng di chuyển với tốc độ cao', 'This is a very fast car.'),
('fast', 'adverb', '/fɑːst/', 'nhanh chóng', 'Với tốc độ cao hoặc trong thời gian ngắn', 'He runs very fast.');
