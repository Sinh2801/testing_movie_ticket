package com.movieticketbooking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
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
                // OTHER API
                .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Độ mạnh mã hóa mặc định là 10 (có thể chỉnh từ 4 đến 31)
        return new BCryptPasswordEncoder();
    }
}
