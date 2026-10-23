package com.koreanwithme.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", unique = true, nullable = false, length = 32)
    private String orderCode;

    // EAGER: Tải luôn thông tin User để AdminDashboard đọc được getFullName() không bị lỗi Lazy
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // EAGER: Tải luôn thông tin Course
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType; // COURSE hoặc EXAM

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status; // PENDING, SUCCESS, FAILED, CANCELLED

    @Column(name = "payment_method", length = 30)
    private String paymentMethod; // VIETQR

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum ItemType {
        COURSE, EXAM
    }

    public enum OrderStatus {
        PENDING, SUCCESS, FAILED, CANCELLED
    }
}