package org.buratishkin.familyhub.meal.plan.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TaskServiceClient {
    private final MealTaskServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Long createShoppingTask(TaskCreateReq req, String authorizationHeader) {
        try {
            String body = objectMapper.writeValueAsString(req);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getTaskServiceBaseUrl() + "/task/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                builder.header("Authorization", authorizationHeader);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Task service returned status " + response.statusCode());
            }

            TaskCreateResponse createResponse = objectMapper.readValue(response.body(), TaskCreateResponse.class);
            if (!createResponse.result() || createResponse.createResp() == null) {
                throw new IllegalStateException("Task service rejected shopping task: " + createResponse.reason());
            }
            return createResponse.createResp().taskId();
        } catch (IOException e) {
            throw new IllegalStateException("Task service I/O error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Task service call interrupted", e);
        } catch (Exception e) {
            if (e instanceof IllegalStateException stateException) {
                throw stateException;
            }
            throw new IllegalStateException("Failed to create shopping task", e);
        }
    }

    public record TaskCreateReq(
            Long familyId,
            String name,
            Long assigneeId,
            Long addressId,
            LocalDateTime start,
            String type,
            String description
    ) {
    }

    public record TaskCreateResponse(boolean result, String reason, TaskCreateBody createResp) {
    }

    public record TaskCreateBody(Long taskId) {
    }
}
