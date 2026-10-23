package com.koreanwithme.backend.service;

import com.koreanwithme.backend.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private GrammarRepository grammarRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TopikExamRepository topikExamRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void testOverviewStatsWithEmptyData() {
        // Arrange: giả lập CSDL không có người dùng và giao dịch
        when(userRepository.findAll())
                .thenReturn(Collections.emptyList());

        when(orderRepository.findTop5ByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        // Act: gọi phương thức thật của Service
        var result = adminDashboardService.getOverviewStats();

        // Assert: kiểm tra kết quả mong đợi
        assertEquals(0, result.getTotalStudents());
        assertEquals(0, result.getTotalCourses());
        assertEquals(0, result.getTotalLessons());
        assertEquals(0, result.getTotalVocabularies());
        assertEquals(0, result.getTotalGrammars());
        assertEquals(0, result.getTotalExams());
        assertEquals(0, result.getTotalQuestions());
        assertEquals(0, result.getTotalExamAttempts());
        assertEquals(0.0, result.getAverageScore());
        assertEquals(0, result.getRecentStudents().size());
        assertEquals(0, result.getRecentOrders().size());
    }

    @Test
    void testOverviewStatsWithCourseData() {
        // Arrange: giả lập Repository trả về số lượng khóa học
        when(userRepository.findAll())
                .thenReturn(Collections.emptyList());

        when(courseRepository.count()).thenReturn(5L);
        when(lessonRepository.count()).thenReturn(20L);
        when(vocabularyRepository.count()).thenReturn(100L);
        when(grammarRepository.count()).thenReturn(15L);

        when(orderRepository.findTop5ByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        // Act
        var result = adminDashboardService.getOverviewStats();

        // Assert
        assertEquals(5, result.getTotalCourses());
        assertEquals(20, result.getTotalLessons());
        assertEquals(100, result.getTotalVocabularies());
        assertEquals(15, result.getTotalGrammars());
    }
}