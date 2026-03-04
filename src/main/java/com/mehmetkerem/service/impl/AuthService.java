package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.dto.request.PasswordResetRequest;
import com.mehmetkerem.dto.request.RegisterRequest;
import com.mehmetkerem.dto.request.TokenRefreshRequest;
import com.mehmetkerem.dto.response.LoginResponse;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.jwt.JwtService;
import com.mehmetkerem.model.PasswordResetToken;
import com.mehmetkerem.model.RefreshToken;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.PasswordResetTokenRepository;
import com.mehmetkerem.repository.UserRepository;
import com.mehmetkerem.service.INotificationService;
import com.mehmetkerem.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements com.mehmetkerem.service.IAuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final IUserService userService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final INotificationService notificationService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public Map<String, String> register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(String.format(ExceptionMessages.EMAIL_ALL_READY_EXISTS, email));
        }

        var user = User.builder()
                .email(email)
                .name(req.getName())
                .passwordHash(encoder.encode(req.getPassword()))
                .role(Role.USER) // Güvenlik: Her zaman USER rolü ile başlar.
                .build();
        userRepository.save(user);

        // Send Welcome Email
        notificationService.sendWelcomeEmail(user.getEmail(), user.getName());

        String token = jwtService.generateToken(user);
        return Map.of("token", token);
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.getPassword()));
        var user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, email, "kullanıcı")));
        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken.getToken())
                .user(userService.getUserResponseById(user.getId()))
                .build();
    }

    @Override
    public LoginResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return LoginResponse.builder()
                            .accessToken(token)
                            .refreshToken(requestRefreshToken)
                            .user(userService.getUserResponseById(user.getId()))
                            .build();
                })
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database!"));
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + email));

        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);

        String link = baseUrl.replaceAll("/$", "") + "/reset-password?token=" + token;
        notificationService.sendPasswordResetLink(email, link);
    }

    @Override
    public void resetPassword(PasswordResetRequest request) {
        String validationResult = userService.validatePasswordResetToken(request.getToken());

        if (validationResult != null) {
            throw new BadRequestException("Geçersiz veya süresi dolmuş token: " + validationResult);
        }

        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Token bulunamadı"));

        User user = passToken.getUser();
        userService.changeUserPassword(user, request.getNewPassword());
        passwordResetTokenRepository.delete(passToken);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userService.getUserById(userId);

        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            if (!encoder.matches(oldPassword, user.getPasswordHash())) {
                throw new BadRequestException("Mevcut şifre hatalı.");
            }
        }

        userService.changeUserPassword(user, newPassword);
    }

    @Override
    public com.mehmetkerem.dto.response.UserResponse updateProfile(Long userId,
            com.mehmetkerem.dto.request.ProfileUpdateRequest request) {
        User user = userService.getUserById(userId);

        String newName = request.getName() != null ? request.getName().trim() : "";
        String oldName = user.getName() != null ? user.getName().trim() : "";

        if (newName.equalsIgnoreCase(oldName)) {
            throw new BadRequestException("Yeni isim eskisiyle aynı olamaz.");
        }

        if (newName.isEmpty()) {
            throw new BadRequestException("İsim alanı boş bırakılamaz.");
        }

        user.setName(newName);
        userRepository.save(user);
        return userService.getUserResponseById(userId);
    }

    @Override
    public com.mehmetkerem.dto.response.UserResponse getMe(Long userId) {
        return userService.getUserResponseById(userId);
    }
}
