package org.buratishkin.familyhub.integration.family;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.alias.api.AliasLookupApi;
import org.buratishkin.familyhub.family.alias.api.AliasView;
import org.buratishkin.familyhub.family.api.FamilyAccessApi;
import org.buratishkin.familyhub.family.api.FamilyLookupApi;
import org.buratishkin.familyhub.family.api.FamilyView;
import org.buratishkin.familyhub.family.member.api.MemberLookupApi;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FamilyServiceClient implements FamilyAccessApi, FamilyLookupApi, MemberLookupApi, AliasLookupApi {
    private final FamilyServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public UserView currentUser(String username) {
        return exchange("/internal/families/current-user/" + encode(username), UserView.class);
    }

    @Override
    public boolean hasFamilyAccess(Long userId, Long familyId) {
        return exchange("/internal/families/" + familyId + "/access/users/" + userId, Boolean.class);
    }

    @Override
    public FamilyView findFamilyById(Long id) {
        return exchange("/internal/families/" + id, FamilyView.class);
    }

    @Override
    public boolean existsById(Long familyId) {
        return exchange("/internal/families/" + familyId + "/exists", Boolean.class);
    }

    @Override
    public MemberView findMemberById(Long memberId) {
        return exchange("/internal/members/" + memberId, MemberView.class);
    }

    @Override
    public MemberView findMemberByFamilyIdAndUserId(Long familyId, Long userId) {
        return exchange("/internal/members/families/" + familyId + "/users/" + userId, MemberView.class);
    }

    @Override
    public List<MemberView> findMembersByUserId(Long userId) {
        MemberView[] members = exchange("/internal/members/users/" + userId, MemberView[].class);
        return List.of(members);
    }

    @Override
    public List<MemberView> findMembersByFamilyId(Long familyId) {
        MemberView[] members = exchange("/internal/members/families/" + familyId, MemberView[].class);
        return List.of(members);
    }

    @Override
    public boolean existsByFamilyIdAndUserId(Long familyId, Long userId) {
        return exchange("/internal/members/families/" + familyId + "/users/" + userId + "/exists", Boolean.class);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return exchange("/internal/members/users/" + userId + "/exists", Boolean.class);
    }

    @Override
    public boolean existsByIdAndFamilyId(Long memberId, Long familyId) {
        return exchange("/internal/members/" + memberId + "/families/" + familyId + "/exists", Boolean.class);
    }

    @Override
    public List<AliasView> findAliasesByFamilyId(Long familyId) {
        AliasView[] aliases = exchange("/internal/aliases/families/" + familyId, AliasView[].class);
        return List.of(aliases);
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
                throw new IllegalStateException("Family service request failed with status " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            throw new IllegalStateException("Family service response cannot be read", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Family service request was interrupted", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
