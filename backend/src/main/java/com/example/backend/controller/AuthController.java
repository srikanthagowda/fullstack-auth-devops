package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Signup API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            User user = userService.signup(
                request.name(),
                request.email(),
                request.password()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                    "message", "Signup successful",
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail()
                )
            );

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                Map.of("message", ex.getMessage())
            );
        }
    }

    // Login API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(
                request.email(),
                request.password()
            );

            return ResponseEntity.ok(
                Map.of(
                    "message", "Login successful",
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail()
                )
            );

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", "Invalid email or password")
            );
        }
    }

    public record SignupRequest(
        String name,
        String email,
        String password
    ) {}

    public record LoginRequest(
        String email,
        String password
    ) {}
}
