package com.mehmetkerem.dto.request;

import com.mehmetkerem.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketReplyRequest {

    @NotNull(message = "Durum boş olamaz.")
    private TicketStatus status;

    private String adminReply;
}
