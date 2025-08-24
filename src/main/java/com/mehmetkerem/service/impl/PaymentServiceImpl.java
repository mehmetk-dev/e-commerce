package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.model.Payment;
import com.mehmetkerem.repository.PaymentRepository;
import com.mehmetkerem.service.IPaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final UserServiceImpl userService;
    private final OrderServiceImpl orderService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, UserServiceImpl userService, OrderServiceImpl orderService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.orderService = orderService;
    }

    @Transactional
    @Override
    public PaymentResponse processPayment(String userId, String orderId, BigDecimal amount, PaymentMethod paymentMethod,PaymentStatus paymentStatus) {

        userService.getUserById(userId);
        orderService.getOrderById(orderId);

        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .paymentStatus(paymentStatus)
                .paymentMethod(paymentMethod)
                .createdAt(LocalDateTime.now())
                .build();

        return convertPaymentToResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND,id,"ödeme")));
        return convertPaymentToResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(String userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::convertPaymentToResponse)
                .toList();
    }

    @Transactional
    @Override
    public PaymentResponse updatePaymentStatus(String paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setPaymentStatus(newStatus);
        return convertPaymentToResponse(paymentRepository.save(payment));

    }

    private PaymentResponse convertPaymentToResponse(Payment payment){

        UserResponse userResponse = userService.getUserResponseById(payment.getUserId());
        OrderResponse orderResponse = orderService.getOrderResponseById(payment.getOrderId());

        return PaymentResponse.builder()
                .id(payment.getId())
                .order(orderResponse)
                .user(userResponse)
                .createdAt(payment.getCreatedAt())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }
}
