package com.sliit.parking_reservation_and_management_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
    private final CustomSuccessHandler customSuccessHandler;

    public SecurityConfig(CustomAuthenticationFailureHandler customAuthenticationFailureHandler, 
                         CustomSuccessHandler customSuccessHandler) {
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
        this.customSuccessHandler = customSuccessHandler;
    }

    // Main security filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public pages and static resources
                        .requestMatchers("/", "/index", "/login", "/register", "/css/**", "/js/**", "/static/**", "/images/**", "/fonts/**", "/security/test-report", "/security/test-template", "/security/report/new", "/security/report/create", "/debug/**", "/public/**").permitAll()

                        // Protected dashboards
                        // ✅ Admin has access to everything
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/logs/**").hasRole("ADMIN")
                        .requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/security/**").hasAnyRole("SECURITY_OFFICER", "ADMIN")
                        .requestMatchers("/finance/**").hasAnyRole("FINANCE_EXECUTIVE", "ADMIN")
                        .requestMatchers("/support/**").hasAnyRole("CUSTOMER_SUPPORT_OFFICER", "ADMIN")
                        .requestMatchers("/slotmanager/**").hasAnyRole("PARKING_SLOT_MANAGER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(customSuccessHandler)
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
