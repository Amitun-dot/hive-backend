package com.hive.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hive.exception.UnauthorizedException;

@Component
public class SecurityUtils {

    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public String getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
