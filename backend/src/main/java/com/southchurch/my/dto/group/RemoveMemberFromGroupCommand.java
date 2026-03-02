package com.southchurch.my.dto.group;

import lombok.Getter;

@Getter
public class RemoveMemberFromGroupCommand {

    private String groupKey;
    private String memberEmail;

    public RemoveMemberFromGroupCommand(String groupKey, String memberEmail) {
        this.groupKey = groupKey;
        this.memberEmail = memberEmail;
    }
}
