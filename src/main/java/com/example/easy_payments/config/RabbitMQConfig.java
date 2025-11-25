package com.example.easy_payments.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures RabbitMQ topology for resilient webhook processing using DLX.
 * This sets up the main queue, two retry queues for exponential backoff,
 * and a final Dead Letter Queue (DLQ).
 */
@Configuration
public class RabbitMQConfig {

   public static final String EXCHANGE_NAME = "payment-exchange";
   public static final String ROUTING_KEY_WEBHOOK = "payment.webhooks";

   // --- MAIN QUEUE AND EXCHANGE ---
   public static final String WEBHOOK_QUEUE = "payment.webhooks";
   public static final String DLQ_NAME = "payment.webhooks.dlq";

   // --- DELAY QUEUE/EXCHANGE CONFIG ---
   public static final String DELAY_EXCHANGE = "payment.webhooks.delay.exchange";
   private static final long DELAY_1S = 1000;
   private static final long DELAY_10S = 10000;

   /**
    * The main exchange where payment success messages are published.
    */
   @Bean
   public TopicExchange exchange() {
      return new TopicExchange(EXCHANGE_NAME);
   }

   /**
    * The main queue for processing webhooks.
    * If a message fails, it is sent to the DELAY_EXCHANGE via dead-lettering.
    */
   @Bean
   public Queue webhookQueue() {
      Map<String, Object> args = new HashMap<>();
      // Set the Dead Letter Exchange (DLX) to handle retries
      args.put("x-dead-letter-exchange", DELAY_EXCHANGE);
      return new Queue(WEBHOOK_QUEUE, true, false, false, args);
   }

   /**
    * Binds the main queue to the main exchange.
    */
   @Bean
   public Binding bindingWebhook(Queue webhookQueue, TopicExchange exchange) {
      return BindingBuilder.bind(webhookQueue).to(exchange).with(ROUTING_KEY_WEBHOOK);
   }

   // --- DLQ & RETRY TOPOLOGY ---

   /**
    * The final Dead Letter Queue where messages land after max retries.
    */
   @Bean
   public Queue deadLetterQueue() {
      return new Queue(DLQ_NAME, true);
   }

   /**
    * A direct exchange to manage the delayed messages.
    */
   @Bean
   public DirectExchange delayExchange() {
      return new DirectExchange(DELAY_EXCHANGE);
   }

   /**
    * Factory method to create a retry queue.
    * @param delayMillis The delay time in milliseconds.
    * @param routingKey The key used to route the message back to the main queue.
    * @return The configured Queue.
    */
   private Queue createRetryQueue(long delayMillis, String routingKey) {
      Map<String, Object> args = new HashMap<>();
      // Sets the time the message must wait in this queue
      args.put("x-message-ttl", delayMillis);
      // Routes the message back to the main exchange when TTL expires
      args.put("x-dead-letter-exchange", EXCHANGE_NAME);
      // The routing key to re-route the message back to the main queue
      args.put("x-dead-letter-routing-key", routingKey);
      return new Queue(WEBHOOK_QUEUE + ".retry." + delayMillis, true, false, false, args);
   }

   // Retry Queues for Exponential Backoff (1s, 10s)
   @Bean public Queue retryQueue1s() { return createRetryQueue(DELAY_1S, ROUTING_KEY_WEBHOOK); }
   @Bean public Queue retryQueue10s() { return createRetryQueue(DELAY_10S, ROUTING_KEY_WEBHOOK); }

   // Bindings for Retry Queues
   @Bean public Binding bindingRetry1s(Queue retryQueue1s) { return BindingBuilder.bind(retryQueue1s).to(delayExchange()).with("delay.1"); }
   @Bean public Binding bindingRetry10s(Queue retryQueue10s) { return BindingBuilder.bind(retryQueue10s).to(delayExchange()).with("delay.2"); }
}
