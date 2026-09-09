package com.messaging.config.kafka;

import com.messaging.notification.dto.NotificationQueueMessage;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "app.notification.async", havingValue = "true")
public class KafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, NotificationQueueMessage> notificationConsumerFactory(
      KafkaProperties properties) {
    Map<String, Object> consumerProperties = properties.buildConsumerProperties();
    consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.messaging.notification.dto");
    consumerProperties.put(
        JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationQueueMessage.class.getName());

    return new DefaultKafkaConsumerFactory<>(consumerProperties);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, NotificationQueueMessage>
      kafkaListenerContainerFactory(
          ConsumerFactory<String, NotificationQueueMessage> notificationConsumerFactory,
          KafkaTemplate<String, NotificationQueueMessage> template) {
    ConcurrentKafkaListenerContainerFactory<String, NotificationQueueMessage> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(notificationConsumerFactory);
    var recoverer =
        new DeadLetterPublishingRecoverer(
            template,
            (record, error) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
    recoverer.setFailIfSendResultIsError(true);
    factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L)));

    return factory;
  }
}
