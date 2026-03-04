package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.*;
import com.mehmetkerem.enums.PaymentStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SuppressWarnings("null")
public class PurchaseDetailedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private Long addrId;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create Admin
        User admin = User.builder()
                .email("admin@test.com")
                .name("Admin")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        // Login Admin
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@test.com");
        adminLogin.setPassword("admin123");
        MvcResult adminRes = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();

        // Create User
        User user = User.builder()
                .email("buyer@test.com")
                .name("Buyer")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Login User
        LoginRequest userLogin = new LoginRequest();
        userLogin.setEmail("buyer@test.com");
        userLogin.setPassword("password");
        MvcResult userRes = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin)))
                .andReturn();
        userToken = objectMapper.readTree(userRes.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();

        // Setup common address
        AddressRequest addrReq = new AddressRequest();
        addrReq.setTitle("Home");
        addrReq.setCountry("Turkey");
        addrReq.setCity("Istanbul");
        addrReq.setDistrict("Kadikoy");
        addrReq.setPostalCode("34710");
        addrReq.setAddressLine("Test st 123");
        MvcResult addrRes = mockMvc.perform(post("/v1/address/save")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addrReq)))
                .andReturn();
        addrId = objectMapper.readTree(addrRes.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    @Test
    void shouldPurchaseMultipleItemsAndDeductStockCorrectly() throws Exception {
        // 1. Create 2 Products
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Antiques");
        MvcResult catRes = mockMvc.perform(post("/v1/category/save")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catReq)))
                .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        ProductRequest p1 = new ProductRequest();
        p1.setTitle("Item A");
        p1.setPrice(new BigDecimal("100"));
        p1.setStock(10);
        p1.setCategoryId(catId);
        p1.setImageUrls(List.of("a.jpg"));
        MvcResult p1Res = mockMvc
                .perform(post("/v1/product/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p1)))
                .andReturn();
        Long p1Id = objectMapper.readTree(p1Res.getResponse().getContentAsString()).path("data").path("id").asLong();

        ProductRequest p2 = new ProductRequest();
        p2.setTitle("Item B");
        p2.setPrice(new BigDecimal("200"));
        p2.setStock(20);
        p2.setCategoryId(catId);
        p2.setImageUrls(List.of("b.jpg"));
        MvcResult p2Res = mockMvc
                .perform(post("/v1/product/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p2)))
                .andReturn();
        Long p2Id = objectMapper.readTree(p2Res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. Add both to cart
        CartItemRequest c1 = new CartItemRequest();
        c1.setProductId(p1Id);
        c1.setQuantity(3);
        mockMvc.perform(post("/v1/cart/items").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(c1)))
                .andExpect(status().isOk());

        CartItemRequest c2 = new CartItemRequest();
        c2.setProductId(p2Id);
        c2.setQuantity(2);
        mockMvc.perform(post("/v1/cart/items").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(c2)))
                .andExpect(status().isOk());

        // 3. Checkout
        OrderRequest orderReq = new OrderRequest();
        orderReq.setAddressId(addrId);
        orderReq.setPaymentStatus(PaymentStatus.PAID);
        mockMvc.perform(post("/v1/order/save").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAmount").value(700.0)); // (3*100) + (2*200)

        // 4. Verify Stocks
        mockMvc.perform(get("/v1/product/" + p1Id)).andExpect(jsonPath("$.data.stock", is(7))); // 10 - 3
        mockMvc.perform(get("/v1/product/" + p2Id)).andExpect(jsonPath("$.data.stock", is(18))); // 20 - 2
    }

    @Test
    void shouldFailWhenInsufficientStockInitially() throws Exception {
        // 1. Create Product with 5 Stock
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Limited");
        MvcResult catRes = mockMvc
                .perform(post("/v1/category/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(catReq)))
                .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        ProductRequest p = new ProductRequest();
        p.setTitle("Rare Coin");
        p.setPrice(new BigDecimal("999"));
        p.setStock(5);
        p.setCategoryId(catId);
        p.setImageUrls(List.of("coin.jpg"));
        MvcResult pRes = mockMvc
                .perform(post("/v1/product/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p)))
                .andReturn();
        Long pId = objectMapper.readTree(pRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. Try to add 10 to cart (should fail based on CartServiceImpl.addItem
        // validation)
        CartItemRequest c = new CartItemRequest();
        c.setProductId(pId);
        c.setQuantity(10);
        mockMvc.perform(post("/v1/cart/items").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExhaustStockAndRejectSubsequentPurchases() throws Exception {
        // 1. Create Product with 1 Stock
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Single");
        MvcResult catRes = mockMvc
                .perform(post("/v1/category/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(catReq)))
                .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        ProductRequest p = new ProductRequest();
        p.setTitle("Last Item");
        p.setPrice(new BigDecimal("50"));
        p.setStock(1);
        p.setCategoryId(catId);
        p.setImageUrls(List.of("last.jpg"));
        MvcResult pRes = mockMvc
                .perform(post("/v1/product/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p)))
                .andReturn();
        Long pId = objectMapper.readTree(pRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. Successful Purchase 1 item
        CartItemRequest c1 = new CartItemRequest();
        c1.setProductId(pId);
        c1.setQuantity(1);
        mockMvc.perform(post("/v1/cart/items").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(c1)))
                .andExpect(status().isOk());

        OrderRequest orderReq = new OrderRequest();
        orderReq.setAddressId(addrId);
        mockMvc.perform(post("/v1/order/save").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isOk());

        // 3. Verify stock is 0
        mockMvc.perform(get("/v1/product/" + pId)).andExpect(jsonPath("$.data.stock", is(0)));

        // 4. Try to buy again (addItem should fail)
        mockMvc.perform(post("/v1/cart/items").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(c1)))
                .andExpect(status().isBadRequest());
    }
}
