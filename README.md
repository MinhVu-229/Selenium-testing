
 KoreanWithMe - Selenium Automation Testing Suite

Dự án kiểm thử tự động hóa (Automation Testing) cho hệ thống **KoreanWithMe Backend** sử dụng công nghệ **Selenium WebDriver** kết hợp với **TestNG**.

---

##  Tổng quan bài kiểm thử

Bộ kiểm thử tập trung vào việc tự động hóa các kịch bản kiểm thử các tính năng cốt lõi của hệ thống bao gồm Xác thực người dùng (Authentication), Quản lý khóa học, Thi thử TOPIK và Thanh toán.

- **Tổng số kịch bản kiểm thử (Test Cases):** 5/5
- **Tỷ lệ thành công (Pass Rate):** 100% 
- **Công cụ kiểm thử chính:** Selenium WebDriver, TestNG, Java
- **Trình duyệt thực thi:** Google Chrome (Headless / UI)

---

##  Danh sách các Test Case tự động hóa (5/5 Passed)

| STT | Tên Test Case | Mô tả chi tiết kịch bản | Kết quả |
| :---: | :--- | :--- | :---: |
| **1** | `testUserLogin` | Kiểm thử tính năng đăng nhập hệ thống với tài khoản người dùng hợp lệ, lưu trữ Token vào Local Storage. | **PASSED** |
| **2** | `testCourseEnrollment` | Kiểm thử quy trình đăng ký khóa học học tiếng Hàn mới và xác minh quyền truy cập. | **PASSED** |
| **3** | `testTopikExamSubmission` | Kiểm thử quy trình làm bài thi thử TOPIK, nộp bài và nhận phản hồi tự động. | **PASSED** |
| **4** | `testPaymentProcessing` | Kiểm thử luồng xử lý thanh toán học phí qua cổng thanh toán tích hợp. | **PASSED** |
| **5** | `testAdminDashboardAccess` | Kiểm thử phân quyền truy cập trang quản trị Dashboard dành cho tài khoản Admin. | **PASSED** |

---

##  Công nghệ & Thư viện sử dụng

- **Ngôn ngữ:** Java 17+
- **Framework kiểm thử:** TestNG / JUnit
- **Automation Tool:** Selenium WebDriver (v4.x)
- **Browser Driver:** ChromeDriver / WebDriverManager
- **Báo cáo & Khẳng định:** AssertJ / TestNG Asserts

---

## Hướng dẫn cài đặt & Thực thi bài Test

### 1. Yêu cầu hệ thống
- Java Development Kit (JDK) 17 trở lên.
- Maven 3.x trở lên.
- Google Chrome Browser phiên bản mới nhất.

### 2. Thực thi kiểm thử
Mở Terminal tại thư mục gốc dự án và chạy câu lệnh Maven:

```bash
# Chạy toàn bộ bộ kiểm thử Selenium TestSuite
mvn test -Dtest=KoreanWithMeAutomationTest
```

### 3. Kết quả
# <img width="3837" height="2040" alt="Screenshot 2026-10-23 081351" src="https://github.com/user-attachments/assets/b60d1e43-c44f-4ac3-a592-4a4c398c6dd2" />
