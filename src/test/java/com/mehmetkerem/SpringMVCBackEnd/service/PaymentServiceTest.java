package com.mehmetkerem.SpringMVCBackEnd.service;

import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.model.Payment;
import com.mehmetkerem.repository.PaymentRepository;
import com.mehmetkerem.service.impl.OrderServiceImpl;
import com.mehmetkerem.service.impl.PaymentServiceImpl;
import com.mehmetkerem.service.impl.UserServiceImpl;
import com.mehmetkerem.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private OrderServiceImpl orderService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // mock'ları init et
    }

    // processPayment: kullanıcı ve sipariş mevcutsa ödeme oluşturulur ve response döner
    @Test
    void processPayment_success() {
        String userId = "u1";
        String orderId = "o1";
        BigDecimal amount = BigDecimal.TEN;

        // getUserById / getOrderById değer döndürmeli (void DEĞİL)
        when(userService.getUserById(userId)).thenReturn(new com.mehmetkerem.model.User());
        when(orderService.getOrderById(orderId)).thenReturn(com.mehmetkerem.model.Order.builder().build());

        // save edilen payment'a id verelim
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId("pay1");
            return p;
        });

        // convertPaymentToResponse için
        UserResponse ur = new UserResponse();
        ur.setId(userId);
        OrderResponse or = new OrderResponse();
        or.setId(orderId);
        when(userService.getUserResponseById(userId)).thenReturn(ur);
        when(orderService.getOrderResponseById(orderId)).thenReturn(or);

        PaymentResponse resp = paymentService.processPayment(
                userId, orderId, amount, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID);

        assertNotNull(resp);
        assertEquals("pay1", resp.getId());
        assertNotNull(resp.getUser());
        assertNotNull(resp.getOrder());
        verify(paymentRepository).save(any(Payment.class));
    }


    // getPaymentById: ödeme bulunursa entity döner
    @Test
    void getPaymentById_success() {
        Payment payment = Payment.builder().id("pay1").build();
        when(paymentRepository.findById("pay1")).thenReturn(Optional.of(payment));

        Payment result = paymentService.getPaymentById("pay1");

        assertEquals("pay1", result.getId()); // id eşleşmeli
    }

    // getPaymentById: ödeme bulunamazsa NotFoundException atmalı
    @Test
    void getPaymentById_notFound() {
        when(paymentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.getPaymentById("missing")); // bulunamayan id
    }

    // getPaymentResponseById: ödeme response'a maplenmeli (kullanıcı/sipariş detaylı)
    @Test
    void getPaymentResponseById_success() {
        Payment payment = Payment.builder()
                .id("pay1")
                .userId("u1")
                .orderId("o1")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById("pay1")).thenReturn(Optional.of(payment));

        UserResponse ur = new UserResponse();
        ur.setId("u1");
        OrderResponse or = new OrderResponse();
        or.setId("o1");
        when(userService.getUserResponseById("u1")).thenReturn(ur);
        when(orderService.getOrderResponseById("o1")).thenReturn(or);

        PaymentResponse resp = paymentService.getPaymentResponseById("pay1");

        assertNotNull(resp);                   // response null olmamalı
        assertEquals("pay1", resp.getId());    // id eşleşmeli
        assertEquals("u1", resp.getUser().getId());  // user doldurulmalı
        assertEquals("o1", resp.getOrder().getId()); // order doldurulmalı
    }

    // getPaymentsByUser: kullanıcının tüm ödemeleri response listesine maplenmeli
    @Test
    void getPaymentsByUser_success() {
        Payment p1 = Payment.builder().id("p1").userId("u1").orderId("o1").createdAt(LocalDateTime.now()).build();
        Payment p2 = Payment.builder().id("p2").userId("u1").orderId("o2").createdAt(LocalDateTime.now()).build();

        when(paymentRepository.findByUserId("u1")).thenReturn(List.of(p1, p2));

        // her ödeme için convertPaymentToResponse çağrısında ihtiyaç duyulacak
        when(userService.getUserResponseById("u1")).thenReturn(new UserResponse());
        when(orderService.getOrderResponseById(anyString())).thenReturn(new OrderResponse());

        List<PaymentResponse> results = paymentService.getPaymentsByUser("u1");

        assertEquals(2, results.size()); // 2 ödeme dönmeli
    }

    // updatePaymentStatus: ödeme durumu güncellenip response dönmeli
    @Test
    void updatePaymentStatus_success() {
        Payment payment = Payment.builder()
                .id("pay1")
                .userId("u1")
                .orderId("o1")
                .paymentStatus(PaymentStatus.REFUNDED)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById("pay1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        when(userService.getUserResponseById("u1")).thenReturn(new UserResponse());
        when(orderService.getOrderResponseById("o1")).thenReturn(new OrderResponse());

        PaymentResponse resp = paymentService.updatePaymentStatus("pay1", PaymentStatus.PAID);

        assertNotNull(resp);                              // response null olmamalı
        assertEquals(PaymentStatus.PAID, resp.getPaymentStatus()); // durum güncellenmeli
        verify(paymentRepository).save(payment);          // save çağrılmış olmalı
    }

    // deletePayment: ödeme silinir ve mesaj dönülür
    @Test
    void deletePayment_success() {
        Payment payment = Payment.builder().id("pay1").build();
        when(paymentRepository.findById("pay1")).thenReturn(Optional.of(payment));

        String result = paymentService.deletePayment("pay1");

        String expected = String.format(Messages.DELETE_VALUE, "pay1", "ödeme");
        assertEquals(expected, result); // mesaj formatı beklenen gibi
        verify(paymentRepository).delete(payment); // delete çağrısı yapılmış olmalı
    }
}
