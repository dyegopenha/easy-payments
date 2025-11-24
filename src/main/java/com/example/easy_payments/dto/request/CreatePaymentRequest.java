package com.example.easy_payments.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {

   @NotBlank(message = "First name is required")
   private String firstName;

   @NotBlank(message = "Last name is required")
   private String lastName;

   @NotBlank(message = "Zip code is required")
   @Pattern(regexp = "^[0-9]{5}(?:-[0-9]{4})?$", message = "Invalid zip code format")
   private String zipCode;

   @NotNull(message = "Amount is required")
   @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
   @Digits(integer = 19, fraction = 2, message = "Amount must have a maximum of 19 integer digits and 2 fraction digits")
   private BigDecimal amount;

   @NotBlank(message = "Card number is required")
   @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13 to 19 digits")
   private String cardNumber;
}
