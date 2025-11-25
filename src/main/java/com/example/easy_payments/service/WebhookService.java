package com.example.easy_payments.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.easy_payments.dto.request.RegisterWebhookRequest;
import com.example.easy_payments.dto.response.WebhookResponse;
import com.example.easy_payments.model.WebhookEntity;
import com.example.easy_payments.repository.WebhookRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebhookService {

   private final WebhookRepository webhookRepository;

   public WebhookService(WebhookRepository webhookRepository) {
      this.webhookRepository = webhookRepository;
   }

   @Transactional
   public WebhookResponse createWebhook(RegisterWebhookRequest request) {
      WebhookEntity webhook = new WebhookEntity();
      webhook.setUrl(request.getUrl());

      WebhookEntity savedWebhook = webhookRepository.save(webhook);

      return new WebhookResponse(savedWebhook.getId(), savedWebhook.getUrl());
   }

}
