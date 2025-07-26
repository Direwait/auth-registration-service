package com.example.auth_registration_service.security;

import com.example.auth_registration_service.security.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        logAuthError(exception, request);

        ErrorResponse error = buildErrorResponse(exception, request);
        objectMapper.writeValue(response.getWriter(), error);
    }

    private void logAuthError(AuthenticationException exception, HttpServletRequest request) {
        log.error("Authentication failed for {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.toString(),
                exception);
    }

    private ErrorResponse buildErrorResponse(AuthenticationException exception,
                                             HttpServletRequest request) {
        String errorType = exception.getClass().getSimpleName();
        String message = getErrorMessage(exception);

        return new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                errorType,
                message,
                sanitizePath(request.getRequestURI())
        );
    }

    private String getErrorMessage(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return "Invalid username or password";
        }
        return "Authentication required";
    }

    private String sanitizePath(String path) {
        int queryIndex = path.indexOf('?');
        return queryIndex > 0 ? path.substring(0, queryIndex) : path;
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorResponse error) throws IOException {
        try {
            objectMapper.writeValue(response.getWriter(), error);
        } catch (IOException e) {
            log.error("Failed to write error response", e);
            throw e;
        }
    }
}