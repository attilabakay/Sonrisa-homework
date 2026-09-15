package com.sonrisa.homework.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * HTTP Basic auth (mvp final.md §1: "email + password is enough, no SSO/OAuth") — no
 * dedicated login endpoint; the client sends credentials on every request and
 * UserDetailsServiceImpl validates them against the User table. Stateless: no server-side
 * session, matches a REST API + SPA client. Only self-registration and the H2 console (local
 * dev tool) are open; everything else requires a valid, active account.
 *
 * Authorization splits along workflow.md's per-entity (admin-facing) / (user-facing) /
 * (system-only) labels: User, DataSource, DataEntry and NotificationAttempt management is
 * admin-only; Alert and Sender (a user's own alerts/delivery channels) are open to any
 * authenticated account. Order matters below — more specific matchers must come first.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole("ADMIN")
                        // Admin-only visibility into a user's full alert history (mvp final.md §5).
                        .requestMatchers(HttpMethod.GET, "/api/alerts/user/*/all").hasRole("ADMIN")
                        .requestMatchers("/api/data-sources/**").hasRole("ADMIN")
                        .requestMatchers("/api/data-entries/**").hasRole("ADMIN")
                        .requestMatchers("/api/notification-attempts/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Lets the Vite dev server (frontend/) call this API from the browser.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
