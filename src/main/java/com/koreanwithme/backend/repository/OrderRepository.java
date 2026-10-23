package com.koreanwithme.backend.repository;

import com.koreanwithme.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalRevenueByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.status = :status AND o.createdAt >= :since")
    BigDecimal sumRevenueSince(@Param("status") Order.OrderStatus status, @Param("since") LocalDateTime since);
}