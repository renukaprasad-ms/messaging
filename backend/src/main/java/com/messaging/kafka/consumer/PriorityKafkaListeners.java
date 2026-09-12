package com.messaging.kafka.consumer;

import com.messaging.kafka.config.KafkaProperties;
import com.messaging.kafka.event.KafkaEvent;
import com.messaging.kafka.idempotency.KafkaEventIdempotencyService;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PriorityKafkaListeners {

  private static final Logger LOGGER = LoggerFactory.getLogger(PriorityKafkaListeners.class);

  private final KafkaEventDispatcher dispatcher;
  private final KafkaEventIdempotencyService idempotencyService;
  private final KafkaProperties kafkaProperties;

  public PriorityKafkaListeners(
      KafkaEventDispatcher dispatcher,
      KafkaEventIdempotencyService idempotencyService,
      KafkaProperties kafkaProperties) {
    this.dispatcher = dispatcher;
    this.idempotencyService = idempotencyService;
    this.kafkaProperties = kafkaProperties;
  }

  @KafkaListener(
      id = "criticalKafkaListener",
      topics = "${app.kafka.topics.critical.name}",
      groupId = "${app.kafka.topics.critical.consumer-group}",
      containerFactory = "criticalKafkaListenerContainerFactory")
  public void critical(ConsumerRecord<String, KafkaEvent<?>> record, Acknowledgment acknowledgment) {
    process(record, acknowledgment, kafkaProperties.getTopics().getCritical().getConsumerGroup());
  }

  @KafkaListener(
      id = "highKafkaListener",
      topics = "${app.kafka.topics.high.name}",
      groupId = "${app.kafka.topics.high.consumer-group}",
      containerFactory = "highKafkaListenerContainerFactory")
  public void high(ConsumerRecord<String, KafkaEvent<?>> record, Acknowledgment acknowledgment) {
    process(record, acknowledgment, kafkaProperties.getTopics().getHigh().getConsumerGroup());
  }

  @KafkaListener(
      id = "mediumKafkaListener",
      topics = "${app.kafka.topics.medium.name}",
      groupId = "${app.kafka.topics.medium.consumer-group}",
      containerFactory = "mediumKafkaListenerContainerFactory")
  public void medium(ConsumerRecord<String, KafkaEvent<?>> record, Acknowledgment acknowledgment) {
    process(record, acknowledgment, kafkaProperties.getTopics().getMedium().getConsumerGroup());
  }

  @KafkaListener(
      id = "lowKafkaListener",
      topics = "${app.kafka.topics.low.name}",
      groupId = "${app.kafka.topics.low.consumer-group}",
      containerFactory = "lowKafkaListenerContainerFactory")
  public void low(ConsumerRecord<String, KafkaEvent<?>> record, Acknowledgment acknowledgment) {
    process(record, acknowledgment, kafkaProperties.getTopics().getLow().getConsumerGroup());
  }

  private void process(
      ConsumerRecord<String, KafkaEvent<?>> record, Acknowledgment acknowledgment, String consumerGroup) {
    long startedAt = System.nanoTime();
    KafkaEvent<?> event = record.value();

    if (event == null) {
      acknowledgment.acknowledge();
      return;
    }

    if (idempotencyService.isProcessed(event)) {
      LOGGER.info(
          "Skipping duplicate Kafka event eventId={} eventType={} priority={} topic={} partition={} offset={} consumerGroup={} correlationId={}",
          event.eventId(),
          event.eventType(),
          event.priority(),
          record.topic(),
          record.partition(),
          record.offset(),
          consumerGroup,
          event.correlationId());
      acknowledgment.acknowledge();
      return;
    }

    dispatcher.dispatch(event);
    idempotencyService.markProcessed(event);
    acknowledgment.acknowledge();

    long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    LOGGER.info(
        "Processed Kafka event eventId={} eventType={} priority={} topic={} partition={} offset={} consumerGroup={} companyId={} correlationId={} processingDurationMs={}",
        event.eventId(),
        event.eventType(),
        event.priority(),
        record.topic(),
        record.partition(),
        record.offset(),
        consumerGroup,
        event.companyId(),
        event.correlationId(),
        durationMs);
  }
}
