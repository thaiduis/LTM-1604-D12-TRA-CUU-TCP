<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   Ứng dụng tra cứu từ điển Anh–Việt (TCP)
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 📖 1. Giới thiệu

Ứng dụng tra cứu từ điển Anh – Việt được xây dựng theo mô hình client–server sử dụng giao thức TCP Socket. Hệ thống cho phép nhiều người dùng (client) cùng lúc gửi yêu cầu tra cứu từ vựng tiếng Anh và nhận lại nghĩa tiếng Việt từ phía server. Toàn bộ quá trình truyền và nhận dữ liệu diễn ra trên nền TCP, đảm bảo tính tin cậy và toàn vẹn của thông tin.

Các chức năng chính:
- 🔎 Tra cứu nghĩa tiếng Việt theo từ tiếng Anh
- 🧩 Tìm các từ chứa một từ khóa (search containing)
- 🗂️ Quản lý từ điển: thêm, sửa, xóa mục từ (qua server/DAO)
- 👥 Hỗ trợ nhiều client kết nối đồng thời
- 🖥️ Giao diện đồ họa (GUI) cho cả phía Server và Client

---

## 🛠️ 2. Công nghệ sử dụng

<p align="center">
  <img src="https://img.shields.io/badge/Java-8%2B-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/GUI-Swing-6DB33F?style=for-the-badge&logo=oracle&logoColor=white" alt="Swing" />
  <img src="https://img.shields.io/badge/MySQL-8.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Maven-3.6%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/Protocol-TCP%20Socket-0A66C2?style=for-the-badge" alt="TCP" />
</p>

Cấu trúc mã nguồn chính:
- 📦 `src/main/java/com/dictionary/client/` — Client TCP và GUI
- 🖧 `src/main/java/com/dictionary/server/` — Server TCP và GUI
- 🔗 `src/main/java/com/dictionary/database/` — Kết nối DB và DAO
- 📘 `src/main/java/com/dictionary/model/` — Lớp mô hình `Word`

---

## 🖼️ 3. Một số hình ảnh hệ thống

- 🖥️ Giao diện Server:

![Server GUI](docs/Server.png)

![Server GUI](docs/Form1.png) ![Server GUI](docs/Form2.png) ![Server GUI](docs/Form3.png)

- 💻 Giao diện Client:

![Client GUI](docs/Client.png)

- 🔎 Kết quả tra cứu:

![Search Result](docs/Tracuu.png)

---

## 🧭 4. Các bước cài đặt

### 4.1. Yêu cầu hệ thống
- 🪟 Windows/Linux/macOS
- ☕ Java 8 trở lên (kiểm tra bằng `java -version`)
- 🚀 Maven 3.6+ (tùy chọn nếu build bằng Maven)
- 🐬 MySQL Server 5.7+ (khuyến nghị 8.0+)

### 4.2. Cài đặt và chuẩn bị CSDL
1) 📥 Cài MySQL Server: tải từ trang chính thức `https://dev.mysql.com/downloads/mysql/`
2) 🗃️ Tạo cơ sở dữ liệu và tài khoản (ví dụ):
```sql
CREATE DATABASE dictionary_db;
CREATE USER 'dictionary_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON dictionary_db.* TO 'dictionary_user'@'localhost';
FLUSH PRIVILEGES;
```
3) 🧩 Khởi tạo bảng/dữ liệu mẫu bằng file `database/schema.sql`:
```bash
mysql -u root -p dictionary_db < database/schema.sql
```

### 4.3. Cấu hình kết nối CSDL
Cập nhật thông tin trong `src/main/java/com/dictionary/database/DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/dictionary_db?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root"; // hoặc dictionary_user
private static final String DB_PASSWORD = "your_password"; // mật khẩu của bạn
```

### 4.4. Build và chạy
Bạn có thể sử dụng các script `.bat` có sẵn (Windows) hoặc Maven.

- 🚀 Cách A: Dùng script
  - 🧱 Build: chạy `build.bat`
  - 🖧 Chạy Server: `run-server.bat`
  - 💻 Chạy Client: `run-client.bat`

- ⚙️ Cách B: Dùng Maven
  - 🧹 Build:
    ```bash
    mvn clean package
    ```
  - ▶️ (Nếu cấu hình `exec-maven-plugin`) chạy bằng:
    ```bash
    mvn exec:java
    ```

- 🛠️ Cách C: Chạy thủ công (ví dụ)
  - 🏗️ Biên dịch:
    ```bash
    javac -cp "lib/mysql-connector-java-8.0.33.jar" -d target/classes src/main/java/com/dictionary/**/*.java
    ```
  - ▶️ Chạy Server và Client (chỉnh class main tương ứng nếu cần):
    ```bash
    java -cp "target/classes;lib/mysql-connector-java-8.0.33.jar" com.dictionary.server.DictionaryServer
    java -cp "target/classes;lib/mysql-connector-java-8.0.33.jar" com.dictionary.client.DictionaryClientGUI
    ```

Mặc định server lắng nghe trên `localhost:12345` (có thể thay đổi trong mã nguồn server nếu cần).

---

## 📫 5. Liên hệ
- Họ và tên: Vũ Duy Thái
- Khoa: Công nghệ thông tin - Trường Đại học Đại Nam
- Liên hệ email: thaiitkk2004@gmail.com

<p align="center">© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.</p>

---
