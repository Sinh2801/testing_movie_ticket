package com.movieticketbooking.request.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StripePaymentRequest {
    @NotNull(message = "Ticket ID is required")
    private Integer ticketId;
}
