package com.movieticketbooking.exception;

import com.movieticketbooking.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("EXC-01 - Validation một field")
    void EXC_01_handlesOneValidationField() {
        MethodArgumentNotValidException exception =
                validationException(new FieldError("request", "email", "Email không hợp lệ"));

        ResponseEntity<ApiResponse> response = handler.handleValidationExceptions(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Dữ liệu đầu vào không hợp lệ", response.getBody().getMessage());
        assertEquals(Map.of("email", "Email không hợp lệ"), response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-02(a) - Validation nhiều field")
    void EXC_02_a_multipleFields() {
        MethodArgumentNotValidException exception = validationException(
                new FieldError("request", "email", "Sai định dạng"),
                new FieldError("request", "password", "Quá ngắn"));

        ResponseEntity<ApiResponse> response = handler.handleValidationExceptions(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Dữ liệu đầu vào không hợp lệ", response.getBody().getMessage());
        assertEquals(
                Map.of("email", "Sai định dạng", "password", "Quá ngắn"),
                response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-02(b) - Validation trùng field ghi đè")
    void EXC_02_b_duplicateField() {
        MethodArgumentNotValidException exception = validationException(
                new FieldError("request", "email", "Lỗi A"),
                new FieldError("request", "email", "Lỗi B"));

        ResponseEntity<ApiResponse> response = handler.handleValidationExceptions(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Dữ liệu đầu vào không hợp lệ", response.getBody().getMessage());
        assertEquals(Map.of("email", "Lỗi B"), response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-03 - Sai thông tin đăng nhập")
    void EXC_03_handlesBadCredentials() {
        ResponseEntity<ApiResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("bad"));

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Email hoặc mật khẩu không chính xác!", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-04 - Không đủ quyền")
    void EXC_04_handlesAccessDenied() {
        ResponseEntity<ApiResponse> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Bạn không có quyền truy cập tài nguyên này!", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-05 - Sai kiểu tham số")
    void EXC_05_handlesTypeMismatch() {
        MethodArgumentTypeMismatchException exception =
                Mockito.mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getValue()).thenReturn("abc");

        ResponseEntity<ApiResponse> response = handler.handleTypeMismatch(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Tham số 'id' có giá trị 'abc' không đúng định dạng",
                response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-06(a) - Không tìm thấy tài nguyên")
    void EXC_06_a_notFound() {
        ResponseEntity<ApiResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("Movie not found"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Movie not found", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-06(b) - Tài nguyên đã tồn tại")
    void EXC_06_b_alreadyExists() {
        ResponseEntity<ApiResponse> response =
                handler.handleAlreadyExist(new ResourceAlreadyExistException("Email already exists"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Email already exists", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-07(a) - RuntimeException có message")
    void EXC_07_a_message() {
        ResponseEntity<ApiResponse> response =
                handler.handleRuntimeException(new RuntimeException("Business error"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Business error", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-07(b) - RuntimeException message null")
    void EXC_07_b_nullMessage() {
        ResponseEntity<ApiResponse> response =
                handler.handleRuntimeException(new RuntimeException((String) null));

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("EXC-08 - Lỗi hệ thống không lường trước")
    void EXC_08_handlesUnexpectedException() {
        ResponseEntity<ApiResponse> response =
                handler.handleException(new Exception("Unexpected failure"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Lỗi hệ thống: Unexpected failure", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    private static MethodArgumentNotValidException validationException(FieldError... errors) {
        MethodArgumentNotValidException exception =
                Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(errors));
        return exception;
    }
}
