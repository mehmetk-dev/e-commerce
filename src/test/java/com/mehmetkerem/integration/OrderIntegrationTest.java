package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.*;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SuppressWarnings("null")
public class OrderIntegrationTest {

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

        private String userToken;
        private Long userId;
        private String adminToken;

        @BeforeEach
        void setUp() throws Exception {
                productRepository.deleteAll();
                categoryRepository.deleteAll();
                userRepository.deleteAll();
                cartRepository.deleteAll();

                // Register/Login User
                RegisterRequest userReg = new RegisterRequest();
                userReg.setEmail("user@test.com");
                userReg.setName("User");
                userReg.setPassword("password");
                userReg.setRole(Role.USER);

                mockMvc.perform(post("/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userReg)));

                LoginRequest userLogin = new LoginRequest();
                userLogin.setEmail("user@test.com");
                userLogin.setPassword("password");

                MvcResult userResult = mockMvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLogin)))
                                .andReturn();

                String userResponse = userResult.getResponse().getContentAsString();
                userToken = objectMapper.readTree(userResponse).path("data").path("accessToken").asText();
                userId = objectMapper.readTree(userResponse).path("data").path("user").path("id").asLong();

                // Register/Login Admin
                RegisterRequest adminReg = new RegisterRequest();
                adminReg.setEmail("admin@test.com");
                adminReg.setName("Admin");
                adminReg.setPassword("admin123");
                adminReg.setRole(Role.ADMIN);

                mockMvc.perform(post("/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(adminReg)));

                LoginRequest adminLogin = new LoginRequest();
                adminLogin.setEmail("admin@test.com");
                adminLogin.setPassword("admin123");

                MvcResult adminResult = mockMvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(adminLogin)))
                                .andReturn();

                adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).path("data")
                                .path("accessToken").asText();
        }

        @Test
        void shouldPlaceAndVerifyOrder() throws Exception {
                // 1. Create Category & Product (Admin)
                CategoryRequest catReq = new CategoryRequest();
                catReq.setName("Antika");
                MvcResult catRes = mockMvc.perform(post("/v1/category/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(catReq)))
                                .andExpect(status().isCreated())
                                .andReturn();
                Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("id").asLong();

                ProductRequest prodReq = new ProductRequest();
                prodReq.setTitle("Antika Gramofon");
                prodReq.setPrice(new BigDecimal("5000"));
                prodReq.setStock(5);
                prodReq.setCategoryId(catId);
                prodReq.setImageUrls(List.of("http://image.com/gramo.jpg"));

                MvcResult prodRes = mockMvc.perform(post("/v1/product/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(prodReq)))
                                .andExpect(status().isCreated())
                                .andReturn();
                Long prodId = objectMapper.readTree(prodRes.getResponse().getContentAsString()).path("id").asLong();

                // 2. Add to Cart (User)
                CartItemRequest cartReq = new CartItemRequest();
                cartReq.setProductId(prodId);
                cartReq.setQuantity(1);

                mockMvc.perform(post("/v1/cart/items")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cartReq)))
                                .andExpect(status().isOk());

                // 3. Create Address (User)
                AddressRequest addrReq = new AddressRequest();
                addrReq.setTitle("Ev");
                addrReq.setCountry("Turkey");
                addrReq.setCity("Istanbul");
                addrReq.setDistrict("Kadikoy");
                addrReq.setPostalCode("34710");
                addrReq.setAddressLine("Test sokak no 5");

                MvcResult addrRes = mockMvc.perform(post("/v1/address/save")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(addrReq)))
                                .andExpect(status().isOk())
                                .andReturn();
                Long addrId = objectMapper.readTree(addrRes.getResponse().getContentAsString()).path("data").path("id")
                                .asLong();

                // 4. Place Order (User)
                OrderRequest orderReq = new OrderRequest();
                orderReq.setAddressId(addrId);
                orderReq.setPaymentStatus(PaymentStatus.PAID);
                orderReq.setNote("Hassas kargo");

                mockMvc.perform(post("/v1/order/save")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.orderStatus", is("PENDING")));

                // 5. Verify Order in List (Admin) - sayfalı yanıt: data.items
                mockMvc.perform(get("/v1/order/all")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.items", hasSize(1)))
                                .andExpect(jsonPath("$.data.items[0].user.id", is(userId.intValue())));
        }
}
