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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        @Value("${api.prefix}")
        private String apiPrefix;

        @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173}")
        private String allowedOrigins;

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final UserDetailsService userDetailsService;
        private final RateLimitingFilter rateLimitingFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(
                                                sessionManagement -> sessionManagement
                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                // SWAGGER & API DOCS
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                // AUTH
                                                .requestMatchers(
                                                                apiPrefix + "/auth/**")
                                                .permitAll()
                                                // GENRES
                                                .requestMatchers(HttpMethod.GET,
                                                                apiPrefix + "/genres/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST,
                                                                apiPrefix + "/genres/add")
                                                .hasAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.PUT,
                                                                apiPrefix + "/genres/edit", apiPrefix + "/genres/edit/**")
                                                .hasAuthority("ADMIN")
                                                // CINEMAS
                                                .requestMatchers(HttpMethod.GET,
                                                                apiPrefix + "/cinemas/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST,
                                                                apiPrefix + "/cinemas/add")
                                                .hasAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.PUT,
                                                                apiPrefix + "/cinemas/edit", apiPrefix + "/cinemas/edit/**")
                                                .hasAuthority("ADMIN")
                                                // MOVIES
                                                .requestMatchers(HttpMethod.GET,
                                                                apiPrefix + "/movies/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST,
                                                                apiPrefix + "/movies/add")
                                                .hasAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.PUT,
                                                                apiPrefix + "/movies/edit", apiPrefix + "/movies/edit/**")
                                                .hasAuthority("ADMIN")
                                                // ROOMS
                                                .requestMatchers(HttpMethod.GET,
                                                                apiPrefix + "/rooms/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST,
                                                                apiPrefix + "/rooms/add")
                                                .hasAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.PUT,
                                                                apiPrefix + "/rooms/edit", apiPrefix + "/rooms/edit/**")
                                                .hasAuthority("ADMIN")
                                                // SHOWTIMES
                                                .requestMatchers(HttpMethod.GET,
                                                                apiPrefix + "/showtimes/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                apiPrefix + "/showtimes/filter")
                                                .hasAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.POST,
                                                                apiPrefix + "/showtimes/add")
                                                .hasAuthority("ADMIN")
                                                // PAYMENTS
                                                .requestMatchers(HttpMethod.GET,
                                                                apiPrefix + "/payments/filter")
                                                .hasAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.POST,
                                                                apiPrefix + "/payments/local/create")
                                                .hasAnyAuthority("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST,
                                                                apiPrefix + "/payments/local/confirm")
                                                .hasAuthority("ADMIN")
                                                // TICKETS
                                                .requestMatchers(HttpMethod.GET,
                                                                apiPrefix + "/tickets/filter")
                                                .hasAuthority("ADMIN")
                                                // OTHER API
                                                .anyRequest().authenticated())
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
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
