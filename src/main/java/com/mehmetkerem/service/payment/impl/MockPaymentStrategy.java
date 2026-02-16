package com.mehmetkerem.service.payment.impl;

import com.mehmetkerem.service.payment.PaymentStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("mockPaymentStrategy")
public class MockPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean pay(BigDecimal amount) {
        // Simulating a successful payment
        return true;
    }
}
