package org.buratishkin.familyhub.meal.adapter.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component("mealInternalServiceProperties")
@ConfigurationProperties(prefix = "family-hub.security.internal")
public class InternalServiceProperties {
    private String headerName = "X-FamilyHub-Internal-Token";
    private String token;

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
