package com.example.auth_registration_service.security.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegistrationRequest {
    @NotEmpty(message = "login can`t be empty")
    private String login;
    @NotEmpty(message = "login can`t be empty")
    private String password;
    @NotEmpty(message = "login can`t be empty")
    private String mail;

    private String Role;
}
