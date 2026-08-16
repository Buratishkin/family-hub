package org.buratishkin.familyhub.integration.task;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskServiceClient {
    private final TaskServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<TaskItem> findTasksByFamilyId(Long familyId, String authorizationHeader) {
        TaskItem[] tasks = exchange(
                "/internal/tasks/families/" + familyId,
                authorizationHeader,
                TaskItem[].class
        );
        return List.of(tasks);
    }

    public List<TaskItem> findOldTasksByFamilyId(Long familyId, String authorizationHeader) {
        TaskItem[] tasks = exchange(
                "/internal/tasks/families/" + familyId + "/old",
                authorizationHeader,
                TaskItem[].class
        );
        return List.of(tasks);
    }

    public int countByAssigneeMemberId(Long memberId, String authorizationHeader) {
        return exchange(
                "/internal/tasks/members/" + memberId + "/assignee-count",
                authorizationHeader,
                Integer.class
        );
    }

    public int countByCreatorMemberId(Long memberId, String authorizationHeader) {
        return exchange(
                "/internal/tasks/members/" + memberId + "/creator-count",
                authorizationHeader,
                Integer.class
        );
    }

    private <T> T exchange(String path, String authorizationHeader, Class<T> responseType) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl() + path))
                    .GET();
            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                builder.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Task service request failed with status " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            throw new IllegalStateException("Task service response cannot be read", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Task service request was interrupted", e);
        }
    }

    public record TaskItem(
            Long id,
            Long familyId,
            Long creatorMemberId,
            Long assigneeMemberId,
            Long addressId,
            String name,
            String description,
            LocalDateTime start,
            String type,
            String status,
            Long recurrenceSeriesId,
            Long recurrenceRootTaskId,
            Long recurrenceParentTaskId,
            Integer recurrenceIndex,
            TaskRecurrenceItem recurrence
    ) {
    }

    public record TaskRecurrenceItem(
            String status,
            String frequency,
            Integer interval,
            LocalDateTime repeatUntil,
            Integer maxOccurrences
    ) {
    }
}
