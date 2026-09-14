package com.movieticketbooking.service;

import com.movieticketbooking.request.payment.StripePaymentRequest;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.response.StripePaymentResponse;

/**
 * Service giao diện xử lý các nghiệp vụ thanh toán trực tuyến qua cổng Stripe (Visa / Mastercard).
 */
public interface StripeService {

    StripePaymentResponse createPaymentIntent(StripePaymentRequest request);

    PaymentResponse confirmStripePayment(int ticketId, String paymentIntentId);
}
