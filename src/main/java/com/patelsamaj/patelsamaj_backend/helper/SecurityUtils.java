package com.patelsamaj.patelsamaj_backend.helper;

import com.patelsamaj.patelsamaj_backend.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Integer> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return Optional.ofNullable(user.getUserId());
        }
        return Optional.empty();
    }

    public static int getAuthenticatedUserId() {
        return getCurrentUserId().orElse(1);
    }
}
