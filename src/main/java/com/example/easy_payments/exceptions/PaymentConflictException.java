package com.example.easy_payments.exceptions;

public class PaymentConflictException extends RuntimeException {
   public PaymentConflictException(String message) {
      super(message);
   }
}
