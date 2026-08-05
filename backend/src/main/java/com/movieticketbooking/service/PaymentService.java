package com.movieticketbooking.service;

import com.movieticketbooking.request.payment.MoMoPaymentRequest;
import com.movieticketbooking.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createMoMoPaymentUrl(MoMoPaymentRequest request);
    PaymentResponse processMoMoCallback(String transactionId, String responseCode, int ticketId);
}
