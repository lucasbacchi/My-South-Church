package com.southchurch.my.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddMemberToGroupCommand {

    private String groupKey;
    private String memberEmail;
    private String role;

}
