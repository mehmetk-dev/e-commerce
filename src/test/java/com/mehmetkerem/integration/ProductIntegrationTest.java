package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.dto.request.RegisterRequest;
import com.mehmetkerem.enums.Role;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SuppressWarnings("null")
public class ProductIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private ObjectMapper objectMapper;

        private String adminToken;

        @BeforeEach
        void setUp() throws Exception {
                productRepository.deleteAll();
                categoryRepository.deleteAll();
                userRepository.deleteAll();

                // Admin kullanıcısı oluştur (Direct DB save)
                com.mehmetkerem.model.User admin = com.mehmetkerem.model.User.builder()
                                .email("admin@test.com")
                                .name("Admin")
                                .passwordHash(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .build();
                userRepository.save(admin);

                LoginRequest login = new LoginRequest();
                login.setEmail("admin@test.com");
                login.setPassword("admin123");

                MvcResult result = mockMvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                // ResultData içindeki data'dan token'ı al
                adminToken = objectMapper.readTree(response).path("data").path("accessToken").asText();
        }

        @Test
        void shouldCreateAndListProducts() throws Exception {
                // 1. Create Category
                CategoryRequest catReq = new CategoryRequest();
                catReq.setName("Antika Saatler");

                MvcResult catResult = mockMvc.perform(post("/v1/category/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(catReq)))
                                .andExpect(status().isOk())
                                .andReturn();

                Long categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString()).path("data")
                                .path("id")
                                .asLong();

                // 2. Create Product
                ProductRequest productReq = new ProductRequest();
                productReq.setTitle("Köstekli Saat");
                productReq.setDescription("19. yy altın saat");
                productReq.setPrice(new BigDecimal("1500"));
                productReq.setStock(10);
                productReq.setCategoryId(categoryId);
                productReq.setImageUrls(java.util.List.of("http://image.com/1.jpg"));

                mockMvc.perform(post("/v1/product/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title", is("Köstekli Saat")));

                // 3. List Products
                mockMvc.perform(get("/v1/product/find-all")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].title", is("Köstekli Saat")));
        }

        @Test
        void shouldUpdateAndDeleteProduct() throws Exception {
                CategoryRequest catReq = new CategoryRequest();
                catReq.setName("Antika");
                MvcResult catRes = mockMvc.perform(post("/v1/category/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(catReq)))
                                .andExpect(status().isOk())
                                .andReturn();
                Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id")
                                .asLong();

                ProductRequest createReq = new ProductRequest();
                createReq.setTitle("Eski Saat");
                createReq.setPrice(new BigDecimal("500"));
                createReq.setStock(3);
                createReq.setCategoryId(catId);
                createReq.setImageUrls(java.util.List.of("http://img.com/1.jpg"));

                MvcResult createRes = mockMvc.perform(post("/v1/product/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq)))
                                .andExpect(status().isOk())
                                .andReturn();
                Long prodId = objectMapper.readTree(createRes.getResponse().getContentAsString()).path("data")
                                .path("id").asLong();

                ProductRequest updateReq = new ProductRequest();
                updateReq.setTitle("Güncel Antika Saat");
                updateReq.setDescription("Güncellendi");
                updateReq.setPrice(new BigDecimal("600"));
                updateReq.setStock(5);
                updateReq.setCategoryId(catId);
                updateReq.setImageUrls(java.util.List.of("http://img.com/1.jpg"));

                mockMvc.perform(put("/v1/product/" + prodId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title", is("Güncel Antika Saat")));

                mockMvc.perform(delete("/v1/product/" + prodId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk());
        }
}
