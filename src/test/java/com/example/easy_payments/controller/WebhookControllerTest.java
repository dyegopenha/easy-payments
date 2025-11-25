package com.example.easy_payments.controller;

import com.example.easy_payments.dto.request.RegisterWebhookRequest;
import com.example.easy_payments.dto.response.WebhookResponse;
import com.example.easy_payments.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

   @Mock
   private WebhookService webhookService;

   @InjectMocks
   private WebhookController webhookRegistrationController;

   private RegisterWebhookRequest validRequest;
   private WebhookResponse serviceResponse;

   @BeforeEach
   void setUp() {
      validRequest = new RegisterWebhookRequest();
      validRequest.setUrl("https://test.callback.com/hook");

      serviceResponse = new WebhookResponse(1L, validRequest.getUrl());
   }

   @Test
   void testRegisterWebhook_Success_Returns201() {
      // Arrange
      when(webhookService.createWebhook(any(RegisterWebhookRequest.class)))
            .thenReturn(serviceResponse);

      // Act
      ResponseEntity<WebhookResponse> response = webhookRegistrationController.createWebhook(validRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertEquals(serviceResponse, response.getBody());

      // Verify that the service was called exactly once with the request DTO
      verify(webhookService, times(1)).createWebhook(validRequest);
   }
}
