package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestPaymentController;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.service.IPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class RestPaymentControllerImpl implements IRestPaymentController {

    private final IPaymentService paymentService;

    public RestPaymentControllerImpl(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestParam String userId,
            @RequestParam String orderId,
            @RequestParam BigDecimal amount,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam PaymentStatus paymentStatus
    ) {
        return ResponseEntity.ok(paymentService.processPayment(userId, orderId, amount, paymentMethod, paymentStatus));
    }


    @Override
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentResponseById(paymentId));
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }

    @Override
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(@PathVariable String paymentId,
                                                               @RequestParam PaymentStatus newStatus) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, newStatus));
    }

    @Override
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<String> deletePayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.deletePayment(paymentId));
    }
}
