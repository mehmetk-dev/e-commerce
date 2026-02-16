package com.mehmetkerem.service;

import com.mehmetkerem.model.Coupon;
import java.math.BigDecimal;

public interface ICouponService {
    Coupon createCoupon(String code, BigDecimal discountAmount, BigDecimal minCartAmount, int daysValid);

    Coupon getCouponByCode(String code);

    BigDecimal applyCoupon(String code, BigDecimal cartTotal);

    void deleteCoupon(Long couponId);
}
