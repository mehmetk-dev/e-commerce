package com.mehmetkerem.controller;

import com.mehmetkerem.model.Coupon;
import com.mehmetkerem.util.ResultData;

import java.math.BigDecimal;

public interface IRestCouponController {
    ResultData<Coupon> createCoupon(String code, BigDecimal discount, BigDecimal minAmount, int days);

    ResultData<Coupon> getCoupon(String code);

    ResultData<BigDecimal> applyCoupon(String code, BigDecimal total);

    ResultData<String> deleteCoupon(Long id);
}
