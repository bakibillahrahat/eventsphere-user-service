package org.com.eventsphere.user.config;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.Auth.JwtAuthenticationFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 * This class is the central point for configuring all security aspects of the application.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Dependencies injected by Spring
    private final UserRepository userRepository;
    // REMOVED: The filter is no longer injected in the constructor to break the circular dependency.
    // private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * The Detective: Tells Spring Security how to find a user in the database.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + username));
    }

    /**
     * The Digital Locker: Provides the tool for securely hashing and checking passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * The Identity Verifier: Connects the "Detective" and the "Locker".
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * The Chief of Security: The main manager that orchestrates the authentication process.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * The Rulebook: Defines all the access rules for our API endpoints.
     * CORRECTED: JwtAuthenticationFilter is now passed as a method parameter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Rule: No CSRF protection needed for our stateless API
                .authorizeHttpRequests(auth -> auth
                        // Rule: The main entrance (/register, /login, etc.) is open to the public.
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Rule: Only people with an 'ADMIN' ID card can enter the server room (/users/all).
                        .requestMatchers("/api/v1/users/all").hasRole("ADMIN")
                        // Rule: To access any other endpoint, you must have a valid ID card (be logged in).
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Rule: No overnight stays (stateless).
                .authenticationProvider(authenticationProvider())
                // Action: Post our custom Security Guard (JwtAuthenticationFilter) at the main gate.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

