package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.southchurch.my.Command;
import com.southchurch.my.dto.group.BulkMembershipRequest;
import com.southchurch.my.dto.group.BulkMembershipResponse;
import com.southchurch.my.exceptions.ErrorMessages;

@Service
public class RemoveMembersFromGroupsBulkService implements Command<BulkMembershipRequest, BulkMembershipResponse> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(RemoveMembersFromGroupsBulkService.class);

    private static final int MAX_PARALLEL_CALLS = 10;

    public RemoveMembersFromGroupsBulkService(Directory directory) {
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

        List<String> members = normalize(request.getMemberEmails(), true);
        List<String> groups = normalize(request.getGroupKeys(), false);

        int requested = members.size() * groups.size();

        AtomicInteger succeeded = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        AtomicInteger notFound = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(MAX_PARALLEL_CALLS);
        List<Future<BulkMembershipResponse.Result>> futures = new ArrayList<>(requested);

        for (String groupKey : groups) {
            for (String memberEmail : members) {
                futures.add(pool.submit(() -> removeOne(groupKey, memberEmail, succeeded, failed, notFound)));
            }
        }

        List<BulkMembershipResponse.Result> results = new ArrayList<>(requested);

        try {
            for (Future<BulkMembershipResponse.Result> f : futures) {
                results.add(f.get());
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bulk operation interrupted");
        } catch (ExecutionException ee) {
            throw new IllegalStateException("Unexpected error during bulk operation", ee.getCause());
        } finally {
            pool.shutdownNow();
        }

        var summary = new BulkMembershipResponse.Summary(
                requested,
                succeeded.get(),
                failed.get(),
                notFound.get());

        return ResponseEntity.status(HttpStatus.OK).body(new BulkMembershipResponse(results, summary));
    }

    private BulkMembershipResponse.Result removeOne(
            String groupKey,
            String memberEmail,
            AtomicInteger succeeded,
            AtomicInteger failed,
            AtomicInteger notFound) {
        try {
            directory.members().delete(groupKey, memberEmail).execute();
            succeeded.incrementAndGet();
            return new BulkMembershipResponse.Result(memberEmail, groupKey, 200, "Removed");

        } catch (GoogleJsonResponseException e) {
            int code = e.getStatusCode();

            if (code == 404) {
                notFound.incrementAndGet();
                return new BulkMembershipResponse.Result(memberEmail, groupKey, 404, "Member not found in group");
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
