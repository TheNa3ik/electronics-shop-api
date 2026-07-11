package com.thena3ik.shopapi.service;

import com.thena3ik.shopapi.entity.Role;
import com.thena3ik.shopapi.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey",
                "this-is-a-test-secret-key-that-is-long-enough-for-hs256");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void generateToken_thenExtractEmail_returnsOriginalEmail() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setRole(Role.CUSTOMER);

        String token = jwtService.generateToken(user);
        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo("test@test.com");
    }

    @Test
    void generateToken_thenExtractRole_returnsOriginalRole() {
        User user = new User();
        user.setEmail("admin@test.com");
        user.setRole(Role.ADMIN);

        String token = jwtService.generateToken(user);
        String extractedRole = jwtService.extractRole(token);

        assertThat(extractedRole).isEqualTo("ADMIN");
    }

    @Test
    void isTokenValid_whenTokenIsWellFormed_returnsTrue() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setRole(Role.CUSTOMER);
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_whenTokenIsGarbage_returnsFalse() {
        boolean isValid = jwtService.isTokenValid("this.is.not.a.real.token");

        assertThat(isValid).isFalse();
    }
}