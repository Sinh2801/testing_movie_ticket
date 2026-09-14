package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Payment;
import com.movieticketbooking.model.PaymentMethod;
import com.movieticketbooking.model.PaymentStatus;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.repository.PaymentRepository;
import com.movieticketbooking.repository.TicketRepository;
import com.movieticketbooking.request.payment.LocalPaymentRequest;
import com.movieticketbooking.response.PagedResponse;
import com.movieticketbooking.response.PaymentDetailResponse;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.service.PaymentService;
import com.movieticketbooking.service.TicketService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TicketService ticketService;

    // Get paged payments with optional paymentCode filter
    @Override
    public PagedResponse<PaymentDetailResponse> getPagedPayments(int page, int size, String paymentCode) {
        int pageIndex = (page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Payment> paymentPage = null;
        if (paymentCode == null || paymentCode.isBlank()) {
            paymentPage = paymentRepository.findAll(pageable);
        } else {
            paymentPage = paymentRepository.findByPaymentCodeContaining(paymentCode, pageable);
        }
        Page<PaymentDetailResponse> paymentResponsePage = paymentPage.map(this::toDetailResponse);
        return new PagedResponse<>(paymentResponsePage);
    }

    //Create payment for local payment method (Cash, Bank Transfer, etc.)
    @Override
    @Transactional
    public PaymentResponse createLocalPaymentUrl(LocalPaymentRequest request) {
        Ticket ticket = ticketService.findTicketById(request.getTicketId());

        String ticketUid = ticket.getUid() != null ? ticket.getUid() : ("TIC-" + ticket.getId());

        Payment payment = paymentRepository.findByTicketId(ticket.getId())
                .orElse(new Payment());

        payment.setTicket(ticket);
        payment.setAmount(ticket.getTotalPrice());
        payment.setMethod(PaymentMethod.LOCAL);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setPaymentCode(ticketUid);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        String redirectUrl = "/local-payment.html?ticketId=" + ticket.getId() + "&transactionId=" + payment.getTransactionId();

        return PaymentResponse.builder()
                .paymentUrl(redirectUrl)
                .transactionId(payment.getTransactionId())
                .ticketId(ticket.getId())
                .message("Generated Local payment info successfully")
                .build();
    }

    // Confirm local payment (Cash, Bank Transfer, etc.)
    @Override
    @Transactional
    public PaymentResponse confirmLocalPayment(int ticketId) {
        Payment payment = paymentRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for ticket: " + ticketId));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        ticketService.confirmTicketPayment(ticketId);

        return PaymentResponse.builder()
                .paymentUrl("/profile.html?payment=success&ticketId=" + ticketId)
                .transactionId(payment.getTransactionId())
                .ticketId(ticketId)
                .message("Local Payment successfully confirmed")
                .build();
    }

    // 4242 4242 4242 4242 - Succeeds for any payment
    // 4000 0025 0000 3155 - Requires 3D Secure authentication
    // 4000 0000 0000 9995 - Always declines

    // map to detail response
    private PaymentDetailResponse toDetailResponse(Payment payment) {
        PaymentDetailResponse response = new PaymentDetailResponse();
        response.setId(payment.getId());
        response.setTicketId(payment.getTicket().getId());
        response.setPaymentCode(payment.getPaymentCode());
        response.setAmount(payment.getAmount());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setPaidAt(payment.getPaidAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
}
