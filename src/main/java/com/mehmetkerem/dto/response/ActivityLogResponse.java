package com.mehmetkerem.dto.response;

import com.mehmetkerem.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private ActivityType activityType;
    private String entityType;
    private Long entityId;
    private String description;
    private String metadata;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
