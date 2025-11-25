package com.example.easy_payments.messaging;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebhookPayload {
   private Long paymentId;
   private String firstName;
   private String lastName;
   private String zipCode;
   private BigDecimal amount;
   private String webhookUrl;
}
