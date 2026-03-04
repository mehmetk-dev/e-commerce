package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.*;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SuppressWarnings("null")
public class ReviewIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private ReviewRepository reviewRepository;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String userToken;
        private Long userId;
        private String adminToken;

        @BeforeEach
        void setUp() throws Exception {
                reviewRepository.deleteAll();
                orderRepository.deleteAll();
                productRepository.deleteAll();
                categoryRepository.deleteAll();
                userRepository.deleteAll();

                // Create Admin
                User adminUser = User.builder()
                                .email("admin@test.com")
                                .name("Admin")
                                .passwordHash(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .build();
                userRepository.save(adminUser);

                // Login Admin
                LoginRequest adminLogin = new LoginRequest();
                adminLogin.setEmail("admin@test.com");
                adminLogin.setPassword("admin123");
                MvcResult adminResult = mockMvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(adminLogin)))
                                .andReturn();
                adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).path("data")
                                .path("accessToken").asText();

                // Create User
                User normalUser = User.builder()
                                .email("user@test.com")
                                .name("User")
                                .passwordHash(passwordEncoder.encode("user123"))
                                .role(Role.USER)
                                .build();
                userRepository.save(normalUser);
                userId = normalUser.getId();

                // Login User
                LoginRequest userLogin = new LoginRequest();
                userLogin.setEmail("user@test.com");
                userLogin.setPassword("user123");
                MvcResult userResult = mockMvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLogin)))
                                .andReturn();
                userToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).path("data")
                                .path("accessToken").asText();
        }

        @Test
        void shouldCreateAndVerifyProductReview() throws Exception {
                // 1. Setup Product (Admin)
                CategoryRequest catReq = new CategoryRequest();
                catReq.setName("Antika");
                MvcResult catRes = mockMvc.perform(post("/v1/category/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(catReq)))
                                .andReturn();
                Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id")
                                .asLong();

                ProductRequest prodReq = new ProductRequest();
                prodReq.setTitle("Eski Kamera");
                prodReq.setPrice(new BigDecimal("1200"));
                prodReq.setStock(5);
                prodReq.setCategoryId(catId);
                prodReq.setImageUrls(List.of("http://img.com/cam.jpg"));
                MvcResult prodRes = mockMvc.perform(post("/v1/product/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(prodReq)))
                                .andReturn();
                Long prodId = objectMapper.readTree(prodRes.getResponse().getContentAsString()).path("data").path("id")
                                .asLong();

                // 2. Setup Order (Manual DB Seed to verify purchase requirement)
                Order order = Order.builder()
                                .userId(userId)
                                .orderStatus(OrderStatus.DELIVERED)
                                .paymentStatus(PaymentStatus.PAID)
                                .totalAmount(new BigDecimal("1200"))
                                .orderItems(List.of(OrderItem.builder()
                                                .productId(prodId)
                                                .title("Eski Kamera")
                                                .quantity(1)
                                                .price(new BigDecimal("1200"))
                                                .build()))
                                .build();
                orderRepository.save(order);

                // 3. Post Review (User)
                ReviewRequest reviewReq = new ReviewRequest();
                reviewReq.setProductId(prodId);
                reviewReq.setComment("Harika bir antika!");
                reviewReq.setRating(5.0);

                mockMvc.perform(post("/v1/review/save")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reviewReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is(true)))
                                .andExpect(jsonPath("$.data.comment", is("Harika bir antika!")))
                                .andExpect(jsonPath("$.data.rating").value(5.0));
        }
}
