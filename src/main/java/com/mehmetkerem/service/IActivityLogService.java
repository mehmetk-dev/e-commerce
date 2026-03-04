package com.mehmetkerem.service;

import com.mehmetkerem.dto.response.ActivityLogResponse;
import com.mehmetkerem.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IActivityLogService {

    /**
     * Aktivite logla — asenkron olarak kaydedilir, ana iş akışını yavaşlatmaz.
     */
    void log(ActivityType activityType, Long userId, String userEmail,
            String entityType, Long entityId, String description, String metadata);

    /**
     * Basit loglama — sadece tip, kullanıcı ve açıklama.
     */
    void log(ActivityType activityType, Long userId, String description);

    /** Tüm logları sayfalı getir (admin). */
    Page<ActivityLogResponse> getAllLogs(Pageable pageable);

    /** Kullanıcıya göre loglar (admin). */
    Page<ActivityLogResponse> getLogsByUserId(Long userId, Pageable pageable);

    /** Aktivite tipine göre loglar (admin). */
    Page<ActivityLogResponse> getLogsByType(ActivityType activityType, Pageable pageable);

    /** Filtreleme ile arama (admin). */
    Page<ActivityLogResponse> searchLogs(Long userId, ActivityType activityType,
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    /** Belirli bir kullanıcının son aktiviteleri. */
    List<ActivityLogResponse> getRecentActivityByUser(Long userId, int days);
}
