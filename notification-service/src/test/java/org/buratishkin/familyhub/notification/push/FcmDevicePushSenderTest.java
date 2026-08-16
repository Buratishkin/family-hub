package org.buratishkin.familyhub.notification.push;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FcmDevicePushSenderTest {
    @Test
    void pollCreatedDataPayloadContainsPollContractFields() {
        FcmDevicePushSender sender = new FcmDevicePushSender(null);
        DevicePushPayload payload = new DevicePushPayload(
                10L,
                1L,
                "POLL_CREATED",
                "Новый опрос: Что купить?",
                "Анна создала опрос",
                "Poll",
                "1001"
        );

        Map<String, String> data = sender.data(payload);

        assertThat(data)
                .containsEntry("notificationId", "10")
                .containsEntry("type", "POLL_CREATED")
                .containsEntry("familyId", "1")
                .containsEntry("pollId", "1001")
                .containsEntry("aggregateType", "Poll")
                .containsEntry("aggregateId", "1001")
                .containsEntry("title", "Новый опрос: Что купить?")
                .containsEntry("body", "Анна создала опрос");
    }

    @Test
    void planDataPayloadContainsPlanIdAlias() {
        FcmDevicePushSender sender = new FcmDevicePushSender(null);
        DevicePushPayload payload = new DevicePushPayload(
                11L,
                1L,
                "PLAN_CREATED",
                "Добавлена занятость",
                "Анна добавила занятость",
                "FamilyPlan",
                "2001"
        );

        Map<String, String> data = sender.data(payload);

        assertThat(data)
                .containsEntry("aggregateType", "FamilyPlan")
                .containsEntry("aggregateId", "2001")
                .containsEntry("planId", "2001");
    }
}
