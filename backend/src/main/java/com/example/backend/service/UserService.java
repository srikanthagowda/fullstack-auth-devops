package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Signup
    public User signup(String name, String email, String password) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                "Password must contain at least 8 characters"
            );
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                "Email is already registered"
            );
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    // Login
    public User login(String email, String password) {

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                "Email and password are required"
            );
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException(
                "Invalid email or password"
            ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException(
                "Invalid email or password"
            );
        }

        return user;
    }
}
