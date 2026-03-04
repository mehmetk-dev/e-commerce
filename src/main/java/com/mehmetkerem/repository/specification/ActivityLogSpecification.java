package com.mehmetkerem.repository.specification;

import com.mehmetkerem.enums.ActivityType;
import com.mehmetkerem.model.ActivityLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Admin paneli için aktivite loglarını filtreleme Specification'ları.
 */
public final class ActivityLogSpecification {

    private ActivityLogSpecification() {
    }

    public static Specification<ActivityLog> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<ActivityLog> hasActivityType(ActivityType activityType) {
        return (root, query, cb) -> activityType == null ? null : cb.equal(root.get("activityType"), activityType);
    }

    public static Specification<ActivityLog> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<ActivityLog> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
