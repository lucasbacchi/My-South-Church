package com.southchurch.my.config;

import com.google.api.services.directory.Directory;
import com.google.api.services.drive.Drive;
import com.google.api.services.groupssettings.Groupssettings;
import com.google.api.services.cloudidentity.v1.CloudIdentity;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestGoogleConfig {

    @Bean
    public Directory directoryClient() {
        return mock(Directory.class);
    }

    @Bean
    public Drive driveClient() {
        return mock(Drive.class);
    }

    @Bean
    public Groupssettings groupsSettingsClient() {
        return mock(Groupssettings.class);
    }

    @Bean
    public CloudIdentity cloudIdentityClient() {
        return mock(CloudIdentity.class);
    }
}