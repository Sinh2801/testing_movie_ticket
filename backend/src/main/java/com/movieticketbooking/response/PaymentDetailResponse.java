package com.movieticketbooking.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.movieticketbooking.model.PaymentMethod;
import com.movieticketbooking.model.PaymentStatus;
import lombok.Data;

@Data
public class PaymentDetailResponse {
    private Integer id;
    private Integer ticketId;
    private String paymentCode;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;

    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
