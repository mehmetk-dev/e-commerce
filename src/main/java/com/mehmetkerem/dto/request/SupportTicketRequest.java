package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupportTicketRequest {

    @NotBlank(message = "Konu boş olamaz.")
    @Size(max = 500)
    private String subject;

    @NotBlank(message = "Mesaj boş olamaz.")
    private String message;
}
