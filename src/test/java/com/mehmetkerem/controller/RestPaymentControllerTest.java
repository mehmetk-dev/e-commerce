package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestPaymentControllerImpl;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.service.IPaymentService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestPaymentControllerTest {

    @Mock
    private IPaymentService paymentService;

    @InjectMocks
    private RestPaymentControllerImpl controller;

    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        SecurityTestUtils.setCurrentUser();
        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .paymentStatus(PaymentStatus.PAID)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clearContext();
    }

    @Test
    @DisplayName("processPayment - ödeme yanıtı döner")
    void processPayment_ShouldReturnPaymentResponse() {
        when(paymentService.processPayment(anyLong(), anyLong(), any(), any())).thenReturn(paymentResponse);

        ResultData<PaymentResponse> response = controller.processPayment(10L,
                new BigDecimal("99.99"), PaymentMethod.CREDIT_CARD);

        assertTrue(response.isStatus());
        PaymentResponse body = response.getData();
        assertNotNull(body);
        assertEquals(1L, body.getId());
        verify(paymentService).processPayment(eq(SecurityTestUtils.DEFAULT_USER_ID), eq(10L), eq(new BigDecimal("99.99")),
                eq(PaymentMethod.CREDIT_CARD));
    }

    @Test
    @DisplayName("getPaymentById - kullanıcı kendi ödemesini görür")
    void getPaymentById_ShouldReturnPayment() {
        when(paymentService.getPaymentResponseByIdAndUserId(eq(1L), eq(SecurityTestUtils.DEFAULT_USER_ID)))
                .thenReturn(paymentResponse);

        ResultData<PaymentResponse> response = controller.getPaymentById(1L);

        assertTrue(response.isStatus());
        PaymentResponse body = response.getData();
        assertNotNull(body);
        assertEquals(1L, body.getId());
        verify(paymentService).getPaymentResponseByIdAndUserId(eq(1L), eq(SecurityTestUtils.DEFAULT_USER_ID));
    }

    @Test
    @DisplayName("getMyPayments - liste döner")
    void getMyPayments_ShouldReturnList() {
        when(paymentService.getPaymentsByUser(SecurityTestUtils.DEFAULT_USER_ID)).thenReturn(List.of(paymentResponse));

        ResultData<List<PaymentResponse>> response = controller.getMyPayments();

        assertTrue(response.isStatus());
        List<PaymentResponse> body = response.getData();
        assertNotNull(body);
        assertEquals(1, body.size());
        verify(paymentService).getPaymentsByUser(SecurityTestUtils.DEFAULT_USER_ID);
    }

    @Test
    @DisplayName("updatePaymentStatus - ADMIN güncel ödeme döner")
    void updatePaymentStatus_WhenAdmin_ShouldReturnUpdated() {
        SecurityTestUtils.setCurrentUser(SecurityTestUtils.DEFAULT_USER_ID, com.mehmetkerem.enums.Role.ADMIN);
        try {
            when(paymentService.updatePaymentStatus(1L, PaymentStatus.REFUNDED)).thenReturn(paymentResponse);

            ResultData<PaymentResponse> response = controller.updatePaymentStatus(1L, PaymentStatus.REFUNDED);

            assertTrue(response.isStatus());
            verify(paymentService).updatePaymentStatus(1L, PaymentStatus.REFUNDED);
        } finally {
            SecurityTestUtils.clearContext();
        }
    }

    @Test
    @DisplayName("deletePayment - ADMIN mesaj döner")
    void deletePayment_WhenAdmin_ShouldReturnMessage() {
        SecurityTestUtils.setCurrentUser(SecurityTestUtils.DEFAULT_USER_ID, com.mehmetkerem.enums.Role.ADMIN);
        try {
            when(paymentService.deletePayment(1L)).thenReturn("1 ID'li ödeme silinmiştir!");

            ResultData<String> response = controller.deletePayment(1L);

            assertTrue(response.isStatus());
            String body = response.getData();
            assertNotNull(body);
            assertTrue(body.contains("1"));
            verify(paymentService).deletePayment(1L);
        } finally {
            SecurityTestUtils.clearContext();
        }
    }
}
