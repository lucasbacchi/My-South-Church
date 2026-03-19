package com.southchurch.my.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateGroupCommand {

    private final String name;
    private final String email;
    private final String description;

}
