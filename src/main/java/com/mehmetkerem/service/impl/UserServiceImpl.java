package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.mapper.UserMapper;
import com.mehmetkerem.model.Address;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.UserRepository;
import com.mehmetkerem.service.IUserService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final AddressServiceImpl addressService;
    private final AddressMapper addressMapper;

    public UserServiceImpl(UserMapper userMapper,
                           UserRepository userRepository,
                           AddressServiceImpl addressService,
                           AddressMapper addressMapper) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.addressMapper = addressMapper;
    }

    private User createUser(UserRequest request) {
        User user = new User();
        return userMapper.toEntity(request);
    }

    @Override
    public UserResponse saveUser(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(String.format(ExceptionMessages.EMAIL_ALL_READY_EXISTS, request.getEmail()));
        }

        List<AddressResponse> addresses = request.getAddressIds().stream()
                .map(addressService::getAddressById)
                .map(addressMapper::toResponse)
                .toList();

        User savedUser = userRepository.save(createUser(request));
        savedUser.setCreatedAt(LocalDateTime.now());

        return userMapper.toResponseWithAddresses(savedUser, addresses);
    }

    @Override
    public String deleteUser(String id) {
        userRepository.delete(getUserById(id));
        return String.format(Messages.DELETE_VALUE, id, "kullanıcı");
    }

    @Override
    public UserResponse updateUser(String id, UserRequest request) {
        User currentUser = getUserById(id);
        if (userRepository.existsByEmail(request.getEmail()) && !request.getEmail().equalsIgnoreCase(currentUser.getEmail())){
            throw new BadRequestException(String.format(ExceptionMessages.EMAIL_ALL_READY_EXISTS,request.getEmail()));
        }
        userMapper.update(currentUser,request);

        return userMapper.toResponseWithAddresses(userRepository.save(currentUser),addressService.getAddressesByUser(currentUser));
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, id, "kullanıcı")));
    }

    @Override
    public UserResponse getUserResponseById(String id) {
        User user = getUserById(id);
        return userMapper.toResponseWithAddresses(user,addressService.getAddressesByUser(user));
    }

    @Override
    public List<UserResponse> findAllUsers() {
        List<User> allUsers = userRepository.findAll();

        List<UserResponse> userResponses = new ArrayList<>();
        return allUsers.stream()
                .map(user -> userMapper.toResponseWithAddresses(
                        user,
                        addressService.getAddressesByUser(user)
                ))
                .toList();

    }
}
