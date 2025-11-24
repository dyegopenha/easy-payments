package com.example.easy_payments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterWebhookRequest {

   @NotBlank(message = "URL is required")
   @Pattern(regexp = "^(http|https)://.*$", message = "URL must be a valid HTTP or HTTPS endpoint")
   private String url;
}
