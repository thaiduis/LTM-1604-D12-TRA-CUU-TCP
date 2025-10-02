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

CREATE INDEX idx_english_word ON dictionary(english_word);
ALTER TABLE dictionary ADD COLUMN image_path TEXT;

INSERT INTO dictionary (english_word, part_of_speech, phonetic_spelling, vietnamese_meaning, detailed_definition, example_sentence) 
VALUES
('hello', 'exclamation', '/həˈləʊ/', 'xin chào', 'Từ dùng để chào hỏi khi gặp ai đó hoặc bắt đầu cuộc nói chuyện', 'Hello, how are you today?'),
('book', 'noun', '/bʊk/', 'sách', 'Một tập hợp các trang được in và đóng thành quyển, thường chứa văn bản hoặc hình ảnh', 'I love reading books in my free time.'),
('book', 'verb', '/bʊk/', 'đặt chỗ, đặt vé', 'Hành động đăng ký trước để sử dụng dịch vụ hoặc mua vé', 'I need to book a flight ticket for tomorrow.'),
('run', 'verb', '/rʌn/', 'chạy', 'Di chuyển nhanh bằng cách dùng hai chân, với tốc độ nhanh hơn đi bộ', 'He runs five kilometers every morning.'),
('run', 'noun', '/rʌn/', 'cuộc chạy, lần chạy', 'Một lần hoặc một quãng đường chạy', 'I went for a quick run this morning.'),
('beautiful', 'adjective', '/ˈbjuːtɪfl/', 'đẹp', 'Có vẻ đẹp về mặt thẩm mỹ, làm hài lòng các giác quan hoặc tâm trí', 'She has a beautiful smile.'),
('love', 'noun', '/lʌv/', 'tình yêu', 'Cảm xúc mãnh liệt và sâu sắc của tình cảm và sự gắn bó', 'Their love for each other is very strong.'),
('love', 'verb', '/lʌv/', 'yêu', 'Cảm thấy tình yêu hoặc tình cảm sâu sắc với ai đó hoặc điều gì đó', 'I love spending time with my family.'),
('fast', 'adjective', '/fɑːst/', 'nhanh', 'Di chuyển hoặc có khả năng di chuyển với tốc độ cao', 'This is a very fast car.'),
('loudly', 'adverb', '/ˈlaʊd.li/', 'to tiếng', 'Với âm thanh lớn', 'The children laughed loudly.'),
('fast', 'adverb', '/fɑːst/', 'nhanh chóng', 'Với tốc độ cao hoặc trong thời gian ngắn', 'He runs very fast.'),
('quickly', 'adverb', '/ˈkwɪk.li/', 'nhanh', 'Trong thời gian ngắn hoặc ít nỗ lực', 'He finished his homework quickly.'),
('happy', 'adjective', '/ˈhæp.i/', 'hạnh phúc', 'Cảm thấy vui vẻ hoặc hài lòng', 'She felt happy after receiving the gift.'),
('carefully', 'adverb', '/ˈkeə.fəli/', 'cẩn thận', 'Làm điều gì đó với sự chú ý và tránh sai sót', 'He drives carefully in the rain.'),
('strong', 'adjective', '/strɒŋ/', 'mạnh mẽ', 'Có sức mạnh về thể chất hoặc tinh thần', 'He is strong enough to lift the box.'),
('quietly', 'adverb', '/ˈkwaɪət.li/', 'yên lặng', 'Không gây ra nhiều tiếng động', 'She closed the door quietly.'),
('hot', 'adjective', '/hɒt/', 'nóng', 'Có nhiệt độ cao', 'The soup is very hot.'),
('cold', 'adjective', '/kəʊld/', 'lạnh', 'Có nhiệt độ thấp', 'It is cold outside in winter.'),
('angry', 'adjective', '/ˈæŋ.ɡri/', 'tức giận', 'Cảm thấy khó chịu, bực bội do một hành động hay tình huống nào đó', 'He was very angry when he heard the bad news.'),
('beautifully', 'adverb', '/ˈbjuː.tɪ.fli/', 'một cách đẹp đẽ', 'Theo cách gây ấn tượng, dễ chịu với mắt hoặc tai', 'She sings beautifully.'),
('jump', 'verb', '/dʒʌmp/', 'nhảy', 'Di chuyển khỏi mặt đất bằng cách dùng chân để bật lên', 'The cat jumped onto the table.'),
('jump', 'noun', '/dʒʌmp/', 'cú nhảy', 'Hành động hoặc sự kiện di chuyển đột ngột lên khỏi mặt đất', 'He made a big jump across the stream.'),
('eat', 'verb', '/iːt/', 'ăn', 'Đưa thức ăn vào miệng, nhai và nuốt', 'We usually eat lunch at noon.'),
('meal', 'noun', '/miːl/', 'bữa ăn', 'Một dịp ăn uống vào một thời điểm cụ thể trong ngày', 'Breakfast is the most important meal of the day.'),
('teacher', 'noun', '/ˈtiː.tʃər/', 'giáo viên', 'Người dạy học, đặc biệt trong trường học', 'The teacher explained the lesson clearly.'),
('student', 'noun', '/ˈstjuː.dənt/', 'học sinh, sinh viên', 'Người tham gia vào việc học tại trường hoặc khóa học', 'The students are studying for their exams.'),
('write', 'verb', '/raɪt/', 'viết', 'Sử dụng bút, chì hoặc bàn phím để ghi lại từ hoặc ký tự', 'She is writing a letter to her friend.'),
('writing', 'noun', '/ˈraɪ.tɪŋ/', 'bài viết', 'Văn bản được tạo ra bằng cách viết', 'His writing is very clear and easy to understand.'),
('friend', 'noun', '/frend/', 'bạn bè', 'Người mà mình có mối quan hệ thân thiết và tin tưởng', 'He is my best friend.'),
('friendly', 'adjective', '/ˈfrend.li/', 'thân thiện', 'Thể hiện sự tử tế và dễ gần', 'The staff were very friendly.'),
('music', 'noun', '/ˈmjuː.zɪk/', 'âm nhạc', 'Nghệ thuật kết hợp âm thanh thành giai điệu, tiết tấu', 'I love listening to classical music.'),
('musician', 'noun', '/mjuːˈzɪʃ.ən/', 'nhạc sĩ', 'Người sáng tác, trình diễn hoặc chơi nhạc cụ', 'She is a talented musician.'),
('sing', 'verb', '/sɪŋ/', 'hát', 'Phát ra giọng theo giai điệu và nhịp điệu', 'They sang a song together.'),
('song', 'noun', '/sɒŋ/', 'bài hát', 'Một tác phẩm âm nhạc có lời hát', 'This is my favorite song.'),
('happy', 'noun', '/ˈhæp.i.nəs/', 'niềm hạnh phúc', 'Trạng thái cảm xúc tích cực, niềm vui', 'Money cannot always bring happiness.'),
('work', 'verb', '/wɜːk/', 'làm việc', 'Thực hiện các nhiệm vụ để đạt kết quả hoặc nhận lương', 'She works at a bank.'),
('work', 'noun', '/wɜːk/', 'công việc', 'Hoạt động mà một người làm để kiếm sống hoặc đạt kết quả', 'He has a lot of work to do today.'),
('worker', 'noun', '/ˈwɜː.kər/', 'người lao động', 'Người tham gia vào công việc, đặc biệt là việc chân tay', 'The factory workers went on strike.'),
('open', 'verb', '/ˈəʊ.pən/', 'mở', 'Di chuyển hoặc thay đổi để cho phép tiếp cận hoặc nhìn thấy bên trong', 'She opened the window to get some fresh air.'),
('walk', 'verb', '/wɔːk/', 'đi bộ', 'Di chuyển bằng cách bước chân với tốc độ bình thường', 'They walk to school every day.'),
('airplane', 'noun', '/ˈeə.pleɪn/', 'máy bay', 'Phương tiện giao thông trên không, bay nhờ động cơ và cánh', 'The airplane is flying in the sky.'),
('bicycle', 'noun', '/ˈbaɪ.sɪ.kəl/', 'xe đạp', 'Phương tiện giao thông hai bánh chạy bằng sức người', 'He goes to school by bicycle.'),
('train', 'noun', '/treɪn/', 'tàu hỏa', 'Phương tiện giao thông chạy trên đường ray', 'The train arrived at the station on time.'),
('phone', 'noun', '/fəʊn/', 'điện thoại', 'Thiết bị dùng để gọi điện và nhắn tin', 'She is talking on the phone.'),
    ('play', 'verb', '/pleɪ/', 'chơi', 'Tham gia vào trò chơi hoặc hoạt động giải trí', 'The children are playing football in the park.'),
    ('player', 'noun', '/ˈpleɪ.ər/', 'người chơi', 'Người tham gia vào một trò chơi hoặc môn thể thao', 'He is a famous football player.'),
    ('city', 'noun', '/ˈsɪt.i/', 'thành phố', 'Khu vực đô thị lớn, nơi nhiều người sinh sống', 'Hanoi is the capital city of Vietnam.'),
    ('country', 'noun', '/ˈkʌn.tri/', 'quốc gia', 'Một vùng lãnh thổ có chính phủ riêng', 'France is a beautiful country.'),
    ('sleep', 'verb', '/sliːp/', 'ngủ', 'Trạng thái nghỉ ngơi tự nhiên của cơ thể và tâm trí', 'I usually sleep eight hours a day.'),
    ('dream', 'noun', '/driːm/', 'giấc mơ', 'Những hình ảnh, ý nghĩ hoặc cảm xúc xuất hiện khi ngủ', 'She had a strange dream last night.'),
    ('dream', 'verb', '/driːm/', 'mơ', 'Trải nghiệm giấc mơ khi đang ngủ', 'He dreamed about flying.'),
    ('school', 'noun', '/skuːl/', 'trường học', 'Nơi học sinh học tập và giáo viên giảng dạy', 'My brother goes to school by bike.'),
    ('pen', 'noun', '/pen/', 'bút', 'Dụng cụ để viết mực', 'She wrote the letter with a blue pen.'),
    ('chair', 'noun', '/tʃeər/', 'ghế', 'Đồ vật để ngồi có lưng tựa', 'He sat on the wooden chair.'),
    ('table', 'noun', '/ˈteɪ.bəl/', 'bàn', 'Đồ vật có mặt phẳng ngang và bốn chân dùng để đặt đồ', 'The books are on the table.'),
    ('light', 'noun', '/laɪt/', 'ánh sáng', 'Thứ làm cho ta nhìn thấy sự vật', 'The room was full of bright light.'),
    ('light', 'adjective', '/laɪt/', 'nhẹ', 'Không nặng, dễ nâng hoặc mang', 'This bag is very light.'),
    ('dark', 'adjective', '/dɑːk/', 'tối', 'Không có nhiều ánh sáng', 'It is too dark to see outside.'),
    ('family', 'noun', '/ˈfæm.əl.i/', 'gia đình', 'Nhóm người có quan hệ huyết thống hoặc sống chung', 'I love spending time with my family.'),
    ('mother', 'noun', '/ˈmʌð.ər/', 'mẹ', 'Người phụ nữ sinh ra hoặc nuôi dưỡng con cái', 'Her mother is a teacher.'),
    ('father', 'noun', '/ˈfɑː.ðər/', 'cha', 'Người đàn ông sinh ra hoặc nuôi dưỡng con cái', 'His father works in a bank.'),
    ('child', 'noun', '/tʃaɪld/', 'đứa trẻ', 'Người còn nhỏ tuổi, chưa trưởng thành', 'There are three children in that family.'),
    ('children', 'noun', '/ˈtʃɪl.drən/', 'trẻ em', 'Số nhiều của "child"', 'The children are playing outside.'),
    ('food', 'noun', '/fuːd/', 'thức ăn', 'Những gì con người hoặc động vật ăn để sống', 'Rice is the main food in Asia.'),
    ('drink', 'verb', '/drɪŋk/', 'uống', 'Đưa chất lỏng vào cơ thể qua miệng', 'He is drinking water.'),
    ('water', 'noun', '/ˈwɔː.tər/', 'nước', 'Chất lỏng trong suốt không màu, cần thiết cho sự sống', 'We need water to survive.'),
    ('fire', 'noun', '/faɪər/', 'lửa', 'Hiện tượng cháy sinh ra ánh sáng và nhiệt', 'The fire kept us warm.'),
    ('sky', 'noun', '/skaɪ/', 'bầu trời', 'Không gian phía trên Trái đất, nơi có mây, mặt trời, mặt trăng, và các ngôi sao', 'The sky is clear today.'),
    ('star', 'noun', '/stɑːr/', 'ngôi sao', 'Thiên thể phát sáng trên bầu trời ban đêm', 'There are many stars in the sky.'),
    ('sun', 'noun', '/sʌn/', 'mặt trời', 'Ngôi sao chiếu sáng và cung cấp năng lượng cho Trái đất', 'The sun rises in the east.'),
    ('moon', 'noun', '/muːn/', 'mặt trăng', 'Vệ tinh tự nhiên của Trái đất, chiếu sáng vào ban đêm', 'The moon looks beautiful tonight.'),
    ('dog', 'noun', '/dɒɡ/', 'chó', 'Động vật nuôi trung thành, bạn đồng hành của con người', 'The dog is barking loudly.'),
    ('cat', 'noun', '/kæt/', 'mèo', 'Động vật nuôi nhỏ, thường bắt chuột và làm thú cưng', 'The cat is sleeping on the sofa.'),
    ('bird', 'noun', '/bɜːd/', 'chim', 'Động vật có lông vũ và cánh, thường biết bay', 'I saw a bird on the tree.'),
    ('fish', 'noun', '/fɪʃ/', 'cá', 'Động vật sống dưới nước, thở bằng mang', 'We caught some fish in the river.'),
    ('house', 'noun', '/haʊs/', 'ngôi nhà', 'Nơi để ở, thường có tường, mái và cửa', 'They live in a big house.'),
    ('home', 'noun', '/həʊm/', 'tổ ấm', 'Nơi mà ai đó sống và cảm thấy gắn bó', 'Home is where the heart is.'),
    ('car', 'noun', '/kɑːr/', 'xe ô tô', 'Phương tiện giao thông bốn bánh chạy bằng động cơ', 'She drives a new car.')

ON DUPLICATE KEY UPDATE
  phonetic_spelling = VALUES(phonetic_spelling),
  vietnamese_meaning = VALUES(vietnamese_meaning),
  detailed_definition = VALUES(detailed_definition),
  example_sentence = VALUES(example_sentence);

