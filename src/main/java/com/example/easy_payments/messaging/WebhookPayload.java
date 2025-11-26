package com.example.easy_payments.messaging;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookPayload {
   private String paymentExternalId;
   private String firstName;
   private String lastName;
   private String zipCode;
   private BigDecimal amount;
   private String webhookUrl;
   private String status;
   private String last4Digits;
}
