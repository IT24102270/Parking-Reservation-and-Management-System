package com.sliit.parking_reservation_and_management_system.config;

import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    public CustomAuthenticationFailureHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String email = request.getParameter("username"); // must match login form input name
        String errorMessage = "Invalid username or password";

        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
                request.getSession().setAttribute("error", "Your account has been temporarily deactivated.");
            } else {
                request.getSession().setAttribute("error", errorMessage);
            }
        }, () -> request.getSession().setAttribute("error", errorMessage));

        // 👇 Redirect without "?error" so only our message shows
        response.sendRedirect("/login");
    }

}
