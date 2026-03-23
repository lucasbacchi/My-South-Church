package com.southchurch.my.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

/**
 * Initializes Firebase Admin SDK on application startup.
 * Uses the same Google service account credentials as other Google services.
 */
@Configuration
@Profile("!test") // skip the entire config during tests
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("[Firebase] Firebase Admin SDK initialized successfully");
            } else {
                logger.info("[Firebase] Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            logger.error("[Firebase] Failed to initialize Firebase Admin SDK: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize Firebase Admin SDK", e);
        }
    }
}
