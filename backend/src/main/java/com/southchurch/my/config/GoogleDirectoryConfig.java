package com.southchurch.my.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.DirectoryScopes;
import com.google.api.services.groupssettings.Groupssettings;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.cloudidentity.v1.CloudIdentity;
import com.google.api.services.cloudidentity.v1.CloudIdentityScopes;

@Configuration
@Profile("!test")
public class GoogleDirectoryConfig {
        private static final String APPLICATION_NAME = "My South Church Backend";
        private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        /**
         * Creates a Directory client using the application default credentials.
         * The client is used for interacting with the Google Workspace Admin SDK.
         * The client is created with the following scopes:
         *   ADMIN_DIRECTORY_GROUP_READONLY
         *   ADMIN_DIRECTORY_GROUP
         *   ADMIN_DIRECTORY_USER_READONLY
         *   ADMIN_DIRECTORY_GROUP_MEMBER
         *   ADMIN_DIRECTORY_GROUP_MEMBER_READONLY
         *
         * @return a Directory client
         * @throws IOException if there is an IO error
         * @throws GeneralSecurityException if there is a security error
         */
        @Bean
        public Directory directoryClient() throws IOException, GeneralSecurityException {
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

                // ERROR CHECK: Fail fast if the key is missing
                if (credentials == null) {
                        throw new IllegalStateException(
                                        "GOOGLE_APPLICATION_CREDENTIALS is missing! Check your environment variables.");
                }

                GoogleCredentials scopedCredentials = credentials
                                .createScoped(List.of(
                                                DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY,
                                                DirectoryScopes.ADMIN_DIRECTORY_GROUP,
                                                DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY,
                                                DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER,
                                                DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER_READONLY));

                return new Directory.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                JSON_FACTORY,
                                new HttpCredentialsAdapter(scopedCredentials))
                                .setApplicationName(APPLICATION_NAME)
                                .build();
        }

        /**
         * Creates a Cloud Identity client using the application default credentials.
         *
         * This client is used to make API calls to the Cloud Identity service.
         *
         * @return a CloudIdentity client
         * @throws IOException if there is an IO error
         * @throws GeneralSecurityException if there is a security error
         */
        @Bean
        public CloudIdentity cloudIdentityClient() throws IOException, GeneralSecurityException {
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

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

        /**
         * Builds a Groupssettings client using the application default credentials.
         *
         * @return a Groupssettings client
         * @throws IOException if there is an IO error
         * @throws GeneralSecurityException if there is a security error
         */
        @Bean
        public Groupssettings groupsSettingsClient() throws IOException, GeneralSecurityException {
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

                // ERROR CHECK: Fail fast if the key is missing
                if (credentials == null) {
                        throw new IllegalStateException(
                                        "GOOGLE_APPLICATION_CREDENTIALS is missing! Check your environment variables.");
                }

                GoogleCredentials scopedCredentials = credentials.createScoped(
                                List.of("https://www.googleapis.com/auth/apps.groups.settings"));

                return new Groupssettings.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                JSON_FACTORY,
                                new HttpCredentialsAdapter(scopedCredentials))
                                .setApplicationName(APPLICATION_NAME)
                                .build();
        }
}
