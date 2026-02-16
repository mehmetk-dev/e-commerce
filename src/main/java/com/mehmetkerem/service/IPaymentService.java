package com.mehmetkerem.service;

import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface IPaymentService {

    PaymentResponse processPayment(Long userId, Long orderId, BigDecimal amount, PaymentMethod paymentMethod);

    PaymentResponse getPaymentResponseById(Long id);

    /** Sadece ödeme sahibi veya ADMIN erişebilir. */
    PaymentResponse getPaymentResponseByIdAndUserId(Long id, Long userId);

    Payment getPaymentById(Long id);

    List<PaymentResponse> getPaymentsByUser(Long userId);

    PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus newStatus);

    /** Sadece ADMIN ödeme siler. */
    String deletePayment(Long id);
}
