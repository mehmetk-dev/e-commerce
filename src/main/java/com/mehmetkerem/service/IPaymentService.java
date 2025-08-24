package com.mehmetkerem.service;

import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public interface IPaymentService {

    PaymentResponse processPayment(String userId, String orderId, BigDecimal amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus);
    PaymentResponse getPaymentById(String id);
    List<PaymentResponse> getPaymentsByUser(String userId);
    PaymentResponse updatePaymentStatus(String paymentId, PaymentStatus newStatus);


}
