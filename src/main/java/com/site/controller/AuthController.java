package com.site.controller;

import com.site.dto.auth.LoginRequest;
import com.site.dto.auth.LoginResponse;
import com.site.dto.auth.RegisterRequest;
import com.site.dto.auth.RegisterResponse;
import com.site.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        return authService.register(request, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        return authService.refresh(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        return authService.logout(request, response);
    }
}