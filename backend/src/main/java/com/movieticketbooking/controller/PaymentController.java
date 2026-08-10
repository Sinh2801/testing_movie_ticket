package com.movieticketbooking.controller;

import com.movieticketbooking.request.payment.MoMoPaymentRequest;
import com.movieticketbooking.request.payment.VnPayPaymentRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * Controller tiếp nhận và xử lý các API endpoint liên quan đến thanh toán trực tuyến
 */
@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * API Tạo liên kết thanh toán MoMo
     */
    @PostMapping("/momo/create")
    public ResponseEntity<ApiResponse> createMoMoPayment(@RequestBody @Valid MoMoPaymentRequest request) {
        try {
            PaymentResponse response = paymentService.createMoMoPaymentUrl(request);
            return ResponseEntity.ok(new ApiResponse("Create MoMo payment success", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Create MoMo payment failed: " + e.getMessage(), null));
        }
    }

    /**
     * API Xử lý Callback thanh toán MoMo
     */
    @PostMapping("/momo/callback")
    public ResponseEntity<ApiResponse> processMoMoCallback(
            @RequestParam String transactionId,
            @RequestParam String responseCode,
            @RequestParam int ticketId) {
        try {
            PaymentResponse response = paymentService.processMoMoCallback(transactionId, responseCode, ticketId);
            return ResponseEntity.ok(new ApiResponse("Process MoMo payment success", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Process MoMo payment failed: " + e.getMessage(), null));
        }
    }

    /**
     * API TẠO LIÊN KẾT THANH TOÁN VNPAY (POST /api/payments/vnpay/create)
     * Frontend truyền vé ticketId -> Backend khởi tạo đơn hàng PENDING trong DB và tạo URL cổng thanh toán VNPay
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<ApiResponse> createVnPayPayment(
            @RequestBody @Valid VnPayPaymentRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Gọi service tạo URL VNPay kèm mã hóa checksum HmacSHA512
            PaymentResponse response = paymentService.createVnPayPaymentUrl(request, httpRequest);
            return ResponseEntity.ok(new ApiResponse("Create VNPay payment success", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Create VNPay payment failed: " + e.getMessage(), null));
        }
    }

    /**
     * API TIẾP NHẬN KẾT QUẢ VNPAY TRẢ VỀ (GET /api/payments/vnpay/callback)
     * VNPay gọi lại URL này sau khi người dùng thực hiện thanh toán trên cổng thanh toán
     */
    @GetMapping("/vnpay/callback")
    public void processVnPayCallback(
            @RequestParam Map<String, String> queryParams,
            HttpServletResponse httpResponse) throws IOException {
        try {
            // Xử lý kiểm tra chữ ký checksum và cập nhật thông tin đơn vé + thanh toán vào DB
            PaymentResponse response = paymentService.processVnPayCallback(queryParams);
            
            // Nếu thanh toán thành công, chuyển hướng người dùng về trang thông tin cá nhân/vé đã mua trên Frontend
            if (response.getMessage() != null && response.getMessage().contains("successfully")) {
                httpResponse.sendRedirect("http://localhost:3000/profile.html?status=success&ticketId=" + response.getTicketId());
            } else {
                // Nếu thất bại, chuyển người dùng về lại trang đặt vé
                httpResponse.sendRedirect("http://localhost:3000/booking.html?status=failed&ticketId=" + response.getTicketId());
            }
        } catch (Exception e) {
            httpResponse.sendRedirect("http://localhost:3000/booking.html?status=error&message=" + e.getMessage());
        }
    }
}
