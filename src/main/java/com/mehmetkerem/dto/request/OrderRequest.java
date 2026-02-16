package com.mehmetkerem.dto.request;

import com.mehmetkerem.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

    @NotNull(message = "Adres ID boş olamaz.")
    private Long addressId;

    /** İstemci tarafından gönderilse bile yok sayılır; sipariş her zaman PENDING ile oluşturulur. */
    private PaymentStatus paymentStatus;

    private String note;
}
