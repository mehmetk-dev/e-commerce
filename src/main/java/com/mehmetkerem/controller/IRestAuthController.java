package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.*;
import com.mehmetkerem.dto.response.LoginResponse;
import com.mehmetkerem.util.ResultData;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface IRestAuthController {
    ResultData<Map<String, String>> register(RegisterRequest req);

    ResultData<LoginResponse> login(LoginRequest req);

    ResultData<LoginResponse> refreshToken(TokenRefreshRequest request);

    ResultData<String> forgotPassword(String email);

    ResultData<String> resetPassword(PasswordResetRequest request);

    ResultData<String> changePassword(ChangePasswordRequest request, Authentication authentication);
}
