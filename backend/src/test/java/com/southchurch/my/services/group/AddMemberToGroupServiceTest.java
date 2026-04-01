package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.southchurch.my.dto.group.AddMemberToGroupCommand;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddMemberToGroupServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private Directory.Members members;

    @Mock
    private Directory.Members.Insert insert;

    @InjectMocks
    private AddMemberToGroupService addMemberToGroupService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    @Test
    void addsMemberSuccessfully() throws IOException {
        Member expected = new Member();
        expected.setEmail("member@test.com");

        when(directory.members()).thenReturn(members);
        when(members.insert(eq("group-key"), any(Member.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(expected);

        AddMemberToGroupCommand command = new AddMemberToGroupCommand("group-key", "member@test.com", "MEMBER");
        ResponseEntity<Member> response = addMemberToGroupService.execute(command);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("member@test.com", response.getBody().getEmail());
    }

    @Test
    void defaultsRoleToMemberWhenNotProvided() throws IOException {
        Member expected = new Member();

        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any())).thenReturn(insert);
        when(insert.execute()).thenReturn(expected);

        AddMemberToGroupCommand command = new AddMemberToGroupCommand("group-key", "member@test.com", null);
        addMemberToGroupService.execute(command);

        verify(members).insert(eq("group-key"), argThat(m -> "MEMBER".equals(m.getRole())));
    }

    @Test
    void normalizesRoleToUpperCase() throws IOException {
        Member expected = new Member();

        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any())).thenReturn(insert);
        when(insert.execute()).thenReturn(expected);

        AddMemberToGroupCommand command = new AddMemberToGroupCommand("group-key", "member@test.com", "manager");
        addMemberToGroupService.execute(command);

        verify(members).insert(eq("group-key"), argThat(m -> "MANAGER".equals(m.getRole())));
    }

    @Test
    void throwsWhenInputIsNull() {
        assertThrows(IllegalArgumentException.class, () -> addMemberToGroupService.execute(null));
    }

    @Test
    void throwsWhenGroupKeyIsBlank() {
        AddMemberToGroupCommand command = new AddMemberToGroupCommand("", "member@test.com", "MEMBER");
        assertThrows(IllegalArgumentException.class, () -> addMemberToGroupService.execute(command));
    }

    @Test
    void throwsWhenMemberEmailIsBlank() {
        AddMemberToGroupCommand command = new AddMemberToGroupCommand("group-key", "", "MEMBER");
        assertThrows(IllegalArgumentException.class, () -> addMemberToGroupService.execute(command));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnApiError() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any())).thenReturn(insert);
        when(insert.execute()).thenThrow(makeGoogleException(500));

        AddMemberToGroupCommand command = new AddMemberToGroupCommand("group-key", "member@test.com", "MEMBER");
        assertThrows(GoogleWorkspaceException.class, () -> addMemberToGroupService.execute(command));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnIOException() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.insert(any(), any())).thenReturn(insert);
        when(insert.execute()).thenThrow(new IOException("network error"));

        AddMemberToGroupCommand command = new AddMemberToGroupCommand("group-key", "member@test.com", "MEMBER");
        assertThrows(GoogleWorkspaceException.class, () -> addMemberToGroupService.execute(command));
    }
}