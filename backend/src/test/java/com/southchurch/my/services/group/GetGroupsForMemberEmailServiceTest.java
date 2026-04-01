package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Groups;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetGroupsForMemberEmailServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private Directory.Users users;

    @Mock
    private Directory.Users.Get usersGet;

    @Mock
    private Directory.Groups groups;

    @Mock
    private Directory.Groups.List groupsList;

    @InjectMocks
    private GetGroupsForMemberEmailService getGroupsForMemberEmailService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    private void injectDomain() {
        ReflectionTestUtils.setField(getGroupsForMemberEmailService, "domain", "test.com");
    }

    private void mockUserExists() throws IOException {
        when(directory.users()).thenReturn(users);
        when(users.get(any())).thenReturn(usersGet);
        when(usersGet.execute()).thenReturn(null);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void returnsGroupsForMember() throws IOException {
        injectDomain();

        Group g1 = new Group();
        g1.setEmail("group1@test.com");
        Groups groupsResult = new Groups();
        groupsResult.setGroups(List.of(g1));

        mockUserExists();

        when(directory.groups()).thenReturn(groups);
        when(groups.list()).thenReturn(groupsList);
        when(groupsList.setDomain(any())).thenReturn(groupsList);
        when(groupsList.setUserKey(any())).thenReturn(groupsList);
        when(groupsList.setPageToken(any())).thenReturn(groupsList);
        when(groupsList.execute()).thenReturn(groupsResult);

        ResponseEntity<List<Group>> response =
                getGroupsForMemberEmailService.execute("member@test.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("group1@test.com", response.getBody().get(0).getEmail());
    }

    @Test
    void returnsEmptyListWhenNoGroups() throws IOException {
        injectDomain();

        Groups groupsResult = new Groups();
        groupsResult.setGroups(null);

        mockUserExists();

        when(directory.groups()).thenReturn(groups);
        when(groups.list()).thenReturn(groupsList);
        when(groupsList.setDomain(any())).thenReturn(groupsList);
        when(groupsList.setUserKey(any())).thenReturn(groupsList);
        when(groupsList.setPageToken(any())).thenReturn(groupsList);
        when(groupsList.execute()).thenReturn(groupsResult);

        ResponseEntity<List<Group>> response =
                getGroupsForMemberEmailService.execute("member@test.com");

        assertTrue(response.getBody().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @Test
    void throwsWhenMemberEmailIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> getGroupsForMemberEmailService.execute(null));
    }

    @Test
    void throwsWhenMemberEmailIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> getGroupsForMemberEmailService.execute("  "));
    }

    // -------------------------------------------------------------------------
    // Google account check
    // -------------------------------------------------------------------------

    @Test
    void throws400WhenUserHasNoGoogleAccount() throws IOException {
        injectDomain();

        when(directory.users()).thenReturn(users);
        when(users.get(any())).thenReturn(usersGet);
        when(usersGet.execute()).thenThrow(makeGoogleException(404));

        GoogleWorkspaceException ex = assertThrows(GoogleWorkspaceException.class,
                () -> getGroupsForMemberEmailService.execute("member@test.com"));

        assertEquals(400, ex.getGoogleStatus());
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnUserCheckApiError() throws IOException {
        injectDomain();

        when(directory.users()).thenReturn(users);
        when(users.get(any())).thenReturn(usersGet);
        when(usersGet.execute()).thenThrow(makeGoogleException(500));

        assertThrows(GoogleWorkspaceException.class,
                () -> getGroupsForMemberEmailService.execute("member@test.com"));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnGroupsListApiError() throws IOException {
        injectDomain();

        mockUserExists();

        when(directory.groups()).thenReturn(groups);
        when(groups.list()).thenReturn(groupsList);
        when(groupsList.setDomain(any())).thenReturn(groupsList);
        when(groupsList.setUserKey(any())).thenReturn(groupsList);
        when(groupsList.setPageToken(any())).thenReturn(groupsList);
        when(groupsList.execute()).thenThrow(makeGoogleException(403));

        assertThrows(GoogleWorkspaceException.class,
                () -> getGroupsForMemberEmailService.execute("member@test.com"));
    }
}