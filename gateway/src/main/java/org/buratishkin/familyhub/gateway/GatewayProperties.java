package org.buratishkin.familyhub.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "family-hub.gateway")
public class GatewayProperties {
    private String monolithBaseUrl = "http://localhost:8080";
    private String taskServiceBaseUrl = "http://localhost:8081";
    private String addressServiceBaseUrl = "http://localhost:8083";
    private String familyServiceBaseUrl = "http://localhost:8084";
    private String authServiceBaseUrl = "http://localhost:8085";
    private String mealServiceBaseUrl = "http://localhost:8086";
    private String notificationServiceBaseUrl = "http://localhost:8087";
}
