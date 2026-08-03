package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceAlreadyExistException;
import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.auth.LoginRequest;
import com.movieticketbooking.request.auth.RegisterRequest;
import com.movieticketbooking.response.AuthResponse;
import com.movieticketbooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    // Register account
    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail();
        if(userRepository.findByEmail(email).isPresent()){
            throw new ResourceAlreadyExistException("Email already exist");
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        if(!password.equals(confirmPassword)){
            throw new RuntimeException("Passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        User saved = userRepository.save(user);

        AuthResponse authResponse = new AuthResponse();
        String jwtToken = jwtService.generateToken(user);
        authResponse.setToken(jwtToken);
        authResponse.setUsername(saved.getUsername());
        authResponse.setEmail(saved.getEmail());
        return authResponse;
    }

    // Login account
    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(jwtToken);
        authResponse.setUsername(user.getUsername());
        authResponse.setEmail(user.getEmail());
        return authResponse;
    }

}
