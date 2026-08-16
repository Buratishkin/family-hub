package org.buratishkin.familyhub.shared.outbox;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.buratishkin.familyhub.shared.event.DomainEventEnvelope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class KafkaOutboxEventPublisher {
    private static final int SCHEMA_VERSION = 1;
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${family-hub.outbox.topic:familyhub.domain-events}")
    private String topic;

    public void publish(OutboxEventEntity event) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                eventKey(event),
                toKafkaPayload(event)
        );
        addHeader(record, "event-id", event.getEventId().toString());
        addHeader(record, "event-type", event.getEventType());
        addHeader(record, "aggregate-type", event.getAggregateType());
        addHeader(record, "aggregate-id", event.getAggregateId());
        addHeader(record, "occurred-at", event.getOccurredAt().toString());

        kafkaTemplate.send(record).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private String toKafkaPayload(OutboxEventEntity event) throws Exception {
        DomainEventEnvelope envelope = new DomainEventEnvelope(
                SCHEMA_VERSION,
                event.getEventId(),
                event.getEventType(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getOccurredAt(),
                event.getPayload()
        );
        return objectMapper.writeValueAsString(envelope);
    }

    private String eventKey(OutboxEventEntity event) {
        return event.getAggregateType() + ":" + event.getAggregateId();
    }

    private void addHeader(ProducerRecord<String, String> record, String name, String value) {
        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
    }
}
