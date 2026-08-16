package org.buratishkin.familyhub.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class GatewayProxyController {
    private static final Set<String> SKIPPED_REQUEST_HEADERS = Set.of(
            "connection",
            "content-length",
            "expect",
            "host",
            "upgrade"
    );
    private static final Set<String> SKIPPED_RESPONSE_HEADERS = Set.of(
            "connection",
            "content-length",
            "transfer-encoding"
    );

    private final GatewayProperties properties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GatewayProxyController(GatewayProperties properties) {
        this.properties = properties;
    }

    @RequestMapping({"/task/**", "/task-recurrence-series/**"})
    public ResponseEntity<byte[]> task(HttpServletRequest request,
                                       @RequestHeader HttpHeaders headers,
                                       @RequestBody(required = false) byte[] body) {
        return proxy(properties.getTaskServiceBaseUrl(), "task-service", request, headers, body);
    }

    @RequestMapping("/auth/**")
    public ResponseEntity<byte[]> auth(HttpServletRequest request,
                                       @RequestHeader HttpHeaders headers,
                                       @RequestBody(required = false) byte[] body) {
        return proxy(properties.getAuthServiceBaseUrl(), "auth-service", request, headers, body);
    }

    @RequestMapping({"/address/**", "/category/**"})
    public ResponseEntity<byte[]> address(HttpServletRequest request,
                                          @RequestHeader HttpHeaders headers,
                                          @RequestBody(required = false) byte[] body) {
        return proxy(properties.getAddressServiceBaseUrl(), "address-service", request, headers, body);
    }

    @RequestMapping({
            "/family/**",
            "/invite/**",
            "/alias/**",
            "/me/families/{familyId}/polls",
            "/me/families/{familyId}/polls/**",
            "/me/families/{familyId}/plans",
            "/me/families/{familyId}/plans/**"
    })
    public ResponseEntity<byte[]> family(HttpServletRequest request,
                                         @RequestHeader HttpHeaders headers,
                                         @RequestBody(required = false) byte[] body) {
        return proxy(properties.getFamilyServiceBaseUrl(), "family-service", request, headers, body);
    }

    @RequestMapping({"/ingredient/**", "/recipe/**", "/meal-plan/**", "/food/**", "/me/families/{familyId}/food/**"})
    public ResponseEntity<byte[]> meal(HttpServletRequest request,
                                       @RequestHeader HttpHeaders headers,
                                       @RequestBody(required = false) byte[] body) {
        return proxy(properties.getMealServiceBaseUrl(), "meal-service", request, headers, body);
    }

    @RequestMapping({"/notifications/**", "/notification-devices/**"})
    public ResponseEntity<byte[]> notifications(HttpServletRequest request,
                                                @RequestHeader HttpHeaders headers,
                                                @RequestBody(required = false) byte[] body) {
        return proxy(properties.getNotificationServiceBaseUrl(), "notification-service", request, headers, body);
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> monolith(HttpServletRequest request,
                                           @RequestHeader HttpHeaders headers,
                                           @RequestBody(required = false) byte[] body) {
        return proxy(properties.getMonolithBaseUrl(), "monolith", request, headers, body);
    }

    private ResponseEntity<byte[]> proxy(String baseUrl,
                                         String targetService,
                                         HttpServletRequest request,
                                         HttpHeaders headers,
                                         byte[] body) {
        try {
            HttpRequest proxyRequest = buildProxyRequest(baseUrl, request, headers, body);
            HttpResponse<byte[]> proxyResponse = httpClient.send(proxyRequest, HttpResponse.BodyHandlers.ofByteArray());

            HttpHeaders responseHeaders = new HttpHeaders();
            for (Map.Entry<String, List<String>> header : proxyResponse.headers().map().entrySet()) {
                if (!SKIPPED_RESPONSE_HEADERS.contains(header.getKey().toLowerCase())) {
                    responseHeaders.put(header.getKey(), header.getValue());
                }
            }
            responseHeaders.add("X-FamilyHub-Gateway-Target", targetService);

            return ResponseEntity
                    .status(proxyResponse.statusCode())
                    .headers(responseHeaders)
                    .body(proxyResponse.body());
        } catch (IOException e) {
            return ResponseEntity.status(502).body(("Gateway proxy I/O error: " + e.getMessage()).getBytes());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(502).body("Gateway proxy interrupted".getBytes());
        }
    }

    private HttpRequest buildProxyRequest(String baseUrl,
                                          HttpServletRequest request,
                                          HttpHeaders headers,
                                          byte[] body) {
        String target = baseUrl + request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            target += "?" + request.getQueryString();
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(target));
        headers.forEach((name, values) -> {
            if (!SKIPPED_REQUEST_HEADERS.contains(name.toLowerCase())) {
                values.forEach(value -> builder.header(name, value));
            }
        });

        HttpRequest.BodyPublisher bodyPublisher = body == null || body.length == 0
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(body);
        return builder.method(HttpMethod.valueOf(request.getMethod()).name(), bodyPublisher).build();
    }
}
