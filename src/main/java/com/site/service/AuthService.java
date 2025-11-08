package com.site.service;

import com.site.dto.auth.LoginRequest;
import com.site.dto.auth.LoginResponse;
import com.site.dto.auth.RegisterRequest;
import com.site.dto.auth.RegisterResponse;
import com.site.model.ExpiredRefreshToken;
import com.site.model.Role;
import com.site.model.User;
import com.site.model.UserRole;
import com.site.repository.ExpiredRefreshTokenRepository;
import com.site.repository.RoleRepository;
import com.site.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExpiredRefreshTokenRepository expiredTokenRepository;

    public AuthService(AuthenticationManager authManager, JwtService jwtService,
                       UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, ExpiredRefreshTokenRepository expiredTokenRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.expiredTokenRepository = expiredTokenRepository;
    }

    @Transactional
    public ResponseEntity<LoginResponse> login(LoginRequest request, HttpServletResponse response) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            List<String> roleNamesStr = user.getUserRoles().stream()
                    .map(role -> role.getRole().getRoleName())
                    .toList();

            String accessToken = jwtService.generateAccessToken(user.getUserId(), roleNamesStr);
            String refreshToken = jwtService.generateRefreshToken(user.getUserId());

            ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(900)
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/api/public/auth/refresh")
                    .maxAge(604800)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity.ok(new LoginResponse("Zalogowano"));

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Nieprawidłowy login lub hasło"));
        }
    }

    @Transactional
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "REFRESH_TOKEN");

        if (refreshToken == null) {
            clearCookies(response);
            return ResponseEntity.status(401).body(new LoginResponse("Brak refresh token"));
        }

        // Sprawdź czy token jest na liście wygasłych/unieważnionych
        if (expiredTokenRepository.existsByRefreshToken(refreshToken)) {
            clearCookies(response);
            return ResponseEntity.status(401).body(new LoginResponse("Token został unieważniony"));
        }

        // Walidacja tokenu
        if (!jwtService.isTokenValid(refreshToken, "refresh")) {
            clearCookies(response);
            return ResponseEntity.status(401).body(new LoginResponse("Nieprawidłowy refresh token"));
        }

        var jws = jwtService.parseToken(refreshToken);
        Long userId = Long.valueOf(jws.getBody().getSubject());
        String createdAtStr = jws.getBody().get("createdAt", String.class);
        Instant createdAt = Instant.parse(createdAtStr);

        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            clearCookies(response);
            return ResponseEntity.status(401).body(new LoginResponse("Użytkownik nie znaleziony"));
        }

        User user = userOpt.get();

        // Sprawdź czy hasło zostało zmienione po wydaniu tokenu
        if (user.getPasswordUpdatedAt() != null && createdAt.isBefore(user.getPasswordUpdatedAt())) {
            // Dodaj stary token do listy wygasłych
            invalidateRefreshToken(refreshToken, user, "password_changed");
            clearCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Token nieważny - hasło zostało zmienione"));
        }

        // Dodaj stary token do listy wygasłych (rotacja tokenów)
        invalidateRefreshToken(refreshToken, user, "rotated");

        List<String> roleNamesStr = user.getUserRoles().stream()
                .map(role -> role.getRole().getRoleName())
                .toList();

        String newAccessToken = jwtService.generateAccessToken(user.getUserId(), roleNamesStr);
        String newRefreshToken = jwtService.generateRefreshToken(user.getUserId());

        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(900)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/public/auth/refresh")
                .maxAge(604800)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(new LoginResponse("Token odświeżony"));
    }

    @Transactional
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "REFRESH_TOKEN");
        System.out.println(refreshToken);

        if (refreshToken != null && jwtService.isTokenValid(refreshToken, "refresh")) {
            var jws = jwtService.parseToken(refreshToken);
            Long userId = Long.valueOf(jws.getBody().getSubject());

            userRepository.findById(userId).ifPresent(user ->
                    invalidateRefreshToken(refreshToken, user, "logout")
            );
        }

        clearCookies(response);
        return ResponseEntity.ok(new LoginResponse("Wylogowano"));
    }

    @Transactional
    public ResponseEntity<RegisterResponse> register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new RegisterResponse("Użytkownik o tym e-mailu już istnieje"));
        }

        Role role = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono roli"));

        String hashed = passwordEncoder.encode(request.password());

        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setPassword(hashed);
        newUser.setCreatedAt(Instant.now());

        UserRole userRole = new UserRole();
        userRole.setUser(newUser);
        userRole.setRole(role);

        newUser.getUserRoles().add(userRole);

        userRepository.save(newUser);

        return ResponseEntity.ok(new RegisterResponse("Zarejestrowano pomyślnie"));
    }

    // ====================================
    // Pomocnicze metody prywatne
    // ====================================

    private void invalidateRefreshToken(String token, User user, String reason) {
        try {
            var jws = jwtService.parseToken(token);
            OffsetDateTime expiredAt = jws.getBody().getExpiration()
                    .toInstant()
                    .atOffset(ZoneOffset.UTC);
            OffsetDateTime issuedAt = jws.getBody().getIssuedAt()
                    .toInstant()
                    .atOffset(ZoneOffset.UTC);

            ExpiredRefreshToken expiredToken = ExpiredRefreshToken.builder()
                    .user(user)
                    .refreshToken(token)
                    .issuedAt(issuedAt)
                    .expiredAt(expiredAt)
                    .invalidatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .reason(reason)
                    .build();

            expiredTokenRepository.save(expiredToken);
        } catch (Exception e) {
            // Log błędu, ale nie przerywaj procesu
            System.err.println("Błąd podczas dodawania tokenu do listy wygasłych: " + e.getMessage());
        }
    }

    private static String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private static void clearCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/public/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}