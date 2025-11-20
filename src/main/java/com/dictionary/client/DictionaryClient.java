package com.dictionary.client;

import com.dictionary.model.Word;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TCP Client cho ứng dụng từ điển Anh-Việt
 */
public class DictionaryClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Object ioLock = new Object();

    public DictionaryClient() throws IOException {
        connect();
    }

    private void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        System.out.println("Đã kết nối đến server");
    }

    /** Gửi request và nhận response theo cách đồng bộ để tránh chồng chéo I/O */
    private String send(String request) throws IOException {
        synchronized (ioLock) {
            if (socket == null || socket.isClosed()) {
                throw new IOException("Kết nối đã bị đóng");
            }
            out.println(request);
            String response = in.readLine();
            if (response == null) {
                throw new IOException("Server đã ngắt kết nối");
            }
            return response;
        }
    }

    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("Đã ngắt kết nối khỏi server");
        } catch (IOException e) {
            System.err.println("Lỗi khi ngắt kết nối: " + e.getMessage());
        }
    }

    /**
     * Tìm kiếm từ và trả về danh sách các định nghĩa theo từ loại
     */
    public List<Word> searchWord(String englishWord) {
        List<Word> results = new ArrayList<>();
        try {
            String request = "SEARCH|" + englishWord;
            String response = send(request);
            
            String[] parts = response.split("\\|", -1);
            System.out.println("[CLIENT][SEARCH] respParts=" + parts.length + ", raw=" + (response == null ? "null" : response));
            if (parts.length >= 2 && "SUCCESS".equals(parts[0])) {
                for (int i = 1; i < parts.length; i += 7) {
                    if (i + 6 < parts.length) {
                        Word word = new Word(
                            parts[i],      // englishWord
                            parts[i + 1],  // partOfSpeech
                            parts[i + 2],  // phoneticSpelling
                            parts[i + 3],  // vietnameseMeaning
                            parts[i + 4],  // detailedDefinition
                            parts[i + 5],  // exampleSentence
                            parts[i + 6]   // imagePath
                        );
                        results.add(word);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
        return results;
    }

    /**
     * Thêm từ mới vào từ điển
     */
    public String addWord(Word word) {
        try {
            String request = String.format("ADD|%s|%s|%s|%s|%s|%s",
                word.getEnglishWord(),
                word.getPartOfSpeech(),
                word.getPhoneticSpelling(),
                word.getVietnameseMeaning(),
                word.getDetailedDefinition(),
                word.getExampleSentence());
            String response = send(request);
            return parseResponse(response);
        } catch (IOException e) {
            return "Lỗi kết nối: " + e.getMessage();
        }
    }

    /**
     * Cập nhật thông tin của từ
     */
    public String updateWord(Word word) {
        try {
            String request = String.format("UPDATE|%s|%s|%s|%s|%s|%s",
                word.getEnglishWord(),
                word.getPartOfSpeech(),
                word.getPhoneticSpelling(),
                word.getVietnameseMeaning(),
                word.getDetailedDefinition(),
                word.getExampleSentence());
            String response = send(request);
            return parseResponse(response);
        } catch (IOException e) {
            return "Lỗi kết nối: " + e.getMessage();
        }
    }

    /**
     * Xóa một từ với từ loại cụ thể
     */
    public String deleteWord(String englishWord, String partOfSpeech) {
        try {
            String request = "DELETE|" + englishWord + "|" + partOfSpeech;
            String response = send(request);
            return parseResponse(response);
        } catch (IOException e) {
            return "Lỗi kết nối: " + e.getMessage();
        }
    }

    /**
     * Xóa tất cả định nghĩa của một từ
     */
    public String deleteAllMeanings(String englishWord) {
        try {
            String request = "DELETE|" + englishWord;
            String response = send(request);
            return parseResponse(response);
        } catch (IOException e) {
            return "Lỗi kết nối: " + e.getMessage();
        }
    }

    /**
     * Tìm kiếm các từ có chứa từ khóa
     */
    public List<Word> searchWordsContaining(String keyword) {
        List<Word> words = new ArrayList<>();
        try {
            String request = "SEARCH_CONTAINING|" + keyword;
            String response = send(request);
            
            String[] parts = response.split("\\|", -1);
            System.out.println("[CLIENT][SEARCH_CONTAINING] respParts=" + parts.length + ", rawHead=" + (response == null ? "null" : response.substring(0, Math.min(200, response.length()))));
            if (parts.length >= 2 && "SUCCESS".equals(parts[0])) {
                for (int i = 1; i < parts.length; i += 7) {
                    if (i + 6 < parts.length) {
                        Word word = new Word(
                            parts[i],      // englishWord
                            parts[i + 1],  // partOfSpeech
                            parts[i + 2],  // phoneticSpelling
                            parts[i + 3],  // vietnameseMeaning
                            parts[i + 4],  // detailedDefinition
                            parts[i + 5],  // exampleSentence
                            parts[i + 6]   // imagePath
                        );
                        words.add(word);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
        return words;
    }

    /**
     * Tìm kiếm từ tiếng Việt và trả về danh sách các từ tiếng Anh tương ứng
     */
    public List<Word> searchVietnameseWord(String vietnameseWord) {
        List<Word> results = new ArrayList<>();
        try {
            String request = "SEARCH_VIETNAMESE|" + vietnameseWord;
            String response = send(request);
            
            String[] parts = response.split("\\|", -1);
            System.out.println("[CLIENT][SEARCH_VIETNAMESE] respParts=" + parts.length + ", rawHead=" + (response == null ? "null" : response.substring(0, Math.min(200, response.length()))));
            if (parts.length >= 2 && "SUCCESS".equals(parts[0])) {
                for (int i = 1; i < parts.length; i += 7) {
                    if (i + 6 < parts.length) {
                        Word word = new Word(
                            parts[i],      // englishWord
                            parts[i + 1],  // partOfSpeech
                            parts[i + 2],  // phoneticSpelling
                            parts[i + 3],  // vietnameseMeaning
                            parts[i + 4],  // detailedDefinition
                            parts[i + 5],  // exampleSentence
                            parts[i + 6]   // imagePath
                        );
                        results.add(word);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
        return results;
    }

    /**
     * Tìm kiếm các từ Việt có chứa từ khóa
     */
    public List<Word> searchVietnameseWordsContaining(String keyword) {
        List<Word> results = new ArrayList<>();
        try {
            String request = "SEARCH_VIETNAMESE_CONTAINING|" + keyword;
            String response = send(request);
            
            String[] parts = response.split("\\|", -1);
            System.out.println("[CLIENT][SEARCH_VIET_CONTAINING] respParts=" + parts.length + ", rawHead=" + (response == null ? "null" : response.substring(0, Math.min(200, response.length()))));
            if (parts.length >= 2 && "SUCCESS".equals(parts[0])) {
                for (int i = 1; i < parts.length; i += 7) {
                    if (i + 6 < parts.length) {
                        Word word = new Word(
                            parts[i],      // englishWord
                            parts[i + 1],  // partOfSpeech
                            parts[i + 2],  // phoneticSpelling
                            parts[i + 3],  // vietnameseMeaning
                            parts[i + 4],  // detailedDefinition
                            parts[i + 5],  // exampleSentence
                            parts[i + 6]   // imagePath
                        );
                        results.add(word);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
        return results;
    }

    /**
     * Lấy tất cả từ trong từ điển
     */
    public List<Word> getAllWords() {
        List<Word> words = new ArrayList<>();
        try {
            String request = "GET_ALL|";
            String response = send(request);
            
            String[] parts = response.split("\\|", -1);
            System.out.println("[CLIENT][GET_ALL] respParts=" + parts.length);
            if (parts.length >= 2 && "SUCCESS".equals(parts[0])) {
                for (int i = 1; i < parts.length; i += 7) {
                    if (i + 6 < parts.length) {
                        Word word = new Word(
                            parts[i],      // englishWord
                            parts[i + 1],  // partOfSpeech
                            parts[i + 2],  // phoneticSpelling
                            parts[i + 3],  // vietnameseMeaning
                            parts[i + 4],  // detailedDefinition
                            parts[i + 5],  // exampleSentence
                            parts[i + 6]   // imagePath
                        );
                        words.add(word);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
        return words;
    }

    /**
     * Phân tích phản hồi từ server
     */
    private String parseResponse(String response) {
        if (response == null) {
            return "Lỗi: Không nhận được phản hồi từ server";
        }
        
        String[] parts = response.split("\\|", 2);
        if (parts.length < 2) {
            return response;
        }
        
        String status = parts[0];
        String message = parts[1];
        
        switch (status) {
            case "SUCCESS":
                return message;
            case "ERROR":
                return "Lỗi: " + message;
            case "NOT_FOUND":
                return "Không tìm thấy: " + message;
            default:
                return response;
        }
    }

    /**
     * Kiểm tra kết nối
     */
    public boolean isConnected() {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            return false;
        }
        
        // Kiểm tra thực tế bằng cách gửi PING
        try {
            synchronized (ioLock) {
                out.println("PING");
                socket.setSoTimeout(1000); // Timeout 1 giây
                String response = in.readLine();
                socket.setSoTimeout(0); // Reset timeout
                return response != null && response.startsWith("PONG");
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Kết nối lại đến server
     */
    public boolean reconnect() {
        try {
            disconnect();
            connect();
            return true;
        } catch (IOException e) {
            System.err.println("Không thể kết nối lại: " + e.getMessage());
            return false;
        }
    }
}
