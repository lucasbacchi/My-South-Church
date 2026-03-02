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

import com.southchurch.my.Command;
import com.southchurch.my.dto.imports.ImportPeopleResult;
import com.southchurch.my.dto.person.PersonRequest;
import com.southchurch.my.dto.person.UpdatePersonCommand;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class ImportPeopleService implements Command<List<JsonNode>, ImportPeopleResult> {

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

    /**
     * Execute a command to import a list of people records.
     *
     * This method is transactional and will clear the people cache if an error
     * occurs.
     *
     * This method is cached for the peopleAllCache, personByEmailCache, and
     * personByFirebaseCache.
     *
     * @param records The list of people records to import.
     * @return A ResponseEntity containing an ImportPeopleResult object.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "peopleAllCache", allEntries = true),
            @CacheEvict(value = "personByEmailCache", allEntries = true),
            @CacheEvict(value = "personByFirebaseCache", allEntries = true)
    })
    @Override
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

    /**
     * Returns a safe ID from a JsonNode record.
     * If the record is null, or the record does not have an "id" field, or the ID
     * is blank, this method returns "unknown".
     * Otherwise, the method returns the ID field from the record as a string.
     * 
     * @param record the JsonNode record
     * @return a safe ID from the record, or "unknown" if the record is null or does
     *         not have an "id" field, or the ID is blank.
     */
    private static String safeId(JsonNode record) {
        if (record != null && record.hasNonNull("id")) {
            String id = record.get("id").asText();
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        return "unknown";
    }

    /**
     * Records an issue with the given message in the ImportPeopleResult.
     * If the result already has {@link #ISSUE_LIMIT} or more issues, this method
     * does nothing.
     * Otherwise, this method adds the given message to the result's issues list.
     * 
     * @param result  the ImportPeopleResult to record the issue in
     * @param message the issue to record
     */
    private static void recordIssue(ImportPeopleResult result, String message) {
        if (result.getIssues().size() < ISSUE_LIMIT) {
            result.getIssues().add(message);
        }
    }

    /**
     * Returns the given string with whitespace removed from the beginning and end,
     * or null if the string is null or whitespace only.
     * 
     * @param value the string to normalize
     * @return the normalized string, or null if the string is null or whitespace
     *         only
     */
    private static String normalize(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Returns the given string with whitespace removed from the beginning and end,
     * or null if the string is null or whitespace only.
     * This method is useful for normalizing strings that are optional, as it will
     * return null if the string is null or whitespace only.
     * 
     * @param value the string to normalize
     * @return the normalized string, or null if the string is null or whitespace
     *         only
     */
    private static String normalizeOptional(String value) {
        String trimmed = normalize(value);
        return trimmed == null ? null : trimmed;
    }

    /**
     * Returns the given string with whitespace removed from the beginning and end,
     * and normalized to the following format if the string is a valid phone number:
     * 123-456-7890.
     * If the string is null or whitespace only, this method returns null.
     * If the string is not a valid phone number, this method returns the original
     * string with whitespace removed from the beginning and end.
     * A valid phone number is a string that starts with "1" followed by 10 digits,
     * or a string that contains 10 digits.
     * 
     * @param value the string to normalize
     * @return the normalized string, or null if the string is null or whitespace
     *         only
     */
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

    /**
     * Returns the given string with whitespace removed from the beginning and end,
     * and normalized to lowercase if the string is not null or whitespace only.
     * If the string is null or whitespace only, this method returns null.
     * 
     * @param value the string to normalize
     * @return the normalized string, or null if the string is null or whitespace
     *         only
     */
    private static String normalizeEmail(String value) {
        String trimmed = normalize(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    /**
     * Returns true if the given string is null or whitespace only, false otherwise.
     * This method is useful for checking if a string is empty or only contains
     * whitespace.
     * 
     * @param value the string to check
     * @return true if the string is null or whitespace only, false otherwise
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Finds a person in the database by their id and primary email.
     * If the person is found by id, that person is returned.
     * If the person is not found by id, then the person is found by primary email.
     * If the person is not found by primary email, then null is returned.
     * 
     * @param id           the id of the person to find
     * @param primaryEmail the primary email of the person to find
     * @return the found person, or null if the person was not found
     */
    private Optional<Person> findExisting(UUID id, String primaryEmail) {
        if (id != null) {
            Optional<Person> byId = peopleRepository.findById(id);
            if (byId.isPresent()) {
                return byId;
            }
        }
        return peopleRepository.findByPrimaryEmailIgnoreCase(primaryEmail);
    }

    /**
     * Parse a JsonNode record into an ImportCandidate object.
     * If the record is null or does not have an "attributes" field, this method
     * returns null.
     * If the record has an "attributes" field but the "attributes" field is null or
     * does not have "first_name", "last_name", "email", "phone", "dateOfBirth",
     * "firebaseUID", or "roles" fields, this method returns null.
     * 
     * @param record the JsonNode record to parse
     * @return the parsed ImportCandidate, or null if the record is null or the
     *         "attributes" field is null or does not have the required fields
     */
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

    /**
     * Returns a UUID from a JsonNode record.
     * If the record is null or does not have a "id" field, or the ID is blank, this
     * method returns null.
     * 
     * @param node the JsonNode record
     * @return the parsed UUID, or null if the record is null or the "id" field is
     *         null or the ID is blank
     */
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

    /**
     * Returns a LocalDate from a JsonNode record.
     * If the record is null or does not have a field with the same name as the
     * method, or the field is blank, this method returns null.
     * If the field is present and is not blank, this method attempts to parse the
     * field as a LocalDate.
     * If the parsing fails, this method returns null.
     * 
     * @param node the JsonNode record
     * @return the parsed LocalDate, or null if the record is null or the field is
     *         null or blank, or the parsing fails
     */
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

    /**
     * Returns a Set of Strings from a JsonNode array.
     * If the record is null or does not have an array field, or the array field is
     * empty, this method returns an empty set.
     * If the array field is present and is not empty, this method attempts to parse
     * each item in the array as a String.
     * If the item is null or the parsed String is blank, the item is skipped.
     * Otherwise, the parsed String is added to the Set.
     * 
     * @param node the JsonNode record
     * @return the parsed Set of Strings, or an empty set if the record is null or
     *         the array field is empty, or if all items in the array field are null
     *         or blank.
     */
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

    /**
     * Returns the text value of a JsonNode, or null if the node is null, or if the
     * node is null or blank.
     * If the node is null or the parsed text is blank, this method returns null.
     * Otherwise, this method returns the parsed text.
     * 
     * @param node the JsonNode record
     * @return the parsed text, or null if the record is null or the text is blank.
     */
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
