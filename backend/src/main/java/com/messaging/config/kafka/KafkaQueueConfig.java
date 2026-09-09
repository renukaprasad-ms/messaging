package com.messaging.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(name = "app.notification.async", havingValue = "true")
@EnableConfigurationProperties(KafkaQueueProperties.class)
public class KafkaQueueConfig {

  @Bean
  public KafkaAdmin.NewTopics deadLetterTopics(KafkaQueueProperties properties) {
    return new KafkaAdmin.NewTopics(
        deadLetterTopic(properties.critical()), deadLetterTopic(properties.high()),
        deadLetterTopic(properties.medium()), deadLetterTopic(properties.low()));
  }

  private NewTopic deadLetterTopic(QueueProperties queue) {
    return TopicBuilder.name(queue.topic() + ".DLT")
        .partitions(queue.partitions())
        .replicas(queue.replicas())
        .config("retention.ms", "86400000")
        .build();
  }

  @Bean
  public NewTopic criticalQueue(KafkaQueueProperties properties) {
    return topic(properties.critical());
  }

  @Bean
  public NewTopic highQueue(KafkaQueueProperties properties) {
    return topic(properties.high());
  }

  @Bean
  public NewTopic mediumQueue(KafkaQueueProperties properties) {
    return topic(properties.medium());
  }

  @Bean
  public NewTopic lowQueue(KafkaQueueProperties properties) {
    return topic(properties.low());
  }

  private NewTopic topic(QueueProperties queue) {
    return TopicBuilder.name(queue.topic())
        .partitions(queue.partitions())
        .replicas(queue.replicas())
        .build();
  }
}
