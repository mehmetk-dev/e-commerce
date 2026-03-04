package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.dto.request.PasswordResetRequest;
import com.mehmetkerem.dto.request.ProfileUpdateRequest;
import com.mehmetkerem.dto.request.RegisterRequest;
import com.mehmetkerem.dto.request.TokenRefreshRequest;
import com.mehmetkerem.dto.response.LoginResponse;
import com.mehmetkerem.dto.response.UserResponse;

import java.util.Map;

public interface IAuthService {
    Map<String, String> register(RegisterRequest req);

    LoginResponse login(LoginRequest req);

    LoginResponse refreshToken(TokenRefreshRequest request);

    void forgotPassword(String email);

    void resetPassword(PasswordResetRequest request);

    void changePassword(Long userId, String oldPassword, String newPassword);

    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);

    UserResponse getMe(Long userId);
}
