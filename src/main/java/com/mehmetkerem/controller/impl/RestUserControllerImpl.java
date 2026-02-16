package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestUserController;
import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.service.IUserService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import jakarta.validation.Valid;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/user")
public class RestUserControllerImpl implements IRestUserController {

    private final IUserService userService;

    public RestUserControllerImpl(IUserService userService) {
        this.userService = userService;
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/save")
    @Override
    public ResultData<UserResponse> saveUser(@Valid @RequestBody UserRequest request) {
        return ResultHelper.success(userService.saveUser(request));
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    @Override
    public ResultData<UserResponse> getUserById(@PathVariable("id") Long id) {
        return ResultHelper.success(userService.getUserResponseById(id));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    @Override
    public ResultData<UserResponse> updateUser(@PathVariable("id") Long id, @RequestBody UserRequest request) {
        return ResultHelper.success(userService.updateUser(id, request));
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @Override
    public ResultData<String> deleteUser(@PathVariable("id") Long id) {
        return ResultHelper.success(userService.deleteUser(id));
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/find-all")
    @Override
    public ResultData<List<UserResponse>> findAllUser() {
        return ResultHelper.success(userService.findAllUsers());
    }
}
