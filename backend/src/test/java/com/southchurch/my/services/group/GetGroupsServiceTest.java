package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.cloudidentity.v1.CloudIdentity;
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
class GetGroupsServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private CloudIdentity ciService;

    @Mock
    private Directory.Groups groups;

    @Mock
    private Directory.Groups.List groupsList;

    @InjectMocks
    private GetGroupsService getGroupsService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    private void injectDomain() {
        ReflectionTestUtils.setField(getGroupsService, "domain", "test.com");
    }

    @Test
    void returnsAllGroups() throws IOException {
        injectDomain();

        Group g1 = new Group();
        g1.setEmail("group1@test.com");
        Group g2 = new Group();
        g2.setEmail("group2@test.com");

        Groups groupsResult = new Groups();
        groupsResult.setGroups(List.of(g1, g2));

        when(directory.groups()).thenReturn(groups);
        when(groups.list()).thenReturn(groupsList);
        when(groupsList.setDomain(any())).thenReturn(groupsList);
        when(groupsList.execute()).thenReturn(groupsResult);

        ResponseEntity<List<Group>> response = getGroupsService.execute(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void returnsNullWhenNoGroups() throws IOException {
        injectDomain();

        Groups groupsResult = new Groups();
        groupsResult.setGroups(null);

        when(directory.groups()).thenReturn(groups);
        when(groups.list()).thenReturn(groupsList);
        when(groupsList.setDomain(any())).thenReturn(groupsList);
        when(groupsList.execute()).thenReturn(groupsResult);

        ResponseEntity<List<Group>> response = getGroupsService.execute(null);

        assertNull(response.getBody());
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnApiError() throws IOException {
        injectDomain();

        when(directory.groups()).thenReturn(groups);
        when(groups.list()).thenReturn(groupsList);
        when(groupsList.setDomain(any())).thenReturn(groupsList);
        when(groupsList.execute()).thenThrow(makeGoogleException(403));

        assertThrows(GoogleWorkspaceException.class, () -> getGroupsService.execute(null));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnIOException() throws IOException {
        injectDomain();

        when(directory.groups()).thenReturn(groups);
        when(groups.list()).thenReturn(groupsList);
        when(groupsList.setDomain(any())).thenReturn(groupsList);
        when(groupsList.execute()).thenThrow(new IOException("network error"));

        assertThrows(GoogleWorkspaceException.class, () -> getGroupsService.execute(null));
    }
}