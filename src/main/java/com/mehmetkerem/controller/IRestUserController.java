package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRestUserController {

    ResponseEntity<UserResponse> saveUser(UserRequest request);

    ResponseEntity<UserResponse> getUserById(String id);

    ResponseEntity<UserResponse> updateUser(String id, UserRequest request);

    ResponseEntity<String> deleteUser(String id);

    ResponseEntity<List<UserResponse>> findAllUser();
}
