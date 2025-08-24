package com.mehmetkerem.service;

import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface IPaymentService {

    PaymentResponse processPayment(String userId, String orderId, BigDecimal amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus);

    PaymentResponse getPaymentResponseById(String id);

    Payment getPaymentById(String id);

    List<PaymentResponse> getPaymentsByUser(String userId);

    PaymentResponse updatePaymentStatus(String paymentId, PaymentStatus newStatus);

    String deletePayment(String id);

}
