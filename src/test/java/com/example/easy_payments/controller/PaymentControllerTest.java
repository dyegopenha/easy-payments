package com.example.easy_payments.controller;

import com.example.easy_payments.dto.request.CreatePaymentRequest;
import com.example.easy_payments.dto.response.PaymentResponse;
import com.example.easy_payments.model.PaymentStatus;
import com.example.easy_payments.service.PaymentServiceImpl;
import com.example.easy_payments.exceptions.PaymentConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

   @Mock
   private PaymentServiceImpl paymentService;

   @InjectMocks
   private PaymentController paymentController;

   private CreatePaymentRequest validRequest;
   private final String idempotencyKey = UUID.randomUUID().toString();
   private PaymentResponse successResponse;

   @BeforeEach
   void setUp() {
      validRequest = new CreatePaymentRequest();
      validRequest.setFirstName("Test");
      validRequest.setLastName("User");
      validRequest.setZipCode("10001");
      validRequest.setAmount(new BigDecimal("10.50"));
      validRequest.setCardNumber("1234123412341234");
      validRequest.setIdempotencyKey(idempotencyKey);

      successResponse = new PaymentResponse(1L, idempotencyKey, PaymentStatus.PROCESSED);
   }

   @Test
   void testCreatePayment_Success_Returns201() {
      // Arrange
      when(paymentService.createPayment(any(CreatePaymentRequest.class)))
            .thenReturn(successResponse);

      // Act
      ResponseEntity<PaymentResponse> response = paymentController.createPayment(validRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertEquals(successResponse, response.getBody());

      // Verify payment was created
      verify(paymentService, times(1)).createPayment(validRequest);
   }

   @Test
   void testCreatePayment_Conflict_AndThrowsException() {
      // Arrange
      doThrow(new PaymentConflictException(idempotencyKey))
            .when(paymentService).createPayment(validRequest);

      // Act & Assert
      // We expect the PaymentConflictException to be thrown here, which the GlobalExceptionHandler
      // will catch and translate into a 409 response.
      try {
         paymentController.createPayment(validRequest);
         // If execution reaches here, the test should fail
         fail("Expected PaymentConflictException was not thrown");
      } catch (PaymentConflictException ex) {
         // Success: Exception thrown as expected
         assertEquals(idempotencyKey, ex.getMessage());
      }
   }
}
