package org.buratishkin.familyhub.task.adapter.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "family-hub.task")
public class TaskRemoteServiceProperties {
    private String addressServiceBaseUrl = "http://localhost:8083";
    private String familyServiceBaseUrl = "http://localhost:8084";

    public String getAddressServiceBaseUrl() {
        return addressServiceBaseUrl;
    }

    public void setAddressServiceBaseUrl(String addressServiceBaseUrl) {
        this.addressServiceBaseUrl = addressServiceBaseUrl;
    }

    public String getFamilyServiceBaseUrl() {
        return familyServiceBaseUrl;
    }

    public void setFamilyServiceBaseUrl(String familyServiceBaseUrl) {
        this.familyServiceBaseUrl = familyServiceBaseUrl;
    }
}
