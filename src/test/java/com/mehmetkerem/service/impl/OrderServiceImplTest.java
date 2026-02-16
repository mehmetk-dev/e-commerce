package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.model.*;
import com.mehmetkerem.repository.OrderRepository;
import com.mehmetkerem.service.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartServiceImpl cartService;

    @Mock
    private ProductServiceImpl productService;

    @Mock
    private AddressServiceImpl addressService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 10L;
    private static final Long PRODUCT_ID = 100L;

    private OrderRequest orderRequest;
    private Cart cartWithItems;
    private Address address;
    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setAddressId(ADDRESS_ID);
        orderRequest.setPaymentStatus(PaymentStatus.PAID);

        CartItem cartItem = CartItem.builder()
                .productId(PRODUCT_ID)
                .quantity(2)
                .price(new BigDecimal("50"))
                .build();
        cartWithItems = Cart.builder()
                .id(1L)
                .userId(USER_ID)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();

        address = Address.builder()
                .id(ADDRESS_ID)
                .userId(USER_ID)
                .city("Istanbul")
                .country("Turkey")
                .build();

        order = Order.builder()
                .id(1L)
                .userId(USER_ID)
                .orderStatus(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("100"))
                .paymentStatus(PaymentStatus.PAID)
                .orderItems(new ArrayList<>())
                .shippingAddress(address)
                .build();

        product = Product.builder()
                .id(PRODUCT_ID)
                .title("Ürün")
                .stock(10)
                .price(new BigDecimal("50"))
                .build();
    }

    @Test
    @DisplayName("saveOrder - boş sepet ile BadRequestException fırlatır")
    void saveOrder_WhenCartEmpty_ShouldThrowBadRequestException() {
        Cart emptyCart = Cart.builder().id(1L).userId(USER_ID).items(new ArrayList<>()).build();
        when(cartService.getCartByUserId(USER_ID)).thenReturn(emptyCart);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> orderService.saveOrder(USER_ID, orderRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(USER_ID)));
        assertTrue(ex.getMessage().toLowerCase().contains("sepet"));
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @DisplayName("saveOrder - başarılı siparişte sepet temizlenir ve stok güncellenir")
    void saveOrder_WhenValid_ShouldSaveOrderClearCartAndUpdateStock() {
        when(cartService.getCartByUserId(USER_ID)).thenReturn(cartWithItems);
        when(addressService.getAddressByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(address);
        when(cartService.calculateTotal(USER_ID)).thenReturn(new BigDecimal("100"));

        ProductResponse productResponse = ProductResponse.builder()
                .id(PRODUCT_ID)
                .title("Ürün")
                .price(new BigDecimal("50"))
                .build();
        when(productService.getProductResponsesByIds(List.of(PRODUCT_ID))).thenReturn(List.of(productResponse));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        when(productService.getProductsByIds(List.of(PRODUCT_ID))).thenReturn(List.of(product));
        when(productService.saveAllProducts(anyList())).thenAnswer(inv -> inv.getArgument(0));

        User user = User.builder().id(USER_ID).email("u@test.com").build();
        when(userService.getUserById(USER_ID)).thenReturn(user);
        UserResponse userResponse = new UserResponse();
        userResponse.setId(USER_ID);
        when(userService.getUserResponseById(USER_ID)).thenReturn(userResponse);
        AddressResponse addressResponse = new AddressResponse();
        when(addressMapper.toResponse(address)).thenReturn(addressResponse);

        OrderResponse result = orderService.saveOrder(USER_ID, orderRequest);

        assertNotNull(result);
        verify(cartService).clearCart(USER_ID);
        verify(productService).saveAllProducts(anyList());
        verify(orderRepository).save(any(Order.class));
        verify(notificationService).sendOrderConfirmation(eq("u@test.com"), anyString());
    }

    @Test
    @DisplayName("saveOrder - yetersiz stokta BadRequestException ve sipariş kaydedilmez")
    void saveOrder_WhenInsufficientStock_ShouldThrowBadRequestException() {
        when(cartService.getCartByUserId(USER_ID)).thenReturn(cartWithItems);
        when(addressService.getAddressByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(address);
        when(cartService.calculateTotal(USER_ID)).thenReturn(new BigDecimal("100"));
        ProductResponse productResponse = ProductResponse.builder()
                .id(PRODUCT_ID)
                .title("Ürün")
                .price(new BigDecimal("50"))
                .build();
        when(productService.getProductResponsesByIds(List.of(PRODUCT_ID))).thenReturn(List.of(productResponse));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });
        product.setStock(1);
        when(productService.getProductsByIds(List.of(PRODUCT_ID))).thenReturn(List.of(product));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> orderService.saveOrder(USER_ID, orderRequest));

        assertTrue(ex.getMessage().toLowerCase().contains("stok") || ex.getMessage().contains("Ürün"));
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @DisplayName("getOrderById - sipariş bulunamazsa NotFoundException")
    void getOrderById_WhenNotExists_ShouldThrowNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    @DisplayName("getOrderById - mevcut sipariş döner")
    void getOrderById_WhenExists_ShouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
    }

    @Test
    @DisplayName("updateOrderStatus - iptal edilmiş siparişin durumu değiştirilemez")
    void updateOrderStatus_WhenAlreadyCancelled_ShouldThrowBadRequestException() {
        order.setOrderStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.SHIPPED));

        assertTrue(ex.getMessage().contains("İptal"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateOrderStatus - iptale çekilince stok iade edilir")
    void updateOrderStatus_WhenCancelling_ShouldRevertStock() {
        OrderItem orderItem = OrderItem.builder()
                .productId(PRODUCT_ID)
                .title("Ürün")
                .quantity(2)
                .price(new BigDecimal("50"))
                .build();
        order.setOrderItems(List.of(orderItem));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productService.getProductsByIds(List.of(PRODUCT_ID))).thenReturn(List.of(product));
        when(productService.saveAllProducts(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(addressMapper.toResponse(any())).thenReturn(new AddressResponse());

        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        verify(productService).saveAllProducts(anyList());
    }

    @Test
    @DisplayName("deleteOrder - sipariş silinir")
    void deleteOrder_WhenExists_ShouldDeleteAndReturnMessage() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        String result = orderService.deleteOrder(1L);

        assertTrue(result.contains("1"));
        assertTrue(result.contains("sipariş"));
        verify(orderRepository).delete(order);
    }
}
