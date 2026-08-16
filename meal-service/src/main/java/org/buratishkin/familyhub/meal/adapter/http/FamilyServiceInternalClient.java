package org.buratishkin.familyhub.meal.adapter.http;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class FamilyServiceInternalClient {
    private final MealFamilyServiceProperties familyServiceProperties;
    private final InternalServiceProperties internalServiceProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public UserView currentUser(String username) {
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        return get("/internal/families/current-user/" + encodedUsername, UserView.class);
    }

    public boolean hasFamilyAccess(Long userId, Long familyId) {
        return Boolean.TRUE.equals(get(
                "/internal/families/" + familyId + "/access/users/" + userId,
                Boolean.class
        ));
    }

    public MemberView findMemberByFamilyIdAndUserId(Long familyId, Long userId) {
        return get("/internal/members/families/" + familyId + "/users/" + userId, MemberView.class);
    }

    private <T> T get(String path, Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(familyServiceProperties.getFamilyServiceBaseUrl() + path))
                    .header(internalServiceProperties.getHeaderName(), internalServiceProperties.getToken())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Family service returned status " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            throw new IllegalStateException("Family service I/O error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Family service call interrupted", e);
        } catch (Exception e) {
            if (e instanceof IllegalStateException stateException) {
                throw stateException;
            }
            throw new IllegalStateException("Failed to call family service", e);
        }
    }

    public record MemberView(
            Long id,
            Long familyId,
            Long userId,
            String username,
            String role,
            String defaultName,
            LocalDateTime joinTime
    ) {
    }
}
