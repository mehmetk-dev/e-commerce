package com.mehmetkerem.service.impl;


import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.jwt.JwtService;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authManager, UserRepository userRepository, PasswordEncoder encoder, JwtService jwtService) {
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public Map<String,String> register(com.mehmetkerem.dto.request.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())){
            throw new BadRequestException(String.format(ExceptionMessages.EMAIL_ALL_READY_EXISTS,req.getEmail()));
        }

        var user = User.builder()
                .email(req.getEmail())
                .name(req.getName())
                .passwordHash(encoder.encode(req.getPassword()))
                .role(req.getRole() == null ? Role.USER : req.getRole())
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return Map.of("token", token);
    }

    public Map<String,String> login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        var user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        String token = jwtService.generateToken(user);
        return Map.of("token", token);
    }
}
