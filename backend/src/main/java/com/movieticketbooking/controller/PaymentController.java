package com.movieticketbooking.controller;

import com.movieticketbooking.request.payment.LocalPaymentRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.PagedResponse;
import com.movieticketbooking.response.PaymentDetailResponse;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Controller tiếp nhận và xử lý các API endpoint liên quan đến thanh toán trực tuyến
 */
@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final com.movieticketbooking.service.StripeService stripeService;

    // Get paged payments with optional paymentCode filter
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> getPagedPayments(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String paymentCode) 
    {
        PagedResponse<PaymentDetailResponse> response = paymentService.getPagedPayments(page, size, paymentCode);
        return ResponseEntity.ok(new ApiResponse("Get paged payments success", response));
    }

    // Create payment for local payment method (Cash, Bank Transfer, etc.)
    @PostMapping("/local/create")
    public ResponseEntity<ApiResponse> createLocalPayment(@RequestBody @Valid LocalPaymentRequest request) 
    {
        PaymentResponse response = paymentService.createLocalPaymentUrl(request);
        return ResponseEntity.ok(new ApiResponse("Create Local payment success", response));
    }

    // Confirm local payment for a ticket
    @PostMapping("/local/confirm")
    public ResponseEntity<ApiResponse> confirmLocalPayment(@RequestParam int ticketId) 
    {
        PaymentResponse response = paymentService.confirmLocalPayment(ticketId);
        return ResponseEntity.ok(new ApiResponse("Confirm Local payment success", response));
    }

    // Create Stripe PaymentIntent
    @PostMapping("/stripe/create-payment-intent")
    public ResponseEntity<ApiResponse> createStripePaymentIntent(
            @RequestBody @Valid com.movieticketbooking.request.payment.StripePaymentRequest request) 
    {
        com.movieticketbooking.response.StripePaymentResponse response = stripeService.createPaymentIntent(request);
        return ResponseEntity.ok(new ApiResponse("Create Stripe PaymentIntent success", response));
    }

    // Confirm Stripe Payment
    @PostMapping("/stripe/confirm")
    public ResponseEntity<ApiResponse> confirmStripePayment(
            @RequestParam int ticketId,
            @RequestParam(required = false) String paymentIntentId) 
    {
        PaymentResponse response = stripeService.confirmStripePayment(ticketId, paymentIntentId);
        return ResponseEntity.ok(new ApiResponse("Confirm Stripe payment success", response));
    }
}
