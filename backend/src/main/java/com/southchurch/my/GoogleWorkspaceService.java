package com.southchurch.my;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Groups;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class GoogleWorkspaceService {

    @Value("${google.admin.email}")
    private String adminEmail;

    // Spring injects the JSON string directly here
    @Value("${google.credentials.json}")
    private String credentialsJson;

    private static final String APPLICATION_NAME = "My South Church Backend";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public Directory getDirectoryService() throws IOException, GeneralSecurityException {
        // ERROR CHECK: Fail fast if the key is missing
        if (credentialsJson == null || credentialsJson.isEmpty()) {
            throw new IllegalStateException("GOOGLE_CREDENTIALS_JSON is missing! Check your environment variables.");
        }

        // Convert the JSON String into an InputStream
        InputStream credentialsStream = new ByteArrayInputStream(
                credentialsJson.getBytes(StandardCharsets.UTF_8));

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsStream)
                .createScoped(Collections.singletonList(DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY))
                .createDelegated(adminEmail);

        return new Directory.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Group> listGroups() throws IOException, GeneralSecurityException {
        Directory service = getDirectoryService();
        Groups result = service.groups().list().setCustomer("my_customer").execute();
        return result.getGroups();
    }
}