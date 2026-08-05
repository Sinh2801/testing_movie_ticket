package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Payment;
import com.movieticketbooking.model.PaymentMethod;
import com.movieticketbooking.model.PaymentStatus;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.repository.PaymentRepository;
import com.movieticketbooking.repository.TicketRepository;
import com.movieticketbooking.request.payment.MoMoPaymentRequest;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.service.PaymentService;
import com.movieticketbooking.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;

    @Override
    @Transactional
    public PaymentResponse createMoMoPaymentUrl(MoMoPaymentRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + request.getTicketId()));

        String transactionId = "MOMO-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Payment payment = paymentRepository.findByTicketId(ticket.getId())
                .orElse(new Payment());

        payment.setTicket(ticket);
        payment.setAmount(ticket.getTotalPrice());
        payment.setMethod(PaymentMethod.MOMO);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(transactionId);
        payment.setUid("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        // Generated simulated MoMo Portal URL for frontend redirection
        String redirectUrl = "/payment-success.html?ticketId=" + ticket.getId() + "&transactionId=" + transactionId;

        return PaymentResponse.builder()
                .paymentUrl(redirectUrl)
                .transactionId(transactionId)
                .ticketId(ticket.getId())
                .message("Generated MoMo payment URL successfully")
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse processMoMoCallback(String transactionId, String responseCode, int ticketId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseGet(() -> paymentRepository.findByTicketId(ticketId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment not found")));

        payment.setResponseCode(responseCode);
        payment.setUpdatedAt(LocalDateTime.now());

        if ("0".equals(responseCode) || "00".equals(responseCode) || "SUCCESS".equalsIgnoreCase(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Confirm ticket and send email
            ticketService.confirmTicketPayment(ticketId);

            return PaymentResponse.builder()
                    .paymentUrl("/profile.html")
                    .transactionId(transactionId)
                    .ticketId(ticketId)
                    .message("MoMo Payment successfully completed")
                    .build();
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentUrl("/booking.html")
                    .transactionId(transactionId)
                    .ticketId(ticketId)
                    .message("MoMo Payment failed")
                    .build();
        }
    }
}
