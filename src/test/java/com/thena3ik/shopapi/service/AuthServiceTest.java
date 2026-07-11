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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenEmailNotTaken_createsUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("new@test.com", "password123", "New User");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        AuthResponse result = authService.register(request);

        assertThat(result.token()).isEqualTo("fake-jwt-token");

        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("hashedPassword")
                        && user.getEmail().equals("new@test.com")
                        && user.getRole() == Role.CUSTOMER
        ));
    }

    @Test
    void register_whenEmailAlreadyExists_throwsResourceConflictException() {
        RegisterRequest request = new RegisterRequest("taken@test.com", "password123", "Someone");

        User existingUser = new User();
        existingUser.setEmail("taken@test.com");

        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Email already registered: taken@test.com");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void login_whenCredentialsAreCorrect_returnsToken() {
        LoginRequest request = new LoginRequest("user@test.com", "correctPassword");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("user@test.com");
        existingUser.setPassword("hashedPassword");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(existingUser)).thenReturn("fake-jwt-token");

        AuthResponse result = authService.login(request);

        assertThat(result.token()).isEqualTo("fake-jwt-token");
    }

    @Test
    void login_whenPasswordIsWrong_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("user@test.com", "wrongPassword");

        User existingUser = new User();
        existingUser.setEmail("user@test.com");
        existingUser.setPassword("hashedPassword");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_whenEmailDoesNotExist_throwsResourceNotFoundException() {
        LoginRequest request = new LoginRequest("ghost@test.com", "anyPassword");

        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: ghost@test.com");

        verify(jwtService, never()).generateToken(any(User.class));
    }
}