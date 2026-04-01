package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.directory.Directory;
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
class DeleteGroupServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private Directory.Groups groups;

    @Mock
    private Directory.Groups.Delete delete;

    @InjectMocks
    private DeleteGroupService deleteGroupService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    @Test
    void deletesGroupSuccessfully() throws IOException {
        when(directory.groups()).thenReturn(groups);
        when(groups.delete(any())).thenReturn(delete);
        doNothing().when(delete).execute();

        ResponseEntity<Void> response = deleteGroupService.execute("group-key");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(groups).delete("group-key");
    }

    @Test
    void throwsWhenGroupKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> deleteGroupService.execute(null));
    }

    @Test
    void throwsWhenGroupKeyIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> deleteGroupService.execute("  "));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnApiError() throws IOException {
        when(directory.groups()).thenReturn(groups);
        when(groups.delete(any())).thenReturn(delete);
        doThrow(makeGoogleException(404)).when(delete).execute();

        assertThrows(GoogleWorkspaceException.class, () -> deleteGroupService.execute("group-key"));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnIOException() throws IOException {
        when(directory.groups()).thenReturn(groups);
        when(groups.delete(any())).thenReturn(delete);
        doThrow(new IOException("network error")).when(delete).execute();

        assertThrows(GoogleWorkspaceException.class, () -> deleteGroupService.execute("group-key"));
    }
}