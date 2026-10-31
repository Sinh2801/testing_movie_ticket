package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Payment;
import com.movieticketbooking.model.PaymentStatus;
import com.movieticketbooking.repository.PaymentRepository;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplConfirmLocalTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("PAY-LOCAL-01 - Xác nhận payment hợp lệ")
    void PAY_LOCAL_01_confirmsPendingPayment() {
        Payment payment = payment(PaymentStatus.PENDING, "TXN-001");
        when(paymentRepository.findByTicketId(5)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.confirmLocalPayment(5);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(PaymentStatus.SUCCESS, savedPayment.getStatus());
        assertNotNull(savedPayment.getPaidAt());
        assertNotNull(savedPayment.getUpdatedAt());
        verify(ticketService).confirmTicketPayment(5);

        assertEquals("/profile.html?payment=success&ticketId=5", response.getPaymentUrl());
        assertEquals("TXN-001", response.getTransactionId());
        assertEquals(5, response.getTicketId());
        assertEquals("Local Payment successfully confirmed", response.getMessage());
    }

    @Test
    @DisplayName("PAY-LOCAL-02 - Payment không tồn tại")
    void PAY_LOCAL_02_rejectsMissingPayment() {
        when(paymentRepository.findByTicketId(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.confirmLocalPayment(999));

        assertEquals("Payment not found for ticket: 999", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(ticketService, never()).confirmTicketPayment(999);
    }

    @Test
    @DisplayName("PAY-LOCAL-03 - Xác nhận lặp payment SUCCESS - Known Limitation")
    void PAY_LOCAL_03_reconfirmsAlreadySuccessfulPayment() {
        LocalDateTime oldPaidAt = LocalDateTime.now().minusDays(1);
        Payment payment = payment(PaymentStatus.SUCCESS, "TXN-001");
        payment.setPaidAt(oldPaidAt);
        payment.setUpdatedAt(oldPaidAt);
        when(paymentRepository.findByTicketId(5)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Known Limitation: thiếu idempotency guard, payment SUCCESS vẫn bị xác nhận lại.
        PaymentResponse response = paymentService.confirmLocalPayment(5);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertTrue(payment.getPaidAt().isAfter(oldPaidAt));
        assertTrue(payment.getUpdatedAt().isAfter(oldPaidAt));
        verify(paymentRepository).save(payment);
        verify(ticketService).confirmTicketPayment(5);
        assertEquals("TXN-001", response.getTransactionId());
    }

    @Test
    @DisplayName("PAY-LOCAL-04 - Ticket không tồn tại khi confirm")
    void PAY_LOCAL_04_savesPaymentBeforeTicketFailure() {
        Payment payment = payment(PaymentStatus.PENDING, "TXN-001");
        when(paymentRepository.findByTicketId(5)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ResourceNotFoundException expected = new ResourceNotFoundException(
                "Ticket not found with id: 5");
        doThrow(expected).when(ticketService).confirmTicketPayment(5);

        // Unit test với mock không có rollback; @Transactional cần integration test.
        ResourceNotFoundException actual = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.confirmLocalPayment(5));

        assertSame(expected, actual);
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        InOrder order = inOrder(paymentRepository, ticketService);
        order.verify(paymentRepository).save(payment);
        order.verify(ticketService).confirmTicketPayment(5);
    }

    @Test
    @DisplayName("PAY-LOCAL-05 - Payment có transactionId null")
    void PAY_LOCAL_05_preservesNullTransactionId() {
        Payment payment = payment(PaymentStatus.PENDING, null);
        when(paymentRepository.findByTicketId(5)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.confirmLocalPayment(5);

        assertNull(response.getTransactionId());
        assertEquals(5, response.getTicketId());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        verify(ticketService).confirmTicketPayment(5);
    }

    private static Payment payment(PaymentStatus status, String transactionId) {
        Payment payment = new Payment();
        payment.setStatus(status);
        payment.setTransactionId(transactionId);
        return payment;
    }
}
