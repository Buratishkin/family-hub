package org.buratishkin.familyhub.meal.plan.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "family-hub.meal")
public class MealTaskServiceProperties {
    private String taskServiceBaseUrl = "http://localhost:8081";

    public String getTaskServiceBaseUrl() {
        return taskServiceBaseUrl;
    }

    public void setTaskServiceBaseUrl(String taskServiceBaseUrl) {
        this.taskServiceBaseUrl = taskServiceBaseUrl;
    }
}
