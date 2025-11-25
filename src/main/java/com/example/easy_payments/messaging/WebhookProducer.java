package com.example.easy_payments.messaging;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.easy_payments.config.RabbitMQConfig;
import com.example.easy_payments.model.PaymentEntity;
import com.example.easy_payments.model.WebhookEntity;
import com.example.easy_payments.repository.WebhookRepository;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class WebhookProducer {

   private final WebhookRepository webhookRepository;
   private final RabbitTemplate rabbitTemplate;

   private final ObjectMapper mapper = new ObjectMapper();

   public WebhookProducer(WebhookRepository webhookRepository, RabbitTemplate rabbitTemplate) {
      this.webhookRepository = webhookRepository;
      this.rabbitTemplate = rabbitTemplate;
   }

   public void produce(PaymentEntity payment) {
      List<WebhookEntity> webhooks = webhookRepository.findAll();

      log.info("Fanning out payment success message for payment ID {} to {} registered webhooks.",
            payment.getId(), webhooks.size());

      for (WebhookEntity webhook : webhooks) {
         WebhookPayload payload = new WebhookPayload(
               payment.getId(),
               payment.getFirstName(),
               payment.getLastName(),
               payment.getZipCode(),
               payment.getAmount(),
               webhook.getUrl()
         );

         rabbitTemplate.convertAndSend(
               RabbitMQConfig.EXCHANGE,
               "payment.created",
               mapper.writeValueAsString(payload)
         );
         log.debug("Webhook message published for payment {} to URL: {}", payment.getId(), webhook.getUrl());
      }
   }
}
