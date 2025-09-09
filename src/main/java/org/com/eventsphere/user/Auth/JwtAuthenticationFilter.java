package org.com.eventsphere.user.Auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter
 * This filter intercepts every incoming HTTP request to check for a valid JWT in the 'Authorization' header.
 * If a valid token is found, it authenticates the user for the current request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            @NonNull  HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        // 1. Check if the request has an Authorization header and if it starts with "Bearer ".
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // 2. Extract the JWT from the header (it's the part after "Bearer ").
            jwt = authorizationHeader.substring(7);

            // 3. Extract the username (email) from the JWT using the JwtService.
            userEmail = jwtService.extractUsername(jwt);
            // 4. Check if the user is not already authenticated for this request.
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. Load the user details from the database or another source.
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // 6. Validate the JWT against the user details.
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 7. If the token is valid, create an authentication token and set it in the security context.
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        // 8. Pass the request along the filter chain.
        filterChain.doFilter(request, response);
    }
}
