package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestCouponControllerImpl;
import com.mehmetkerem.model.Coupon;
import com.mehmetkerem.service.ICouponService;
import com.mehmetkerem.util.ResultData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestCouponControllerTest {

    @Mock
    private ICouponService couponService;

    @InjectMocks
    private RestCouponControllerImpl controller;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .id(1L)
                .code("INDIRIM10")
                .discountAmount(new BigDecimal("10"))
                .minCartAmount(new BigDecimal("100"))
                .build();
    }

    @Test
    @DisplayName("createCoupon - kupon oluşturulur")
    void createCoupon_ShouldReturnCreatedCoupon() {
        when(couponService.createCoupon(anyString(), any(), any(), anyInt())).thenReturn(coupon);

        ResultData<Coupon> result = controller.createCoupon("INDIRIM10", new BigDecimal("10"), new BigDecimal("100"), 7);

        assertTrue(result.isStatus());
        assertEquals("INDIRIM10", result.getData().getCode());
        verify(couponService).createCoupon(eq("INDIRIM10"), any(), any(), eq(7));
    }

    @Test
    @DisplayName("getCoupon - kupon döner")
    void getCoupon_ShouldReturnCoupon() {
        when(couponService.getCouponByCode("INDIRIM10")).thenReturn(coupon);

        ResultData<Coupon> result = controller.getCoupon("INDIRIM10");

        assertTrue(result.isStatus());
        assertEquals(1L, result.getData().getId());
        verify(couponService).getCouponByCode("INDIRIM10");
    }

    @Test
    @DisplayName("applyCoupon - indirimli tutar döner")
    void applyCoupon_ShouldReturnDiscountedTotal() {
        when(couponService.applyCoupon("INDIRIM10", new BigDecimal("150"))).thenReturn(new BigDecimal("140"));

        ResultData<BigDecimal> result = controller.applyCoupon("INDIRIM10", new BigDecimal("150"));

        assertTrue(result.isStatus());
        assertEquals(new BigDecimal("140"), result.getData());
        verify(couponService).applyCoupon("INDIRIM10", new BigDecimal("150"));
    }

    @Test
    @DisplayName("deleteCoupon - mesaj döner")
    void deleteCoupon_ShouldReturnSuccessMessage() {
        ResultData<String> result = controller.deleteCoupon(1L);

        assertTrue(result.isStatus());
        assertTrue(result.getData().contains("silindi"));
        verify(couponService).deleteCoupon(1L);
    }
}
