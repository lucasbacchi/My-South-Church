package com.southchurch.my.dto.group;

import java.util.List;

import lombok.Getter;

@Getter
public class BulkMembershipResponse {

    private List<Result> results;
    private Summary summary;

    public BulkMembershipResponse(List<Result> results, Summary summary) {
        this.results = results;
        this.summary = summary;
    }

    @Getter
    public static class Result {
        private String memberEmail;
        private String groupKey;
        private int status; // 200, 409, 404 etc
        private String message; // Message for context

        public Result(String memberEmail, String groupKey, int status, String message) {
            this.memberEmail = memberEmail;
            this.groupKey = groupKey;
            this.status = status;
            this.message = message;
        }
    }

    @Getter
    public static class Summary {
        private int requested;
        private int succeeded;
        private int failed;
        private int alreadyExists;

        public Summary(int requested, int succeeded, int failed, int alreadyExists) {
            this.requested = requested;
            this.succeeded = succeeded;
            this.failed = failed;
            this.alreadyExists = alreadyExists;
        }
    }
}
