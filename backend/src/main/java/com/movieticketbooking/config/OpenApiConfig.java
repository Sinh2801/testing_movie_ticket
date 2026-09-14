package com.movieticketbooking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Khai báo Bean OpenAPI để SpringDoc tự động quét và tạo tài liệu Swagger UI.
     * 
     * (bắt buộc có) @Bean public OpenAPI customOpenAPI() -> Để Spring Boot hiểu đây là cấu hình OpenAPI chuẩn.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // Tên định danh cho cơ chế xác thực JWT trong tài liệu Swagger
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // 1. .info() (Tùy chọn - Nên có): Khai báo các thông tin mô tả chung về API
                .info(new Info()
                        .title("Movie Ticket Booking API")
                        .description("Tài liệu API Hệ thống Đặt vé Xem phim Trực tuyến")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Backend Developer")
                                .email("backend@example.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))

                // 2. .addSecurityItem() (Bắt buộc có nếu dự án dùng Spring Security JWT):
                // Áp dụng cơ chế xác thực "bearerAuth" vào tất cả các API hiển thị trên trang Swagger UI.
                // Nhờ dòng này, các API bảo mật sẽ hiển thị biểu tượng "Ổ khóa" để người dùng nhập Token.
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // 3. .components() & .addSecuritySchemes() (Bắt buộc có nếu dự án dùng Spring Security JWT):
                // Định nghĩa cấu hình chi tiết cho kiểu xác thực: Kiểu HTTP, Scheme là Bearer, Định dạng JWT.
                // Giúp Swagger UI tạo ra ô nhập JWT Token (Header: Authorization: Bearer <token>).
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

