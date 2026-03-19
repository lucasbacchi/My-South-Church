package com.southchurch.my.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.southchurch.my.services.drive.DriveAccessIndexService;

import jakarta.annotation.PostConstruct;

@Component
public class DriveIndexingTask {

    private static final Logger logger = LoggerFactory.getLogger(DriveIndexingTask.class);

    private final DriveAccessIndexService driveAccessIndexService;

    @Value("${drive.index.enabled:true}")
    private boolean enabled;

    @Value("${drive.index.refresh-on-startup:true}")
    private boolean refreshOnStartup;

    public DriveIndexingTask(DriveAccessIndexService driveAccessIndexService) {
        this.driveAccessIndexService = driveAccessIndexService;
    }

    @PostConstruct
    public void startupRefresh() {
        if (!enabled || !refreshOnStartup) {
            logger.info("[DriveIndexer] Startup refresh skipped (enabled={}, refreshOnStartup={})", enabled,
                    refreshOnStartup);
            return;
        }

        driveAccessIndexService.requestRebuildAsync("startup");
    }

    @Scheduled(fixedDelayString = "${drive.index.refresh-interval-ms:3600000}", initialDelayString = "${drive.index.initial-delay-ms:60000}")
    public void scheduledRefresh() {
        if (!enabled) {
            return;
        }

        driveAccessIndexService.rebuildIndex("scheduled");
    }
}