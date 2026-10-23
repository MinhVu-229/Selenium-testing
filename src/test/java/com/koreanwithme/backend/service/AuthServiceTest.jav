package com.koreanwithme.backend.service;

import com.koreanwithme.backend.entity.User;
import com.koreanwithme.backend.repository.UserRepository;
import com.koreanwithme.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(2);
        mockUser.setEmail("student@st.phenikaa-uni.edu.vn");
        mockUser.setPassword("$2a$10$hashedPasswordHere");
        mockUser.setLocked(false);
    }

    @Test
    @DisplayName("UT-AUTH-01: Đăng nhập thành công trả về JWT Token")
    void testLogin_Success() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", mockUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(mockUser.getEmail())).thenReturn("mocked-jwt-token");

        String token = authService.login(mockUser.getEmail(), "password123");

        assertNotNull(token);
        assertEquals("mocked-jwt-token", token);
        verify(jwtTokenProvider, times(1)).generateToken(mockUser.getEmail());
    }

    @Test
    @DisplayName("UT-AUTH-02: Đăng nhập sai mật khẩu ném ra BadCredentialsException")
    void testLogin_WrongPassword() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", mockUser.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(mockUser.getEmail(), "wrongPassword");
        });
    }

    @Test
    @DisplayName("UT-AUTH-03: Đăng nhập tài khoản không tồn tại ném ra BadCredentialsException/Exception")
    void testLogin_UserNotFound() {
        when(userRepository.findByEmail("notfound@st.phenikaa-uni.edu.vn")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            authService.login("notfound@st.phenikaa-uni.edu.vn", "password123");
        });
    }

    @Test
    @DisplayName("UT-AUTH-04: Đăng nhập tài khoản bị khóa (isLocked = true) ném ra LockedException")
    void testLogin_LockedAccount() {
        mockUser.setLocked(true);
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));

        assertThrows(LockedException.class, () -> {
            authService.login(mockUser.getEmail(), "password123");
        });
    }

    @Test
    @DisplayName("UT-AUTH-05: Đăng ký với Email đã tồn tại ném ra IllegalArgumentException")
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByEmail(mockUser.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(mockUser.getEmail(), "Password123!");
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("UT-AUTH-06: Mã hóa mật khẩu BCrypt trước khi lưu khi Đăng ký")
    void testRegister_PasswordEncodingBeforeSave() {
        when(userRepository.existsByEmail("newstudent@st.phenikaa-uni.edu.vn")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$encodedSecureHash");

        authService.register("newstudent@st.phenikaa-uni.edu.vn", "Password123!");

        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getPassword().equals("$2a$10$encodedSecureHash")
        ));
    }
    @Test
    @DisplayName("RED TEST: BUG_AUTH_01 - Tài khoản isLocked = true vẫn đăng nhập thành công (Kỳ vọng THẤT BẠI)")
    void redTest_BUG_AUTH_01_LockedUserCanLogin() {
        User lockedUser = new User();
        lockedUser.setEmail("locked_victim@st.phenikaa-uni.edu.vn");
        lockedUser.setPassword("$2a$10$hashedPassword");
        lockedUser.setLocked(true); // Tài khoản đang BỊ KHÓA

        when(userRepository.findByEmail(lockedUser.getEmail())).thenReturn(Optional.of(lockedUser));
        when(passwordEncoder.matches("password123", lockedUser.getPassword())).thenReturn(true);

        // Test kỳ vọng ném ra LockedException nếu hệ thống chuẩn.
        // TRƯỚC KHI SỬA CODE PRODUCTION: Test này sẽ bị FAILED (ĐỎ) vì AuthService thiếu đoạn check isLocked().
        assertThrows(LockedException.class, () -> {
            authService.login(lockedUser.getEmail(), "password123");
        });
    }
}