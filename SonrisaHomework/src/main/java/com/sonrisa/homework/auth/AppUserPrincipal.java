package com.sonrisa.homework.auth;

import com.sonrisa.homework.modules.user.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

// Custom UserDetails carrying the domain User's id/admin flag alongside the standard
// authentication fields, so controllers can resolve "who is this and do they own this
// resource" without a second database lookup per request.
public class AppUserPrincipal extends org.springframework.security.core.userdetails.User {

    private final UUID id;
    private final boolean admin;

    public AppUserPrincipal(User user) {
        super(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER")));
        this.id = user.getId();
        this.admin = user.isAdmin();
    }

    public UUID getId() {
        return id;
    }

    public boolean isAdmin() {
        return admin;
    }
}
