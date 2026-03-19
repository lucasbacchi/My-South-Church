package com.southchurch.my.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Configuration
public class GoogleDriveConfig {
    private static final String APPLICATION_NAME = "My South Church Backend";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public Drive driveClient() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

        // ERROR CHECK: Fail fast if the key is missing
        if (credentials == null) {
            throw new IllegalStateException(
                    "GOOGLE_APPLICATION_CREDENTIALS is missing! Check your environment variables.");
        }

        GoogleCredentials scopedCredentials = credentials
                .createScoped(Collections.singletonList(DriveScopes.DRIVE));

        HttpCredentialsAdapter credentialsAdapter = new HttpCredentialsAdapter(scopedCredentials);
        HttpRequestInitializer requestInitializer = request -> {
            credentialsAdapter.initialize(request);
            request.setConnectTimeout(20_000);
            request.setReadTimeout(120_000);
        };

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
            requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
