package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.*;
import com.mehmetkerem.dto.response.OrderReturnResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.enums.ReturnStatus;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SuppressWarnings("null")
public class LifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderReturnRepository returnRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        returnRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        couponRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Admin
        User admin = User.builder()
                .email("admin@test.com")
                .name("Admin")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@test.com");
        adminLogin.setPassword("admin123");
        MvcResult adminRes = mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin))).andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();

        // User
        User user = User.builder()
                .email("tester@test.com")
                .name("Tester")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        userId = user.getId();

        LoginRequest userLogin = new LoginRequest();
        userLogin.setEmail("tester@test.com");
        userLogin.setPassword("password");
        MvcResult userRes = mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin))).andReturn();
        userToken = objectMapper.readTree(userRes.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();
    }

    @Test
    void shouldCompleteCouponLifecycle() throws Exception {
        // 1. Admin creates coupon
        mockMvc.perform(post("/v1/coupons/create")
                .header("Authorization", "Bearer " + adminToken)
                .param("code", "SAVE20")
                .param("discount", "20")
                .param("minAmount", "100")
                .param("days", "7"))
                .andExpect(status().isOk());

        // 2. Setup Order context
        CategoryRequest cat = new CategoryRequest();
        cat.setName("Books");
        MvcResult catRes = mockMvc
                .perform(post("/v1/category/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cat)))
                .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        ProductRequest prod = new ProductRequest();
        prod.setTitle("Rare Book");
        prod.setPrice(new BigDecimal("150"));
        prod.setStock(10);
        prod.setCategoryId(catId);
        prod.setImageUrls(List.of("book.jpg"));
        MvcResult prodRes = mockMvc
                .perform(post("/v1/product/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(prod)))
                .andReturn();
        Long prodId = objectMapper.readTree(prodRes.getResponse().getContentAsString()).path("data").path("id")
                .asLong();

        // 3. User adds to cart
        CartItemRequest ci = new CartItemRequest();
        ci.setProductId(prodId);
        ci.setQuantity(1);
        mockMvc.perform(post("/v1/cart/items").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ci)))
                .andExpect(status().isOk());

        // 4. Apply Coupon
        mockMvc.perform(post("/v1/cart/coupon/SAVE20")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // 5. Total should be 150 - 20 = 130
        mockMvc.perform(get("/v1/cart/total").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(130.0));
    }

    @Test
    void shouldCompleteReturnLifecycle() throws Exception {
        // 1. Create Order as Delivered
        CategoryRequest cat = new CategoryRequest();
        cat.setName("Antiques");
        MvcResult catRes = mockMvc
                .perform(post("/v1/category/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cat)))
                .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        ProductRequest prod = new ProductRequest();
        prod.setTitle("Old Clock");
        prod.setPrice(new BigDecimal("1000"));
        prod.setStock(5);
        prod.setCategoryId(catId);
        prod.setImageUrls(List.of("clock.jpg"));
        MvcResult prodRes = mockMvc
                .perform(post("/v1/product/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(prod)))
                .andReturn();
        Long prodId = objectMapper.readTree(prodRes.getResponse().getContentAsString()).path("data").path("id")
                .asLong();

        // Place Order via Direct Seed for simplicity of state
        com.mehmetkerem.model.Order order = com.mehmetkerem.model.Order.builder()
                .userId(userId)
                .orderStatus(OrderStatus.DELIVERED)
                .paymentStatus(PaymentStatus.PAID)
                .totalAmount(new BigDecimal("1000"))
                .orderItems(List.of(com.mehmetkerem.model.OrderItem.builder().productId(prodId).title("Old Clock")
                        .quantity(1).price(new BigDecimal("1000")).build()))
                .build();
        order = orderRepository.save(order);
        Long orderId = order.getId();

        // 2. User requests return
        OrderReturnRequest returnReq = new OrderReturnRequest();
        returnReq.setOrderId(orderId);
        returnReq.setReason("Arızalı ürün");
        MvcResult returnRes = mockMvc.perform(post("/v1/order/return")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(returnReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andReturn();
        Long returnId = objectMapper.readTree(returnRes.getResponse().getContentAsString()).path("data").path("id")
                .asLong();

        // 3. Admin approves return
        mockMvc.perform(put("/v1/order/return/" + returnId + "/approve")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("APPROVED")));

        // 4. Verify stock restored (5 initially -> 4 after buy (manual order didn't
        // deduct, so let's check current logic))
        // Actually, seed didn't deduct stock.
        // But OrderReturnServiceImpl.approve calls
        // orderService.revertStockForOrder(orderId).
        // Let's check if stock increases.
        mockMvc.perform(get("/v1/product/" + prodId))
                .andExpect(jsonPath("$.data.stock", is(6))); // 5 (seed) + 1 (revert)
    }
}
