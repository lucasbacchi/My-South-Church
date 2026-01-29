package com.southchurch.my;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@RestController
public class GroupController {

    @Autowired
    private GoogleWorkspaceService workspaceService;

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups(@RequestHeader("Authorization") String authHeader) {
        try {
            // 1. Strip "Bearer " from the header
            String idToken = authHeader.replace("Bearer ", "");

            // 2. Verify with Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();

            // 3. (Optional) Check if this UID is allowed in your SQL DB

            // 4. Fetch data from Google
            return ResponseEntity.ok(workspaceService.listGroups());

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}