package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.model.User;

import java.util.List;

public interface IUserService {

    UserResponse saveUser(UserRequest request);
    String deleteUser(String id);
    UserResponse updateUser(String id,UserRequest request);
    User getUserById(String id);
    UserResponse getUserResponseById(String id);
    List<UserResponse> findAllUsers();
}
