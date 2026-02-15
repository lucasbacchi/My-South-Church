package com.southchurch.my.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.DirectoryScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.cloudidentity.v1.CloudIdentity;
import com.google.api.services.cloudidentity.v1.CloudIdentityScopes;

@Configuration
public class GoogleDirectoryConfig {
        private static final String APPLICATION_NAME = "My South Church Backend";
        private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        private GoogleCredentials credentials;

        @Bean
        public Directory directoryClient() throws IOException, GeneralSecurityException {
                this.credentials = GoogleCredentials.getApplicationDefault();

                // ERROR CHECK: Fail fast if the key is missing
                if (credentials == null) {
                        throw new IllegalStateException(
                                        "GOOGLE_APPLICATION_CREDENTIALS is missing! Check your environment variables.");
                }

                GoogleCredentials scopedCredentials = credentials
                                .createScoped(List.of(
                                        DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY,
                                        DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER_READONLY
                                ));

                return new Directory.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                JSON_FACTORY,
                                new HttpCredentialsAdapter(scopedCredentials))
                                .setApplicationName(APPLICATION_NAME)
                                .build();
        }

        @Bean
        public CloudIdentity cloudIdentityClient() throws IOException, GeneralSecurityException {
                this.credentials = GoogleCredentials.getApplicationDefault();

                // ERROR CHECK: Fail fast if the key is missing
                if (credentials == null) {
                        throw new IllegalStateException(
                                        "GOOGLE_APPLICATION_CREDENTIALS is missing! Check your environment variables.");
                }

                GoogleCredentials scopedCredentials = credentials
                                .createScoped(Collections
                                                .singletonList(CloudIdentityScopes.CLOUD_IDENTITY_GROUPS_READONLY));

                CloudIdentity ciService = new CloudIdentity.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                JSON_FACTORY,
                                new HttpCredentialsAdapter(scopedCredentials))
                                .setApplicationName(APPLICATION_NAME)
                                .build();
                return ciService;
        }
}
