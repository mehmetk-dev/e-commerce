package com.mehmetkerem.controller;

import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.util.ResultData;

import java.math.BigDecimal;
import java.util.List;

public interface IRestPaymentController {

    /** Oturum açmış kullanıcı için ödeme işler; orderId siparişin bu kullanıcıya ait olduğu doğrulanır. */
    ResultData<PaymentResponse> processPayment(
            Long orderId,
            BigDecimal amount,
            PaymentMethod paymentMethod);

    ResultData<PaymentResponse> getPaymentById(Long paymentId);

    /** Oturum açmış kullanıcının ödemelerini döner. */
    ResultData<List<PaymentResponse>> getMyPayments();

    ResultData<PaymentResponse> updatePaymentStatus(Long paymentId, PaymentStatus newStatus);

    ResultData<String> deletePayment(Long paymentId);
}
