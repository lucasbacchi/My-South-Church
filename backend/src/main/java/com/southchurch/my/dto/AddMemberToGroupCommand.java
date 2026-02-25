package com.southchurch.my.dto;

import lombok.Getter;

@Getter
public class AddMemberToGroupCommand {

    private String groupKey;
    private String memberEmail;
    private String role;

    public AddMemberToGroupCommand(String groupKey, String memberEmail, String role) {
        this.groupKey = groupKey;
        this.memberEmail = memberEmail;
        this.role = role;
    }
}
