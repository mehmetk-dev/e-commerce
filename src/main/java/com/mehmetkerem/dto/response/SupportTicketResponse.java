package com.mehmetkerem.dto.response;

import com.mehmetkerem.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupportTicketResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String subject;
    private String message;
    private TicketStatus status;
    private String adminReply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
