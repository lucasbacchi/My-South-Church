package com.southchurch.my.services.person;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.southchurch.my.dto.PersonRequest;
import com.southchurch.my.dto.UpdatePersonCommand;
import com.southchurch.my.dto.imports.ImportPeopleResult;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class ImportPeopleService {

    private static final int ISSUE_LIMIT = 50;

    private final PeopleRepository peopleRepository;
    private final CreatePersonService createPersonService;
    private final UpdatePersonService updatePersonService;

    public ImportPeopleService(
            PeopleRepository peopleRepository,
            CreatePersonService createPersonService,
            UpdatePersonService updatePersonService) {
        this.peopleRepository = peopleRepository;
        this.createPersonService = createPersonService;
        this.updatePersonService = updatePersonService;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "peopleAllCache", allEntries = true),
            @CacheEvict(value = "personByEmailCache", allEntries = true),
            @CacheEvict(value = "personByFirebaseCache", allEntries = true)
    })
    public ResponseEntity<ImportPeopleResult> execute(List<JsonNode> records) {
        ImportPeopleResult result = new ImportPeopleResult();
        if (records == null || records.isEmpty()) {
            return ResponseEntity.ok(result);
        }

        result.setTotal(records.size());
        Set<String> seenEmails = new HashSet<>();

        for (JsonNode record : records) {
            if (record == null) {
                recordIssue(result, "Skipped empty record.");
                result.setInvalid(result.getInvalid() + 1);
                result.setInvalidMissingAttributes(result.getInvalidMissingAttributes() + 1);
                continue;
            }

            ImportCandidate candidate = parseCandidate(record);
            if (candidate == null) {
                recordIssue(result, "Skipped record without attributes.");
                result.setInvalid(result.getInvalid() + 1);
                result.setInvalidMissingAttributes(result.getInvalidMissingAttributes() + 1);
                continue;
            }

            String firstName = normalize(candidate.firstName());
            String lastName = normalize(candidate.lastName());
            String primaryEmail = normalizeEmail(candidate.primaryEmail());
            String phoneNumber = normalizePhone(candidate.phoneNumber());
            String secondaryEmail = normalizeOptional(candidate.secondaryEmail());
            String firebaseUid = normalizeOptional(candidate.firebaseUid());
            LocalDate dateOfBirth = candidate.dateOfBirth();
            Set<String> roles = candidate.roles();

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

            Optional<Person> existing = findExisting(candidate.id(), primaryEmail);

            if (existing.isPresent()) {
                PersonRequest request = new PersonRequest();
                request.setFirstName(firstName);
                request.setLastName(lastName);
                request.setPrimaryEmail(primaryEmail);

                if (candidate.exportFormat()) {
                    request.setSecondaryEmail(secondaryEmail == null ? "" : secondaryEmail);
                    request.setPhoneNumber(phoneNumber == null ? "" : phoneNumber);
                    request.setDateOfBirth(dateOfBirth);
                    request.setFirebaseUID(firebaseUid == null ? "" : firebaseUid);
                    request.setRoles(roles == null ? Set.of() : roles);
                } else {
                    if (secondaryEmail != null) {
                        request.setSecondaryEmail(secondaryEmail);
                    }
                    if (phoneNumber != null) {
                        request.setPhoneNumber(phoneNumber);
                    }
                    if (dateOfBirth != null) {
                        request.setDateOfBirth(dateOfBirth);
                    }
                    if (firebaseUid != null) {
                        request.setFirebaseUID(firebaseUid);
                    }
                    if (roles != null) {
                        request.setRoles(roles);
                    }
                }

                updatePersonService.execute(new UpdatePersonCommand(existing.get().getId(), request));
                result.setUpdated(result.getUpdated() + 1);
                continue;
            }

            PersonRequest request = new PersonRequest();
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setPrimaryEmail(primaryEmail);
            request.setSecondaryEmail(secondaryEmail);
            request.setPhoneNumber(phoneNumber);
            request.setDateOfBirth(dateOfBirth);
            request.setFirebaseUID(firebaseUid);
            request.setRoles(roles == null ? Set.of() : roles);

            createPersonService.execute(request);
            result.setCreated(result.getCreated() + 1);
        }

        return ResponseEntity.ok(result);
    }

    private static String safeId(JsonNode record) {
        if (record != null && record.hasNonNull("id")) {
            String id = record.get("id").asText();
            if (id != null && !id.isBlank()) {
                return id;
            }
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

    private static String normalizeOptional(String value) {
        String trimmed = normalize(value);
        return trimmed == null ? null : trimmed;
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

    private Optional<Person> findExisting(UUID id, String primaryEmail) {
        if (id != null) {
            Optional<Person> byId = peopleRepository.findById(id);
            if (byId.isPresent()) {
                return byId;
            }
        }
        return peopleRepository.findByPrimaryEmailIgnoreCase(primaryEmail);
    }

    private static ImportCandidate parseCandidate(JsonNode record) {
        if (record == null || record.isNull()) {
            return null;
        }

        if (record.has("attributes")) {
            JsonNode attributes = record.get("attributes");
            if (attributes == null || attributes.isNull()) {
                return null;
            }
            return new ImportCandidate(
                    parseUuid(record.get("id")),
                    text(attributes.get("first_name")),
                    text(attributes.get("last_name")),
                    text(attributes.get("email")),
                    null,
                    text(attributes.get("phone")),
                    null,
                    null,
                    null,
                    false);
        }

        return new ImportCandidate(
                parseUuid(record.get("id")),
                text(record.get("firstName")),
                text(record.get("lastName")),
                text(record.get("primaryEmail")),
                text(record.get("secondaryEmail")),
                text(record.get("phoneNumber")),
                parseDate(record.get("dateOfBirth")),
                text(record.get("firebaseUID")),
                parseRoles(record.get("roles")),
                true);
    }

    private static UUID parseUuid(JsonNode node) {
        if (node == null || node.isNull())
            return null;
        String raw = node.asText();
        if (raw == null || raw.isBlank())
            return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static LocalDate parseDate(JsonNode node) {
        if (node == null || node.isNull())
            return null;
        String raw = node.asText();
        if (raw == null || raw.isBlank())
            return null;
        try {
            return LocalDate.parse(raw);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Set<String> parseRoles(JsonNode node) {
        if (node == null || !node.isArray())
            return Set.of();
        Set<String> roles = new HashSet<>();
        node.forEach(item -> {
            String value = text(item);
            if (value != null && !value.isBlank()) {
                roles.add(value);
            }
        });
        return roles;
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull())
            return null;
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private record ImportCandidate(
            UUID id,
            String firstName,
            String lastName,
            String primaryEmail,
            String secondaryEmail,
            String phoneNumber,
            LocalDate dateOfBirth,
            String firebaseUid,
            Set<String> roles,
            boolean exportFormat) {
    }
}
