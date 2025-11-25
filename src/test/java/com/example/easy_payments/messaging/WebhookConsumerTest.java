package com.example.easy_payments.messaging;

import com.example.easy_payments.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class WebhookConsumerTest {

   @Mock
   private RabbitTemplate rabbitTemplate;

   @Mock
   private RestTemplate restTemplate;

   private final ObjectMapper objectMapper = new ObjectMapper();

   private WebhookConsumer webhookConsumer;

   private String deliveryMessageJson;

   private WebhookPayload deliveryMessageDto;

   private final String targetUrl = "http://mock-webhook-target.com/api";

   @BeforeEach
   void setUp() throws Exception {
      // Initialize listener with RabbitTemplate AND ObjectMapper
      webhookConsumer = new WebhookConsumer(rabbitTemplate);

      // Use reflection to inject the mocked RestTemplate
      java.lang.reflect.Field field = WebhookConsumer.class.getDeclaredField("restTemplate");
      field.setAccessible(true);
      field.set(webhookConsumer, restTemplate);

      BigDecimal testAmount = new BigDecimal("99.99");

      // 1. Create the internal DTO object
      deliveryMessageDto = new WebhookPayload(
            100L,
            "Jane",
            "Doe",
            "12345",
            testAmount,
            targetUrl
      );

      // 2. Serialize the DTO into the expected JSON string for the queue payload
      deliveryMessageJson = objectMapper.writeValueAsString(deliveryMessageDto);
   }

   @Test
   void testHandlePaymentMessage_Success() {
      // Arrange: Webhook returns 200 OK
      when(restTemplate.postForEntity(eq(targetUrl), any(WebhookPayload.class), any(Class.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

      // Act: Initial attempt (xDeathList is null)
      webhookConsumer.handlePaymentMessage(deliveryMessageJson, Collections.emptyList());

      // Assert: Successful delivery using the DTO object (verify URL and HTTP body)
      verify(restTemplate, times(1)).postForEntity(eq(targetUrl), eq(deliveryMessageDto), eq(String.class));
      verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any())); // No retry/DLQ needed
   }

   @Test
   void testHandlePaymentMessage_Failure_Retry1() {
      // Arrange: Webhook throws ResourceAccessException (connection error)
      when(restTemplate.postForEntity(anyString(), any(WebhookPayload.class), any(Class.class)))
            .thenThrow(ResourceAccessException.class);

      // Act: First attempt (xDeathList is empty, currentAttempt = 0)
      webhookConsumer.handlePaymentMessage(deliveryMessageJson, Collections.emptyList());

      // Assert: Message sent to the 1st delay exchange (delay.1)
      ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

      verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.DELAY_EXCHANGE),
            routingKeyCaptor.capture(),
            messageCaptor.capture()
      );
      assertEquals("delay.1", routingKeyCaptor.getValue());
      assertEquals(deliveryMessageJson, messageCaptor.getValue());
   }

   @Test
   void testHandlePaymentMessage_Failure_Retry2() {
      // Arrange: Webhook throws HttpClientErrorException (e.g., 500 status)
      when(restTemplate.postForEntity(anyString(), any(WebhookPayload.class), any(Class.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

      // Act: Second attempt (xDeathList has 1 element, currentAttempt = 1)
      List<Map<String, Object>> xDeathList = List.of(Map.of("count", 1));
      webhookConsumer.handlePaymentMessage(deliveryMessageJson, xDeathList);

      // Assert: Message sent to the 2nd delay exchange (delay.2)
      ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

      verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.DELAY_EXCHANGE),
            routingKeyCaptor.capture(),
            messageCaptor.capture()
      );
      assertEquals("delay.2", routingKeyCaptor.getValue());
      assertEquals(deliveryMessageJson, messageCaptor.getValue());
   }

   @Test
   void testHandlePaymentMessage_MaxAttempts_SendToDLQ() {
      // Arrange: Failure happens on the final (3rd) attempt
      when(restTemplate.postForEntity(anyString(), any(WebhookPayload.class), any(Class.class)))
            .thenThrow(new RuntimeException("Final delivery failed"));

      // Act: Third attempt (xDeathList has 2 elements, currentAttempt = 2 -> DLQ)
      // This simulates the failure AFTER the second (final) retry.
      List<Map<String, Object>> xDeathList = List.of(Map.of("count", 1), Map.of("count", 2));
      webhookConsumer.handlePaymentMessage(deliveryMessageJson, xDeathList);

      // Assert: Message sent directly to the DLQ
      ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

      verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.DLQ_NAME),
            messageCaptor.capture()
      );
      verify(rabbitTemplate, never()).convertAndSend(eq(RabbitMQConfig.DELAY_EXCHANGE), anyString(), Optional.ofNullable(any()));
      assertEquals(deliveryMessageJson, messageCaptor.getValue());
   }
}
