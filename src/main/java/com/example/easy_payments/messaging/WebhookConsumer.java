package com.example.easy_payments.messaging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

   private static final int MAX_ATTEMPTS = 3;

   @Autowired
   public WebhookConsumer(RabbitTemplate rabbitTemplate) {
      this.rabbitTemplate = rabbitTemplate;
      this.restTemplate = new RestTemplate();
   }

   @RabbitListener(queues = RabbitMQConfig.MAIN_QUEUE)
   public void consume(Message message) {
      WebhookPayload payload = getPayload(message);
      int attempt = getRetryCount(message);

      log.info("Received delivery message for payment {} to URL: {} (Attempt {}/{}).",
            payload.getPaymentExternalId(), payload.getWebhookUrl(), attempt, MAX_ATTEMPTS);

      postWebhook(attempt, payload, message);
   }

   private WebhookPayload getPayload(Message message) {
      return mapper.readValue(message.getBody(), WebhookPayload.class);
   }

   private int getRetryCount(Message message) {
      Integer attempt = (Integer) message.getMessageProperties().getHeaders().getOrDefault("x-attempt", 1);
      if (attempt == null) attempt = 1;
      return attempt;
   }

   private void postWebhook(int attempt, WebhookPayload payload, Message message) {
      String url = payload.getWebhookUrl();
      try {
         // Should add signature header for enchanced security
         restTemplate.postForEntity(url, payload, String.class);
         log.info("Successfully delivered webhook for payment {} to URL: {}", payload.getPaymentExternalId(), url);
      } catch (Exception e) {
         log.warn("Webhook delivery failed to URL: {} (Attempt {}). Error: {}", url, attempt, e.getMessage());
         handleFailure(attempt, message, payload);
      }
   }

   private void handleFailure(int attempt, Message message, WebhookPayload payload) {
      int nextAttempt = attempt + 1;
      if (nextAttempt > MAX_ATTEMPTS) {
         saveFailedMessage(payload);
         return;
      }
      handleRetry(nextAttempt, message);
   }

   private void handleRetry(int nextAttempt, Message message) {
      String routing = "retry." + nextAttempt;
      MessageProperties newProps = new MessageProperties();
      newProps.getHeaders().put("x-attempt", nextAttempt);
      newProps.setContentType(message.getMessageProperties().getContentType());
      Message newMsg = new Message(message.getBody(), newProps);
      rabbitTemplate.send(RabbitMQConfig.RETRY_EXCHANGE, routing, newMsg);
   }

   private void saveFailedMessage(WebhookPayload payload) {
      log.warn("Saving failed message... paymentExternalId: {}", payload.getPaymentExternalId());
      // TODO persist to failed_messages table for later investigation
      //FailedMessage fm = new FailedMessage();
      //fm.setPayload(payload);
      //fm.setReason("Max attempts reached");
      //failedMessageRepository.save(fm);
   }
}
