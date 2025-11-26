package com.example.easy_payments.service;

import com.example.easy_payments.dto.request.RegisterWebhookRequest;
import com.example.easy_payments.dto.response.WebhookResponse;

public interface IWebhookService {

   public WebhookResponse createWebhook(RegisterWebhookRequest request);
}
