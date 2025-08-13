package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestUserController;
import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/user")
public class RestUserControllerImpl implements IRestUserController {

    private final IUserService userService;

    public RestUserControllerImpl(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/save")
    @Override
    public ResponseEntity<UserResponse> saveUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(request));
    }
}
