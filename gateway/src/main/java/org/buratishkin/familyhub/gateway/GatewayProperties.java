package org.buratishkin.familyhub.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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

    public String getMonolithBaseUrl() {
        return monolithBaseUrl;
    }

    public void setMonolithBaseUrl(String monolithBaseUrl) {
        this.monolithBaseUrl = monolithBaseUrl;
    }

    public String getTaskServiceBaseUrl() {
        return taskServiceBaseUrl;
    }

    public void setTaskServiceBaseUrl(String taskServiceBaseUrl) {
        this.taskServiceBaseUrl = taskServiceBaseUrl;
    }

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

    public String getAuthServiceBaseUrl() {
        return authServiceBaseUrl;
    }

    public void setAuthServiceBaseUrl(String authServiceBaseUrl) {
        this.authServiceBaseUrl = authServiceBaseUrl;
    }

    public String getMealServiceBaseUrl() {
        return mealServiceBaseUrl;
    }

    public void setMealServiceBaseUrl(String mealServiceBaseUrl) {
        this.mealServiceBaseUrl = mealServiceBaseUrl;
    }

    public String getNotificationServiceBaseUrl() {
        return notificationServiceBaseUrl;
    }

    public void setNotificationServiceBaseUrl(String notificationServiceBaseUrl) {
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }
}
