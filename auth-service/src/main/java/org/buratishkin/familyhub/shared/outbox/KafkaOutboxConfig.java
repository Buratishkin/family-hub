package org.buratishkin.familyhub.shared.outbox;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaOutboxConfig {
    @Bean
    public NewTopic domainEventsTopic(
            @Value("${family-hub.outbox.topic:familyhub.domain-events}") String topic
    ) {
        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
