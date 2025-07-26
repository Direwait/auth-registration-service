package com.example.auth_registration_service.security.dto;

import lombok.Data;


public record TokenPair(String accessToken, String refreshToken) {}
