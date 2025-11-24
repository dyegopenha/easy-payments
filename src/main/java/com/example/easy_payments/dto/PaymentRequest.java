package com.example.easy_payments.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
      @NotBlank(message = "Idempotency key is required")
      @Size(min = 1, max = 100, message = "Idempotency key must be between 1 and 100 characters")
      String idempotencyKey,

      @NotBlank(message = "First name is required")
      @Size(max = 50, message = "First name must be under 50 characters")
      String firstName,

      @NotBlank(message = "Last name is required")
      @Size(max = 50, message = "Last name must be under 50 characters")
      String lastName,

      @NotBlank(message = "Zip code is required")
      @Pattern(regexp = "^\\d{5}$", message = "Zip code must be a 5-digit number")
      String zipCode,

      @NotBlank(message = "Card number is required")
      @Pattern(regexp = "^\\d{13,19}$", message = "Card number must be between 13 and 19 digits")
      String cardNumber,

      @Positive(message = "Amount must be positive")
      BigDecimal amount
) {

}
