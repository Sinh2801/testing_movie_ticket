package com.movieticketbooking.config;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${api.prefix}")
    private String apiPrefix;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                // AUTH
                .requestMatchers(
                        "/api/auth/**"
                ).permitAll()
                // GENRES
                .requestMatchers(HttpMethod.GET,
                        "/api/genres/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/genres/add"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/genres/edit/**"
                ).hasRole("ADMIN")
                // CINEMAS
                .requestMatchers(HttpMethod.GET,
                        "/api/cinemas/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/cinemas/add"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/cinemas/edit/**"
                ).hasRole("ADMIN")
                // MOVIES
                .requestMatchers(HttpMethod.GET,
                        "/api/movies/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/movies/add"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/movies/edit/**"
                ).hasRole("ADMIN")
                // ROOMS
                .requestMatchers(HttpMethod.GET,
                        "/api/rooms/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/rooms/add"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/rooms/edit/**"
                ).hasRole("ADMIN")
                // SHOWTIMES
                .requestMatchers(HttpMethod.GET,
                        "/api/showtimes/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/showtimes/add"
                ).hasRole("ADMIN")
                // PAYMENTS CALLBACK
                .requestMatchers(HttpMethod.POST,
                        "/api/payments/momo/callback"
                ).permitAll()
                // OTHER API
                .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Độ mạnh mã hóa mặc định là 10 (có thể chỉnh từ 4 đến 31)
        return new BCryptPasswordEncoder();
    }
}
