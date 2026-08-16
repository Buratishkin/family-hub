package org.buratishkin.familyhub.integration.address;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.api.AddressLookupApi;
import org.buratishkin.familyhub.address.api.AddressView;
import org.buratishkin.familyhub.address.category.api.CategoryLookupApi;
import org.buratishkin.familyhub.address.category.api.CategoryView;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AddressServiceClient implements AddressLookupApi, CategoryLookupApi {
    private final AddressServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public AddressView findAddressById(Long id) {
        return exchange("/internal/addresses/" + id, AddressView.class);
    }

    @Override
    public List<AddressView> findAddressesByFamilyId(Long familyId) {
        AddressView[] addresses = exchange("/internal/addresses/families/" + familyId, AddressView[].class);
        return List.of(addresses);
    }

    @Override
    public CategoryView findCategoryById(Long id) {
        return exchange("/internal/categories/" + id, CategoryView.class);
    }

    @Override
    public boolean existsById(Long id) {
        return exchange("/internal/categories/" + id + "/exists", Boolean.class);
    }

    @Override
    public List<CategoryView> findCategoriesByFamilyId(Long familyId) {
        CategoryView[] categories = exchange("/internal/categories/families/" + familyId, CategoryView[].class);
        return List.of(categories);
    }

    private <T> T exchange(String path, Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl() + path))
                    .header(properties.getInternalHeaderName(), properties.getInternalToken())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Address service request failed with status " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            throw new IllegalStateException("Address service response cannot be read", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Address service request was interrupted", e);
        }
    }
}
