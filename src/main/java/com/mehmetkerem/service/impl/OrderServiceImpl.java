package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderInvoiceResponse;
import com.mehmetkerem.dto.response.OrderItemResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.model.*;
import com.mehmetkerem.repository.OrderRepository;
import com.mehmetkerem.service.IOrderService;
import com.mehmetkerem.util.Messages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartServiceImpl cartService;
    private final ProductServiceImpl productService;
    private final AddressServiceImpl addressService;
    private final UserServiceImpl userService;
    private final AddressMapper addressMapper;
    private final com.mehmetkerem.service.INotificationService notificationService;

    public OrderServiceImpl(OrderRepository orderRepository, CartServiceImpl cartService,
            ProductServiceImpl productService, AddressServiceImpl addressService, UserServiceImpl userService,
            AddressMapper addressMapper, com.mehmetkerem.service.INotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.addressService = addressService;
        this.userService = userService;
        this.addressMapper = addressMapper;
        this.notificationService = notificationService;
    }

    private static final int STOCK_UPDATE_MAX_RETRIES = 3;

    @Override
    public OrderResponse saveOrder(Long userId, OrderRequest request) {
        int attempt = 0;

        while (attempt < STOCK_UPDATE_MAX_RETRIES) {
            try {
                return doSaveOrder(userId, request);
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

    @Transactional
    protected OrderResponse doSaveOrder(Long userId, OrderRequest request) {
        log.info("Yeni sipariş oluşturma isteği alındı. Kullanıcı ID: {}", userId);
        Cart cart = cartService.getCartByUserId(userId);

        if (cart.getItems().isEmpty()) {
            log.warn("Sipariş başarısız: Kullanıcının sepeti boş. Kullanıcı ID: {}", userId);
            throw new BadRequestException(String.format(ExceptionMessages.CART_NOT_FOUND, userId));
        }

        log.debug("Sepet doğrulandı, {} kalem ürün siparişe dönüştürülüyor.", cart.getItems().size());
        List<OrderItem> orderItems = convertCartItemsToOrderItems(cart.getItems());

        // Adresin bu kullanıcıya ait olduğunu doğrula
        var shippingAddress = addressService.getAddressByIdAndUserId(request.getAddressId(), userId);
        // Ödeme durumunu istemciden alma; sipariş her zaman PENDING ile başlar
        var paymentStatus = com.mehmetkerem.enums.PaymentStatus.PENDING;

        Order order = Order.builder()
                .userId(userId)
                .orderStatus(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .totalAmount(cartService.calculateTotal(userId))
                .shippingAddress(shippingAddress)
                .paymentStatus(paymentStatus)
                .orderItems(orderItems)
                .build();

        orderRepository.save(order);
        log.info("Sipariş veritabanına kaydedildi. Sipariş ID: {}, Toplam Tutar: {}", order.getId(),
                order.getTotalAmount());

        updateStockLevels(orderItems);
        cartService.clearCart(userId);
        log.debug("Kullanıcının sepeti temizlendi. Kullanıcı ID: {}", userId);

        // Send email
        try {
            User user = userService.getUserById(userId);
            notificationService.sendOrderConfirmation(user.getEmail(), "ORD-" + order.getId());
            log.info("Sipariş onayı gönderildi: {}", user.getEmail());
        } catch (Exception e) {
            // Log error but don't fail the order if email fails
            log.error("Sipariş onayı gönderilemedi: {}", e.getMessage());
        }

        return convertOrderToOrderResponse(order);
    }

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
    public List<OrderResponse> getOrdersByUser(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertOrderToOrderResponse)
                .toList();
    }

    @Override
    public Page<OrderResponse> getOrdersByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::convertOrderToOrderResponse);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertOrderToOrderResponse)
                .toList();
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::convertOrderToOrderResponse);
    }

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

        // Kargo bilgisi girildiğinde statüyü SHIPPED yapabiliriz (Opsiyonel)
        if (order.getOrderStatus() == OrderStatus.PENDING || order.getOrderStatus() == OrderStatus.PAID) {
            order.setOrderStatus(OrderStatus.SHIPPED);
        }

        orderRepository.save(order);

        // Send Tracking Email
        try {
            UserResponse user = userService.getUserResponseById(order.getUserId());
            notificationService.sendOrderTrackingEmail(user.getEmail(), "ORD-" + order.getId(), trackingNumber,
                    carrierName);
        } catch (Exception e) {
            log.error("Kargo takip e-postası gönderilemedi: {}", e.getMessage());
        }

        return convertOrderToOrderResponse(order);
    }

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

    private void updateStockLevels(List<OrderItem> orderItems) {

        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .toList();

        List<Product> products = productService.getProductsByIds(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProductId());

            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException(
                        String.format(ExceptionMessages.INSUFFICIENT_STOCK, product.getTitle()));
            }

            int newStock = product.getStock() - item.getQuantity();
            product.setStock(newStock);

            // Stock threshold for alert
            if (newStock < 5) {
                try {
                    notificationService.sendStockAlert(product.getTitle());
                } catch (Exception e) {
                    log.error("Stok uyarısı gönderilemedi: {}", e.getMessage());
                }
            }
        }

        productService.saveAllProducts(products);
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

    @Override
    @Transactional
    public void revertStockForOrder(Long orderId) {
        Order order = getOrderById(orderId);
        revertStockLevels(order.getOrderItems());
        log.info("Sipariş stokları iade edildi. Sipariş ID: {}", orderId);
    }

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

}
