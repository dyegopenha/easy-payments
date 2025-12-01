package com.example.easy_payments.exceptions;

public class BadRequestException extends RuntimeException {

   public BadRequestException(String message) {
      super(message);
   }
}
