package org.com.eventsphere.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 * This class is the central point for configuring all security aspects of the application.
 */
@Configuration
@EnableWebSecurity // Enables Spring Security's web security support.
public class SecurityConfig {

    /**
     * Creates a bean for the PasswordEncoder. We use BCrypt, which is the industry standard for hashing passwords.
     * This bean will be available for dependency injection throughout the application.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Cross-Site Request Forgery) protection. This is common for stateless REST APIs.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Define authorization rules for HTTP requests.
                .authorizeHttpRequests(auth -> auth
                        // 3. IMPORTANT: Explicitly permit all requests to our public registration and login endpoints.
                        .requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()

                        // 4. NEW RULE: Only users with the 'ADMIN' role can access endpoints under '/api/v1/users/all'.
                        // Spring Security automatically adds the 'ROLE_' prefix, so we just specify 'ADMIN'.
                        .requestMatchers("/api/v1/users/all").hasRole("ADMIN")

                        // 5. For all other requests, the user simply needs to be authenticated (logged in).
                        .anyRequest().authenticated()
                )

                // 6. Configure session management to be STATELESS.
                // Since we will be using JWTs, the server will not hold any session state.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}

