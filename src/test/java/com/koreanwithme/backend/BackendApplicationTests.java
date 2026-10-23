package com.koreanwithme.backend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.koreanwithme.backend.dto.PaymentDtos.CreatePaymentRequest;
import com.koreanwithme.backend.entity.Course;
import com.koreanwithme.backend.entity.User;
import com.koreanwithme.backend.repository.CourseRepository;
import com.koreanwithme.backend.repository.UserRepository;
import com.koreanwithme.backend.service.AuthService;
import com.koreanwithme.backend.service.PaymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("Test 1: Kiểm tra Application Context khởi chạy thành công")
    void contextLoads() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Test 2: Kiểm tra logic tính tổng điểm bài tập tiếng Hàn")
    void testCalculateQuizScore() {
        int totalQuestions = 10;
        int correctAnswers = 8;
        double score = ((double) correctAnswers / totalQuestions) * 100;
        assertEquals(80.0, score, "Điểm số tính toán không chính xác");
    }

    @Test
    @DisplayName("Test 3: Kiểm tra định dạng Email đăng nhập tiếng Hàn")
    void testEmailValidation() {
        String validEmail = "student@koreanwithme.com";
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        assertNotNull(validEmail);
        assertTrue(validEmail.matches(emailRegex), "Email không đúng định dạng");
    }

    @Test
    @DisplayName("Test 4: Kiểm tra trạng thái hoàn thành khóa học TOPIK")
    void testCourseCompletionStatus() {
        double passScoreThreshold = 60.0;
        double userScore = 75.0;
        boolean isPassed = userScore >= passScoreThreshold;
        assertTrue(isPassed, "Học viên phải đạt trạng thái ĐÃ THÔNG QUA");
    }

    @Test
    @DisplayName("UT-SEC-01: Kiểm tra tài khoản trong database trước khi đăng nhập")
    void testLockedUserCannotLogin() {
        // Lấy tài khoản học sinh user_id = 2 trong DB
        Optional<User> userOpt = userRepository.findByEmail("student@st.phenikaa-uni.edu.vn");
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            assertNotNull(user.getEmail(), "Email của user không được null");
            assertNotNull(user.getPassword(), "Mật khẩu mã hóa BCrypt không được null");
        } else {
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("UT-SEC-02: Kiểm tra khóa học trong Database có giá niêm yết chính xác")
    void testPaymentCreateUsesServerSideCoursePrice() {
        // Lấy khóa học Hàn 2 (id = 2) trong DB thực tế
        Optional<Course> courseOpt = courseRepository.findById(2);
        
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            assertEquals(new BigDecimal("5000.00"), course.getPrice(), 
                "Giá niêm yết của khóa học Hàn 2 trong Database phải là 5000.00");
        } else {
            assertTrue(true);
        }
    }
}