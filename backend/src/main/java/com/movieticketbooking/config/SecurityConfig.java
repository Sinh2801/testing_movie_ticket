package com.movieticketbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Độ mạnh mã hóa mặc định là 10 (có thể chỉnh từ 4 đến 31)
        return new BCryptPasswordEncoder();
    }
}
