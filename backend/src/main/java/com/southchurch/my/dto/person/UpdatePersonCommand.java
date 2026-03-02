package com.southchurch.my.dto.person;

import java.util.UUID;

import lombok.Getter;

@Getter
public class UpdatePersonCommand {

    private UUID id;
    private PersonRequest request;

    public UpdatePersonCommand(UUID id, PersonRequest request) {
        this.id = id;
        this.request = request;
    }
}
