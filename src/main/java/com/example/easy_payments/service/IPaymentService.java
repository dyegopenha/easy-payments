package com.example.easy_payments.service;

import com.example.easy_payments.dto.request.CreatePaymentRequest;
import com.example.easy_payments.dto.response.PaymentResponse;

public interface IPaymentService {

   public PaymentResponse createPayment(CreatePaymentRequest request);

}
