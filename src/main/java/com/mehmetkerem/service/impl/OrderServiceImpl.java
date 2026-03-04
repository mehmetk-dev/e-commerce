package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderInvoiceResponse;
import com.mehmetkerem.dto.response.OrderItemResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.event.OrderEvent;
import com.mehmetkerem.event.OrderEventType;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.model.*;
import com.mehmetkerem.repository.OrderRepository;
import com.mehmetkerem.repository.OrderStatusHistoryRepository;
import com.mehmetkerem.service.IOrderService;
import com.mehmetkerem.util.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sipariş iş mantığı servisi.
 *
 * <p>
 * Bildirim gönderimi (e-posta, in-app) bu sınıfta YOKTUR.
 * Transaction commit'lendikten SONRA
 * {@link com.mehmetkerem.event.OrderEventListener}
 * tarafından event dinlenerek yapılır.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final com.mehmetkerem.service.ICartService cartService;
    private final com.mehmetkerem.service.IProductService productService;
    private final com.mehmetkerem.service.IAddressService addressService;
    private final com.mehmetkerem.service.IUserService userService;
    private final AddressMapper addressMapper;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.stock.alert-threshold:5}")
    private int stockAlertThreshold;

    private static final int STOCK_UPDATE_MAX_RETRIES = 3;

    // ══════════════════════════════════════════════════════════════════════════
    // Sipariş Oluşturma
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public OrderResponse saveOrder(Long userId, OrderRequest request) {
        int attempt = 0;

        while (attempt < STOCK_UPDATE_MAX_RETRIES) {
            try {
                return transactionTemplate.execute(status -> doSaveOrder(userId, request));
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                log.warn("Stok güncelleme çakışması (deneme {}/{}). Tekrar deneniyor. Kullanıcı ID: {}",
                        attempt, STOCK_UPDATE_MAX_RETRIES, userId);
            }
        }

        log.error("Stok güncelleme {} denemede başarısız. Kullanıcı ID: {}", STOCK_UPDATE_MAX_RETRIES, userId);
        throw new BadRequestException(
                "Sipariş şu anda tamamlanamadı (yoğun trafik). Lütfen kısa süre sonra tekrar deneyin.");
    }

    private OrderResponse doSaveOrder(Long userId, OrderRequest request) {
        log.info("Yeni sipariş oluşturma isteği alındı. Kullanıcı ID: {}", userId);
        Cart cart = cartService.getCartByUserId(userId);

        if (cart.getItems().isEmpty()) {
            log.warn("Sipariş başarısız: Kullanıcının sepeti boş. Kullanıcı ID: {}", userId);
            throw new BadRequestException(String.format(ExceptionMessages.CART_NOT_FOUND, userId));
        }

        log.debug("Sepet doğrulandı, {} kalem ürün siparişe dönüştürülüyor.", cart.getItems().size());
        List<OrderItem> orderItems = convertCartItemsToOrderItems(cart.getItems());

        // Stok kontrolü ve düşürme — düşük stok uyarılarını topla
        List<OrderEvent.StockAlertInfo> stockAlerts = validateAndDeductStock(orderItems);
        log.debug("Stok kontrolü başarılı ve stoklar düşürüldü.");

        // Adresin bu kullanıcıya ait olduğunu doğrula
        var shippingAddress = addressService.getAddressByIdAndUserId(request.getAddressId(), userId);
        var paymentStatus = com.mehmetkerem.enums.PaymentStatus.PENDING;

        Order order = Order.builder()
                .userId(userId)
                .orderStatus(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .totalAmount(cartService.calculateTotal(userId))
                .shippingAddress(shippingAddress)
                .paymentStatus(paymentStatus)
                .orderItems(orderItems)
                .note(request.getNote())
                .build();

        orderRepository.save(order);
        log.info("Sipariş veritabanına kaydedildi. Sipariş ID: {}, Toplam Tutar: {}", order.getId(),
                order.getTotalAmount());

        // İlk timeline kaydı
        orderStatusHistoryRepository.save(
                OrderStatusHistory.builder()
                        .orderId(order.getId())
                        .oldStatus(null)
                        .newStatus(OrderStatus.PENDING)
                        .changedBy(userId)
                        .changedAt(LocalDateTime.now())
                        .note("Sipariş oluşturuldu")
                        .build());

        cartService.clearCart(userId);
        log.debug("Kullanıcının sepeti temizlendi. Kullanıcı ID: {}", userId);

        // Event yayınla — bildirimler Transaction COMMIT sonrası gönderilir
        User user = userService.getUserById(userId);
        eventPublisher.publishEvent(OrderEvent.builder()
                .type(OrderEventType.ORDER_CREATED)
                .orderId(order.getId())
                .userId(userId)
                .userEmail(user.getEmail())
                .orderCode("ORD-" + order.getId())
                .stockAlerts(stockAlerts.isEmpty() ? null : stockAlerts)
                .build());

        return convertOrderToOrderResponse(order);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Sipariş Sorgulama
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, orderId, "sipariş")));
    }

    @Override
    public OrderResponse getOrderResponseById(Long orderId) {
        return convertOrderToOrderResponse(getOrderById(orderId));
    }

    @Override
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::convertOrderToOrderResponse);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertOrderToOrderResponse);
    }

    @Override
    public Page<OrderResponse> searchOrders(
            OrderStatus status, com.mehmetkerem.enums.PaymentStatus paymentStatus,
            Long userId, LocalDateTime from, LocalDateTime to, String query, Pageable pageable) {

        Specification<Order> spec = Specification
                .where(com.mehmetkerem.repository.specification.OrderSpecification.hasStatus(status))
                .and(com.mehmetkerem.repository.specification.OrderSpecification.hasPaymentStatus(paymentStatus))
                .and(com.mehmetkerem.repository.specification.OrderSpecification.hasUserId(userId))
                .and(com.mehmetkerem.repository.specification.OrderSpecification.dateBetween(from, to))
                .and(com.mehmetkerem.repository.specification.OrderSpecification.searchByOrderCode(query));

        return orderRepository.findAll(spec, pageable).map(this::convertOrderToOrderResponse);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Sipariş Güncelleme
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getOrderStatus();

        if (oldStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("İptal edilmiş bir siparişin durumu değiştirilemez.");
        }

        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            log.info("Sipariş iptal ediliyor, stoklar iade ediliyor. Sipariş ID: {}", orderId);
            revertStockLevels(order.getOrderItems());
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);
        log.info("Sipariş durumu güncellendi: {} -> {}", oldStatus, newStatus);

        // Timeline kaydı
        Long currentUserId = com.mehmetkerem.util.SecurityUtils.getCurrentUserId();
        orderStatusHistoryRepository.save(
                OrderStatusHistory.builder()
                        .orderId(orderId)
                        .oldStatus(oldStatus)
                        .newStatus(newStatus)
                        .changedBy(currentUserId)
                        .changedAt(LocalDateTime.now())
                        .build());

        // Event yayınla — bildirimler Transaction COMMIT sonrası
        UserResponse user = userService.getUserResponseById(order.getUserId());
        eventPublisher.publishEvent(OrderEvent.builder()
                .type(OrderEventType.STATUS_UPDATED)
                .orderId(orderId)
                .userId(order.getUserId())
                .userEmail(user.getEmail())
                .orderCode("ORD-" + orderId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build());

        return convertOrderToOrderResponse(order);
    }

    @Transactional
    @Override
    public void updatePaymentStatus(Long orderId, com.mehmetkerem.enums.PaymentStatus newStatus) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public String deleteOrder(Long orderId) {
        orderRepository.delete(getOrderById(orderId));
        return String.format(Messages.DELETE_VALUE, orderId, "sipariş");
    }

    @Override
    @Transactional
    public OrderResponse updateOrderTracking(Long orderId, String trackingNumber, String carrierName) {
        Order order = getOrderById(orderId);
        order.setTrackingNumber(trackingNumber);
        order.setCarrierName(carrierName);
        order.setLastUpdated(LocalDateTime.now());

        if (order.getOrderStatus() == OrderStatus.PENDING || order.getOrderStatus() == OrderStatus.PAID) {
            order.setOrderStatus(OrderStatus.SHIPPED);
        }

        orderRepository.save(order);

        // Event yayınla — bildirimler Transaction COMMIT sonrası
        UserResponse user = userService.getUserResponseById(order.getUserId());
        eventPublisher.publishEvent(OrderEvent.builder()
                .type(OrderEventType.TRACKING_UPDATED)
                .orderId(orderId)
                .userId(order.getUserId())
                .userEmail(user.getEmail())
                .orderCode("ORD-" + orderId)
                .trackingNumber(trackingNumber)
                .carrierName(carrierName)
                .build());

        return convertOrderToOrderResponse(order);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Sipariş İptali & Stok
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Bu sipariş size ait değil.");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Sadece bekleyen siparişler iptal edilebilir.");
        }
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    @Override
    @Transactional
    public void revertStockForOrder(Long orderId) {
        Order order = getOrderById(orderId);
        revertStockLevels(order.getOrderItems());
        log.info("Sipariş stokları iade edildi. Sipariş ID: {}", orderId);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Fatura & Timeline
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public OrderInvoiceResponse getOrderInvoice(Long orderId) {
        Order order = getOrderById(orderId);
        UserResponse user = userService.getUserResponseById(order.getUserId());
        String addressSummary = order.getShippingAddress() != null
                ? String.join(", ", order.getShippingAddress().getAddressLine(),
                        order.getShippingAddress().getDistrict(), order.getShippingAddress().getCity(),
                        order.getShippingAddress().getCountry())
                : "";

        List<OrderInvoiceResponse.InvoiceItemLine> lines = order.getOrderItems().stream()
                .map(item -> OrderInvoiceResponse.InvoiceItemLine.builder()
                        .productTitle(item.getTitle())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getPrice())
                        .lineTotal(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderInvoiceResponse.builder()
                .invoiceNumber("INV-" + order.getId())
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .customerName(user.getName())
                .shippingAddressSummary(addressSummary)
                .items(lines)
                .subtotal(order.getTotalAmount())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .build();
    }

    @Override
    public List<com.mehmetkerem.dto.response.OrderStatusHistoryResponse> getOrderTimeline(Long orderId) {
        Order order = getOrderById(orderId);

        // Yetkilendirme: sipariş sahibi veya admin görebilir
        Long currentUserId = com.mehmetkerem.util.SecurityUtils.getCurrentUserId();
        User currentUser = com.mehmetkerem.util.SecurityUtils.getCurrentUser();
        boolean isOwner = currentUserId != null && order.getUserId().equals(currentUserId);
        boolean isAdmin = currentUser != null && currentUser.getRole() == com.mehmetkerem.enums.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BadRequestException("Bu siparişin geçmişini görüntüleme yetkiniz yok.");
        }

        return orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId)
                .stream()
                .map(h -> com.mehmetkerem.dto.response.OrderStatusHistoryResponse.builder()
                        .id(h.getId())
                        .oldStatus(h.getOldStatus())
                        .newStatus(h.getNewStatus())
                        .changedBy(h.getChangedBy())
                        .note(h.getNote())
                        .changedAt(h.getChangedAt())
                        .build())
                .toList();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private List<OrderItem> convertCartItemsToOrderItems(List<CartItem> cartItems) {
        List<ProductResponse> productList = productService.getProductResponsesByIds(
                cartItems.stream().map(CartItem::getProductId).toList());

        Map<Long, ProductResponse> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        return cartItems.stream()
                .map(ci -> {
                    ProductResponse product = productMap.get(ci.getProductId());
                    if (product == null) {
                        throw new NotFoundException("Ürün bulunamadı. ID: " + ci.getProductId());
                    }
                    return OrderItem.builder()
                            .productId(ci.getProductId())
                            .title(product.getTitle())
                            .price(product.getPrice())
                            .quantity(ci.getQuantity())
                            .build();
                })
                .toList();
    }

    private OrderResponse convertOrderToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .user(userService.getUserResponseById(order.getUserId()))
                .totalAmount(order.getTotalAmount())
                .shippingAddress(addressMapper.toResponse(order.getShippingAddress()))
                .orderItems(convertToResponseOrderItems(order.getOrderItems()))
                .trackingNumber(order.getTrackingNumber())
                .carrierName(order.getCarrierName())
                .note(order.getNote())
                .build();
    }

    private List<OrderItemResponse> convertToResponseOrderItems(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem -> OrderItemResponse.builder()
                        .product(new ProductResponse(orderItem.getProductId(), orderItem.getTitle(),
                                orderItem.getPrice()))
                        .quantity(orderItem.getQuantity())
                        .price(orderItem.getPrice())
                        .build())
                .toList();
    }

    /**
     * Stok kontrolü ve düşürme — düşük stok uyarılarını döner.
     * Hiçbir dış servis çağrısı yapmaz (SRP).
     */
    private List<OrderEvent.StockAlertInfo> validateAndDeductStock(List<OrderItem> orderItems) {
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .toList();

        List<Product> products = productService.getProductsByIds(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 1. Önce TÜM ürünlerin stok yeterliliğini kontrol et
        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new NotFoundException("Ürün bulunamadı. ID: " + item.getProductId());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException(
                        String.format(ExceptionMessages.INSUFFICIENT_STOCK, product.getTitle()));
            }
        }

        // 2. Kontrol geçtiyse stokları düşür ve düşük stok uyarılarını topla
        List<OrderEvent.StockAlertInfo> stockAlerts = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProductId());
            int newStock = product.getStock() - item.getQuantity();
            product.setStock(newStock);

            if (newStock <= stockAlertThreshold) {
                stockAlerts.add(OrderEvent.StockAlertInfo.builder()
                        .productTitle(product.getTitle())
                        .remainingStock(newStock)
                        .productId(product.getId())
                        .build());
            }
        }

        productService.saveAllProducts(products);
        return stockAlerts;
    }

    private void revertStockLevels(List<OrderItem> orderItems) {
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .toList();

        List<Product> products = productService.getProductsByIds(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProductId());
            int newStock = product.getStock() + item.getQuantity();
            product.setStock(newStock);
            log.debug("Stok iade edildi. Ürün: {}, Yeni Stok: {}", product.getTitle(), newStock);
        }

        productService.saveAllProducts(products);
    }
}
