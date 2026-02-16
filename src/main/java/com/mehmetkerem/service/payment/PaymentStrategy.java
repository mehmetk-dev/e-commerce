package com.mehmetkerem.service.payment;

import java.math.BigDecimal;

public interface PaymentStrategy {
    boolean pay(BigDecimal amount);
}
