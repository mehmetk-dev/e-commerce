package com.mehmetkerem.repository.specification;

import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Admin sipariş filtreleme için Specification tanımları.
 * Kullanım:
 * OrderSpecification.hasStatus(PENDING).and(OrderSpecification.dateBetween(...))
 */
public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("orderStatus"), status);
    }

    public static Specification<Order> hasPaymentStatus(PaymentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("paymentStatus"), status);
    }

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<Order> dateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("orderDate"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("orderDate"), from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(root.get("orderDate"), to);
            }
            return null;
        };
    }

    public static Specification<Order> searchByOrderCode(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank())
                return null;
            try {
                Long orderId = Long.parseLong(query.replaceAll("\\D", ""));
                return cb.equal(root.get("id"), orderId);
            } catch (NumberFormatException e) {
                return null;
            }
        };
    }
}
