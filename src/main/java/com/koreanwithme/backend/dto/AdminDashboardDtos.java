package com.koreanwithme.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDashboardDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OverviewStatsResponse {
        // 5 Thẻ KPI số liệu thật
        private long totalStudents;
        private long newStudentsThisWeek;

        private long totalCourses;
        private long totalLessons;
        private long totalVocabularies;
        private long totalGrammars;

        private long totalExams;
        private long totalQuestions;

        private long totalExamAttempts;
        private double averageScore;

        private BigDecimal totalRevenue;
        private BigDecimal revenueThisMonth;

        // Biểu đồ xu hướng 7 ngày
        private List<TrendPoint> trend7Days;

        // Danh sách hoạt động gần đây
        private List<RecentStudentDto> recentStudents;
        private List<RecentExamAttemptDto> recentExamAttempts;
        private List<RecentOrderDto> recentOrders;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private String date; // dd/MM
        private long attempts; // Số lượt luyện thi trong ngày
        private long newUsers; // Số học viên đăng ký mới
        private BigDecimal revenue; // Doanh thu trong ngày
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentStudentDto {
        private Integer id;
        private String fullName;
        private String email;
        private String status;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentExamAttemptDto {
        private Long id;
        private String studentName;
        private String examTitle;
        private double score;
        private boolean passed;
        private LocalDateTime submittedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentOrderDto {
        private Long id;
        private String orderCode;
        private String studentName;
        private String courseName;
        private BigDecimal amount;
        private String status;
        private LocalDateTime createdAt;
    }
}