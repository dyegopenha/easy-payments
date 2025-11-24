package com.example.easy_payments.dto.response;

import com.example.easy_payments.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
   private Long id;
   private String externalId;
   private PaymentStatus status;
}
