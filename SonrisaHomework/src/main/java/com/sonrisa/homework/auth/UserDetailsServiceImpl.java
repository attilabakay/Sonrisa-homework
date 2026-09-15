package com.sonrisa.homework.auth;

import com.sonrisa.homework.modules.user.model.User;
import com.sonrisa.homework.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Backs HTTP Basic auth (SecurityConfig) directly off the User table — email as username,
// the already-BCrypt-hashed password, isActive gating login (a disabled user can't
// authenticate at all, reinforcing the matching-time check in AlertRepository).
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isActive())
                .roles(user.isAdmin() ? "ADMIN" : "USER")
                .build();
    }
}
