package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.dto.request.SupportTicketRequest;
import com.mehmetkerem.dto.request.TicketReplyRequest;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.enums.TicketStatus;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.SupportTicketRepository;
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
public class SupportTicketIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SupportTicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // User
        User user = User.builder()
                .email("help@test.com")
                .name("Help Desk")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        LoginRequest userLogin = new LoginRequest();
        userLogin.setEmail("help@test.com");
        userLogin.setPassword("password");
        MvcResult userRes = mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin))).andReturn();
        userToken = objectMapper.readTree(userRes.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();

        // Admin
        User admin = User.builder()
                .email("admin-help@test.com")
                .name("Admin Support")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin-help@test.com");
        adminLogin.setPassword("admin123");
        MvcResult adminRes = mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin))).andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();
    }

    @Test
    void shouldCompleteSupportTicketLifecycle() throws Exception {
        // 1. User creates ticket
        SupportTicketRequest req = new SupportTicketRequest();
        req.setSubject("Hatalı ürün");
        req.setMessage("Aldığım kamera çalışmıyor.");

        MvcResult createRes = mockMvc.perform(post("/v1/support")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subject", is("Hatalı ürün")))
                .andExpect(jsonPath("$.data.status", is("OPEN")))
                .andReturn();

        Long ticketId = objectMapper.readTree(createRes.getResponse().getContentAsString()).path("data").path("id")
                .asLong();

        // 2. Admin replies to ticket
        TicketReplyRequest reply = new TicketReplyRequest();
        reply.setAdminReply("Sorununuz için özür dileriz. İnceleme başlatıldı.");
        reply.setStatus(TicketStatus.IN_PROGRESS);

        mockMvc.perform(put("/v1/support/" + ticketId + "/reply")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reply)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")));

        // 3. User verifies their tickets
        mockMvc.perform(get("/v1/support/my-tickets")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status", is("IN_PROGRESS")));

        // 4. Admin deletes ticket (cleanup/archival)
        mockMvc.perform(delete("/v1/support/" + ticketId + "/admin")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 5. Verify no tickets left for admin (in filtered view)
        mockMvc.perform(get("/v1/support/all-paged")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)));
    }
}
