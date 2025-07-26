package com.example.auth_registration_service.database.enums;


import org.springframework.security.core.GrantedAuthority;

public enum Roles implements GrantedAuthority {
    GUEST,
    PREMIUM_USER,
    ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
