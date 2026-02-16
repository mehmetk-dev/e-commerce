package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.model.Payment;
import com.mehmetkerem.repository.PaymentRepository;
import com.mehmetkerem.service.IPaymentService;
import com.mehmetkerem.service.payment.PaymentStrategy;
import com.mehmetkerem.util.Messages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@SuppressWarnings("null")
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final UserServiceImpl userService;
    private final OrderServiceImpl orderService;
    private final PaymentStrategy paymentStrategy;

    public PaymentServiceImpl(PaymentRepository paymentRepository, UserServiceImpl userService,
            OrderServiceImpl orderService, PaymentStrategy paymentStrategy) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.orderService = orderService;
        this.paymentStrategy = paymentStrategy;
    }

    @Transactional
    @Override
    public PaymentResponse processPayment(Long userId, Long orderId, BigDecimal amount, PaymentMethod paymentMethod) {

        log.info("Ödeme işlemi başlatıldı. Kullanıcı ID: {}, Sipariş ID: {}, Tutar: {}", userId, orderId, amount);
        userService.getUserById(userId);
        var order = orderService.getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Bu sipariş size ait değil.");
        }

        boolean isSuccess = paymentStrategy.pay(amount);
        log.debug("Ödeme stratejisi sonucu: {} (Strateji: {})", isSuccess, paymentStrategy.getClass().getSimpleName());

        PaymentStatus finalStatus = isSuccess ? PaymentStatus.PAID : PaymentStatus.UNPAID;

        if (isSuccess) {
            log.info("Ödeme başarılı! Sipariş durumu güncelleniyor. Sipariş ID: {}", orderId);
            orderService.updateOrderStatus(orderId, com.mehmetkerem.enums.OrderStatus.PAID);
            orderService.updatePaymentStatus(orderId, PaymentStatus.PAID);
        } else {
            log.warn("Ödeme başarısız. Kullanıcı ID: {}, Sipariş ID: {}", userId, orderId);
        }

        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .paymentStatus(finalStatus)
                .paymentMethod(paymentMethod)
                .createdAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Ödeme kaydı oluşturuldu. Ödeme ID: {}, Durum: {}", savedPayment.getId(), finalStatus);

        return convertPaymentToResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentResponseById(Long id) {
        return convertPaymentToResponse(getPaymentById(id));
    }

    @Override
    public PaymentResponse getPaymentResponseByIdAndUserId(Long id, Long userId) {
        Payment payment = getPaymentById(id);
        if (!payment.getUserId().equals(userId)) {
            throw new BadRequestException("Bu ödemeye erişim yetkiniz yok.");
        }
        return convertPaymentToResponse(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, id, "ödeme")));
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        log.debug("Kullanıcı ödemeleri getiriliyor. Kullanıcı ID: {}", userId);
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::convertPaymentToResponse)
                .toList();
    }

    @Transactional
    @Override
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        log.info("Ödeme durumu güncelleniyor. Ödeme ID: {}, Yeni Durum: {}", paymentId, newStatus);
        Payment payment = getPaymentById(paymentId);
        payment.setPaymentStatus(newStatus);
        return convertPaymentToResponse(paymentRepository.save(payment));
    }

    @Override
    public String deletePayment(Long id) {
        log.warn("Ödeme kaydı siliniyor. Ödeme ID: {}", id);
        paymentRepository.delete(getPaymentById(id));
        return String.format(Messages.DELETE_VALUE, id, "ödeme");
    }

    private PaymentResponse convertPaymentToResponse(Payment payment) {
        UserResponse userResponse = userService.getUserResponseById(payment.getUserId());
        OrderResponse orderResponse = orderService.getOrderResponseById(payment.getOrderId());

        return PaymentResponse.builder()
                .id(payment.getId())
                .order(orderResponse)
                .user(userResponse)
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }
}
