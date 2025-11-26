package com.example.easy_payments.exceptions;

public class WebhookConflictException extends RuntimeException {
   public WebhookConflictException(String message) { super(message); }
}
