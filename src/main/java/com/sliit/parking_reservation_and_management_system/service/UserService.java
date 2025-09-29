package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    /**
     * Centralized save:
     * - Hash password if not already BCrypt
     * - Uppercase role
     * - Default status = ACTIVE when missing
     */
    public User saveUser(User user) {
        // Hash only if not already BCrypt
        if (user.getPasswordHash() != null && !isBcrypt(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        // Normalize role to uppercase (e.g., admin -> ADMIN)
        if (user.getRole() != null) {
            user.setRole(user.getRole().toUpperCase());
        }

        // Default status
        if (user.getStatus() == null || user.getStatus().isBlank()) {
            user.setStatus("ACTIVE");
        }

        return userRepository.save(user);
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    public void deactivateUser(int id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus("INACTIVE");
            userRepository.save(user);
        });
    }

    public void activateUser(int id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus("ACTIVE");
            userRepository.save(user);
        });
    }

    private boolean isBcrypt(String value) {
        // Typical BCrypt hashes start with $2a$, $2b$, or $2y$
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    public Page<User> getPaginatedUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }
}
