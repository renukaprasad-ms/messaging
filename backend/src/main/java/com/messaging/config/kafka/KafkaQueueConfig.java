package com.messaging.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaQueueProperties.class)
public class KafkaQueueConfig {

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
