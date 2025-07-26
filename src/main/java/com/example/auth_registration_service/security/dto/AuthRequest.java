package com.example.auth_registration_service.security.dto;

import lombok.Data;

@Data
public class AuthRequest {
    String login;
    String password;
}
