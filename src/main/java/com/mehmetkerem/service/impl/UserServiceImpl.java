package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.UserMapper;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.UserRepository;
import com.mehmetkerem.service.IUserService;
import com.mehmetkerem.service.IAddressService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.mehmetkerem.model.PasswordResetToken;
import com.mehmetkerem.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@SuppressWarnings("null")
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final IAddressService addressService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,
            UserRepository userRepository,
            IAddressService addressService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User createUser(UserRequest request) {
        return userMapper.toEntity(request);
    }

    @Override
    public UserResponse saveUser(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(String.format(ExceptionMessages.EMAIL_ALL_READY_EXISTS, request.getEmail()));
        }

        // Adresler kullanıcı oluşturulduktan sonra eklenir
        User savedUser = userRepository.save(createUser(request));
        savedUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(savedUser);

        return userMapper.toResponseWithAddresses(savedUser, Collections.emptyList());
    }

    @Override
    public String deleteUser(Long id) {
        userRepository.delete(getUserById(id));
        return String.format(Messages.DELETE_VALUE, id, "kullanıcı");
    }

    @Transactional
    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User currentUser = getUserById(id);
        if (userRepository.existsByEmail(request.getEmail())
                && !request.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new BadRequestException(String.format(ExceptionMessages.EMAIL_ALL_READY_EXISTS, request.getEmail()));
        }
        userMapper.update(currentUser, request);

        return userMapper.toResponseWithAddresses(userRepository.save(currentUser),
                addressService.getAddressesByUserId(id));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, id, "kullanıcı")));
    }

    @Override
    public UserResponse getUserResponseById(Long id) {
        User user = getUserById(id);
        return userMapper.toResponseWithAddresses(user, addressService.getAddressesByUserId(id));
    }

    @Override
    public List<UserResponse> findAllUsers() {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .map(user -> userMapper.toResponseWithAddresses(
                        user,
                        addressService.getAddressesByUserId(user.getId())))
                .toList();
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(String.format("User with email %s not found", email)));
        return userMapper.toResponseWithAddresses(user, addressService.getAddressesByUserId(user.getId()));
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        // Eski token varsa temizle (Opsiyonel, iş kuralına bağlı)
        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

        PasswordResetToken myToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        passwordResetTokenRepository.save(myToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token).orElse(null);

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                        : null;
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        return passToken.getExpiryDate().isBefore(LocalDateTime.now());
    }

    @Override
    public void changeUserPassword(User user, String password) {
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
