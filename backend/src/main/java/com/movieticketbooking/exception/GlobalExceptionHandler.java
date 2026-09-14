package com.movieticketbooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.movieticketbooking.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi Validate DTO (các annotation @NotBlank, @Min, @Pattern, v.v...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("Dữ liệu đầu vào không hợp lệ", errors));
    }

    // 2. Xử lý khi sai thông tin Đăng nhập (Email/Mật khẩu sai)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("Email hoặc mật khẩu không chính xác!", null));
    }

    // 3. Xử lý khi không đủ quyền hạn (Ví dụ: User thường cố truy cập API Admin)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse("Bạn không có quyền truy cập tài nguyên này!", null));
    }

    // 4. Xử lý khi truyền sai kiểu dữ liệu trên URL (Ví dụ: id là chuỗi chữ thay vì số)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format("Tham số '%s' có giá trị '%s' không đúng định dạng", e.getName(), e.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(message, null));
    }

    // 5. Xử lý không tìm thấy tài nguyên (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(e.getMessage(), null));
    }
    
    // 6. Xử lý tài nguyên đã tồn tại (409 Conflict)
    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ApiResponse> handleAlreadyExist(ResourceAlreadyExistException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(e.getMessage(), null));
    }

    // 7. Xử lý các Runtime Exception chung
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(e.getMessage(), null));
    }

    // 8. Xử lý các lỗi hệ thống không lường trước được (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Lỗi hệ thống: " + e.getMessage(), null));
    }
}

