package com.patelsamaj.patelsamaj_backend.helper;

import com.patelsamaj.patelsamaj_backend.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static int getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User loggedInUser = (User) authentication.getPrincipal();
            return loggedInUser.getUserId() != null ? loggedInUser.getUserId() : 1;
        }

        return 1;
    }
}
