package com.example.easy_payments.messaging;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.example.easy_payments.config.RabbitMQConfig;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class WebhookConsumer {
   private final RestTemplate restTemplate;

   private final RabbitTemplate rabbitTemplate;

   private final ObjectMapper mapper = new ObjectMapper();

   private static final List<String> DELAY_ROUTING_KEYS = Arrays.asList("delay.1", "delay.2");
   private static final int MAX_ATTEMPTS = 2;

   @Autowired
   public WebhookConsumer(RabbitTemplate rabbitTemplate) {
      this.rabbitTemplate = rabbitTemplate;
      this.restTemplate = new RestTemplate();
   }

   @RabbitListener(queues = RabbitMQConfig.WEBHOOK_QUEUE)
   public void handlePaymentMessage(String message, @Header(value = "x-death", required = false) List<Map<String, Object>> xDeathList) {
      int currentAttempt = xDeathList != null ? xDeathList.size() : 0;

      WebhookPayload payload = mapper.readValue(message, WebhookPayload.class);
      String webhookUrl = payload.getWebhookUrl();

      log.info("Received delivery message for payment {} to URL: {} (Attempt {}/{}).",
            payload.getPaymentId(), webhookUrl, currentAttempt + 1, MAX_ATTEMPTS);

      try {
         restTemplate.postForEntity(webhookUrl, payload, String.class);
         log.info("Successfully delivered webhook for payment {} to URL: {}", payload.getPaymentId(), webhookUrl);
      } catch (ResourceAccessException | HttpClientErrorException e) {
         log.warn("Webhook delivery failed to URL: {} (Attempt {}). Error: {}", webhookUrl, currentAttempt + 1, e.getMessage());
         handleFailure(message, currentAttempt);
      } catch (Exception e) {
         log.error("Critical, non-transient error during webhook delivery to URL: {}. Sending directly to DLQ.", webhookUrl, e);
         handleFailure(message, MAX_ATTEMPTS);
      }
   }

   private void handleFailure(String message, int currentAttempt) {
      if (currentAttempt < MAX_ATTEMPTS) {
         String delayRoutingKey = DELAY_ROUTING_KEYS.get(currentAttempt);

         rabbitTemplate.convertAndSend(
               RabbitMQConfig.DELAY_EXCHANGE,
               delayRoutingKey,
               message
         );
         log.warn("Message sent to DLX with key '{}' for retry.", delayRoutingKey);
      } else {
         rabbitTemplate.convertAndSend(
               RabbitMQConfig.DLQ_NAME,
               message
         );
         log.error("Webhook delivery failed after {} attempts. Message moved to DLQ.", MAX_ATTEMPTS);
      }
   }
}
