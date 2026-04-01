package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.directory.Directory;
import com.southchurch.my.dto.group.RemoveMemberFromGroupCommand;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveMemberFromGroupServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private Directory.Members members;

    @Mock
    private Directory.Members.Delete delete;

    @InjectMocks
    private RemoveMemberFromGroupService removeMemberFromGroupService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    @Test
    void removesMemberSuccessfully() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.delete(any(), any())).thenReturn(delete);
        doNothing().when(delete).execute();

        ResponseEntity<Void> response = removeMemberFromGroupService.execute(
                new RemoveMemberFromGroupCommand("group-key", "member@test.com"));

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(members).delete("group-key", "member@test.com");
    }

    @Test
    void throwsWhenInputIsNull() {
        assertThrows(IllegalArgumentException.class, () -> removeMemberFromGroupService.execute(null));
    }

    @Test
    void throwsWhenGroupKeyIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                removeMemberFromGroupService.execute(new RemoveMemberFromGroupCommand("", "member@test.com")));
    }

    @Test
    void throwsWhenMemberEmailIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                removeMemberFromGroupService.execute(new RemoveMemberFromGroupCommand("group-key", "")));
    }

    @Test
    void throws404WhenMemberNotFound() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.delete(any(), any())).thenReturn(delete);
        doThrow(makeGoogleException(404)).when(delete).execute();

        GoogleWorkspaceException ex = assertThrows(GoogleWorkspaceException.class, () ->
                removeMemberFromGroupService.execute(
                        new RemoveMemberFromGroupCommand("group-key", "member@test.com")));

        assertEquals(404, ex.getGoogleStatus());
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnIOException() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.delete(any(), any())).thenReturn(delete);
        doThrow(new IOException("network error")).when(delete).execute();

        assertThrows(GoogleWorkspaceException.class, () ->
                removeMemberFromGroupService.execute(
                        new RemoveMemberFromGroupCommand("group-key", "member@test.com")));
    }
}