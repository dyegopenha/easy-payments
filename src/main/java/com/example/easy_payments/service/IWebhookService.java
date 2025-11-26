package com.example.easy_payments.service;

import java.util.List;

import com.example.easy_payments.dto.request.RegisterWebhookRequest;
import com.example.easy_payments.dto.response.WebhookResponse;

public interface IWebhookService {

   WebhookResponse createWebhook(RegisterWebhookRequest request);

   List<WebhookResponse> getAllWebhooks();

   void deleteWebhookById(Long id);
}
