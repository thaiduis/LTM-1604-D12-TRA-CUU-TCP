package com.dictionary.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class quản lý ket noi cơ sở dữ liệu MySQL
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dictionary_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "020901"; // Thay đổi mật khẩu theo cài đặt của bạn
    
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("ket noi database thành công!");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Lỗi ket noi database: " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo ket noi mới: " + e.getMessage());
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Da dong ket noi database");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi dong ket noi: " + e.getMessage());
        }
    }
}
