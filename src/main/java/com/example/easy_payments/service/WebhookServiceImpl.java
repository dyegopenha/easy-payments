package com.example.easy_payments.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.easy_payments.dto.request.RegisterWebhookRequest;
import com.example.easy_payments.dto.response.WebhookResponse;
import com.example.easy_payments.exceptions.PaymentConflictException;
import com.example.easy_payments.exceptions.WebhookConflictException;
import com.example.easy_payments.model.WebhookEntity;
import com.example.easy_payments.repository.WebhookRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebhookServiceImpl implements IWebhookService {

   private final WebhookRepository webhookRepository;

   public WebhookServiceImpl(WebhookRepository webhookRepository) {
      this.webhookRepository = webhookRepository;
   }

   @Transactional
   @Override
   public WebhookResponse createWebhook(RegisterWebhookRequest request) {
      validateWebhook(request.getUrl());

      WebhookEntity webhook = new WebhookEntity();
      webhook.setUrl(request.getUrl());
      WebhookEntity savedWebhook = webhookRepository.save(webhook);

      return new WebhookResponse(savedWebhook.getId(), savedWebhook.getUrl());
   }

   private void validateWebhook(String url) {
      if (webhookRepository.findByUrl(url).isPresent()) {
         throw new WebhookConflictException(url);
      }
   }

}
