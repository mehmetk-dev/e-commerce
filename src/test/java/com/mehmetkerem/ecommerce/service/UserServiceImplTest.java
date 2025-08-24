package com.mehmetkerem.ecommerce.service;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.mapper.UserMapper;
import com.mehmetkerem.model.Address;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.UserRepository;
import com.mehmetkerem.service.impl.AddressServiceImpl;
import com.mehmetkerem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressServiceImpl addressService;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveUser_success() {
        UserRequest request = new UserRequest();
        request.setEmail("test@example.com");
        request.setAddressIds(List.of("1"));

        User userEntity = new User();
        UserResponse response = new UserResponse();
        Address address = new Address();
        AddressResponse addressResponse = new AddressResponse();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(addressService.getAddressById("1")).thenReturn(address);
        when(addressMapper.toResponse(address)).thenReturn(addressResponse);
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toResponseWithAddresses(userEntity, List.of(addressResponse))).thenReturn(response);

        UserResponse result = userService.saveUser(request);
        assertNotNull(result);
    }

    @Test
    void testSaveUser_emailExists() {
        UserRequest request = new UserRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.saveUser(request));
    }

    @Test
    void testGetUserById_success() {
        User user = new User();
        user.setId("1");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        User result = userService.getUserById("1");
        assertEquals("1", result.getId());
    }

    @Test
    void testGetUserById_notFound() {
        when(userRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserById("2"));
    }

    @Test
    void testGetUserResponseById_success() {
        User user = new User();
        user.setId("1");
        UserResponse response = new UserResponse();
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(addressService.getAddressesByUser(user)).thenReturn(List.of());
        when(userMapper.toResponseWithAddresses(user, List.of())).thenReturn(response);

        UserResponse result = userService.getUserResponseById("1");
        assertNotNull(result);
    }

    @Test
    void testUpdateUser_success() {
        UserRequest request = new UserRequest();
        request.setEmail("new@example.com");
        User currentUser = new User();
        currentUser.setEmail("old@example.com");
        UserResponse response = new UserResponse();

        when(userRepository.findById("1")).thenReturn(Optional.of(currentUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        doNothing().when(userMapper).update(currentUser, request);
        when(userRepository.save(currentUser)).thenReturn(currentUser);
        when(addressService.getAddressesByUser(currentUser)).thenReturn(List.of());
        when(userMapper.toResponseWithAddresses(currentUser, List.of())).thenReturn(response);

        UserResponse result = userService.updateUser("1", request);
        assertNotNull(result);
    }

    @Test
    void testUpdateUser_emailConflict() {
        UserRequest request = new UserRequest();
        request.setEmail("conflict@example.com");
        User currentUser = new User();
        currentUser.setEmail("old@example.com");

        when(userRepository.findById("1")).thenReturn(Optional.of(currentUser));
        when(userRepository.existsByEmail("conflict@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateUser("1", request));
    }

    @Test
    void testDeleteUser_success() {
        User user = new User();
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        String result = userService.deleteUser("1");
        assertTrue(result.contains("1"));
    }

    @Test
    void testDeleteUser_notFound() {
        when(userRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.deleteUser("2"));
    }

    @Test
    void testFindAllUsers_success() {
        User user1 = new User();
        User user2 = new User();
        UserResponse response1 = new UserResponse();
        UserResponse response2 = new UserResponse();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(addressService.getAddressesByUser(user1)).thenReturn(List.of());
        when(addressService.getAddressesByUser(user2)).thenReturn(List.of());
        when(userMapper.toResponseWithAddresses(user1, List.of())).thenReturn(response1);
        when(userMapper.toResponseWithAddresses(user2, List.of())).thenReturn(response2);

        List<UserResponse> results = userService.findAllUsers();
        assertEquals(2, results.size());
    }
}
