package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestAuthController;
import com.mehmetkerem.dto.request.*;
import com.mehmetkerem.dto.response.LoginResponse;
import com.mehmetkerem.model.User;
import com.mehmetkerem.service.impl.AuthService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class RestAuthControllerImpl implements IRestAuthController {

    private final AuthService authService;

    @Override
    @PostMapping("/register")
    public ResultData<Map<String, String>> register(@RequestBody RegisterRequest req) {
        return ResultHelper.success(authService.register(req));
    }

    @Override
    @PostMapping("/login")
    public ResultData<LoginResponse> login(@RequestBody LoginRequest req) {
        return ResultHelper.success(authService.login(req));
    }

    @Override
    @PostMapping("/refresh-token")
    public ResultData<LoginResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResultHelper.success(authService.refreshToken(request));
    }

    @Override
    @PostMapping("/forgot-password")
    public ResultData<String> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResultHelper.success("Şifre sıfırlama linki e-postanıza gönderildi.");
    }

    @Override
    @PostMapping("/reset-password")
    public ResultData<String> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResultHelper.success("Şifreniz başarıyla değiştirildi.");
    }

    @Override
    @PostMapping("/change-password")
    public ResultData<String> changePassword(@RequestBody @Valid ChangePasswordRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        return ResultHelper.success("Şifreniz güncellendi.");
    }
}
