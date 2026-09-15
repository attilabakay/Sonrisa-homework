package com.sonrisa.homework.auth;

import org.springframework.security.core.Authentication;

import java.util.UUID;

// Who's actually calling the endpoint, resolved once per request from the HTTP Basic
// principal — lets Alert/Sender services enforce "your own resources, unless you're admin"
// (workflow.md documents Alert reads as "scoped to the logged-in user", which nothing was
// actually enforcing server-side before this).
public record RequestingUser(UUID id, boolean admin) {

    public boolean canAccess(UUID ownerId) {
        return admin || id.equals(ownerId);
    }

    public static RequestingUser from(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return new RequestingUser(principal.getId(), principal.isAdmin());
    }
}
