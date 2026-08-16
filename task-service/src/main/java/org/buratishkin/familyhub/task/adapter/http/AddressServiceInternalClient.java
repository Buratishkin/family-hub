package org.buratishkin.familyhub.task.adapter.http;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.api.AddressView;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@RequiredArgsConstructor
public class AddressServiceInternalClient {
    private final TaskRemoteServiceProperties remoteServiceProperties;
    private final InternalServiceProperties internalServiceProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AddressView findAddressById(Long addressId) {
        return get("/internal/addresses/" + addressId, AddressView.class);
    }

    private <T> T get(String path, Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(remoteServiceProperties.getAddressServiceBaseUrl() + path))
                    .header(internalServiceProperties.getHeaderName(), internalServiceProperties.getToken())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Address service returned status " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            throw new IllegalStateException("Address service I/O error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Address service call interrupted", e);
        } catch (Exception e) {
            if (e instanceof IllegalStateException stateException) {
                throw stateException;
            }
            throw new IllegalStateException("Failed to call address service", e);
        }
    }
}
