package org.buratishkin.familyhub.meal.adapter.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "family-hub.meal")
public class MealFamilyServiceProperties {
    private String familyServiceBaseUrl = "http://localhost:8084";

    public String getFamilyServiceBaseUrl() {
        return familyServiceBaseUrl;
    }

    public void setFamilyServiceBaseUrl(String familyServiceBaseUrl) {
        this.familyServiceBaseUrl = familyServiceBaseUrl;
    }
}
