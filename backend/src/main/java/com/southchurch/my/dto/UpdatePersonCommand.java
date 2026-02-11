package com.southchurch.my.dto;

import java.util.UUID;

import lombok.Getter;

@Getter
public class UpdatePersonCommand {

    private UUID id;
    private UpdatePersonRequest input;

    public UpdatePersonCommand(UUID id, UpdatePersonRequest input) {
        this.id = id;
        this.input = input;
    }
}
