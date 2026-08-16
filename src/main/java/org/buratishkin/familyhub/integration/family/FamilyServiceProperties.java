package org.buratishkin.familyhub.integration.family;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "family-hub.family-service")
public class FamilyServiceProperties {
    private String baseUrl = "http://localhost:8084";
    private String internalHeaderName = "X-FamilyHub-Internal-Token";
    private String internalToken;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getInternalHeaderName() {
        return internalHeaderName;
    }

    public void setInternalHeaderName(String internalHeaderName) {
        this.internalHeaderName = internalHeaderName;
    }

    public String getInternalToken() {
        return internalToken;
    }

    public void setInternalToken(String internalToken) {
        this.internalToken = internalToken;
    }
}
