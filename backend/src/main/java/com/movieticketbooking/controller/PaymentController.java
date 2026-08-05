package com.movieticketbooking.controller;

import com.movieticketbooking.request.payment.MoMoPaymentRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Create MoMo payment URL
    @PostMapping("/momo/create")
    public ResponseEntity<ApiResponse> createMoMoPayment(@RequestBody @Valid MoMoPaymentRequest request) {
        try {
            PaymentResponse response = paymentService.createMoMoPaymentUrl(request);
            return ResponseEntity.ok(new ApiResponse("Create MoMo payment success", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Create MoMo payment failed: " + e.getMessage(), null));
        }
    }

    // Process MoMo callback / payment execution result
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
}
