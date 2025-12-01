package com.example.easy_payments.exceptions;

import com.example.easy_payments.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global Exception Handler to catch exceptions across all Controllers
 * and map them to standardized HTTP error responses.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

   /**
    * Handles exceptions related to bad request (400 Bad Request).
    */
   @ExceptionHandler(BadRequestException.class)
   public ResponseEntity<ErrorResponse> handleBadRequestException(
         BadRequestException ex,
         HttpServletRequest request) {

      String path = request.getRequestURI();
      log.warn("Bad Request: {}", ex.getMessage());

      return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), path));
   }

   /**
    * Handles exceptions related to idempotency violation (409 Conflict).
    * This can be triggered by a manual cache check or a database constraint violation.
    */
   @ExceptionHandler(PaymentConflictException.class)
   public ResponseEntity<ErrorResponse> handlePaymentConflictException(
         PaymentConflictException ex,
         HttpServletRequest request) {

      String path = request.getRequestURI();
      log.warn("Payment Conflict: {}", ex.getMessage());

      return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), path));
   }

   /**
    * Handles exceptions related to webhook url violation (409 Conflict).
    * This can be triggered by a database constraint violation.
    */
   @ExceptionHandler(WebhookConflictException.class)
   public ResponseEntity<ErrorResponse> handleWebhookConflictException(
         WebhookConflictException ex,
         HttpServletRequest request) {

      String path = request.getRequestURI();
      log.warn("Webhook Conflict: {}", ex.getMessage());

      return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), path));
   }

   /**
    * Handles database constraint violations, which often map to idempotency conflicts
    * in a race condition scenario.
    */
   @ExceptionHandler(DataIntegrityViolationException.class)
   public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
         DataIntegrityViolationException ex,
         HttpServletRequest request) {

      String path = request.getRequestURI();
      String message = "A unique constraint was violated. This often indicates a duplicate record submission.";

      // Log the detailed exception
      log.error("Database Integrity Violation at {}: {}", path, ex.getMessage());

      return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT, message, path));
   }

   /**
    * Handles Spring's validation failures (e.g., @Valid annotations fail) (400 Bad Request).
    */
   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ErrorResponse> handleValidationException(
         MethodArgumentNotValidException ex,
         HttpServletRequest request) {

      String path = request.getRequestURI();
      // Extract the most relevant error message
      String message = ex.getBindingResult().getFieldError() != null
            ? ex.getBindingResult().getFieldError().getDefaultMessage()
            : "Validation failed.";

      log.warn("Validation Error at {}: {}", path, message);

      return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(HttpStatus.BAD_REQUEST, message, path));
   }

   /**
    * Catches all other unexpected exceptions (500 Internal Server Error).
    */
   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
      String path = request.getRequestURI();
      String message = "An unexpected internal error occurred: " + ex.getMessage();

      log.error("Unhandled Exception at {}: {}", path, ex.getMessage(), ex);

      return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, path));
   }
}
