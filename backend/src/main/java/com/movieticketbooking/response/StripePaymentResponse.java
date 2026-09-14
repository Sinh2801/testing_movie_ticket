package com.movieticketbooking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripePaymentResponse {
    private String clientSecret;
    private String publishableKey;
    private Long amount;
    private String currency;
    private Integer ticketId;
    private String paymentIntentId;
}
