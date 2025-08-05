package com.downloadmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Abstract class for all fetch data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractFetch {
    protected String id;
    protected Job job;
    protected String org;
    protected String url;
    protected Status status;
    protected String statusMessage;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public void markPending() { this.status = Status.PENDING; }
    public void markStarted() { this.status = Status.STARTED; }
    public void markCompleted() { this.status = Status.COMPLETED; }
    public void markFailed(String msg) { this.status = Status.FAILED; this.statusMessage = msg; }

    public enum Status {
        PENDING,
        STARTED,
        COMPLETED,
        FAILED
    }

    public enum Job {
        REPO_LIST,
        REPO_DETAIL,
        TEAM_LIST,
        TEAM_DETAIL
    }
}