package com.downloadmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a download task with status tracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadTask {
    
    private int id;
    private String url;
    private String filename;
    private long fileSize;
    private long downloadedBytes;
    private Status status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private double progress; // 0.0 to 1.0
    
    public DownloadTask(int id, String url) {
        this.id = id;
        this.url = url;
        this.status = Status.QUEUED;
        this.createdAt = LocalDateTime.now();
        this.progress = 0.0;
        this.downloadedBytes = 0;
    }
    
    /**
     * Enum representing the status of a download task.
     */
    public enum Status {
        QUEUED("Queued"),
        DOWNLOADING("Downloading"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        PAUSED("Paused"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Updates the download progress.
     * 
     * @param downloadedBytes Number of bytes downloaded
     * @param totalBytes Total number of bytes to download
     */
    public void updateProgress(long downloadedBytes, long totalBytes) {
        this.downloadedBytes = downloadedBytes;
        this.fileSize = totalBytes;
        
        if (totalBytes > 0) {
            this.progress = (double) downloadedBytes / totalBytes;
        }
    }
    
    /**
     * Marks the task as started.
     */
    public void markStarted() {
        this.status = Status.DOWNLOADING;
        this.startedAt = LocalDateTime.now();
    }
    
    /**
     * Marks the task as completed.
     */
    public void markCompleted() {
        this.status = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.progress = 1.0;
    }
    
    /**
     * Marks the task as failed.
     * 
     * @param errorMessage Error message describing the failure
     */
    public void markFailed(String errorMessage) {
        this.status = Status.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Gets a formatted progress string.
     * 
     * @return Progress as percentage string
     */
    public String getProgressString() {
        return String.format("%.1f%%", progress * 100);
    }
    
    /**
     * Gets formatted file size string.
     * 
     * @return File size in human readable format
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Gets formatted download speed.
     * 
     * @return Download speed in human readable format
     */
    public String getFormattedSpeed() {
        if (startedAt == null) {
            return "0 B/s";
        }
        
        long elapsedSeconds = java.time.Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        if (elapsedSeconds == 0) {
            return "0 B/s";
        }
        
        long bytesPerSecond = downloadedBytes / elapsedSeconds;
        
        if (bytesPerSecond < 1024) {
            return bytesPerSecond + " B/s";
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format("%.1f KB/s", bytesPerSecond / 1024.0);
        } else {
            return String.format("%.1f MB/s", bytesPerSecond / (1024.0 * 1024.0));
        }
    }
} 