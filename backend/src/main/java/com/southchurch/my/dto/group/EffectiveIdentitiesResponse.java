package com.southchurch.my.dto.group;

import java.util.List;

import lombok.Getter;

@Getter
public class EffectiveIdentitiesResponse {

    private final String subjectEmail;
    private final List<String> identities;

    public EffectiveIdentitiesResponse(String subjectEmail, List<String> identities) {
        this.subjectEmail = subjectEmail;
        this.identities = identities;
    }
}