package com.example.auth_registration_service.service;

import com.example.auth_registration_service.database.enums.Roles;
import com.example.auth_registration_service.database.model.User;
import com.example.auth_registration_service.security.CustomUserDetails;
import com.example.auth_registration_service.security.JwtService;
import com.example.auth_registration_service.security.dto.AuthRequest;
import com.example.auth_registration_service.security.dto.RegistrationRequest;
import com.example.auth_registration_service.security.dto.TokenPair;
import com.example.auth_registration_service.security.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, String> validRefreshTokens = new ConcurrentHashMap<>();


    @Transactional
    public TokenPair login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken((CustomUserDetails) userDetails);

        String refreshToken = jwtService.generateRefreshToken(userDetails);
        validRefreshTokens.put(userDetails.getUsername(), refreshToken);

        return new TokenPair(token, refreshToken);
    }

    public TokenResponse registerUser(RegistrationRequest request) {
        var user = User.builder()
                .userLogin(request.getLogin())
                .userPassword(request.getPassword())
                .role(Roles.valueOf(request.getRole()))
                .userMail(request.getMail())
                .build();
        userService.addUser(user);

        CustomUserDetails userDetails = (CustomUserDetails) userService.loadUserByUsername(user.getUserLogin());
        String token = jwtService.generateToken(userDetails);

        return new TokenResponse(token);
    }

    public TokenPair refreshTokens(String refreshToken) {
        String username = jwtService.extractLoginFromRefreshToken(refreshToken);
        UserDetails userDetails = userService.loadUserByUsername(username);

        if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        return jwtService.generateTokenPair(userDetails);
    }

    @Transactional
    public void logout(String authHeader) {
        try {
            // 1. Извлекаем токен из заголовка
            String token = jwtService.extractTokenFromHeader(authHeader);
            if (token == null) {
                throw new BadCredentialsException("Invalid authorization header");
            }

            // 2. Получаем имя пользователя из токена
            String username = jwtService.extractLogin(token);
            if (username == null) {
                throw new BadCredentialsException("Invalid token");
            }
            blacklistedTokens.add(token);
            validRefreshTokens.remove(username);
            SecurityContextHolder.clearContext();

        } catch (Exception e) {
            throw new BadCredentialsException("Logout failed: " + e.getMessage());
        }
    }
}

