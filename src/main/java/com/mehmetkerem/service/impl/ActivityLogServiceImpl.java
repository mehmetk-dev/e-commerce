package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.response.ActivityLogResponse;
import com.mehmetkerem.enums.ActivityType;
import com.mehmetkerem.model.ActivityLog;
import com.mehmetkerem.repository.ActivityLogRepository;
import com.mehmetkerem.repository.specification.ActivityLogSpecification;
import com.mehmetkerem.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements IActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    @Async
    public void log(ActivityType activityType, Long userId, String userEmail,
            String entityType, Long entityId, String description, String metadata) {
        try {
            String ipAddress = resolveIpAddress();
            String userAgent = resolveUserAgent();
            String userName = resolveUserName();

            ActivityLog activityLog = ActivityLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .userName(userName)
                    .activityType(activityType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            activityLogRepository.save(activityLog);
            log.debug("Aktivite loglandı: {} — Kullanıcı: {} — {}", activityType, userId, description);
        } catch (Exception e) {
            // Loglama hatası ana iş akışını durdurmamalı
            log.error("Aktivite loglama hatası: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void log(ActivityType activityType, Long userId, String description) {
        log(activityType, userId, null, null, null, description, null);
    }

    @Override
    public Page<ActivityLogResponse> getAllLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ActivityLogResponse> getLogsByUserId(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ActivityLogResponse> getLogsByType(ActivityType activityType, Pageable pageable) {
        return activityLogRepository.findByActivityTypeOrderByCreatedAtDesc(activityType, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ActivityLogResponse> searchLogs(Long userId, ActivityType activityType,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<ActivityLog> spec = Specification
                .where(ActivityLogSpecification.hasUserId(userId))
                .and(ActivityLogSpecification.hasActivityType(activityType))
                .and(ActivityLogSpecification.createdAfter(from))
                .and(ActivityLogSpecification.createdBefore(to));

        return activityLogRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public List<ActivityLogResponse> getRecentActivityByUser(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return activityLogRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Private Helpers ──

    private ActivityLogResponse toResponse(ActivityLog entity) {
        return ActivityLogResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .userEmail(entity.getUserEmail())
                .activityType(entity.getActivityType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .description(entity.getDescription())
                .metadata(entity.getMetadata())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String resolveIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                var request = attrs.getRequest();
                String forwarded = request.getHeader("X-Forwarded-For");
                return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.trace("IP adresi çözümlenemedi: {}", e.getMessage());
        }
        return null;
    }

    private String resolveUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.trace("User-Agent çözümlenemedi: {}", e.getMessage());
        }
        return null;
    }

    private String resolveUserName() {
        try {
            com.mehmetkerem.model.User user = com.mehmetkerem.util.SecurityUtils.getCurrentUser();
            return user != null ? user.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
