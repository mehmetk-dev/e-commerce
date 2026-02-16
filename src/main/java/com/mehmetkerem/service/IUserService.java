package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.model.User;

import java.util.List;

public interface IUserService {

    UserResponse saveUser(UserRequest request);

    String deleteUser(Long id);

    UserResponse updateUser(Long id, UserRequest request);

    User getUserById(Long id);

    UserResponse getUserResponseById(Long id);

    List<UserResponse> findAllUsers();

    UserResponse getUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordResetToken(String token);

    void changeUserPassword(User user, String password);
}
