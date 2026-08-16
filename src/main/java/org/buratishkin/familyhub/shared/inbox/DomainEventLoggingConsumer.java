package org.buratishkin.familyhub.shared.inbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.buratishkin.familyhub.shared.event.DomainEventEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventLoggingConsumer {
    private final ProcessedEventService processedEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${family-hub.outbox.topic:familyhub.domain-events}",
            groupId = "${family-hub.kafka.consumer.domain-events.group-id:familyhub-domain-event-logger}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        KafkaDomainEventMessage message = toMessage(record);
        boolean processed = processedEventService.processOnce(message, () -> log.info(
                "Consumed domain event: eventId={}, eventType={}, aggregateType={}, aggregateId={}",
                message.eventId(),
                message.eventType(),
                message.aggregateType(),
                message.aggregateId()
        ));

        if (!processed) {
            log.info("Skipped already processed domain event: eventId={}", message.eventId());
        }
    }

    private KafkaDomainEventMessage toMessage(ConsumerRecord<String, String> record) {
        DomainEventEnvelope envelope = readEnvelope(record.value());
        if (envelope != null) {
            return new KafkaDomainEventMessage(
                    envelope.eventId(),
                    envelope.eventType(),
                    envelope.aggregateType(),
                    envelope.aggregateId(),
                    envelope.payload()
            );
        }

        return new KafkaDomainEventMessage(
                UUID.fromString(requiredHeader(record, "event-id")),
                requiredHeader(record, "event-type"),
                requiredHeader(record, "aggregate-type"),
                requiredHeader(record, "aggregate-id"),
                record.value()
        );
    }

    private DomainEventEnvelope readEnvelope(String value) {
        try {
            return objectMapper.readValue(value, DomainEventEnvelope.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String requiredHeader(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        if (header == null) {
            throw new IllegalArgumentException("Kafka domain event is missing required header: " + name);
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
