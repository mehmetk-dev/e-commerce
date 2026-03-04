package com.mehmetkerem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.request.OrderReturnRequest;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.OrderItem;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SuppressWarnings("null")
public class SecurityDetailedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderReturnRepository returnRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String user1Token;
    private String user2Token;
    private Long user2Id;

    @BeforeEach
    void setUp() throws Exception {
        returnRepository.deleteAll();
        orderRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();

        // User 1
        User user1 = User.builder()
                .email("u1@test.com")
                .passwordHash(passwordEncoder.encode("p1"))
                .role(Role.USER)
                .build();
        userRepository.save(user1);

        LoginRequest l1 = new LoginRequest();
        l1.setEmail("u1@test.com");
        l1.setPassword("p1");
        MvcResult res1 = mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(l1))).andReturn();
        user1Token = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();

        // User 2
        User user2 = User.builder()
                .email("u2@test.com")
                .passwordHash(passwordEncoder.encode("p2"))
                .role(Role.USER)
                .build();
        userRepository.save(user2);
        user2Id = user2.getId();

        LoginRequest l2 = new LoginRequest();
        l2.setEmail("u2@test.com");
        l2.setPassword("p2");
        MvcResult res2 = mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(l2))).andReturn();
        user2Token = objectMapper.readTree(res2.getResponse().getContentAsString()).path("data").path("accessToken")
                .asText();
    }

    @Test
    void shouldPreventUserFromReturningOtherUsersOrder() throws Exception {
        // 1. Create Order for User 2
        com.mehmetkerem.model.Order order = com.mehmetkerem.model.Order.builder()
                .userId(user2Id)
                .orderStatus(OrderStatus.DELIVERED)
                .paymentStatus(PaymentStatus.PAID)
                .totalAmount(BigDecimal.TEN)
                .build();
        order = orderRepository.save(order);

        // 2. User 1 tries to return User 2's order
        OrderReturnRequest req = new OrderReturnRequest();
        req.setOrderId(order.getId());
        req.setReason("Steal");

        mockMvc.perform(post("/v1/order/return")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest()); // Handled by service check
    }

    @Test
    void shouldPreventUserFromPlacingOrderWithOtherUsersAddress() throws Exception {
        // 1. User 2 creates address
        AddressRequest addrReq = new AddressRequest();
        addrReq.setTitle("Secret");
        addrReq.setCountry("Tr");
        addrReq.setCity("Is");
        addrReq.setDistrict("Ka");
        addrReq.setPostalCode("123");
        addrReq.setAddressLine("Hidden");
        MvcResult addrRes = mockMvc.perform(post("/v1/address/save")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addrReq)))
                .andReturn();
        Long addrId = objectMapper.readTree(addrRes.getResponse().getContentAsString()).path("data").path("id")
                .asLong();

        // 2. User 1 tries to order using User 2's address ID
        OrderRequest orderReq = new OrderRequest();
        orderReq.setAddressId(addrId);

        mockMvc.perform(post("/v1/order/save")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isBadRequest()); // Handled by addressService.getAddressByIdAndUserId
    }

    @Test
    void shouldPreventUserFromApprovingReturns() throws Exception {
        mockMvc.perform(put("/v1/order/return/1/approve")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isForbidden());
    }
}
