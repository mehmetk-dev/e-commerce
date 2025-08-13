package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;

public interface IUserService {

    UserResponse saveUser(UserRequest request);
}
