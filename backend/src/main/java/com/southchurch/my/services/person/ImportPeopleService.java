package com.southchurch.my.services.person;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.southchurch.my.dto.PersonRequest;
import com.southchurch.my.dto.imports.ImportPeopleResult;
import com.southchurch.my.dto.imports.ImportProfileAttributes;
import com.southchurch.my.dto.imports.ImportProfileRecord;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class ImportPeopleService {

    private static final int ISSUE_LIMIT = 50;

    private final PeopleRepository peopleRepository;

    public ImportPeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "peopleAllCache", allEntries = true),
            @CacheEvict(value = "personByEmailCache", allEntries = true),
            @CacheEvict(value = "personByFirebaseCache", allEntries = true)
    })
    public ResponseEntity<ImportPeopleResult> execute(List<ImportProfileRecord> records) {
        ImportPeopleResult result = new ImportPeopleResult();
        if (records == null || records.isEmpty()) {
            return ResponseEntity.ok(result);
        }

        result.setTotal(records.size());
        Set<String> seenEmails = new HashSet<>();

        for (ImportProfileRecord record : records) {
            if (record == null) {
                recordIssue(result, "Skipped empty record.");
                result.setInvalid(result.getInvalid() + 1);
                result.setInvalidMissingAttributes(result.getInvalidMissingAttributes() + 1);
                continue;
            }

            ImportProfileAttributes attributes = record.getAttributes();
            if (attributes == null) {
                recordIssue(result, "Skipped record without attributes.");
                result.setInvalid(result.getInvalid() + 1);
                result.setInvalidMissingAttributes(result.getInvalidMissingAttributes() + 1);
                continue;
            }

            String firstName = normalize(attributes.getFirstName());
            String lastName = normalize(attributes.getLastName());
            String primaryEmail = normalizeEmail(attributes.getEmail());
            String phoneNumber = normalizePhone(attributes.getPhone());

            boolean missingEmail = isBlank(primaryEmail);
            boolean missingFirstName = isBlank(firstName);
            boolean missingLastName = isBlank(lastName);

            if (missingEmail || missingFirstName || missingLastName) {
                recordIssue(result, "Skipped record missing required fields: " + safeId(record));
                result.setInvalid(result.getInvalid() + 1);
                result.setInvalidMissingRequired(result.getInvalidMissingRequired() + 1);
                if (missingEmail) {
                    result.setInvalidMissingEmail(result.getInvalidMissingEmail() + 1);
                }
                if (missingFirstName) {
                    result.setInvalidMissingFirstName(result.getInvalidMissingFirstName() + 1);
                }
                if (missingLastName) {
                    result.setInvalidMissingLastName(result.getInvalidMissingLastName() + 1);
                }
                continue;
            }

            if (seenEmails.contains(primaryEmail)) {
                recordIssue(result, "Skipped duplicate email in import file: " + primaryEmail);
                result.setSkipped(result.getSkipped() + 1);
                result.setSkippedDuplicateEmail(result.getSkippedDuplicateEmail() + 1);
                continue;
            }
            seenEmails.add(primaryEmail);

            if (peopleRepository.existsByPrimaryEmail(primaryEmail)) {
                recordIssue(result, "Skipped existing email: " + primaryEmail);
                result.setSkipped(result.getSkipped() + 1);
                result.setSkippedExistingEmail(result.getSkippedExistingEmail() + 1);
                continue;
            }

            PersonRequest request = new PersonRequest();
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setPrimaryEmail(primaryEmail);
            request.setSecondaryEmail(null);
            request.setPhoneNumber(phoneNumber);
            request.setDateOfBirth(null);
            request.setFirebaseUID(null);
            request.setRoles(Set.of());

            Person person = new Person(request);
            peopleRepository.save(person);
            result.setCreated(result.getCreated() + 1);
        }

        return ResponseEntity.ok(result);
    }

    private static String safeId(ImportProfileRecord record) {
        if (record.getId() != null && !record.getId().isBlank()) {
            return record.getId();
        }
        return "unknown";
    }

    private static void recordIssue(ImportPeopleResult result, String message) {
        if (result.getIssues().size() < ISSUE_LIMIT) {
            result.getIssues().add(message);
        }
    }

    private static String normalize(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizePhone(String value) {
        String trimmed = normalize(value);
        if (trimmed == null)
            return null;

        String digits = trimmed.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("1")) {
            digits = digits.substring(1);
        }

        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }

        return trimmed;
    }

    private static String normalizeEmail(String value) {
        String trimmed = normalize(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
