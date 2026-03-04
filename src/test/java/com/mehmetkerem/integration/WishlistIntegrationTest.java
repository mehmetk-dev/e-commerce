package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.CategoryRepository;
import com.mehmetkerem.repository.ProductRepository;
import com.mehmetkerem.repository.UserRepository;
import com.mehmetkerem.repository.WishlistRepository;
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
public class WishlistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        wishlistRepository.deleteAll();
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
        MvcResult adminRes = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();

        // User
        User user = User.builder()
                .email("user@test.com")
                .name("User")
                .passwordHash(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        LoginRequest userLogin = new LoginRequest();
        userLogin.setEmail("user@test.com");
        userLogin.setPassword("user123");
        MvcResult userRes = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin)))
                .andReturn();
        userToken = objectMapper.readTree(userRes.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();
    }

    @Test
    void shouldManageWishlist() throws Exception {
        // 1. Create Product
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Jewelry");
        MvcResult catRes = mockMvc.perform(post("/v1/category/save")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catReq)))
                .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        ProductRequest prodReq = new ProductRequest();
        prodReq.setTitle("Gold Ring");
        prodReq.setPrice(new BigDecimal("5000"));
        prodReq.setStock(2);
        prodReq.setCategoryId(catId);
        prodReq.setImageUrls(List.of("img.jpg"));
        MvcResult prodRes = mockMvc.perform(post("/v1/product/save")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prodReq)))
                .andExpect(status().isOk())
                .andReturn();
        Long prodId = objectMapper.readTree(prodRes.getResponse().getContentAsString()).path("data").path("id")
                .asLong();

        // 2. Add to Wishlist
        mockMvc.perform(post("/v1/wishlist/add/" + prodId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id", is(prodId.intValue())));

        // 3. Get Wishlist
        mockMvc.perform(get("/v1/wishlist")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)));

        // 4. Remove from Wishlist
        mockMvc.perform(delete("/v1/wishlist/remove/" + prodId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // 5. Verify Empty
        mockMvc.perform(get("/v1/wishlist")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)));
    }
}
