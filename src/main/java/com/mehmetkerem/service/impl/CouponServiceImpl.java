package com.mehmetkerem.service.impl;

import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.model.Coupon;
import com.mehmetkerem.repository.CouponRepository;
import com.mehmetkerem.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {

    private final CouponRepository couponRepository;

    @Override
    public Coupon createCoupon(String code, BigDecimal discountAmount, BigDecimal minCartAmount, int daysValid) {
        if (couponRepository.findByCode(code).isPresent()) {
            throw new BadRequestException("Bu kupon kodu zaten mevcut!");
        }

        Coupon coupon = Coupon.builder()
                .code(code.toUpperCase())
                .discountAmount(discountAmount)
                .minCartAmount(minCartAmount != null ? minCartAmount : BigDecimal.ZERO)
                .expirationDate(LocalDateTime.now().plusDays(daysValid))
                .isActive(true)
                .usageCount(0)
                .build();

        return couponRepository.save(coupon);
    }

    @Override
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Kupon bulunamadı!"));
    }

    @Transactional
    @Override
    public BigDecimal applyCoupon(String code, BigDecimal cartTotal) {
        Coupon coupon = getCouponByCode(code);

        if (!coupon.isActive()) {
            throw new BadRequestException("Bu kupon pasif durumda.");
        }

        if (LocalDateTime.now().isAfter(coupon.getExpirationDate())) {
            throw new BadRequestException("Bu kuponun süresi dolmuş.");
        }

        // Kullanım limiti kontrolü
        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() > 0
                && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Bu kuponun kullanım limiti dolmuştur.");
        }

        if (cartTotal.compareTo(coupon.getMinCartAmount()) < 0) {
            throw new BadRequestException(
                    "Sepet tutarı bu kupon için yetersiz. Minimum tutar: " + coupon.getMinCartAmount());
        }

        // Kullanım sayacını artır
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        BigDecimal newTotal = cartTotal.subtract(coupon.getDiscountAmount());
        return newTotal.max(BigDecimal.ZERO); // Total cannot be negative
    }

    @Override
    public void deleteCoupon(Long couponId) {
        if (!couponRepository.existsById(couponId)) {
            throw new NotFoundException("Kupon bulunamadı id: " + couponId);
        }
        couponRepository.deleteById(couponId);
    }
}
