package com.example.easy_payments.service;

import java.util.List;
import java.util.Optional;

import com.example.easy_payments.dto.request.CreatePaymentRequest;
import com.example.easy_payments.dto.response.PaymentResponse;

public interface IPaymentService {

   PaymentResponse createPayment(CreatePaymentRequest request);

   List<PaymentResponse> getAllPayments();

   Optional<PaymentResponse> findByExternalId(String externalId);
}
