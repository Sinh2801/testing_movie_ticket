package com.movieticketbooking.service;

import com.movieticketbooking.request.payment.LocalPaymentRequest;
import com.movieticketbooking.response.PagedResponse;
import com.movieticketbooking.response.PaymentDetailResponse;
import com.movieticketbooking.response.PaymentResponse;

public interface PaymentService {
    PagedResponse<PaymentDetailResponse> getPagedPayments(int page, int size, String transactionId);
    PaymentResponse createLocalPaymentUrl(LocalPaymentRequest request);

    PaymentResponse confirmLocalPayment(int ticketId);
}
