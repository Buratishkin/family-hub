package org.buratishkin.familyhub.notification.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.buratishkin.familyhub.notification.dto.CreateNotificationCommand;
import org.buratishkin.familyhub.notification.projection.NotificationProjectionService;
import org.buratishkin.familyhub.notification.service.NotificationService;
import org.buratishkin.familyhub.shared.event.DomainEventEnvelope;
import org.buratishkin.familyhub.shared.inbox.ProcessedEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDomainEventConsumerTest {
    private static final String POLL_CREATED_EVENT =
            "org.buratishkin.familyhub.family.poll.api.event.PollCreatedEvent";
    private static final String FAMILY_PLAN_CREATED_EVENT =
            "org.buratishkin.familyhub.family.plan.api.event.FamilyPlanCreatedEvent";
    private static final String TASK_REMINDER_DUE_EVENT =
            "org.buratishkin.familyhub.task.api.event.TaskReminderDueEvent";

    @Mock
    private ProcessedEventService processedEventService;

    @Mock
    private NotificationProjectionService projectionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void createsPollNotificationWithPollNavigationMetadata() throws Exception {
        UUID eventId = UUID.randomUUID();
        String envelopeJson = "envelope-json";
        String payloadJson = "payload-json";
        DomainEventEnvelope envelope = new DomainEventEnvelope(
                1,
                eventId,
                POLL_CREATED_EVENT,
                "Poll",
                "1001",
                LocalDateTime.parse("2026-08-04T14:50:00"),
                payloadJson
        );
        NotificationDomainEventConsumer.PollCreatedPayload payload =
                new NotificationDomainEventConsumer.PollCreatedPayload(
                        1001L,
                        1L,
                        77L,
                        5L,
                        "Что купить?",
                        "Анна",
                        LocalDateTime.parse("2026-08-04T14:50:00")
                );

        when(processedEventService.processOnce(any(), any())).thenAnswer(invocation -> {
            Runnable handler = invocation.getArgument(1);
            handler.run();
            return true;
        });
        when(objectMapper.readValue(envelopeJson, DomainEventEnvelope.class)).thenReturn(envelope);
        when(objectMapper.readValue(payloadJson, NotificationDomainEventConsumer.PollCreatedPayload.class))
                .thenReturn(payload);
        when(projectionService.findActiveUserIdsByFamilyId(1L))
                .thenReturn(new LinkedHashSet<>(java.util.List.of(5L, 6L, 7L)));

        NotificationDomainEventConsumer consumer = new NotificationDomainEventConsumer(
                processedEventService,
                projectionService,
                notificationService,
                objectMapper
        );
        consumer.consume(new ConsumerRecord<>("familyhub.domain-events", 0, 0L, "key", envelopeJson));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> recipientsCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<CreateNotificationCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationService).createForUsers(recipientsCaptor.capture(), commandCaptor.capture());

        assertThat(recipientsCaptor.getValue()).containsExactly(6L, 7L);
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.type()).isEqualTo("POLL_CREATED");
        assertThat(command.familyId()).isEqualTo(1L);
        assertThat(command.title()).isEqualTo("Новый опрос: Что купить?");
        assertThat(command.body()).isEqualTo("Анна создала опрос");
        assertThat(command.aggregateType()).isEqualTo("Poll");
        assertThat(command.aggregateId()).isEqualTo("1001");
    }

    @Test
    void createsPlanNotificationForFamilyExceptActor() throws Exception {
        UUID eventId = UUID.randomUUID();
        String envelopeJson = "plan-envelope-json";
        String payloadJson = "plan-payload-json";
        DomainEventEnvelope envelope = new DomainEventEnvelope(
                1,
                eventId,
                FAMILY_PLAN_CREATED_EVENT,
                "FamilyPlan",
                "2001",
                LocalDateTime.parse("2026-08-12T12:00:00"),
                payloadJson
        );
        NotificationDomainEventConsumer.FamilyPlanChangedPayload payload =
                new NotificationDomainEventConsumer.FamilyPlanChangedPayload(
                        2001L,
                        1L,
                        77L,
                        5L,
                        "English lesson",
                        "Анна",
                        LocalDateTime.parse("2026-08-12T18:00:00"),
                        LocalDateTime.parse("2026-08-12T19:30:00"),
                        LocalDateTime.parse("2026-08-12T12:00:00")
                );

        when(processedEventService.processOnce(any(), any())).thenAnswer(invocation -> {
            Runnable handler = invocation.getArgument(1);
            handler.run();
            return true;
        });
        when(objectMapper.readValue(envelopeJson, DomainEventEnvelope.class)).thenReturn(envelope);
        when(objectMapper.readValue(payloadJson, NotificationDomainEventConsumer.FamilyPlanChangedPayload.class))
                .thenReturn(payload);
        when(projectionService.findActiveUserIdsByFamilyId(1L))
                .thenReturn(new LinkedHashSet<>(java.util.List.of(5L, 6L)));

        NotificationDomainEventConsumer consumer = new NotificationDomainEventConsumer(
                processedEventService,
                projectionService,
                notificationService,
                objectMapper
        );
        consumer.consume(new ConsumerRecord<>("familyhub.domain-events", 0, 0L, "key", envelopeJson));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> recipientsCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<CreateNotificationCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationService).createForUsers(recipientsCaptor.capture(), commandCaptor.capture());

        assertThat(recipientsCaptor.getValue()).containsExactly(6L);
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.type()).isEqualTo("PLAN_CREATED");
        assertThat(command.title()).isEqualTo("Добавлена занятость: English lesson");
        assertThat(command.body()).isEqualTo("Анна добавила занятость: \"English lesson\"");
        assertThat(command.aggregateType()).isEqualTo("FamilyPlan");
        assertThat(command.aggregateId()).isEqualTo("2001");
    }

    @Test
    void createsTaskReminderForAssignee() throws Exception {
        UUID eventId = UUID.randomUUID();
        String envelopeJson = "task-reminder-envelope-json";
        String payloadJson = "task-reminder-payload-json";
        DomainEventEnvelope envelope = new DomainEventEnvelope(
                1,
                eventId,
                TASK_REMINDER_DUE_EVENT,
                "Task",
                "3001",
                LocalDateTime.parse("2026-08-12T16:00:00"),
                payloadJson
        );
        NotificationDomainEventConsumer.TaskReminderDuePayload payload =
                new NotificationDomainEventConsumer.TaskReminderDuePayload(
                        9001L,
                        3001L,
                        1L,
                        77L,
                        88L,
                        "Buy milk",
                        LocalDateTime.parse("2026-08-12T18:00:00"),
                        120,
                        LocalDateTime.parse("2026-08-12T16:00:00")
                );

        when(processedEventService.processOnce(any(), any())).thenAnswer(invocation -> {
            Runnable handler = invocation.getArgument(1);
            handler.run();
            return true;
        });
        when(objectMapper.readValue(envelopeJson, DomainEventEnvelope.class)).thenReturn(envelope);
        when(objectMapper.readValue(payloadJson, NotificationDomainEventConsumer.TaskReminderDuePayload.class))
                .thenReturn(payload);
        when(projectionService.findUserIdByMemberId(88L)).thenReturn(java.util.Optional.of(6L));

        NotificationDomainEventConsumer consumer = new NotificationDomainEventConsumer(
                processedEventService,
                projectionService,
                notificationService,
                objectMapper
        );
        consumer.consume(new ConsumerRecord<>("familyhub.domain-events", 0, 0L, "key", envelopeJson));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> recipientsCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<CreateNotificationCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationService).createForUsers(recipientsCaptor.capture(), commandCaptor.capture());

        assertThat(recipientsCaptor.getValue()).containsExactly(6L);
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.type()).isEqualTo("TASK_REMINDER_120M");
        assertThat(command.title()).isEqualTo("Задача через 120 мин: Buy milk");
        assertThat(command.body()).isEqualTo("Задача \"Buy milk\" начнется через 120 мин");
        assertThat(command.aggregateType()).isEqualTo("Task");
        assertThat(command.aggregateId()).isEqualTo("3001");
    }
}
