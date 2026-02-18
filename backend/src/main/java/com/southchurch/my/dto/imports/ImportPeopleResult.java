package com.southchurch.my.dto.imports;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ImportPeopleResult {
    private int total;
    private int created;
    private int updated;
    private int skipped;
    private int skippedExistingEmail;
    private int skippedDuplicateEmail;
    private int invalid;
    private int invalidMissingAttributes;
    private int invalidMissingRequired;
    private int invalidMissingEmail;
    private int invalidMissingFirstName;
    private int invalidMissingLastName;
    private List<String> issues = new ArrayList<>();
}
