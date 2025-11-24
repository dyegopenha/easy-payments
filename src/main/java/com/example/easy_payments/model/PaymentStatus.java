package com.example.easy_payments.model;

public enum PaymentStatus {
   INITIATED,      // Payment requested but not yet processed
   PROCESSED,      // Successfully completed payment (final state)
   FAILED,         // Payment failed during processing (final state)
   REFUNDED,       // Payment was refunded (final state)
   CANCELLED       // Payment was manually cancelled before processing (final state)
}
