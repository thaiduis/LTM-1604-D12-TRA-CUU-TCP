package com.dictionary.client;

import com.dictionary.model.Word;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CSVLogger {
    private static final String CSV_FILE = "search_history.csv";
    private static final String CSV_HEADER = "Thời gian,Từ tìm kiếm,Hướng dịch,Từ tiếng Anh,Từ loại,Phiên âm,Nghĩa tiếng Việt,Định nghĩa,Ví dụ\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static CSVLogger instance;
    
    private CSVLogger() {
        initializeCSVFile();
    }
    
    public static synchronized CSVLogger getInstance() {
        if (instance == null) {
            instance = new CSVLogger();
        }
        return instance;
    }
    
    private void initializeCSVFile() {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(CSV_FILE, true), StandardCharsets.UTF_8)) {
            // Kiểm tra xem file có tồn tại và có header chưa
            java.io.File file = new java.io.File(CSV_FILE);
            if (!file.exists() || file.length() == 0) {
                // Thêm BOM UTF-8 để Excel hiểu đúng encoding
                writer.write('\uFEFF');
                writer.write(CSV_HEADER);
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi khởi tạo file CSV: " + e.getMessage());
        }
    }
    
    public void logSearch(String searchTerm, String direction, Word word) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(CSV_FILE, true), StandardCharsets.UTF_8)) {
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String csvLine = String.format("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                timestamp,
                escapeCSV(searchTerm),
                escapeCSV(direction),
                escapeCSV(word.getEnglishWord()),
                escapeCSV(word.getPartOfSpeech()),
                escapeCSV(word.getPhoneticSpelling()),
                escapeCSV(word.getVietnameseMeaning()),
                escapeCSV(word.getDetailedDefinition()),
                escapeCSV(word.getExampleSentence())
            );
            writer.write(csvLine);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi log CSV: " + e.getMessage());
        }
    }
    
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes và newlines trong CSV
        return value.replace("\"", "\"\"")
                   .replace("\n", " ")
                   .replace("\r", " ");
    }
}
