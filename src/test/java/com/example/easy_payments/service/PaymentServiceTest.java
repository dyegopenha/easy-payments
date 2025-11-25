package com.example.easy_payments.service;

import com.example.easy_payments.dto.request.CreatePaymentRequest;
import com.example.easy_payments.dto.response.PaymentResponse;
import com.example.easy_payments.messaging.WebhookProducer;
import com.example.easy_payments.model.PaymentEntity;
import com.example.easy_payments.model.PaymentStatus;
import com.example.easy_payments.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

   @Mock
   private PaymentRepository paymentRepository;

   @Mock
   private WebhookProducer webhookProducer;

   private PaymentService paymentService;

   @BeforeEach
   void setUp() {
      paymentService = new PaymentService(paymentRepository, webhookProducer);
   }

   @Test
   void testCreatePayment_Success_DelegatesMessageFanOut() {
      // Arrange
      String idempotencyKey = "uuid-test-123";
      BigDecimal testAmount = new BigDecimal("99.99");
      CreatePaymentRequest request = new CreatePaymentRequest();
      request.setFirstName("Jane");
      request.setCardNumber("1111222233334444");
      request.setZipCode("12345");
      request.setLastName("Doe");
      request.setAmount(testAmount);
      request.setIdempotencyKey(idempotencyKey);

      PaymentEntity savedEntity = new PaymentEntity();
      savedEntity.setId(100L);
      savedEntity.setExternalId(idempotencyKey);
      savedEntity.setStatus(PaymentStatus.PROCESSED);
      savedEntity.setAmount(testAmount);
      savedEntity.setFirstName(request.getFirstName());
      savedEntity.setLastName(request.getLastName());
      savedEntity.setZipCode(request.getZipCode());

      when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedEntity);

      // Act
      PaymentResponse response = paymentService.createPayment(request);

      // Assert
      assertNotNull(response);

      // 1. Verify payment entity was saved
      ArgumentCaptor<PaymentEntity> entityCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
      verify(paymentRepository, times(1)).save(entityCaptor.capture());

      // 2. Verify message fan-out was delegated to the producer service with the saved entity
      verify(webhookProducer, times(1)).produce(savedEntity);

      // 3. Verify final response
      assertEquals(PaymentStatus.PROCESSED, response.getStatus());
      assertEquals(idempotencyKey, response.getExternalId());
   }
}
