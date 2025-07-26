package com.example.auth_registration_service.aspect;

import com.example.auth_registration_service.security.dto.AuthRequest;
import com.example.auth_registration_service.security.dto.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
@Aspect
public class AspectAuthService {

        @Around("execution(* com.example.auth_registration_service.service.AuthService.login(..))")
        public Object logLoginExecution(ProceedingJoinPoint joinPoint) throws Throwable {
            AuthRequest request = (AuthRequest) joinPoint.getArgs()[0];
            log.info("Attempting login for user: {}", request.getLogin());

            try {
                Object result = joinPoint.proceed();
                log.info("Login successful for user: {}", request.getLogin());
                return result;
            } catch (Exception e) {
                log.error("Login failed for user: {}. Reason: {}", request.getLogin(), e.getMessage());
                throw e;
            }
        }

        @Around("execution(* com.example.auth_registration_service.service.AuthService.registerUser(..))")
        public Object logRegisterExecution(ProceedingJoinPoint joinPoint) throws Throwable {
            RegistrationRequest request = (RegistrationRequest) joinPoint.getArgs()[0];
            log.info("Attempting registration for user: {}", request.getLogin());

            try {
                Object result = joinPoint.proceed();
                log.info("Registration successful for user: {}", request.getLogin());
                return result;
            } catch (Exception e) {
                log.error("Registration failed for user: {}. Reason: {}", request.getLogin(), e.getMessage());
                throw e;
            }
        }

}

