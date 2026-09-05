package com.hive.config;

import com.hive.security.CustomUserDetailsService;
import com.hive.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    // ============================================================
    // PASSWORD ENCODER
    // ============================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ============================================================
    // AUTHENTICATION PROVIDER
    // ============================================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // ============================================================
    // AUTHENTICATION MANAGER
    // ============================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }

    // ============================================================
    // CORS
    // ============================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(frontendUrl)
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // ============================================================
    // AUTHENTICATION ENTRY POINT
    // ============================================================
    //
    // This handles requests where the user is NOT authenticated.
    //
    // Expected:
    //     401 Unauthorized
    //
    // Instead of Spring Security's default:
    //     403 Forbidden
    //
    // ============================================================

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authenticationException) -> {

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");

            response.getWriter().write(
                    """
                    {
                        "status": 401,
                        "message": "Authentication required"
                    }
                    """
            );
        };
    }

    // ============================================================
    // ACCESS DENIED HANDLER
    // ============================================================
    //
    // This is different from authentication failure.
    //
    // 401 = user is NOT authenticated
    //
    // 403 = user IS authenticated but doesn't have permission
    //
    // ============================================================

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");

            response.getWriter().write(
                    """
                    {
                        "status": 403,
                        "message": "Access denied"
                    }
                    """
            );
        };
    }

    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        http

                // ----------------------------------------------------
                // CSRF
                // ----------------------------------------------------

                .csrf(csrf -> csrf.disable())

                // ----------------------------------------------------
                // CORS
                // ----------------------------------------------------

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // ----------------------------------------------------
                // SESSION
                // ----------------------------------------------------

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // ----------------------------------------------------
                // EXCEPTION HANDLING
                // ----------------------------------------------------

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                authenticationEntryPoint()
                        )

                        .accessDeniedHandler(
                                accessDeniedHandler()
                        )
                )

                // ----------------------------------------------------
                // AUTHORIZATION
                // ----------------------------------------------------

                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // WebSocket handshake
                        .requestMatchers(
                                "/ws/**"
                        ).permitAll()

                        // Actuator health endpoint
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()

                        // Everything else requires JWT
                        .anyRequest().authenticated()
                )

                // ----------------------------------------------------
                // AUTHENTICATION PROVIDER
                // ----------------------------------------------------

                .authenticationProvider(
                        authenticationProvider()
                )

                // ----------------------------------------------------
                // JWT FILTER
                // ----------------------------------------------------

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}