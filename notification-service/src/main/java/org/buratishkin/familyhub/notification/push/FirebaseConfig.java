package org.buratishkin.familyhub.notification.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "family-hub.notification.push-provider", havingValue = "firebase", matchIfMissing = true)
public class FirebaseConfig {
    private final FirebaseProperties properties;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        Path credentialsPath = requireCredentialsPath();
        try (FileInputStream credentialsStream = new FileInputStream(credentialsPath.toFile())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            }
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private Path requireCredentialsPath() {
        String configuredPath = properties.getCredentialsPath();
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalStateException("FIREBASE_CREDENTIALS_PATH is required for notification-service");
        }

        Path path = Path.of(configuredPath);
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Firebase credentials file was not found: " + path);
        }
        return path;
    }
}
