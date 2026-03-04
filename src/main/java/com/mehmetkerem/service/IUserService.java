package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.Role;
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

    /** KVKK: Kullanıcı kendi hesabını deaktive eder (soft-delete). */
    void deactivateAccount(Long userId);

    /** KVKK: Oturumdaki kullanıcının kendi hesabını deaktive eder. */
    void deactivateCurrentAccount();

    /** Admin: Kullanıcıyı banla (login engellensin). */
    void banUser(Long userId);

    /** Admin: Kullanıcı banını kaldır. */
    void unbanUser(Long userId);

    /** Admin: Kullanıcı rolünü değiştir. */
    void updateUserRole(Long userId, Role newRole);
}
