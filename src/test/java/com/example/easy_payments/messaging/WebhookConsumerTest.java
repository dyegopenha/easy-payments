package com.example.easy_payments.messaging;

import com.example.easy_payments.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
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
            targetUrl,
            "PROCESSED",
            "************1234"
      );

      // 2. Serialize the DTO into the expected JSON string for the queue payload
      deliveryMessageJson = objectMapper.writeValueAsString(deliveryMessageDto);
   }

   @Test
   void testHandlePaymentMessage_Success() {
      // Arrange: Webhook returns 200 OK
      when(restTemplate.postForEntity(eq(targetUrl), any(WebhookPayload.class), any(Class.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

      MessageProperties newProps = new MessageProperties();
      newProps.getHeaders().put("x-attempt", 0);
      newProps.setContentType("application/json");
      Message message = new Message(deliveryMessageJson.getBytes(), newProps);

      // Act: Initial attempt
      webhookConsumer.consume(message);

      // Assert: Successful delivery using the DTO object (verify URL and HTTP body)
      verify(restTemplate, times(1)).postForEntity(eq(targetUrl), eq(deliveryMessageDto), eq(String.class));
      verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any())); // No retry/DLQ needed
   }

   @Test
   void testHandlePaymentMessage_Failure_Retry1() {
      // Arrange: Webhook throws ResourceAccessException (connection error)
      when(restTemplate.postForEntity(anyString(), any(WebhookPayload.class), any(Class.class)))
            .thenThrow(ResourceAccessException.class);

      MessageProperties newProps = new MessageProperties();
      newProps.getHeaders().put("x-attempt", 0);
      newProps.setContentType("application/json");
      Message message = new Message(deliveryMessageJson.getBytes(), newProps);

      // Act: First attempt
      webhookConsumer.consume(message);

      // Assert: Message sent to the 1st retry
      ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

      verify(rabbitTemplate, times(1)).send(
            eq(RabbitMQConfig.RETRY_EXCHANGE),
            routingKeyCaptor.capture(),
            messageCaptor.capture()
      );
      assertEquals("retry.1", routingKeyCaptor.getValue());
   }

   @Test
   void testHandlePaymentMessage_Failure_Retry2() {
      // Arrange: Webhook throws HttpClientErrorException (e.g., 500 status)
      when(restTemplate.postForEntity(anyString(), any(WebhookPayload.class), any(Class.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

      MessageProperties newProps = new MessageProperties();
      newProps.getHeaders().put("x-attempt", 1);
      newProps.setContentType("application/json");
      Message message = new Message(deliveryMessageJson.getBytes(), newProps);

      // Act: Second attempt
      webhookConsumer.consume(message);

      // Assert: Message sent to the 2nd delay exchange (delay.2)
      ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

      verify(rabbitTemplate, times(1)).send(
            eq(RabbitMQConfig.RETRY_EXCHANGE),
            routingKeyCaptor.capture(),
            messageCaptor.capture()
      );
      assertEquals("retry.2", routingKeyCaptor.getValue());
   }

   @Test
   void testHandlePaymentMessage_MaxAttempts_SendToDLQ() {
      // Arrange: Failure happens on the final (3rd) attempt
      when(restTemplate.postForEntity(anyString(), any(WebhookPayload.class), any(Class.class)))
            .thenThrow(new RuntimeException("Final delivery failed"));

      MessageProperties newProps = new MessageProperties();
      newProps.getHeaders().put("x-attempt", 3);
      newProps.setContentType("application/json");
      Message message = new Message(deliveryMessageJson.getBytes(), newProps);

      // Act: Third attempt
      webhookConsumer.consume(message);

      // Assert
      verify(rabbitTemplate, never()).send(
            eq(RabbitMQConfig.RETRY_EXCHANGE),
            anyString(),
            any()
      );
      verify(rabbitTemplate, never()).convertAndSend(eq(RabbitMQConfig.EXCHANGE), anyString(), Optional.ofNullable(any()));

      // Check save failed message
   }
}
