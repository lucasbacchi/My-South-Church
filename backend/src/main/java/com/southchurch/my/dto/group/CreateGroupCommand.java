package com.southchurch.my.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateGroupCommand {

    private String name;
    private String email;
    private String description;

}
