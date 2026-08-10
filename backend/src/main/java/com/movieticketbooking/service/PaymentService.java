package com.movieticketbooking.service;

import java.util.Map;

import com.movieticketbooking.request.payment.MoMoPaymentRequest;
import com.movieticketbooking.request.payment.VnPayPaymentRequest;
import com.movieticketbooking.response.PaymentResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentResponse createMoMoPaymentUrl(MoMoPaymentRequest request);

    PaymentResponse processMoMoCallback(String transactionId, String responseCode, int ticketId);

    PaymentResponse createVnPayPaymentUrl(VnPayPaymentRequest request, HttpServletRequest httpRequest);

    PaymentResponse processVnPayCallback(Map<String, String> queryParams);
}
