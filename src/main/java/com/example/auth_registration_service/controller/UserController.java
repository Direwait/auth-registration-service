package com.example.auth_registration_service.controller;

import com.example.auth_registration_service.database.model.User;
import com.example.auth_registration_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User controller", description = "Все, что связано с пользователями")
@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @Operation(summary = "Получить список всех пользователей")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        var allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

}
