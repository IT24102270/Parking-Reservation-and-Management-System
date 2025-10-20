package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        boolean enabled = "ACTIVE".equalsIgnoreCase(user.getStatus()); // 👈 check status
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        // Handle role properly - check if it already has ROLE_ prefix
        String role = user.getRole().toUpperCase();
        
        // Debug logging
        System.out.println("=== USER LOADING DEBUG ===");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Raw Role from DB: '" + user.getRole() + "'");
        System.out.println("Processed Role: '" + role + "'");
        System.out.println("Status: " + user.getStatus());
        System.out.println("Enabled: " + enabled);
        System.out.println("========================");
        
        if (role.startsWith("ROLE_")) {
            // Role already has ROLE_ prefix, use authorities() instead of roles()
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities(role)
                    .disabled(!enabled)
                    .accountExpired(!accountNonExpired)
                    .credentialsExpired(!credentialsNonExpired)
                    .accountLocked(!accountNonLocked)
                    .build();
        } else {
            // Role doesn't have ROLE_ prefix, use roles() to add it
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .roles(role)
                    .disabled(!enabled)
                    .accountExpired(!accountNonExpired)
                    .credentialsExpired(!credentialsNonExpired)
                    .accountLocked(!accountNonLocked)
                    .build();
        }
    }
}
