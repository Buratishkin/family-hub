package org.buratishkin.familyhub.notification.push;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "family-hub.notification.push-provider", havingValue = "firebase", matchIfMissing = true)
public class FcmDevicePushSender implements DevicePushSender {
    private final FirebaseMessaging firebaseMessaging;

    @Override
    public DevicePushResult send(String deviceToken, DevicePushPayload payload) {
        try {
            String messageId = firebaseMessaging.send(toMessage(deviceToken, payload));
            return new DevicePushResult(messageId);
        } catch (FirebaseMessagingException e) {
            if (isInvalidToken(e)) {
                throw new InvalidDeviceTokenException("FCM rejected device token: " + errorDetails(e));
            }
            throw new IllegalStateException("FCM send failed: " + actionableErrorDetails(e), e);
        }
    }

    private Message toMessage(String deviceToken, DevicePushPayload payload) {
        return Message.builder()
                .setToken(deviceToken)
                .putAllData(data(payload))
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setCategory("OPEN_NOTIFICATION")
                                .build())
                        .build())
                .build();
    }

    Map<String, String> data(DevicePushPayload payload) {
        Map<String, String> data = new HashMap<>();
        put(data, "notificationId", payload.notificationId());
        put(data, "familyId", payload.familyId());
        put(data, "type", payload.type());
        put(data, "aggregateType", payload.aggregateType());
        put(data, "aggregateId", payload.aggregateId());
        put(data, "title", payload.title());
        put(data, "body", payload.body());
        if (payload.aggregateType() != null) {
            switch (payload.aggregateType()) {
                case "Task" -> put(data, "taskId", payload.aggregateId());
                case "Address" -> put(data, "addressId", payload.aggregateId());
                case "Category" -> put(data, "categoryId", payload.aggregateId());
                case "FoodRecipe", "Recipe" -> put(data, "recipeId", payload.aggregateId());
                case "Poll" -> put(data, "pollId", payload.aggregateId());
                case "FamilyPlan", "Plan" -> put(data, "planId", payload.aggregateId());
                case "Member" -> put(data, "memberId", payload.aggregateId());
                default -> {
                }
            }
        }
        return data;
    }

    private void put(Map<String, String> data, String key, Object value) {
        if (value != null) {
            data.put(key, value.toString());
        }
    }

    private boolean isInvalidToken(FirebaseMessagingException e) {
        return e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT;
    }

    private String errorDetails(FirebaseMessagingException e) {
        String code = e.getMessagingErrorCode() == null ? "UNKNOWN" : e.getMessagingErrorCode().name();
        String message = e.getMessage() == null ? "" : ": " + e.getMessage();
        return code + message;
    }

    private String actionableErrorDetails(FirebaseMessagingException e) {
        String details = errorDetails(e);
        if (details.contains("invalid_grant") || details.contains("Invalid JWT")) {
            return details + " (check server system clock/NTP and FIREBASE_CREDENTIALS_PATH service account)";
        }
        return details;
    }
}
