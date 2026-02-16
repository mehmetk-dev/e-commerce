package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.model.Order;
import com.mehmetkerem.model.Payment;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.PaymentRepository;
import com.mehmetkerem.service.payment.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private OrderServiceImpl orderService;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 10L;
    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .build();
        payment = Payment.builder()
                .id(1L)
                .userId(USER_ID)
                .orderId(ORDER_ID)
                .amount(new BigDecimal("199.99"))
                .paymentStatus(PaymentStatus.PAID)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("processPayment - başarılı ödemede sipariş PAID olur")
    void processPayment_WhenSuccess_ShouldUpdateOrderToPaid() {
        when(userService.getUserById(USER_ID)).thenReturn(User.builder().id(USER_ID).build());
        when(orderService.getOrderById(ORDER_ID)).thenReturn(order);
        when(paymentStrategy.pay(any(BigDecimal.class))).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(orderService.getOrderResponseById(ORDER_ID)).thenReturn(new OrderResponse());

        PaymentResponse result = paymentService.processPayment(
                USER_ID, ORDER_ID, new BigDecimal("199.99"), PaymentMethod.CREDIT_CARD);

        assertNotNull(result);
        verify(orderService).updateOrderStatus(ORDER_ID, com.mehmetkerem.enums.OrderStatus.PAID);
        verify(orderService).updatePaymentStatus(ORDER_ID, PaymentStatus.PAID);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("processPayment - ödeme başarısızsa sipariş PAID yapılmaz")
    void processPayment_WhenFails_ShouldNotUpdateOrderToPaid() {
        when(userService.getUserById(USER_ID)).thenReturn(User.builder().id(USER_ID).build());
        when(orderService.getOrderById(ORDER_ID)).thenReturn(order);
        when(paymentStrategy.pay(any(BigDecimal.class))).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(orderService.getOrderResponseById(ORDER_ID)).thenReturn(new OrderResponse());

        PaymentResponse result = paymentService.processPayment(
                USER_ID, ORDER_ID, new BigDecimal("199.99"), PaymentMethod.CREDIT_CARD);

        assertNotNull(result);
        verify(orderService, never()).updateOrderStatus(eq(ORDER_ID), eq(com.mehmetkerem.enums.OrderStatus.PAID));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("getPaymentById - ödeme bulunamazsa NotFoundException")
    void getPaymentById_WhenNotExists_ShouldThrowNotFoundException() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(999L));
    }

    @Test
    @DisplayName("getPaymentById - mevcut ödeme döner")
    void getPaymentById_WhenExists_ShouldReturnPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Payment result = paymentService.getPaymentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
    }

    @Test
    @DisplayName("getPaymentResponseByIdAndUserId - sahip değilse BadRequestException")
    void getPaymentResponseByIdAndUserId_WhenNotOwner_ShouldThrow() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class,
                () -> paymentService.getPaymentResponseByIdAndUserId(1L, 999L));

        verify(paymentRepository).findById(1L);
    }

    @Test
    @DisplayName("getPaymentResponseByIdAndUserId - sahip ise ödeme döner")
    void getPaymentResponseByIdAndUserId_WhenOwner_ShouldReturnPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(orderService.getOrderResponseById(ORDER_ID)).thenReturn(new OrderResponse());

        PaymentResponse result = paymentService.getPaymentResponseByIdAndUserId(1L, USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("updatePaymentStatus - durum güncellenir")
    void updatePaymentStatus_ShouldUpdateAndReturn() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(orderService.getOrderResponseById(ORDER_ID)).thenReturn(new OrderResponse());

        PaymentResponse result = paymentService.updatePaymentStatus(1L, PaymentStatus.REFUNDED);

        assertNotNull(result);
        verify(paymentRepository).save(argThat(p -> p.getPaymentStatus() == PaymentStatus.REFUNDED));
    }

    @Test
    @DisplayName("getPaymentsByUser - kullanıcının ödemeleri listelenir")
    void getPaymentsByUser_ShouldReturnUserPayments() {
        when(paymentRepository.findByUserId(USER_ID)).thenReturn(List.of(payment));
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(orderService.getOrderResponseById(ORDER_ID)).thenReturn(new OrderResponse());

        List<PaymentResponse> result = paymentService.getPaymentsByUser(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("deletePayment - ödeme silinir")
    void deletePayment_WhenExists_ShouldDeleteAndReturnMessage() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        doNothing().when(paymentRepository).delete(payment);

        String result = paymentService.deletePayment(1L);

        assertTrue(result.contains("1"));
        assertTrue(result.contains("ödeme"));
        verify(paymentRepository).delete(payment);
    }
}
