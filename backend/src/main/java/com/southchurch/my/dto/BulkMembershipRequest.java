package com.southchurch.my.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class BulkMembershipRequest {

    private List<String> memberEmails;
    private List<String> groupKeys;
    private String role;

    public BulkMembershipRequest(List<String> memberEmails, List<String> groupKeys, String role) {
        this.memberEmails = memberEmails;
        this.groupKeys = groupKeys;
        this.role = role;
    }
}
