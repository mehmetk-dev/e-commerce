package com.mehmetkerem.jwt;

import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@test.com")
                .passwordHash("encoded")
                .name("User")
                .role(com.mehmetkerem.enums.Role.USER)
                .build();
    }

    @Test
    @DisplayName("loadUserByUsername - kullanıcı bulunursa UserDetails döner")
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("user@test.com");

        assertNotNull(details);
        assertEquals("user@test.com", details.getUsername());
        verify(userRepository).findByEmail("user@test.com");
    }

    @Test
    @DisplayName("loadUserByUsername - kullanıcı yoksa UsernameNotFoundException")
    void loadUserByUsername_WhenUserNotExists_ShouldThrow() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown@test.com"));
        verify(userRepository).findByEmail("unknown@test.com");
    }
}
