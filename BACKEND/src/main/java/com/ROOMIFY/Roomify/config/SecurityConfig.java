package com.ROOMIFY.Roomify.config;

import com.ROOMIFY.Roomify.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:8080,http://localhost:8081,http://localhost:8082,https://roomify.co.tz,https://roomify-api3.fly.dev,https://roomifytz-api3.fly.dev,https://roomifytz-api.fly.dev,https://roomify-api.fly.dev}")
    private String allowedOriginsProperty;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ==========================================
                        // PUBLIC ENDPOINTS
                        // ==========================================

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/furniture/**").permitAll()
                        .requestMatchers("/api/shops/**").permitAll()
                        .requestMatchers("/api/properties/**").permitAll()
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers("/api/favorites/**").permitAll()
                        .requestMatchers("/api/notifications/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Anyone can VIEW rooms
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()

                        // ==========================================
                        // PROTECTED ENDPOINTS
                        // ==========================================

                        // Creating/updating/deleting rooms requires login
                        // Anyone authenticated can POST (to be checked in controller if specific roles needed, but usually Owner/Dalali)
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").authenticated()
                        
                        // Restrict approval/rejection/featured/promoted to Admin/SuperAdmin
                        .requestMatchers("/api/rooms/*/approve").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/rooms/*/reject").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/rooms/*/featured").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/rooms/*/promoted").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/rooms/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").authenticated()

                        // Bookings require authentication
                        .requestMatchers("/api/bookings/**").authenticated()

                        // User endpoints require authentication
                        .requestMatchers("/api/users/profile", "/api/users/profile/**", "/api/users/change-password", "/api/users/delete-account").authenticated()
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Analytics available to authenticated Owners, Dalalis, Furniture Sellers, and Admins
                        .requestMatchers(HttpMethod.GET, "/api/analytics/**").permitAll()
                        .requestMatchers("/api/analytics/**").hasAnyRole("OWNER", "DALALI", "FURNITURE_SELLER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/system/audit/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Admin management
                        .requestMatchers("/api/admin-management/admins/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/admin-management/users/*/role").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/admin-management/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        List<String> allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "*"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));

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
}