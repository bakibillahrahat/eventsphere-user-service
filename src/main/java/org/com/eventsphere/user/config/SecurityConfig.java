package org.com.eventsphere.user.config;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.exception.UserNotFoundException;
import org.com.eventsphere.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 * This class is the central point for configuring all security aspects of the application.
 */
@Configuration
@EnableWebSecurity // Enables Spring Security's web security support.
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    /**
     * This bean defines how to find a user. Spring Security will use this service
     * to load user details from the database during authentication.
     * We provide a lambda implementation that uses our UserRepository.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found with email: " + username));
    }

    /**
     * Creates a bean for the PasswordEncoder. We use BCrypt, which is the industry standard for hashing passwords.
     * This bean will be available for dependency injection throughout the application.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * This is the main authentication manager bean. Spring Boot uses this to process authentication requests.
     * It's the "chief of security" that delegates the work to the appropriate provider.
     */

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * This bean is the data access object for authentication.
     * It connects the UserDetailsService (for finding the user) and the PasswordEncoder (for checking the password).
     */

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    /**
     * This is the core of our security configuration.
     * It defines a filter chain that tells Spring Security how to handle incoming requests.
     */
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

