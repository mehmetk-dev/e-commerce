package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestUserController;
import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id")String id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserResponseById(id));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id")String id,@RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(id,request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteUser(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.deleteUser(id));
    }

    @GetMapping("/find-all")
    @Override
    public ResponseEntity<List<UserResponse>> findAllUser() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAllUsers());
    }
}
