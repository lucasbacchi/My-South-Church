package com.southchurch.my.dto.group;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
