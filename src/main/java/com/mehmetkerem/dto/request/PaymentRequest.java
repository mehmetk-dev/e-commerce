package com.mehmetkerem.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentRequest {

    @NotBlank(message = "Order ID boş olamaz.")
    private String orderId;

    @NotNull(message = "Ödeme yöntemi boş olamaz.")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Ödeme durumu boş olamaz.")
    private PaymentStatus paymentStatus;

    private String transactionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    @PastOrPresent(message = "Ödeme tarihi gelecek bir zaman olamaz.")
    private LocalDateTime paidAt;

    @AssertTrue(message = "Ödeme 'PAID' ise transactionId zorunludur.")
    public boolean isTransactionIdPresentWhenPaid() {
        if (paymentStatus == PaymentStatus.PAID) {
            return transactionId != null && !transactionId.isBlank();
        }
        return true;
    }

    @AssertTrue(message = "Ödeme 'PAID' ise paidAt zorunludur.")
    public boolean isPaidAtPresentWhenPaid() {
        if (paymentStatus == PaymentStatus.PAID) {
            return paidAt != null;
        }
        return true;
    }
}
