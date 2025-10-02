package com.dictionary.server;

import com.dictionary.database.DictionaryDAO;
import com.dictionary.model.Word;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class DictionaryServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private DictionaryDAO dictionaryDAO;
    private boolean isRunning;
    private DictionaryServerGUI gui;
    private final List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    

    public DictionaryServer(DictionaryServerGUI gui) {
        this.dictionaryDAO = new DictionaryDAO();
        this.isRunning = false;
        this.gui = gui;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;
        System.out.println("Server đang chạy trên port " + PORT);
        System.out.println("Chờ kết nối từ client...");

        // Xử lý kết nối client trong thread riêng
        new Thread(() -> {
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client đã kết nối: " + clientSocket.getInetAddress());
                    
                    ClientHandler clientHandler = new ClientHandler(clientSocket, dictionaryDAO, clientHandlers);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Lỗi chấp nhận kết nối: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server đã dừng");
            }
            // Đóng tất cả kết nối client đang mở
            synchronized (clientHandlers) {
                for (ClientHandler handler : clientHandlers) {
                    try {
                        handler.shutdown();
                    } catch (Exception ignore) { }
                }
                clientHandlers.clear();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi dừng server: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static void main(String[] args) {
        try {
            DictionaryServer server = new DictionaryServer(null);
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } catch (IOException e) {
            System.err.println("Không thể khởi động server: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private DictionaryDAO dictionaryDAO;
        private BufferedReader in;
        private PrintWriter out;
        private final List<ClientHandler> registry;

        public ClientHandler(Socket socket, DictionaryDAO dao, List<ClientHandler> registry) {
            this.clientSocket = socket;
            this.dictionaryDAO = dao;
            this.registry = registry;
            this.registry.add(this);
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String response = processRequest(inputLine);
                    out.println(response);
                    
                    if ("QUIT".equals(inputLine)) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Lỗi xử lý client: " + e.getMessage());
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                    System.out.println("Client đã ngắt kết nối");
                } catch (IOException e) {
                    System.err.println("Lỗi đóng kết nối client: " + e.getMessage());
                }
                if (registry != null) {
                    registry.remove(this);
                }
            }
        }

        private String processRequest(String request) {
            String[] parts = request.split("\\|");
            if (parts.length < 1) {
                return "ERROR|Định dạng yêu cầu không hợp lệ";
            }

            String command = parts[0];
            String data = parts.length > 1 ? parts[1] : "";

            try {
                switch (command) {
                    case "SEARCH":
                        return handleSearch(data);
                    case "SEARCH_CONTAINING":
                        return handleSearchContaining(data);
                    case "SEARCH_VIETNAMESE":
                        return handleSearchVietnamese(data);
                    case "SEARCH_VIETNAMESE_CONTAINING":
                        return handleSearchVietnameseContaining(data);
                    case "PING":
                        return "PONG";
                    case "QUIT":
                        return "OK|Tạm biệt!";
                    default:
                        return "ERROR|Lệnh không được hỗ trợ: " + command;
                }
            } catch (Exception e) {
                return "ERROR|Lỗi xử lý yêu cầu: " + e.getMessage();
            }
        }

        private String handleSearch(String englishWord) {
            List<Word> words = dictionaryDAO.searchWord(englishWord);
            if (!words.isEmpty()) {
                StringBuilder result = new StringBuilder("SUCCESS");
                for (Word word : words) {
                    result.append(String.format("|%s|%s|%s|%s|%s|%s|%s",
                        word.getEnglishWord(),
                        word.getPartOfSpeech(),
                        word.getPhoneticSpelling(),
                        word.getVietnameseMeaning(),
                        word.getDetailedDefinition(),
                        word.getExampleSentence(),
                        word.getImagePath() != null ? word.getImagePath() : ""));
                }
                return result.toString();
            } else {
                return "NOT_FOUND|Không tìm thấy từ: " + englishWord;
            }
        }

        private String handleSearchContaining(String keyword) {
            List<Word> words = dictionaryDAO.searchWordsContaining(keyword);
            if (!words.isEmpty()) {
                StringBuilder result = new StringBuilder("SUCCESS");
                for (Word word : words) {
                    result.append(String.format("|%s|%s|%s|%s|%s|%s|%s",
                        word.getEnglishWord(),
                        word.getPartOfSpeech(),
                        word.getPhoneticSpelling(),
                        word.getVietnameseMeaning(),
                        word.getDetailedDefinition(),
                        word.getExampleSentence(),
                        word.getImagePath() != null ? word.getImagePath() : ""));
                }
                return result.toString();
            } else {
                return "NOT_FOUND|Không tìm thấy từ nào chứa: " + keyword;
            }
        }

        private String handleSearchVietnamese(String vietnameseWord) {
            List<Word> words = dictionaryDAO.searchVietnameseWord(vietnameseWord);
            if (!words.isEmpty()) {
                StringBuilder result = new StringBuilder("SUCCESS");
                for (Word word : words) {
                    result.append(String.format("|%s|%s|%s|%s|%s|%s|%s",
                        word.getEnglishWord(),
                        word.getPartOfSpeech(),
                        word.getPhoneticSpelling(),
                        word.getVietnameseMeaning(),
                        word.getDetailedDefinition(),
                        word.getExampleSentence(),
                        word.getImagePath() != null ? word.getImagePath() : ""));
                }
                return result.toString();
            } else {
                return "NOT_FOUND|Không tìm thấy từ Việt: " + vietnameseWord;
            }
        }

        private String handleSearchVietnameseContaining(String keyword) {
            List<Word> words = dictionaryDAO.searchVietnameseWordsContaining(keyword);
            if (!words.isEmpty()) {
                StringBuilder result = new StringBuilder("SUCCESS");
                for (Word word : words) {
                    result.append(String.format("|%s|%s|%s|%s|%s|%s|%s",
                        word.getEnglishWord(),
                        word.getPartOfSpeech(),
                        word.getPhoneticSpelling(),
                        word.getVietnameseMeaning(),
                        word.getDetailedDefinition(),
                        word.getExampleSentence(),
                        word.getImagePath() != null ? word.getImagePath() : ""));
                }
                return result.toString();
            } else {
                return "NOT_FOUND|Không tìm thấy từ Việt nào chứa: " + keyword;
            }
        }

        private void shutdown() {
            try {
                if (out != null) out.close();
            } catch (Exception ignore) { }
            try {
                if (in != null) in.close();
            } catch (Exception ignore) { }
            try {
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch (Exception ignore) { }
        }
    }
}
