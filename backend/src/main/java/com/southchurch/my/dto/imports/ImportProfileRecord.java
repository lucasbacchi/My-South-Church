package com.southchurch.my.dto.imports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportProfileRecord {
    private String id;
    private String type;
    private ImportProfileAttributes attributes;
}
