package com.mehmetkerem.controller;

import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IRestPaymentController {

    ResponseEntity<PaymentResponse> processPayment(
            String userId,
            String orderId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus
    );

    ResponseEntity<PaymentResponse> getPaymentById(String paymentId);

    ResponseEntity<List<PaymentResponse>> getPaymentsByUser(String userId);

    ResponseEntity<PaymentResponse> updatePaymentStatus(String paymentId, PaymentStatus newStatus);

    ResponseEntity<String> deletePayment(String paymentId);
}
