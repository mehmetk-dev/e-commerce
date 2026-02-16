package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestCouponController;
import com.mehmetkerem.model.Coupon;
import com.mehmetkerem.service.ICouponService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
public class RestCouponControllerImpl implements IRestCouponController {

    private final ICouponService couponService;

    @Override
    @PostMapping("/create")
    public ResultData<Coupon> createCoupon(@RequestParam String code,
            @RequestParam BigDecimal discount,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam int days) {
        return ResultHelper.success(couponService.createCoupon(code, discount, minAmount, days));
    }

    @Override
    @GetMapping("/{code}")
    public ResultData<Coupon> getCoupon(@PathVariable String code) {
        return ResultHelper.success(couponService.getCouponByCode(code));
    }

    @Override
    @PostMapping("/apply")
    public ResultData<BigDecimal> applyCoupon(@RequestParam String code, @RequestParam BigDecimal total) {
        return ResultHelper.success(couponService.applyCoupon(code, total));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResultData<String> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResultHelper.success("Kupon silindi.");
    }
}
