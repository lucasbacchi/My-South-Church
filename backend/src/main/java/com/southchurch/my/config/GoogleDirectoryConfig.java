package com.southchurch.my.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.DirectoryScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Configuration
public class GoogleDirectoryConfig {
    
    @Value("${google.credentials.json}")
    private String credentialsJson;

    private static final String APPLICATION_NAME = "My South Church Backend";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public Directory directoryClient() throws Exception {
        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new IllegalStateException("google.credentials.json is missing");
        }
       
        InputStream credentialsStream = new ByteArrayInputStream(
            credentialsJson.getBytes(StandardCharsets.UTF_8));

        GoogleCredentials creds = GoogleCredentials
            .fromStream(credentialsStream)
            .createScoped(List.of(DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY));

        return new Directory.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            new HttpCredentialsAdapter(creds)
        )
        .setApplicationName(APPLICATION_NAME)
        .build();
    }
}
