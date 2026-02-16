package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestOrderReturnController;
import com.mehmetkerem.dto.request.OrderReturnRequest;
import com.mehmetkerem.dto.response.OrderReturnResponse;
import com.mehmetkerem.service.IOrderReturnService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import com.mehmetkerem.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/order/return")
@RequiredArgsConstructor
public class RestOrderReturnControllerImpl implements IRestOrderReturnController {

    private final IOrderReturnService orderReturnService;

    private static long requireCurrentUserId() {
        Long id = SecurityUtils.getCurrentUserId();
        if (id == null) {
            throw new InsufficientAuthenticationException("Oturum gerekli");
        }
        return id;
    }

    @Override
    @PostMapping
    public ResultData<OrderReturnResponse> createReturn(@Valid @RequestBody OrderReturnRequest request) {
        return ResultHelper.success(orderReturnService.createReturn(requireCurrentUserId(), request));
    }

    @Override
    @GetMapping("/my-returns")
    public ResultData<List<OrderReturnResponse>> getMyReturns() {
        return ResultHelper.success(orderReturnService.getReturnsByUser(requireCurrentUserId()));
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<List<OrderReturnResponse>> getAllReturns() {
        return ResultHelper.success(orderReturnService.getAllReturns());
    }

    @Override
    @PutMapping("/{returnId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<OrderReturnResponse> approve(@PathVariable Long returnId) {
        return ResultHelper.success(orderReturnService.approve(returnId));
    }

    @Override
    @PutMapping("/{returnId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<OrderReturnResponse> reject(@PathVariable Long returnId) {
        return ResultHelper.success(orderReturnService.reject(returnId));
    }
}
