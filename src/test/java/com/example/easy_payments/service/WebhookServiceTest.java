package com.example.easy_payments.service;

import com.example.easy_payments.dto.request.RegisterWebhookRequest;
import com.example.easy_payments.dto.response.WebhookResponse;
import com.example.easy_payments.model.WebhookEntity;
import com.example.easy_payments.repository.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class WebhookServiceTest {

   @Mock
   private WebhookRepository webhookRepository;

   private WebhookServiceImpl webhookService;

   @BeforeEach
   void setUp() {
      webhookService = new WebhookServiceImpl(webhookRepository);
   }

   @Test
   void testRegisterWebhook_Success() {
      // Arrange
      String testUrl = "https://test.service.com/notify";
      RegisterWebhookRequest request = new RegisterWebhookRequest();
      request.setUrl(testUrl);

      WebhookEntity savedEntity = new WebhookEntity();
      savedEntity.setId(5L);
      savedEntity.setUrl(testUrl);

      // Mock the repository save operation
      when(webhookRepository.save(any(WebhookEntity.class))).thenReturn(savedEntity);

      // Act
      WebhookResponse response = webhookService.createWebhook(request);

      // Assert
      ArgumentCaptor<WebhookEntity> entityCaptor = ArgumentCaptor.forClass(WebhookEntity.class);

      // Verify that the repository save method was called with the correct entity
      verify(webhookRepository).save(entityCaptor.capture());

      WebhookEntity capturedEntity = entityCaptor.getValue();
      assertEquals(testUrl, capturedEntity.getUrl());

      // Verify the response DTO
      assertNotNull(response);
      assertEquals(5L, response.getId());
      assertEquals(testUrl, response.getUrl());
   }
}
