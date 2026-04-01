package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
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
class GetGroupByIdServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private Directory.Groups groups;

    @Mock
    private Directory.Groups.Get get;

    @InjectMocks
    private GetGroupByIdService getGroupByIdService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    @Test
    void returnsGroupById() throws IOException {
        Group expected = new Group();
        expected.setId("group-id");
        expected.setEmail("group@test.com");

        when(directory.groups()).thenReturn(groups);
        when(groups.get(any())).thenReturn(get);
        when(get.execute()).thenReturn(expected);

        ResponseEntity<Group> response = getGroupByIdService.execute("group-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("group@test.com", response.getBody().getEmail());
    }

    @Test
    void throwsWhenGroupIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> getGroupByIdService.execute(null));
    }

    @Test
    void throwsWhenGroupIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> getGroupByIdService.execute("  "));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnApiError() throws IOException {
        when(directory.groups()).thenReturn(groups);
        when(groups.get(any())).thenReturn(get);
        when(get.execute()).thenThrow(makeGoogleException(404));

        assertThrows(GoogleWorkspaceException.class, () -> getGroupByIdService.execute("group-id"));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnIOException() throws IOException {
        when(directory.groups()).thenReturn(groups);
        when(groups.get(any())).thenReturn(get);
        when(get.execute()).thenThrow(new IOException("network error"));

        assertThrows(GoogleWorkspaceException.class, () -> getGroupByIdService.execute("group-id"));
    }
}