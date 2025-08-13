package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;

public interface IRestUserController {

    ResponseEntity<UserResponse> saveUser(UserRequest request);
}
