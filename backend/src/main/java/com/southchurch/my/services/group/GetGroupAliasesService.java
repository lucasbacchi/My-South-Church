package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Aliases;
import com.southchurch.my.Query;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class GetGroupAliasesService implements Query<String, List<String>> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(GetGroupAliasesService.class);

    public GetGroupAliasesService(Directory directory) {
        this.directory = directory;
    }


    @Override
    public ResponseEntity<List<String>> execute(String groupKey) {

        logger.info("Executing " + getClass() + " input : " + groupKey);

        if(groupKey == null || groupKey.isBlank()){
            throw new IllegalArgumentException("groupKey must not be blank");
        }

        try{
            Aliases response = directory.groups().aliases().list(groupKey).execute();
           
            List<String> aliases = response.getAliases() == null
                ? List.of()
                : response.getAliases().stream()
                    .map(a -> (String) ((java.util.Map<?, ?>) a).get("alias"))
                    .toList();

            return ResponseEntity.status(HttpStatus.OK).body(aliases);

        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new GoogleWorkspaceException("Group not found: " + groupKey, 404, e);
            }
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while listing aliases";
            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException("Google Workspace call failed while listing aliases", 502, e);
        }
    }

}
