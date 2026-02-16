package com.mehmetkerem.service.payment;

import com.mehmetkerem.service.payment.impl.MockPaymentStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MockPaymentStrategyTest {

    private final MockPaymentStrategy strategy = new MockPaymentStrategy();

    @Test
    @DisplayName("pay - her zaman true döner (mock başarılı ödeme)")
    void pay_AlwaysReturnsTrue() {
        assertTrue(strategy.pay(BigDecimal.ZERO));
        assertTrue(strategy.pay(new BigDecimal("99.99")));
        assertTrue(strategy.pay(new BigDecimal("1000")));
    }
}
