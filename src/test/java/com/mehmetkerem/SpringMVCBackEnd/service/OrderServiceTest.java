package com.mehmetkerem.SpringMVCBackEnd.service;

import com.mehmetkerem.dto.request.OrderRequest;
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
import com.mehmetkerem.service.impl.AddressServiceImpl;
import com.mehmetkerem.service.impl.CartServiceImpl;
import com.mehmetkerem.service.impl.OrderServiceImpl;
import com.mehmetkerem.service.impl.ProductServiceImpl;
import com.mehmetkerem.service.impl.UserServiceImpl;
import com.mehmetkerem.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartServiceImpl cartService;
    @Mock private ProductServiceImpl productService;
    @Mock private AddressServiceImpl addressService;
    @Mock private UserServiceImpl userService;
    @Mock private AddressMapper addressMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // mock'ları init et
    }

    // saveOrder: sepette ürün varsa sipariş oluşturma, stok düşme ve sepetin temizlenmesi
    @Test
    void saveOrder_success() {
        String userId = "u1";
        String addrId = "a1";
        String p1 = "p1";

        // Sepet ve item
        CartItem ci = new CartItem(p1, 2, BigDecimal.TEN);
        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>(List.of(ci))).build();

        // Request
        OrderRequest req = new OrderRequest();
        req.setAddressId(addrId);
        req.setPaymentStatus(PaymentStatus.PAID);

        // Ürün detayları (OrderItem oluşturmak için)
        ProductResponse pResp = new ProductResponse();
        pResp.setId(p1);
        pResp.setTitle("Prod 1");
        pResp.setPrice(BigDecimal.TEN);
        pResp.setStock(10);

        // Stok güncellemesi için domain Product
        Product prodDomain = new Product();
        prodDomain.setId(p1);
        prodDomain.setTitle("Prod 1");
        prodDomain.setStock(10);

        Address shipping = new Address();
        UserResponse userResp = new UserResponse(); userResp.setId(userId);

        when(cartService.getCartByUserId(userId)).thenReturn(cart);
        when(productService.getProductResponsesByIds(List.of(p1))).thenReturn(List.of(pResp));
        when(cartService.calculateTotal(userId)).thenReturn(BigDecimal.valueOf(20));
        when(addressService.getAddressById(addrId)).thenReturn(shipping);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId("o1"); // id atayalım
            return o;
        });
        when(productService.getProductsByIds(List.of(p1))).thenReturn(List.of(prodDomain));
        // saveAllProducts herhangi bir şey döndürmüyor: void gibi davranan mock (doNothing default)
        when(userService.getUserResponseById(userId)).thenReturn(userResp);
        when(addressMapper.toResponse(shipping)).thenReturn(new com.mehmetkerem.dto.response.AddressResponse());

        // Act
        OrderResponse resp = orderService.saveOrder(userId, req);

        // Assert
        assertNotNull(resp);                              // response dönmeli
        assertEquals("o1", resp.getId());                // id set edilmiş olmalı
        assertEquals(BigDecimal.valueOf(20), resp.getTotalAmount()); // total doğru olmalı
        verify(orderRepository).save(any(Order.class));  // order kaydedilmeli
        verify(productService).saveAllProducts(anyList()); // stoklar güncellenmeli
        verify(cartService).clearCart(userId);           // sepet temizlenmeli
    }

    // saveOrder: sepet boşsa BadRequestException atılmalı
    @Test
    void saveOrder_emptyCart_throwsBadRequest() {
        String userId = "u1";
        OrderRequest req = new OrderRequest();
        req.setAddressId("a1");

        Cart empty = Cart.builder().userId(userId).items(new ArrayList<>()).build();
        when(cartService.getCartByUserId(userId)).thenReturn(empty);

        assertThrows(BadRequestException.class, () -> orderService.saveOrder(userId, req)); // boş sepet kontrolü
        verify(orderRepository, never()).save(any());
        verify(productService, never()).saveAllProducts(anyList());
        verify(cartService, never()).clearCart(anyString());
    }

    // saveOrder: stok yetersizse updateStockLevels içinde BadRequestException fırlatmalı
    @Test
    void saveOrder_insufficientStock_throwsBadRequest() {
        String userId = "u1";
        String addrId = "a1";
        String p1 = "p1";

        CartItem ci = new CartItem(p1, 5, BigDecimal.TEN);
        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>(List.of(ci))).build();

        OrderRequest req = new OrderRequest();
        req.setAddressId(addrId);
        req.setPaymentStatus(PaymentStatus.PAID);

        ProductResponse pResp = new ProductResponse();
        pResp.setId(p1);
        pResp.setTitle("Prod 1");
        pResp.setPrice(BigDecimal.TEN);
        pResp.setStock(5);

        Product prodDomain = new Product();
        prodDomain.setId(p1);
        prodDomain.setTitle("Prod 1");
        prodDomain.setStock(2); // yetersiz stok (2 < 5)

        Address shipping = new Address();

        when(cartService.getCartByUserId(userId)).thenReturn(cart);
        when(productService.getProductResponsesByIds(List.of(p1))).thenReturn(List.of(pResp));
        when(cartService.calculateTotal(userId)).thenReturn(BigDecimal.valueOf(50));
        when(addressService.getAddressById(addrId)).thenReturn(shipping);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productService.getProductsByIds(List.of(p1))).thenReturn(List.of(prodDomain));

        assertThrows(BadRequestException.class, () -> orderService.saveOrder(userId, req)); // stok yetersizliği
        verify(productService, never()).saveAllProducts(anyList()); // stoklar kaydedilmemeli
        verify(cartService, never()).clearCart(anyString()); // sepet temizlenmemeli
    }

    // getOrderById: sipariş varsa entity dönmeli
    @Test
    void getOrderById_success() {
        Order order = Order.builder().id("o1").build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById("o1");

        assertEquals("o1", result.getId()); // id eşleşmeli
    }

    // getOrderById: sipariş yoksa NotFoundException
    @Test
    void getOrderById_notFound() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById("missing"));
    }

    // getOrderResponseById: sipariş response'a maplenmeli (kullanıcı, adres, item'lar)
    @Test
    void getOrderResponseById_success() {
        String userId = "u1";
        Address shipping = new Address();
        OrderItem oi = OrderItem.builder().productId("p1").title("Prod 1").price(BigDecimal.ONE).quantity(1).build();
        Order order = Order.builder()
                .id("o1")
                .userId(userId)
                .orderStatus(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.PAID)
                .totalAmount(BigDecimal.TEN)
                .shippingAddress(shipping)
                .orderItems(List.of(oi))
                .build();

        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(userService.getUserResponseById(userId)).thenReturn(new UserResponse());
        when(addressMapper.toResponse(shipping)).thenReturn(new com.mehmetkerem.dto.response.AddressResponse());

        OrderResponse resp = orderService.getOrderResponseById("o1");

        assertNotNull(resp);                         // response null olmamalı
        assertEquals("o1", resp.getId());           // id eşleşmeli
        assertEquals(1, resp.getOrderItems().size());// item'lar maplenmeli
    }

    // getOrdersByUser: ilgili kullanıcının tüm siparişleri response'a maplenir
    @Test
    void getOrdersByUser_success() {
        Order o1 = Order.builder().id("o1").userId("u1").orderItems(List.of()).shippingAddress(new Address()).build();
        Order o2 = Order.builder().id("o2").userId("u1").orderItems(List.of()).shippingAddress(new Address()).build();

        when(orderRepository.findByUserId("u1")).thenReturn(List.of(o1, o2));
        when(userService.getUserResponseById("u1")).thenReturn(new UserResponse());
        when(addressMapper.toResponse(any(Address.class))).thenReturn(new com.mehmetkerem.dto.response.AddressResponse());

        List<OrderResponse> results = orderService.getOrdersByUser("u1");

        assertEquals(2, results.size()); // 2 sipariş dönmeli
    }

    // getAllOrders: tüm siparişler response'a maplenir
    @Test
    void getAllOrders_success() {
        Order o1 = Order.builder().id("o1").userId("u1").orderItems(List.of()).shippingAddress(new Address()).build();
        Order o2 = Order.builder().id("o2").userId("u2").orderItems(List.of()).shippingAddress(new Address()).build();

        when(orderRepository.findAll()).thenReturn(List.of(o1, o2));
        when(userService.getUserResponseById(anyString())).thenReturn(new UserResponse());
        when(addressMapper.toResponse(any(Address.class))).thenReturn(new com.mehmetkerem.dto.response.AddressResponse());

        List<OrderResponse> results = orderService.getAllOrders();

        assertEquals(2, results.size()); // 2 sipariş dönmeli
    }

    // updateOrderStatus: sipariş durumu güncellenir ve response döner
    @Test
    void updateOrderStatus_success() {
        Order order = Order.builder()
                .id("o1")
                .userId("u1")
                .orderStatus(OrderStatus.PENDING)
                .orderItems(List.of())
                .shippingAddress(new Address())
                .build();

        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(userService.getUserResponseById("u1")).thenReturn(new UserResponse());
        when(addressMapper.toResponse(any(Address.class))).thenReturn(new com.mehmetkerem.dto.response.AddressResponse());

        OrderResponse resp = orderService.updateOrderStatus("o1", OrderStatus.SHIPPED);

        assertNotNull(resp);                       // response null olmamalı
        assertEquals(OrderStatus.SHIPPED, resp.getOrderStatus()); // durum güncellenmeli
    }

    // deleteOrder: mevcut sipariş silinir ve mesaj döner
    @Test
    void deleteOrder_success() {
        Order order = Order.builder().id("o1").build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        String result = orderService.deleteOrder("o1");

        String expected = String.format(Messages.DELETE_VALUE, "o1", "sipariş");
        assertEquals(expected, result); // mesaj formatı beklenen gibi
        verify(orderRepository).delete(order); // delete çağrısı yapılmalı
    }
}
