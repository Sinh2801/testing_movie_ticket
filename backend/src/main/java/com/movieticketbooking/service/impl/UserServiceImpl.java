package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.user.CreateUserRequest;
import com.movieticketbooking.request.user.UpdateInfoUserRequest;
import com.movieticketbooking.response.UserResponse;
import com.movieticketbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // find user by id
    @Override
    public User findUserById(int id) {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()) throw new ResourceNotFoundException("User not found");
        return user.get();
    }

    // get user by id
    @Override
    public UserResponse getUserById(int id) {
        return toResponse(findUserById(id));
    }

    // add new user
    @Override
    public UserResponse addUser(CreateUserRequest createUserRequest) {
        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        String hashPassword = passwordEncoder.encode(createUserRequest.getPassword());
        user.setPassword(hashPassword);
        user.setEmail(createUserRequest.getEmail());
        user.setPhoneNumber(createUserRequest.getPhoneNumber());
        user.setRole(createUserRequest.getRole());

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    // update user infor
    @Override
    public UserResponse updateUser(UpdateInfoUserRequest updateInfoUserRequest) {
        User user = findUserById(updateInfoUserRequest.getId());

        user.setUsername(updateInfoUserRequest.getUsername());
        user.setEmail(updateInfoUserRequest.getEmail());
        user.setPhoneNumber(updateInfoUserRequest.getPhoneNumber());
        user.setRole(updateInfoUserRequest.getRole());

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    // write controller


    // map to response
    private UserResponse toResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setRole(user.getRole());
        return userResponse;
    }
}
