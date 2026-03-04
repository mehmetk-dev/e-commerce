package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.AddressRepository;
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
public class AddressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        addressRepository.deleteAll();
        userRepository.deleteAll();

        // Create User
        User user = User.builder()
                .email("address@test.com")
                .name("Address User")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Login
        LoginRequest login = new LoginRequest();
        login.setEmail("address@test.com");
        login.setPassword("password");
        MvcResult res = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        userToken = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();
    }

    @Test
    void shouldManageUserAddresses() throws Exception {
        // 1. Save Address
        AddressRequest req = new AddressRequest();
        req.setTitle("Home");
        req.setCountry("Turkey");
        req.setCity("Istanbul");
        req.setDistrict("Besiktas");
        req.setAddressLine("Carsi");
        req.setPostalCode("34340");

        mockMvc.perform(post("/v1/address/save")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Home")));

        // 2. List Addresses
        mockMvc.perform(get("/v1/address/my-addresses")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        // 3. Update Address
        var addr = addressRepository.findAll().get(0);
        req.setTitle("Work");
        mockMvc.perform(put("/v1/address/" + addr.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Work")));

        // 4. Delete Address
        mockMvc.perform(delete("/v1/address/" + addr.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // 5. Verify Empty
        mockMvc.perform(get("/v1/address/my-addresses")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
