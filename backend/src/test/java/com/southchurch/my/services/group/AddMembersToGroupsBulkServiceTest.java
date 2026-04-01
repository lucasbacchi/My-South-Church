package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.southchurch.my.dto.group.BulkMembershipRequest;
import com.southchurch.my.dto.group.BulkMembershipResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddMembersToGroupsBulkServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private Directory.Members members;

    @Mock
    private Directory.Members.Insert insert;

    @InjectMocks
    private AddMembersToGroupsBulkService addMembersToGroupsBulkService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    private BulkMembershipRequest validRequest() {
        BulkMembershipRequest request = new BulkMembershipRequest();
        request.setMemberEmails(List.of("member1@test.com", "member2@test.com"));
        request.setGroupKeys(List.of("group-key-1"));
        request.setRole("MEMBER");
        return request;
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void addsAllMembersSuccessfully() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any(Member.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(new Member());

        ResponseEntity<BulkMembershipResponse> response =
                addMembersToGroupsBulkService.execute(validRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(2, response.getBody().getSummary().getRequested());
        assertEquals(2, response.getBody().getSummary().getSucceeded());
        assertEquals(0, response.getBody().getSummary().getFailed());
    }

    @Test
    void defaultsRoleToMemberWhenNotProvided() throws IOException {
        BulkMembershipRequest request = validRequest();
        request.setRole(null);

        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any(Member.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(new Member());

        addMembersToGroupsBulkService.execute(request);

        verify(members, times(2)).insert(any(), argThat(m -> "MEMBER".equals(m.getRole())));
    }

    @Test
    void tracksAlreadyExistsOn409() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any(Member.class))).thenReturn(insert);
        when(insert.execute()).thenThrow(makeGoogleException(409));

        ResponseEntity<BulkMembershipResponse> response =
                addMembersToGroupsBulkService.execute(validRequest());

        assertEquals(2, response.getBody().getSummary().getAlreadyExists());
        assertEquals(0, response.getBody().getSummary().getFailed());
    }

    @Test
    void tracksFailedOnApiError() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any(Member.class))).thenReturn(insert);
        when(insert.execute()).thenThrow(makeGoogleException(500));

        ResponseEntity<BulkMembershipResponse> response =
                addMembersToGroupsBulkService.execute(validRequest());

        assertEquals(2, response.getBody().getSummary().getFailed());
        assertEquals(0, response.getBody().getSummary().getSucceeded());
    }

    @Test
    void tracksFailedOnIOException() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any(Member.class))).thenReturn(insert);
        when(insert.execute()).thenThrow(new IOException("network error"));

        ResponseEntity<BulkMembershipResponse> response =
                addMembersToGroupsBulkService.execute(validRequest());

        assertEquals(2, response.getBody().getSummary().getFailed());
    }

    @Test
    void normalizesEmailsToLowerCase() throws IOException {
        BulkMembershipRequest request = new BulkMembershipRequest();
        request.setMemberEmails(List.of("UPPER@TEST.COM"));
        request.setGroupKeys(List.of("group-key"));
        request.setRole("MEMBER");

        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any(Member.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(new Member());

        addMembersToGroupsBulkService.execute(request);

        verify(members).insert(any(), argThat(m -> "upper@test.com".equals(m.getEmail())));
    }

    @Test
    void skipsNullAndBlankEmails() throws IOException {
        BulkMembershipRequest request = new BulkMembershipRequest();
        request.setMemberEmails(List.of("valid@test.com", " ", ""));
        request.setGroupKeys(List.of("group-key"));
        request.setRole("MEMBER");

        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any(Member.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(new Member());

        ResponseEntity<BulkMembershipResponse> response =
                addMembersToGroupsBulkService.execute(request);

        assertEquals(1, response.getBody().getSummary().getRequested());
    }

    // -------------------------------------------------------------------------
    // Validation failures
    // -------------------------------------------------------------------------

    @Test
    void throwsWhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> addMembersToGroupsBulkService.execute(null));
    }

    @Test
    void throwsWhenMemberEmailsIsEmpty() {
        BulkMembershipRequest request = new BulkMembershipRequest();
        request.setMemberEmails(List.of());
        request.setGroupKeys(List.of("group-key"));

        assertThrows(IllegalArgumentException.class,
                () -> addMembersToGroupsBulkService.execute(request));
    }

    @Test
    void throwsWhenGroupKeysIsEmpty() {
        BulkMembershipRequest request = new BulkMembershipRequest();
        request.setMemberEmails(List.of("member@test.com"));
        request.setGroupKeys(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> addMembersToGroupsBulkService.execute(request));
    }
}