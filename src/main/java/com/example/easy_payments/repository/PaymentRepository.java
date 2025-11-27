package com.example.easy_payments.repository;

import com.example.easy_payments.dto.response.PaymentResponse;
import com.example.easy_payments.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

   Optional<PaymentEntity> findByExternalId(String externalId);

   @Query("""
         SELECT new com.example.easy_payments.dto.response.PaymentResponse(p.id, p.externalId, p.status)
         FROM PaymentEntity p
         """)
   List<PaymentResponse> findAllPayments();
}
