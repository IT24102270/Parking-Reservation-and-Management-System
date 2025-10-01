package com.sliit.parking_reservation_and_management_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    // Password encoder bean (BCrypt for secure hashing)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication manager bean (Spring Security uses this internally)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public SecurityConfig(CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    // Success handler: redirects users based on their role
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String redirectUrl = "/";

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                redirectUrl = "/admin/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                redirectUrl = "/customer/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARKING_SLOT_MANAGER"))) {
                redirectUrl = "/slotmanager/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_FINANCE_EXECUTIVE"))) {
                redirectUrl = "/finance/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SECURITY_OFFICER"))) {
                redirectUrl = "/security/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER_SUPPORT_OFFICER"))) {
                redirectUrl = "/support/dashboard";
            }

            response.sendRedirect(redirectUrl);
        };
    }

    // Main security filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public pages
                        .requestMatchers("/", "/index", "/login", "/register", "/css/**", "/js/**").permitAll()

                        // Protected dashboards
                        // ✅ Admin has access to everything
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/security/**").hasAnyRole("SECURITY_OFFICER", "ADMIN")
                        .requestMatchers("/finance/**").hasAnyRole("FINANCE_EXECUTIVE", "ADMIN")
                        .requestMatchers("/support/**").hasAnyRole("CUSTOMER_SUPPORT_OFFICER", "ADMIN")
                        .requestMatchers("/slotmanager/**").hasAnyRole("PARKING_SLOT_MANAGER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(customSuccessHandler())
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // back to index
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/")) // 👈 key line
                );

        return http.build();
    }



}
