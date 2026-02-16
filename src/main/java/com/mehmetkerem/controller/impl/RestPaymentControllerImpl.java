package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestPaymentController;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.service.IPaymentService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import com.mehmetkerem.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/payment")
public class RestPaymentControllerImpl implements IRestPaymentController {

    private final IPaymentService paymentService;

    public RestPaymentControllerImpl(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    private static long requireCurrentUserId() {
        Long id = SecurityUtils.getCurrentUserId();
        if (id == null) {
            throw new InsufficientAuthenticationException("Oturum gerekli");
        }
        return id;
    }

    @Override
    @PostMapping("/process")
    public ResultData<PaymentResponse> processPayment(
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam PaymentMethod paymentMethod) {
        return ResultHelper.success(paymentService.processPayment(requireCurrentUserId(), orderId, amount, paymentMethod));
    }

    @Override
    @GetMapping("/{paymentId}")
    public ResultData<PaymentResponse> getPaymentById(@PathVariable("paymentId") Long paymentId) {
        Long userId = requireCurrentUserId();
        var user = SecurityUtils.getCurrentUser();
        if (user != null && user.getRole() == com.mehmetkerem.enums.Role.ADMIN) {
            return ResultHelper.success(paymentService.getPaymentResponseById(paymentId));
        }
        return ResultHelper.success(paymentService.getPaymentResponseByIdAndUserId(paymentId, userId));
    }

    @Override
    @GetMapping("/my-payments")
    public ResultData<List<PaymentResponse>> getMyPayments() {
        return ResultHelper.success(paymentService.getPaymentsByUser(requireCurrentUserId()));
    }

    @Override
    @PutMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<PaymentResponse> updatePaymentStatus(@PathVariable("paymentId") Long paymentId,
            @RequestParam PaymentStatus newStatus) {
        return ResultHelper.success(paymentService.updatePaymentStatus(paymentId, newStatus));
    }

    @Override
    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<String> deletePayment(@PathVariable("paymentId") Long paymentId) {
        return ResultHelper.success(paymentService.deletePayment(paymentId));
    }
}
