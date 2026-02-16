package com.mehmetkerem.service.impl;

import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.model.Coupon;
import com.mehmetkerem.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .id(1L)
                .code("INDIRIM10")
                .discountAmount(new BigDecimal("10"))
                .minCartAmount(new BigDecimal("100"))
                .expirationDate(LocalDateTime.now().plusDays(7))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("createCoupon - yeni kupon oluşturulur")
    void createCoupon_WhenCodeNotExists_ShouldCreate() {
        when(couponRepository.findByCode("indirim10")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        Coupon result = couponService.createCoupon("indirim10", new BigDecimal("10"),
                new BigDecimal("100"), 7);

        assertNotNull(result);
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("createCoupon - aynı kod varsa BadRequestException fırlatır")
    void createCoupon_WhenCodeExists_ShouldThrowBadRequestException() {
        when(couponRepository.findByCode("INDIRIM10")).thenReturn(Optional.of(coupon));

        assertThrows(BadRequestException.class, () ->
                couponService.createCoupon("INDIRIM10", new BigDecimal("10"),
                        new BigDecimal("100"), 7));
        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCouponByCode - kupon bulunur")
    void getCouponByCode_WhenExists_ShouldReturnCoupon() {
        when(couponRepository.findByCode("INDIRIM10")).thenReturn(Optional.of(coupon));

        Coupon result = couponService.getCouponByCode("INDIRIM10");

        assertNotNull(result);
        assertEquals("INDIRIM10", result.getCode());
        assertEquals(new BigDecimal("10"), result.getDiscountAmount());
    }

    @Test
    @DisplayName("getCouponByCode - kupon yoksa NotFoundException fırlatır")
    void getCouponByCode_WhenNotExists_ShouldThrowNotFoundException() {
        when(couponRepository.findByCode("YOK")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> couponService.getCouponByCode("YOK"));
    }

    @Test
    @DisplayName("applyCoupon - geçerli kupon uygulanır")
    void applyCoupon_WhenValid_ShouldReturnDiscountedTotal() {
        when(couponRepository.findByCode("INDIRIM10")).thenReturn(Optional.of(coupon));
        BigDecimal cartTotal = new BigDecimal("150");

        BigDecimal result = couponService.applyCoupon("INDIRIM10", cartTotal);

        assertEquals(new BigDecimal("140"), result);
    }

    @Test
    @DisplayName("applyCoupon - minimum tutar altında BadRequestException")
    void applyCoupon_WhenCartBelowMin_ShouldThrowBadRequestException() {
        when(couponRepository.findByCode("INDIRIM10")).thenReturn(Optional.of(coupon));
        BigDecimal cartTotal = new BigDecimal("50");

        assertThrows(BadRequestException.class, () -> couponService.applyCoupon("INDIRIM10", cartTotal));
    }

    @Test
    @DisplayName("applyCoupon - süresi dolmuş kupon BadRequestException")
    void applyCoupon_WhenExpired_ShouldThrowBadRequestException() {
        coupon.setExpirationDate(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCode("INDIRIM10")).thenReturn(Optional.of(coupon));

        assertThrows(BadRequestException.class, () ->
                couponService.applyCoupon("INDIRIM10", new BigDecimal("150")));
    }

    @Test
    @DisplayName("applyCoupon - pasif kupon BadRequestException")
    void applyCoupon_WhenInactive_ShouldThrowBadRequestException() {
        coupon.setActive(false);
        when(couponRepository.findByCode("INDIRIM10")).thenReturn(Optional.of(coupon));

        assertThrows(BadRequestException.class, () ->
                couponService.applyCoupon("INDIRIM10", new BigDecimal("150")));
    }

    @Test
    @DisplayName("applyCoupon - indirim toplamdan büyükse 0 döner")
    void applyCoupon_WhenDiscountExceedsTotal_ShouldReturnZero() {
        coupon.setDiscountAmount(new BigDecimal("200"));
        when(couponRepository.findByCode("INDIRIM10")).thenReturn(Optional.of(coupon));

        BigDecimal result = couponService.applyCoupon("INDIRIM10", new BigDecimal("150"));

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("deleteCoupon - kupon silinir")
    void deleteCoupon_WhenExists_ShouldDelete() {
        when(couponRepository.existsById(1L)).thenReturn(true);
        doNothing().when(couponRepository).deleteById(1L);

        couponService.deleteCoupon(1L);

        verify(couponRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteCoupon - olmayan id ile NotFoundException")
    void deleteCoupon_WhenNotExists_ShouldThrowNotFoundException() {
        when(couponRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> couponService.deleteCoupon(999L));
        verify(couponRepository, never()).deleteById(any());
    }
}
