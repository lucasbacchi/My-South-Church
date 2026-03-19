package com.southchurch.my.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.services.groupssettings.model.Groups;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateGroupSettingsCommand {

    private String groupKey;
    private Groups settings;

    public UpdateGroupSettingsCommand(String groupKey, Groups settings) {
        this.groupKey = groupKey;
        this.settings = settings;
    }
}
