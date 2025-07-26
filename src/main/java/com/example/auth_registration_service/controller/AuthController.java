package com.example.auth_registration_service.controller;

import com.example.auth_registration_service.security.dto.*;
import com.example.auth_registration_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth controller", description = "Авторизация, регистрация.")
@RequiredArgsConstructor
@Controller
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Аутентификация пользователя")
    @PostMapping("/auth")
    public ResponseEntity<TokenPair> auth(@RequestBody AuthRequest request){
        TokenPair login = authService.login(request);
        return ResponseEntity.ok(login);
    }

    @Operation(summary = "Logout пользователя")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Регистрация нового пользователя")
    @Transactional
    @PostMapping("/registration")
    public ResponseEntity<TokenResponse> register(@RequestBody RegistrationRequest request) {
        var tokenResponse = authService.registerUser(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "Получение refresh токена")
    @PostMapping("/refresh")
    public ResponseEntity<TokenPair> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshTokens(request.refreshToken()));
    }

}
