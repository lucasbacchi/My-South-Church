package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.southchurch.my.dto.group.CreateGroupCommand;
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
class CreateGroupServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private Directory.Groups groups;

    @Mock
    private Directory.Groups.Insert insert;

    @InjectMocks
    private CreateGroupService createGroupService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    private CreateGroupCommand validCommand() {
        CreateGroupCommand cmd = new CreateGroupCommand();
        cmd.setEmail("group@test.com");
        cmd.setName("Test Group");
        cmd.setDescription("A test group");
        return cmd;
    }

    @Test
    void createsGroupSuccessfully() throws IOException {
        Group expected = new Group();
        expected.setEmail("group@test.com");
        expected.setName("Test Group");

        when(directory.groups()).thenReturn(groups);
        when(groups.insert(any(Group.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(expected);

        ResponseEntity<Group> response = createGroupService.execute(validCommand());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("group@test.com", response.getBody().getEmail());
    }

    @Test
    void setsDescriptionWhenProvided() throws IOException {
        when(directory.groups()).thenReturn(groups);
        when(groups.insert(any(Group.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(new Group());

        createGroupService.execute(validCommand());

        verify(groups).insert(argThat(g -> "A test group".equals(g.getDescription())));
    }

    @Test
    void doesNotSetDescriptionWhenBlank() throws IOException {
        CreateGroupCommand cmd = validCommand();
        cmd.setDescription("");

        when(directory.groups()).thenReturn(groups);
        when(groups.insert(any(Group.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(new Group());

        createGroupService.execute(cmd);

        verify(groups).insert(argThat(g -> g.getDescription() == null));
    }

    @Test
    void throwsWhenInputIsNull() {
        assertThrows(IllegalArgumentException.class, () -> createGroupService.execute(null));
    }

    @Test
    void throwsWhenEmailIsBlank() {
        CreateGroupCommand cmd = validCommand();
        cmd.setEmail("");
        assertThrows(IllegalArgumentException.class, () -> createGroupService.execute(cmd));
    }

    @Test
    void throwsWhenNameIsBlank() {
        CreateGroupCommand cmd = validCommand();
        cmd.setName(null);
        assertThrows(IllegalArgumentException.class, () -> createGroupService.execute(cmd));
    }

    @Test
    void throwsConflictExceptionWhenGroupAlreadyExists() throws IOException {
        when(directory.groups()).thenReturn(groups);
        when(groups.insert(any())).thenReturn(insert);
        when(insert.execute()).thenThrow(makeGoogleException(409));

        GoogleWorkspaceException ex = assertThrows(GoogleWorkspaceException.class,
                () -> createGroupService.execute(validCommand()));

        assertEquals(409, ex.getGoogleStatus());
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnIOException() throws IOException {
        when(directory.groups()).thenReturn(groups);
        when(groups.insert(any())).thenReturn(insert);
        when(insert.execute()).thenThrow(new IOException("network error"));

        assertThrows(GoogleWorkspaceException.class, () -> createGroupService.execute(validCommand()));
    }
}