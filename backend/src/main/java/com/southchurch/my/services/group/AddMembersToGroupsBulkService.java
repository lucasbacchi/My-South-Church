package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.southchurch.my.Command;
import com.southchurch.my.dto.group.BulkMembershipRequest;
import com.southchurch.my.dto.group.BulkMembershipResponse;
import com.southchurch.my.exceptions.ErrorMessages;

@Service
public class AddMembersToGroupsBulkService implements Command<BulkMembershipRequest, BulkMembershipResponse> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(AddMembersToGroupsBulkService.class);

    // To be tuned; Tune this 5–15 is usually safe.
    private static final int MAX_PARALLEL_CALLS = 10;

    public AddMembersToGroupsBulkService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<BulkMembershipResponse> execute(BulkMembershipRequest request) {

        logger.info("Executing {} input: {}", getClass().getSimpleName(), request);

        if (request == null) {
            throw new IllegalArgumentException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }
        if (request.getMemberEmails() == null || request.getMemberEmails().isEmpty()) {
            throw new IllegalArgumentException("memberEmails must not be empty");
        }
        if (request.getGroupKeys() == null || request.getGroupKeys().isEmpty()) {
            throw new IllegalArgumentException("groupKeys must not be empty");
        }

        String role = (request.getRole() == null || request.getRole().trim().isEmpty())
                ? "MEMBER"
                : request.getRole().trim().toUpperCase();

        List<String> members = normalize(request.getMemberEmails(), true);
        List<String> groups = normalize(request.getGroupKeys(), false);

        int requested = members.size() * groups.size();

        // Atomic variables to be shared between threads
        AtomicInteger succeeded = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        AtomicInteger alreadyExists = new AtomicInteger(0);

        // Create a pool of threads to execute the task
        ExecutorService pool = Executors.newFixedThreadPool(MAX_PARALLEL_CALLS);
        List<Future<BulkMembershipResponse.Result>> futures = new ArrayList<>(requested);

        for (String groupKey : groups) {
            for (String memberEmail : members) {
                futures.add(pool.submit(() -> addOne(groupKey, memberEmail, role, succeeded, failed, alreadyExists)));
            }
        }

        List<BulkMembershipResponse.Result> results = new ArrayList<>(requested);

        try {
            for (Future<BulkMembershipResponse.Result> f : futures) {
                results.add(f.get()); // waits per task
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            // If the thread was interrupted, treat remaining tasks as failed
            throw new IllegalStateException("Bulk operation interrupted");
        } catch (ExecutionException ee) {
            // This should be rare because addOne returns a Result even on failure.
            // But in case a bug throws unexpectedly:
            throw new IllegalStateException("Unexpected error during bulk operation", ee.getCause());
        } finally {
            pool.shutdownNow();
        }

        var summary = new BulkMembershipResponse.Summary(
                requested,
                succeeded.get(),
                failed.get(),
                alreadyExists.get());

        return ResponseEntity.status(HttpStatus.CREATED).body(new BulkMembershipResponse(results, summary));
    }

    private BulkMembershipResponse.Result addOne(
            String groupKey,
            String memberEmail,
            String role,
            AtomicInteger succeeded,
            AtomicInteger failed,
            AtomicInteger alreadyExists) {
        try {
            Member m = new Member();
            m.setEmail(memberEmail);
            m.setRole(role);

            directory.members().insert(groupKey, m).execute();
            succeeded.incrementAndGet();
            return new BulkMembershipResponse.Result(memberEmail, groupKey, 200, "Added");

        } catch (GoogleJsonResponseException e) {
            int code = e.getStatusCode();

            if (code == 409) {
                alreadyExists.incrementAndGet();
                return new BulkMembershipResponse.Result(memberEmail, groupKey, 409, "Already a member");
            }

            failed.incrementAndGet();
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google error";
            return new BulkMembershipResponse.Result(memberEmail, groupKey, code, msg);

        } catch (IOException e) {
            failed.incrementAndGet();
            return new BulkMembershipResponse.Result(memberEmail, groupKey, 502,
                    "Upstream error calling Google Workspace");
        }
    }

    private List<String> normalize(List<String> input, boolean isEmail) {
        List<String> out = new ArrayList<>();
        for (String s : input) {
            if (s == null)
                continue;
            String v = s.trim();
            if (v.isEmpty())
                continue;
            out.add(isEmail ? v.toLowerCase() : v);
        }
        return out;
    }
}
