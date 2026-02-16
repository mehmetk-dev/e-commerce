package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestUserControllerImpl;
import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.service.IUserService;
import com.mehmetkerem.util.ResultData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RestUserControllerTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private RestUserControllerImpl controller;

    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setName("Test");
        userRequest.setEmail("test@test.com");
        userRequest.setPassword("password123");
        userRequest.setRole(com.mehmetkerem.enums.Role.USER);
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@test.com");
    }

    @Test
    @DisplayName("saveUser - 201 ve kullanıcı döner")
    void saveUser_ShouldReturn201() {
        when(userService.saveUser(any(UserRequest.class))).thenReturn(userResponse);

        ResultData<UserResponse> response = controller.saveUser(userRequest);

        assertTrue(response.isStatus());
        assertEquals(1L, response.getData().getId());
        verify(userService).saveUser(userRequest);
    }

    @Test
    @DisplayName("getUserById - kullanıcı döner")
    void getUserById_ShouldReturnUser() {
        when(userService.getUserResponseById(1L)).thenReturn(userResponse);

        ResultData<UserResponse> response = controller.getUserById(1L);

        assertTrue(response.isStatus());
        assertEquals(1L, response.getData().getId());
        verify(userService).getUserResponseById(1L);
    }

    @Test
    @DisplayName("updateUser - güncel kullanıcı döner")
    void updateUser_ShouldReturnUpdated() {
        when(userService.updateUser(1L, userRequest)).thenReturn(userResponse);

        ResultData<UserResponse> response = controller.updateUser(1L, userRequest);

        assertTrue(response.isStatus());
        verify(userService).updateUser(1L, userRequest);
    }

    @Test
    @DisplayName("deleteUser - mesaj döner")
    void deleteUser_ShouldReturnMessage() {
        when(userService.deleteUser(1L)).thenReturn("1 ID'li kullanıcı silinmiştir!");

        ResultData<String> response = controller.deleteUser(1L);

        assertTrue(response.isStatus());
        assertTrue(response.getData().contains("1"));
        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("findAllUser - liste döner")
    void findAllUser_ShouldReturnList() {
        when(userService.findAllUsers()).thenReturn(List.of(userResponse));

        ResultData<List<UserResponse>> response = controller.findAllUser();

        assertTrue(response.isStatus());
        assertEquals(1, response.getData().size());
        verify(userService).findAllUsers();
    }
}
