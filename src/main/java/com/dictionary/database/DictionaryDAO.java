package com.dictionary.database;

import com.dictionary.model.Word;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho việc thao tác với bảng từ điển
 */
public class DictionaryDAO {
    private DatabaseConnection dbConnection;

    public DictionaryDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /** Tìm kiếm từ tiếng Anh */
    public List<Word> searchWord(String englishWord) {
        List<Word> results = new ArrayList<>();
        String sql = "SELECT * FROM dictionary WHERE english_word = ? ORDER BY part_of_speech";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, englishWord.toLowerCase());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToWord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm từ: " + e.getMessage());
        }
        return results;
    }

    /** Thêm từ mới */
    public boolean addWord(Word word) {
        String sql = "INSERT INTO dictionary (english_word, part_of_speech, phonetic_spelling, " +
                "vietnamese_meaning, detailed_definition, example_sentence, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, word.getEnglishWord().toLowerCase());
            stmt.setString(2, word.getPartOfSpeech());
            stmt.setString(3, word.getPhoneticSpelling());
            stmt.setString(4, word.getVietnameseMeaning());
            stmt.setString(5, word.getDetailedDefinition());
            stmt.setString(6, word.getExampleSentence());
            stmt.setString(7, word.getImagePath());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm từ: " + e.getMessage());
            return false;
        }
    }

    /** Cập nhật thông tin từ */
    public boolean updateWord(Word word) {
        String sql = "UPDATE dictionary SET phonetic_spelling = ?, " +
                "vietnamese_meaning = ?, detailed_definition = ?, example_sentence = ?, image_path = ? " +
                "WHERE english_word = ? AND part_of_speech = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, word.getPhoneticSpelling());
            stmt.setString(2, word.getVietnameseMeaning());
            stmt.setString(3, word.getDetailedDefinition());
            stmt.setString(4, word.getExampleSentence());
            stmt.setString(5, word.getImagePath());
            stmt.setString(6, word.getEnglishWord().toLowerCase());
            stmt.setString(7, word.getPartOfSpeech());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật từ: " + e.getMessage());
            return false;
        }
    }

    /** Xóa 1 nghĩa cụ thể */
    public boolean deleteWord(String englishWord, String partOfSpeech) {
        String sql = "DELETE FROM dictionary WHERE english_word = ? AND part_of_speech = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, englishWord.toLowerCase());
            stmt.setString(2, partOfSpeech);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa từ: " + e.getMessage());
            return false;
        }
    }

    /** Xóa tất cả nghĩa của một từ */
    public boolean deleteAllMeanings(String englishWord) {
        String sql = "DELETE FROM dictionary WHERE english_word = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, englishWord.toLowerCase());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa từ: " + e.getMessage());
            return false;
        }
    }

    /** Tìm kiếm từ chứa keyword */
    public List<Word> searchWordsContaining(String keyword) {
        List<Word> words = new ArrayList<>();
        String sql = "SELECT * FROM dictionary WHERE english_word LIKE ? ORDER BY english_word, part_of_speech";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                words.add(mapResultSetToWord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm từ: " + e.getMessage());
        }
        return words;
    }

    /** Tìm kiếm theo nghĩa tiếng Việt */
    public List<Word> searchVietnameseWord(String vietnameseWord) {
        List<Word> results = new ArrayList<>();
        String sql = "SELECT * FROM dictionary WHERE vietnamese_meaning LIKE ? ORDER BY english_word, part_of_speech";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + vietnameseWord + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToWord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm từ Việt: " + e.getMessage());
        }
        return results;
    }

    /** Tìm kiếm từ Việt có chứa keyword */
    public List<Word> searchVietnameseWordsContaining(String keyword) {
        List<Word> results = new ArrayList<>();
        String sql = "SELECT * FROM dictionary WHERE vietnamese_meaning LIKE ? ORDER BY vietnamese_meaning, english_word";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToWord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm từ Việt chứa: " + e.getMessage());
        }
        return results;
    }

    /** Lấy toàn bộ danh sách từ */
    public List<Word> getAllWords() {
        List<Word> words = new ArrayList<>();
        String sql = "SELECT * FROM dictionary ORDER BY english_word, part_of_speech";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                words.add(mapResultSetToWord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách từ: " + e.getMessage());
        }
        return words;
    }

    /** Helper: Map kết quả DB -> Word */
    private Word mapResultSetToWord(ResultSet rs) throws SQLException {
        Word word = new Word();
        word.setEnglishWord(rs.getString("english_word"));
        word.setPartOfSpeech(rs.getString("part_of_speech"));
        word.setPhoneticSpelling(rs.getString("phonetic_spelling"));
        word.setVietnameseMeaning(rs.getString("vietnamese_meaning"));
        word.setDetailedDefinition(rs.getString("detailed_definition"));
        word.setExampleSentence(rs.getString("example_sentence"));
    
        // Nếu bảng đã có cột image_path thì lấy, không thì để null
        try {
            word.setImagePath(rs.getString("image_path"));
        } catch (SQLException ignore) {
            // cột chưa tồn tại -> bỏ qua để tránh lỗi khi DB chưa nâng cấp
        }
    
        return word;
    }
    
}
