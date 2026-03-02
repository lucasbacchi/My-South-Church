package com.southchurch.my.dto.group;

import lombok.Getter;

@Getter
public class GroupAliasCommand {

    private String groupKey;
    private String alias;

    public GroupAliasCommand(String groupKey, String alias) {
        this.groupKey = groupKey;
        this.alias = alias;
    }
}
