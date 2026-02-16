package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.UserMapper;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.UserRepository;
import com.mehmetkerem.service.IAddressService;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IAddressService addressService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest userRequest;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setName("Test User");
        userRequest.setEmail("test@test.com");
        userRequest.setPassword("password123");
        userRequest.setRole(Role.USER);

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@test.com")
                .passwordHash("encoded")
                .role(Role.USER)
                .build();

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("Test User");
        userResponse.setEmail("test@test.com");
    }

    @Test
    @DisplayName("saveUser - yeni kullanıcı kaydedilir")
    void saveUser_WhenEmailNotExists_ShouldSaveAndReturnResponse() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseWithAddresses(user, Collections.emptyList())).thenReturn(userResponse);

        UserResponse result = userService.saveUser(userRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@test.com", result.getEmail());
        verify(userRepository, atLeast(1)).save(any(User.class));
    }

    @Test
    @DisplayName("saveUser - email zaten kayıtlıysa BadRequestException")
    void saveUser_WhenEmailExists_ShouldThrowBadRequestException() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.saveUser(userRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserById - mevcut kullanıcı döner")
    void getUserById_WhenExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    @DisplayName("getUserById - olmayan id ile NotFoundException")
    void getUserById_WhenNotExists_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("getUserResponseById - response döner")
    void getUserResponseById_ShouldReturnResponseWithAddresses() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressService.getAddressesByUserId(1L)).thenReturn(Collections.emptyList());
        when(userMapper.toResponseWithAddresses(user, Collections.emptyList())).thenReturn(userResponse);

        UserResponse result = userService.getUserResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(addressService).getAddressesByUserId(1L);
    }

    @Test
    @DisplayName("updateUser - aynı email ile güncelleme başarılı")
    void updateUser_WhenSameEmail_ShouldUpdate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(addressService.getAddressesByUserId(1L)).thenReturn(Collections.emptyList());
        when(userMapper.toResponseWithAddresses(user, Collections.emptyList())).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, userRequest);

        assertNotNull(result);
        verify(userMapper).update(eq(user), eq(userRequest));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser - başka kullanıcının email'i ile BadRequestException")
    void updateUser_WhenNewEmailAlreadyExists_ShouldThrowBadRequestException() {
        userRequest.setEmail("other@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateUser(1L, userRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUser - kullanıcı silinir")
    void deleteUser_WhenExists_ShouldDeleteAndReturnMessage() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        String result = userService.deleteUser(1L);

        assertTrue(result.contains("1"));
        assertTrue(result.contains("kullanıcı"));
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("findAllUsers - tüm kullanıcılar listelenir")
    void findAllUsers_ShouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(addressService.getAddressesByUserId(1L)).thenReturn(Collections.emptyList());
        when(userMapper.toResponseWithAddresses(user, Collections.emptyList())).thenReturn(userResponse);

        List<UserResponse> result = userService.findAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
