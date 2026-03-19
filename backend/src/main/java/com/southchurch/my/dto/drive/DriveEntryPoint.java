package com.southchurch.my.dto.drive;

import java.util.List;

import lombok.Getter;

@Getter
public class DriveEntryPoint {

    private final String id;
    private final String name;
    private final String mimeType;
    private final boolean folder;
    private final String webViewLink;
    private final String driveId;
    private final List<String> parentIds;

    public DriveEntryPoint(String id, String name, String mimeType, boolean folder, String webViewLink, String driveId,
            List<String> parentIds) {
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
        this.folder = folder;
        this.webViewLink = webViewLink;
        this.driveId = driveId;
        this.parentIds = parentIds;
    }
}