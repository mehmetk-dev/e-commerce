package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.User;
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
public class StatsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
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
    void adminShouldAccessStats() throws Exception {
        mockMvc.perform(get("/v1/admin/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(true)))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void userShouldNotAccessStats() throws Exception {
        mockMvc.perform(get("/v1/admin/stats")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
