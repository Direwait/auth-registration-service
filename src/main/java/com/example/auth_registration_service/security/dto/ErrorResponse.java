package com.example.auth_registration_service.security.dto;

public record ErrorResponse(
        int status,
        String code,
        String message,
        String path
) {}
