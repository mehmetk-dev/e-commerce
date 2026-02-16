package com.mehmetkerem.repository;

import com.mehmetkerem.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndOrderItemsProductId(Long userId, Long productId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o")
    long countTotalOrders();

    @Query(value = "SELECT TO_CHAR(order_date, 'YYYY-MM-DD') as date, SUM(total_amount) as revenue, COUNT(*) as count "
            +
            "FROM orders WHERE order_date >= :startDate " +
            "GROUP BY TO_CHAR(order_date, 'YYYY-MM-DD') ORDER BY date ASC", nativeQuery = true)
    List<Object[]> getDailyStats(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);
}
