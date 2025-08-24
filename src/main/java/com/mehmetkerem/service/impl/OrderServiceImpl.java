package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderItemResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.mapper.OrderMapper;
import com.mehmetkerem.model.*;
import com.mehmetkerem.repository.OrderRepository;
import com.mehmetkerem.service.IOrderService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartServiceImpl cartService;
    private final ProductServiceImpl productService;
    private final AddressServiceImpl addressService;
    private final UserServiceImpl userService;
    private final AddressMapper addressMapper;


    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper, CartServiceImpl cartService, ProductServiceImpl productService, AddressServiceImpl addressService, UserServiceImpl userService, AddressMapper addressMapper) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.addressService = addressService;
        this.userService = userService;
        this.addressMapper = addressMapper;
    }

    @Transactional
    public OrderResponse saveOrder(String userId, OrderRequest request){

        Cart cart = cartService.getCartByUserId(userId);

        if (cart.getItems().isEmpty()){
            throw new BadRequestException(String.format(ExceptionMessages.CART_NOT_FOUND,userId));
        }

        List<OrderItem> orderItems = convertCartItemsToOrderItems(cart.getItems());

        Order order = Order.builder()
                .userId(userId)
                .orderStatus(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .totalAmount(cartService.calculateTotal(userId))
                .shippingAddress(addressService.getAddressById(request.getAddressId()))
                .paymentStatus(request.getPaymentStatus())
                .orderItems(orderItems)
                .build();

        orderRepository.save(order);
        updateStockLevels(orderItems);
        cartService.clearCart(userId);

        return convertOrderToOrderResponse(order);
    }

    @Override
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND,orderId,"sipariş")));
    }

    @Override
    public OrderResponse getOrderResponseById(String orderId) {
        return convertOrderToOrderResponse(getOrderById(orderId));
    }

    @Override
    public List<OrderResponse> getOrdersByUser(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertOrderToOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertOrderToOrderResponse)
                .toList();
    }

    @Transactional
    @Override
    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);

        order.setOrderStatus(newStatus);
        orderRepository.save(order);

        return convertOrderToOrderResponse(order);
    }

    @Override
    public String deleteOrder(String orderId) {
        orderRepository.delete(getOrderById(orderId));
        return String.format(Messages.DELETE_VALUE,orderId,"sipariş");
    }

    private List<OrderItem> convertCartItemsToOrderItems(List<CartItem> cartItems){

        List<ProductResponse> productList = productService.getProductResponsesByIds(
                cartItems.stream().map(CartItem::getProductId).toList());

        Map<String, ProductResponse> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

       return  cartItems.stream()
                .map(ci -> {
                    ProductResponse product = productMap.get(ci.getProductId());
                    return OrderItem.builder()
                            .productId(ci.getProductId())
                            .title(product.getTitle())
                            .price(product.getPrice())
                            .quantity(ci.getQuantity())
                            .build();
                })
                .toList();

    }

    private OrderResponse  convertOrderToOrderResponse(Order order){

        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .user(userService.getUserResponseById(order.getUserId()))
                .totalAmount(order.getTotalAmount())
                .shippingAddress(addressMapper.toResponse(order.getShippingAddress()))
                .orderItems(convertToResponseOrderItems(order.getOrderItems()))
                .build();
    }

    private List<OrderItemResponse> convertToResponseOrderItems(List<OrderItem> orderItems){
        return orderItems.stream()
                .map(orderItem -> OrderItemResponse.builder()
                        .product(new ProductResponse(orderItem.getProductId(), orderItem.getTitle(), orderItem.getPrice()))
                        .quantity(orderItem.getQuantity())
                        .price(orderItem.getPrice())
                        .build())
                .toList();
    }


    private void updateStockLevels(List<OrderItem> orderItems) {

        List<String> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .toList();

        List<Product> products = productService.getProductsByIds(productIds);

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProductId());

            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException(
                        String.format(ExceptionMessages.INSUFFICIENT_STOCK,product.getTitle()));
            }

            product.setStock(product.getStock() - item.getQuantity());
        }

        productService.saveAllProducts(products);
    }

}
