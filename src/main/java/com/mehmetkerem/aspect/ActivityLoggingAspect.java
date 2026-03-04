package com.mehmetkerem.aspect;

import com.mehmetkerem.dto.request.*;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.enums.ActivityType;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.model.User;
import com.mehmetkerem.service.IActivityLogService;
import com.mehmetkerem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP ile otomatik aktivite loglama.
 * <p>
 * Service metotları başarıyla tamamlandığında (@AfterReturning) ilgili
 * aktiviteyi loglar.
 * Hata durumları loglanmaz çünkü GlobalExceptionHandler zaten hataları yönetir.
 * </p>
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private final IActivityLogService activityLogService;

    // ═══════════════════════════════════════════════════════════════════
    // Auth İşlemleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning("execution(* com.mehmetkerem.service.impl.AuthService.login(..))")
    public void logLogin(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof LoginRequest request) {
            activityLogService.log(ActivityType.LOGIN, null, request.getEmail(),
                    "User", null, "Giriş yapıldı: " + request.getEmail(), null);
        }
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.AuthService.register(..))")
    public void logRegister(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof RegisterRequest request) {
            activityLogService.log(ActivityType.REGISTER, null, request.getEmail(),
                    "User", null,
                    "Yeni kayıt: " + request.getName() + " (" + request.getEmail() + ")", null);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Sepet İşlemleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning("execution(* com.mehmetkerem.service.impl.CartServiceImpl.addItem(..))")
    public void logCartAddItem(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        CartItemRequest request = (CartItemRequest) args[1];
        logActivity(ActivityType.CART_ADD_ITEM, userId, "CartItem", request.getProductId(),
                String.format("Sepete ürün eklendi. Ürün ID: %d, Adet: %d",
                        request.getProductId(), request.getQuantity()));
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.CartServiceImpl.removeItem(..))")
    public void logCartRemoveItem(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        Long productId = (Long) args[1];
        logActivity(ActivityType.CART_REMOVE_ITEM, userId, "CartItem", productId,
                "Sepetten ürün çıkarıldı. Ürün ID: " + productId);
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.CartServiceImpl.clearCart(..))")
    public void logCartClear(JoinPoint joinPoint) {
        Long userId = (Long) joinPoint.getArgs()[0];
        logActivity(ActivityType.CART_CLEAR, userId, "Cart", null, "Sepet temizlendi");
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.CartServiceImpl.applyCoupon(..))")
    public void logCouponApply(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        String couponCode = (String) args[1];
        logActivity(ActivityType.CART_APPLY_COUPON, userId, "Coupon", null,
                "Kupon uygulandı: " + couponCode);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Sipariş İşlemleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning(pointcut = "execution(* com.mehmetkerem.service.impl.OrderServiceImpl.saveOrder(..))", returning = "result")
    public void logOrderCreate(JoinPoint joinPoint, Object result) {
        Long userId = (Long) joinPoint.getArgs()[0];
        if (result instanceof OrderResponse order) {
            logActivity(ActivityType.ORDER_CREATE, userId, "Order", order.getId(),
                    String.format("Yeni sipariş oluşturuldu. Sipariş ID: %d, Toplam: %s",
                            order.getId(), order.getTotalAmount()));
        }
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.OrderServiceImpl.cancelOrder(..))")
    public void logOrderCancel(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long orderId = (Long) args[0];
        Long userId = (Long) args[1];
        logActivity(ActivityType.ORDER_CANCEL, userId, "Order", orderId,
                "Sipariş iptal edildi. Sipariş ID: " + orderId);
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.OrderServiceImpl.updateOrderStatus(..))")
    public void logOrderStatusUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long orderId = (Long) args[0];
        OrderStatus newStatus = (OrderStatus) args[1];
        User currentUser = SecurityUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        logActivity(ActivityType.ORDER_STATUS_UPDATE, userId, "Order", orderId,
                String.format("Sipariş durumu güncellendi → %s. Sipariş ID: %d",
                        newStatus, orderId));
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.OrderServiceImpl.updateOrderTracking(..))")
    public void logOrderTracking(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long orderId = (Long) args[0];
        String trackingNumber = (String) args[1];
        String carrier = (String) args[2];
        User currentUser = SecurityUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        logActivity(ActivityType.ORDER_TRACKING_UPDATE, userId, "Order", orderId,
                String.format("Kargo takip güncellendi. Sipariş: %d, Takip No: %s, Kargo: %s",
                        orderId, trackingNumber, carrier));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Ödeme İşlemleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning(pointcut = "execution(* com.mehmetkerem.service.impl.PaymentServiceImpl.processPayment(..))", returning = "result")
    public void logPayment(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        Long orderId = (Long) args[1];
        if (result instanceof PaymentResponse payment) {
            ActivityType type = payment.getPaymentStatus() == com.mehmetkerem.enums.PaymentStatus.PAID
                    ? ActivityType.PAYMENT_SUCCESS
                    : ActivityType.PAYMENT_FAIL;
            logActivity(type, userId, "Payment", payment.getId(),
                    String.format("Ödeme %s. Sipariş: %d, Tutar: %s, Yöntem: %s",
                            type == ActivityType.PAYMENT_SUCCESS ? "başarılı" : "başarısız",
                            orderId, payment.getAmount(), payment.getPaymentMethod()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // İade İşlemleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning("execution(* com.mehmetkerem.service.impl.OrderReturnServiceImpl.createReturn(..))")
    public void logReturnRequest(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        OrderReturnRequest request = (OrderReturnRequest) args[1];
        logActivity(ActivityType.RETURN_REQUEST, userId, "OrderReturn", request.getOrderId(),
                String.format("İade talebi oluşturuldu. Sipariş ID: %d, Sebep: %s",
                        request.getOrderId(), request.getReason()));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Yorum İşlemleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning(pointcut = "execution(* com.mehmetkerem.service.impl.ReviewServiceImpl.saveReview(..))", returning = "result")
    public void logReviewCreate(JoinPoint joinPoint, Object result) {
        Long userId = (Long) joinPoint.getArgs()[0];
        if (result instanceof ReviewResponse review) {
            logActivity(ActivityType.REVIEW_CREATE, userId, "Review", review.getId(),
                    String.format("Yeni yorum eklendi. Ürün: %s, Puan: %s",
                            review.getProduct() != null ? review.getProduct().getTitle() : "?",
                            review.getRating()));
        }
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.ReviewServiceImpl.deleteReview(..))")
    public void logReviewDelete(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        Long reviewId = (Long) args[1];
        logActivity(ActivityType.REVIEW_DELETE, userId, "Review", reviewId,
                "Yorum silindi. Yorum ID: " + reviewId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Favori İşlemleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning("execution(* com.mehmetkerem.service.impl.WishlistServiceImpl.addItemToWishlist(..))")
    public void logWishlistAdd(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        Long productId = (Long) args[1];
        logActivity(ActivityType.WISHLIST_ADD, userId, "WishlistItem", productId,
                "Favorilere ürün eklendi. Ürün ID: " + productId);
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.WishlistServiceImpl.removeItemFromWishlist(..))")
    public void logWishlistRemove(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        Long productId = (Long) args[1];
        logActivity(ActivityType.WISHLIST_REMOVE, userId, "WishlistItem", productId,
                "Favorilerden ürün çıkarıldı. Ürün ID: " + productId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Adres İşlemleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning("execution(* com.mehmetkerem.service.impl.AddressServiceImpl.saveAddress(..))")
    public void logAddressCreate(JoinPoint joinPoint) {
        Long userId = (Long) joinPoint.getArgs()[0];
        logActivity(ActivityType.ADDRESS_CREATE, userId, "Address", null, "Yeni adres eklendi");
    }

    @AfterReturning("execution(* com.mehmetkerem.service.impl.AddressServiceImpl.deleteAddressForUser(..))")
    public void logAddressDelete(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long addressId = (Long) args[0];
        Long userId = (Long) args[1];
        logActivity(ActivityType.ADDRESS_DELETE, userId, "Address", addressId,
                "Adres silindi. Adres ID: " + addressId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Destek Talepleri
    // ═══════════════════════════════════════════════════════════════════

    @AfterReturning("execution(* com.mehmetkerem.service.impl.SupportTicketServiceImpl.createTicket(..))")
    public void logTicketCreate(JoinPoint joinPoint) {
        User currentUser = SecurityUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        logActivity(ActivityType.TICKET_CREATE, userId, "SupportTicket", null,
                "Yeni destek talebi oluşturuldu");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper
    // ═══════════════════════════════════════════════════════════════════

    private void logActivity(ActivityType type, Long userId, String entityType,
            Long entityId, String description) {
        User user = SecurityUtils.getCurrentUser();
        String email = user != null ? user.getEmail() : null;
        activityLogService.log(type, userId, email, entityType, entityId, description, null);
    }
}
