package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.util.ResultData;

import java.util.List;

public interface IRestUserController {

    ResultData<UserResponse> saveUser(UserRequest request);

    ResultData<UserResponse> getUserById(Long id);

    ResultData<UserResponse> updateUser(Long id, UserRequest request);

    ResultData<String> deleteUser(Long id);

    ResultData<List<UserResponse>> findAllUser();
}
