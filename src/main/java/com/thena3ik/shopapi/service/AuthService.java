package com.thena3ik.shopapi.service;

import com.thena3ik.shopapi.dto.auth.AuthResponse;
import com.thena3ik.shopapi.dto.auth.LoginRequest;
import com.thena3ik.shopapi.dto.auth.RegisterRequest;
import com.thena3ik.shopapi.entity.Role;
import com.thena3ik.shopapi.entity.User;
import com.thena3ik.shopapi.exception.InvalidCredentialsException;
import com.thena3ik.shopapi.exception.ResourceConflictException;
import com.thena3ik.shopapi.exception.ResourceNotFoundException;
import com.thena3ik.shopapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register (RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResourceConflictException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        return new AuthResponse(token);
    }

    public AuthResponse login (LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Password is incorrect");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

}
