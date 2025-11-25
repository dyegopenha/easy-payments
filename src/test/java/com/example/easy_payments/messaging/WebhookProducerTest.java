package com.example.easy_payments.messaging;

import com.example.easy_payments.config.RabbitMQConfig;
import com.example.easy_payments.model.PaymentEntity;
import com.example.easy_payments.model.PaymentStatus;
import com.example.easy_payments.model.WebhookEntity;
import com.example.easy_payments.repository.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import tools.jackson.databind.ObjectMapper;


@ExtendWith(MockitoExtension.class)
class WebhookProducerTest {

   @Mock
   private WebhookRepository webhookRepository;

   @Mock
   private RabbitTemplate rabbitTemplate;

   private final ObjectMapper objectMapper = new ObjectMapper();

   private WebhookProducer webhookProducer;

   @BeforeEach
   void setUp() {
      webhookProducer = new WebhookProducer(webhookRepository, rabbitTemplate);
   }

   @Test
   void testFanOutPaymentSuccess_MultipleWebhooks() throws Exception {
      // Arrange
      long paymentId = 100L;
      String webhookUrl1 = "http://target1.com/webhook";
      String webhookUrl2 = "http://target2.com/webhook";
      BigDecimal amount = new BigDecimal("45.50");

      // Mock payment entity
      PaymentEntity payment = new PaymentEntity();
      payment.setId(paymentId);
      payment.setFirstName("Test");
      payment.setLastName("User");
      payment.setZipCode("10001");
      payment.setAmount(amount);
      payment.setStatus(PaymentStatus.PROCESSED);
      payment.setCreatedAt(LocalDateTime.now());

      // Mock webhook entities
      WebhookEntity webhook1 = new WebhookEntity();
      webhook1.setUrl(webhookUrl1);
      WebhookEntity webhook2 = new WebhookEntity();
      webhook2.setUrl(webhookUrl2);

      when(webhookRepository.findAll()).thenReturn(List.of(webhook1, webhook2));

      // Act
      webhookProducer.produce(payment);

      // Assert
      // 1. Verify the repository was called
      verify(webhookRepository, times(1)).findAll();

      // 2. Verify RabbitTemplate was called exactly twice (once for each webhook)
      verify(rabbitTemplate, times(2)).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE),
            anyString(), Optional.ofNullable(any())
      );

      // 3. Capture the published messages and verify the content (JSON string)
      ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
      verify(rabbitTemplate, times(2)).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE),
            messageCaptor.capture(), Optional.ofNullable(any())
      );

      List<String> publishedMessages = messageCaptor.getAllValues();

      // Ensure both expected messages were published (order might vary, so we check existence)
      assertEquals(2, publishedMessages.size());
   }

   @Test
   void testFanOutPaymentSuccess_NoWebhooks() {
      // Arrange
      PaymentEntity payment = new PaymentEntity();
      payment.setId(1L);

      when(webhookRepository.findAll()).thenReturn(Collections.emptyList());

      // Act
      webhookProducer.produce(payment);

      // Assert
      // 1. Verify repository was called
      verify(webhookRepository, times(1)).findAll();

      // 2. Verify RabbitTemplate was never called
      verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
   }
}
