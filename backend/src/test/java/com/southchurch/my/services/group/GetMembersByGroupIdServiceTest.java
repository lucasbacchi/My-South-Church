package com.southchurch.my.services.group;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.google.api.services.directory.model.Members;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

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
class GetMembersByGroupIdServiceTest {

    @Mock
    private Directory directory;

    @Mock
    private Directory.Members members;

    @Mock
    private Directory.Members.List list;

    @InjectMocks
    private GetMembersByGroupIdService getMembersByGroupIdService;

    private GoogleJsonResponseException makeGoogleException(int status) {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(status);
        error.setMessage("Google error " + status);
        HttpResponseException.Builder builder = new HttpResponseException.Builder(status, "error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, error);
    }

    @Test
    void returnsMembersForGroup() throws IOException {
        Member m1 = new Member();
        m1.setEmail("member1@test.com");
        Member m2 = new Member();
        m2.setEmail("member2@test.com");

        Members membersResult = new Members();
        membersResult.setMembers(List.of(m1, m2));

        when(directory.members()).thenReturn(members);
        when(members.list(any())).thenReturn(list);
        when(list.setPageToken(any())).thenReturn(list);
        when(list.execute()).thenReturn(membersResult);

        ResponseEntity<List<Member>> response = getMembersByGroupIdService.execute("group-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void returnsEmptyListWhenNoMembers() throws IOException {
        Members membersResult = new Members();
        membersResult.setMembers(null);

        when(directory.members()).thenReturn(members);
        when(members.list(any())).thenReturn(list);
        when(list.setPageToken(any())).thenReturn(list);
        when(list.execute()).thenReturn(membersResult);

        ResponseEntity<List<Member>> response = getMembersByGroupIdService.execute("group-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void throwsWhenGroupIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> getMembersByGroupIdService.execute(null));
    }

    @Test
    void throwsWhenGroupIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> getMembersByGroupIdService.execute(""));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnApiError() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.list(any())).thenReturn(list);
        when(list.setPageToken(any())).thenReturn(list);
        when(list.execute()).thenThrow(makeGoogleException(403));

        assertThrows(GoogleWorkspaceException.class, () -> getMembersByGroupIdService.execute("group-id"));
    }

    @Test
    void throwsGoogleWorkspaceExceptionOnIOException() throws IOException {
        when(directory.members()).thenReturn(members);
        when(members.list(any())).thenReturn(list);
        when(list.setPageToken(any())).thenReturn(list);
        when(list.execute()).thenThrow(new IOException("network error"));

        assertThrows(GoogleWorkspaceException.class, () -> getMembersByGroupIdService.execute("group-id"));
    }
}