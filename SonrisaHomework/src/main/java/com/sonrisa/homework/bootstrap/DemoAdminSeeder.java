package com.sonrisa.homework.bootstrap;

import com.sonrisa.homework.modules.user.model.User;
import com.sonrisa.homework.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Dev/demo convenience only: there's no endpoint to promote a user to admin (mvp final.md
// doesn't define one — admin accounts are assumed to be provisioned out of band), so without
// this the admin-only views (SecurityConfig) would be unreachable on a fresh database.
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoAdminSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "admin12345";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByAdminTrue()) {
            return;
        }
        userRepository.save(User.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .admin(true)
                .active(true)
                .build());
        log.info("Seeded a default admin account for local/dev use: {} / {}", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
