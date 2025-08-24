package com.mehmetkerem.SpringMVCBackEnd.service;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartItemResponse;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.CartItemMapper;
import com.mehmetkerem.mapper.CartMapper;
import com.mehmetkerem.model.Cart;
import com.mehmetkerem.model.CartItem;
import com.mehmetkerem.model.Product;
import com.mehmetkerem.repository.CartRepository;
import com.mehmetkerem.service.IProductService;
import com.mehmetkerem.service.IUserService;
import com.mehmetkerem.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private IUserService userService;
    @Mock
    private IProductService productService;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // mock nesnelerini başlat
    }

    // saveCart: yeni sepet oluşturulur ve kaydedilir
    @Test
    void saveCart_success() {
        String userId = "user1";
        String pid = "p1";

        CartItemRequest req = new CartItemRequest();
        req.setProductId(pid);
        req.setQuantity(2);

        CartItem item = new CartItem(pid, 2, BigDecimal.TEN);

        ProductResponse pResp = new ProductResponse();
        pResp.setId(pid);
        pResp.setPrice(BigDecimal.TEN);
        pResp.setStock(10);

        Cart savedCart = Cart.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        // userService mock
        com.mehmetkerem.model.User u = new com.mehmetkerem.model.User();
        u.setId(userId);
        when(userService.getUserById(userId)).thenReturn(u);

        when(cartRepository.findById(userId)).thenReturn(Optional.empty());
        when(cartItemMapper.toEntityCartItem(List.of(req))).thenReturn(List.of(item));
        when(productService.getProductResponsesByIds(List.of(pid))).thenReturn(List.of(pResp));
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        CartResponse mapped = new CartResponse();
        mapped.setId(userId);
        mapped.setItems(new ArrayList<>());
        when(cartMapper.toResponse(any(Cart.class))).thenReturn(mapped);

        CartResponse response = cartService.saveCart(userId, List.of(req));

        assertNotNull(response); // response null olmamalı
        verify(userService).getUserById(userId); // user bilgisi alınmalı
        verify(cartRepository).save(any(Cart.class)); // sepet kaydedilmeli
    }

    // getCartByUserId: bulunamazsa NotFoundException fırlatır
    @Test
    void getCartByUserId_notFound() {
        when(cartRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.getCartByUserId("u1")); // sepet yoksa exception
    }

    // addItem: sepete yeni ürün eklenir
    @Test
    void addItem_success() {
        String userId = "u1";
        String pid = "p1";

        CartItemRequest req = new CartItemRequest();
        req.setProductId(pid);
        req.setQuantity(1);

        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>()).build();

        Product product = new Product();
        product.setId(pid);
        product.setPrice(BigDecimal.ONE);
        product.setStock(5);

        CartItem item = new CartItem(pid, 1, BigDecimal.ONE);

        ProductResponse prodResp = new ProductResponse();
        prodResp.setId(pid);
        prodResp.setPrice(BigDecimal.ONE);
        prodResp.setStock(5);

        CartItemResponse itemResp = new CartItemResponse();

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(productService.getProductById(pid)).thenReturn(product);
        when(cartItemMapper.toEntity(req)).thenReturn(item);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        when(productService.getProductResponsesByIds(List.of(pid))).thenReturn(List.of(prodResp));
        when(cartItemMapper.toResponseWithProduct(any(CartItem.class), eq(prodResp))).thenReturn(itemResp);

        CartResponse resp = cartService.addItem(userId, req);

        assertNotNull(resp); // response dönmeli
        assertEquals(1, resp.getItems().size()); // eklenen ürün listede olmalı
        verify(cartRepository).save(any(Cart.class));
    }

    // updateItemQuantity: sepetteki ürün adedi güncellenir
    @Test
    void updateItemQuantity_success() {
        String userId = "u1";
        String pid = "p1";

        Product product = new Product();
        product.setId(pid);
        product.setPrice(BigDecimal.TEN);
        product.setStock(10);

        CartItem cartItem = new CartItem(pid, 1, BigDecimal.TEN);
        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>(List.of(cartItem))).build();

        ProductResponse prodResp = new ProductResponse();
        prodResp.setId(pid);
        prodResp.setPrice(BigDecimal.TEN);
        prodResp.setStock(10);

        CartItemResponse itemResp = new CartItemResponse();

        when(productService.getProductById(pid)).thenReturn(product);
        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(productService.getProductResponsesByIds(List.of(pid))).thenReturn(List.of(prodResp));
        when(cartItemMapper.toResponseWithProduct(any(CartItem.class), eq(prodResp))).thenReturn(itemResp);

        CartResponse resp = cartService.updateItemQuantity(userId, pid, 5);

        assertNotNull(resp); // response boş olmamalı
        assertEquals(5, cart.getItems().get(0).getQuantity()); // quantity güncellenmeli
        assertEquals(1, resp.getItems().size()); // ürün listesi aynı kalmalı
    }

    // updateItemQuantity: ürün sepette yoksa NotFoundException
    @Test
    void updateItemQuantity_productNotInCart_throwsNotFound() {
        String userId = "u1";
        String pid = "p1";

        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>()).build();
        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        Product product = new Product();
        product.setId(pid);
        product.setStock(10);
        product.setPrice(BigDecimal.TEN);
        when(productService.getProductById(pid)).thenReturn(product);

        assertThrows(NotFoundException.class, () -> cartService.updateItemQuantity(userId, pid, 5)); // ürün yoksa exception
        verify(cartRepository, never()).save(any());
    }

    // removeItem: ürün varsa listeden silinir
    @Test
    void removeItem_success() {
        String userId = "u1";
        String pid = "p1";
        CartItem cartItem = new CartItem(pid, 1, BigDecimal.ONE);
        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>(List.of(cartItem))).build();

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        CartResponse resp = cartService.removeItem(userId, pid);

        assertNotNull(resp); // response boş olmamalı
        assertTrue(resp.getItems().isEmpty()); // item silinmiş olmalı
    }

    // removeItem: ürün bulunamazsa NotFoundException
    @Test
    void removeItem_notFound() {
        Cart cart = Cart.builder().userId("u1").items(new ArrayList<>()).build();
        when(cartRepository.findById("u1")).thenReturn(Optional.of(cart));

        assertThrows(NotFoundException.class, () -> cartService.removeItem("u1", "pX")); // bulunamayan ürün
    }

    // clearCart: sepet tamamen boşaltılır
    @Test
    void clearCart_success() {
        Cart cart = Cart.builder()
                .userId("u1")
                .items(new ArrayList<>(List.of(new CartItem("p1", 1, BigDecimal.ONE))))
                .build();

        when(cartRepository.findById("u1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        String result = cartService.clearCart("u1");

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        Cart saved = captor.getValue();

        assertTrue(saved.getItems().isEmpty(), "Cart items should be cleared"); // sepet boş olmalı
        assertNotNull(result); // mesaj null olmamalı
        assertFalse(result.isBlank()); // mesaj boş string olmamalı
    }

    // calculateTotal: sepetteki ürünlerin toplam fiyatı hesaplanır
    @Test
    void calculateTotal_success() {
        String userId = "u1";
        CartItem item1 = new CartItem("p1", 2, BigDecimal.TEN); // toplam 20
        CartItem item2 = new CartItem("p2", 1, BigDecimal.valueOf(5)); // toplam 5
        Cart cart = Cart.builder().userId(userId).items(List.of(item1, item2)).build();

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        BigDecimal total = cartService.calculateTotal(userId);

        assertEquals(BigDecimal.valueOf(25), total); // toplam doğru hesaplanmalı
    }

    // updateItemQuantity: ürün stoğu yetersizse BadRequestException
    @Test
    void updateItemQuantity_insufficientStock_throwsBadRequest() {
        String userId = "u1";
        String pid = "p1";

        CartItem existing = new CartItem(pid, 1, BigDecimal.TEN);
        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>(List.of(existing))).build();
        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        Product product = new Product();
        product.setId(pid);
        product.setStock(1); // yetersiz stok
        product.setPrice(BigDecimal.TEN);
        when(productService.getProductById(pid)).thenReturn(product);

        assertThrows(BadRequestException.class, () -> cartService.updateItemQuantity(userId, pid, 5)); // stok yetersiz olmalı
    }
}
