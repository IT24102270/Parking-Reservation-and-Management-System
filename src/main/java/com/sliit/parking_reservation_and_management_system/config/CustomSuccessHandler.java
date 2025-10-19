package com.sliit.parking_reservation_and_management_system.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String redirectUrl = "/"; // Default redirect URL

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            String authorityName = grantedAuthority.getAuthority();

            if (authorityName.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (authorityName.equals("ROLE_CUSTOMER")) {
                redirectUrl = "/customer/dashboard";
                break;
            } else if (authorityName.equals("ROLE_FINANCE_EXECUTIVE")) {
                redirectUrl = "/finance/dashboard";
                break;
            } else if (authorityName.equals("ROLE_SECURITY_OFFICER")) {
                redirectUrl = "/security/dashboard";
                break;
            } else if (authorityName.equals("ROLE_PARKING_SLOT_MANAGER")) {
                redirectUrl = "/slotmanager/dashboard";
                break;
            } else if (authorityName.equals("ROLE_CUSTOMER_SUPPORT_OFFICER")) {
                redirectUrl = "/support/dashboard";
                break;
            }
        }
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}

