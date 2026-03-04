package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.CategoryRepository;
import com.mehmetkerem.repository.ProductRepository;
import com.mehmetkerem.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SuppressWarnings("null")
public class AdvancedProductIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
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

        // Seed Data
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Electronics");
        MvcResult catRes = mockMvc
                .perform(post("/v1/category/save").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(catReq)))
                .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        createProduct("Phone", new BigDecimal("1000"), 10, catId);
        createProduct("Laptop", new BigDecimal("3000"), 5, catId);
        createProduct("Watch", new BigDecimal("500"), 20, catId);
    }

    private void createProduct(String title, BigDecimal price, int stock, Long catId) throws Exception {
        ProductRequest req = new ProductRequest();
        req.setTitle(title);
        req.setPrice(price);
        req.setStock(stock);
        req.setCategoryId(catId);
        req.setImageUrls(List.of("http://example.com/" + title + ".jpg"));
        mockMvc.perform(post("/v1/product/save").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterProductsByPriceRange() throws Exception {
        // Find products between 400 and 1200 (Watch and Phone)
        mockMvc.perform(get("/v1/product/search")
                .param("minPrice", "400")
                .param("maxPrice", "1200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(2)));
    }

    @Test
    void shouldSearchProductsByTitleCaseInsensitive() throws Exception {
        mockMvc.perform(get("/v1/product/search")
                .param("title", "pho")) // phone
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].title", is("Phone")));
    }

    @Test
    void shouldSortProductsByPriceDesc() throws Exception {
        mockMvc.perform(get("/v1/product")
                .param("sortBy", "price")
                .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].title", is("Laptop")))
                .andExpect(jsonPath("$.data.items[2].title", is("Watch")));
    }

    @Test
    void shouldHandlePaginationCorrectly() throws Exception {
        // Page size 1
        mockMvc.perform(get("/v1/product")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElement", is(3)));

        // Page 1
        mockMvc.perform(get("/v1/product")
                .param("page", "1")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }
}
