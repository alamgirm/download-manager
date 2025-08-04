package com.downloadmanager.service;

import com.downloadmanager.model.DownloadTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service that manages the download queue and coordinates parallel downloads.
 * Uses queue management system.
 */
@Service
@Slf4j
public class DownloadQueueService {
    
    private final BlockingQueue<DownloadTask> downloadQueue;
    private final DownloadService downloadService;
    private final AtomicInteger taskIdCounter;
    private final int maxConcurrentDownloads;
    
    @Autowired
    public DownloadQueueService(DownloadService downloadService) {
        this.downloadQueue = new LinkedBlockingQueue<>();
        this.downloadService = downloadService;
        this.taskIdCounter = new AtomicInteger(1);
        this.maxConcurrentDownloads = 3; // Configurable
        
        // Start the download processor
        startDownloadProcessor();
    }
    
    /**
     * Adds a URL to the download queue.
     * 
     * @param url The URL to download
     */
    public void addToQueue(String url) {
        DownloadTask task = new DownloadTask(taskIdCounter.getAndIncrement(), url);
        downloadQueue.offer(task);
        log.info("Added download task {} to queue: {}", task.getId(), url);
    }
    
    /**
     * Starts the download processor that continuously processes queued downloads.
     */
    private void startDownloadProcessor() {
        Thread processorThread = new Thread(() -> {
            log.info("Starting download processor with max {} concurrent downloads", maxConcurrentDownloads);
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Take a task from the queue (blocks if queue is empty)
                    DownloadTask task = downloadQueue.take();
                    log.info("Processing download task {}: {}", task.getId(), task.getUrl());
                    
                    // Process the download asynchronously
                    processDownloadAsync(task);
                    
                } catch (InterruptedException e) {
                    log.info("Download processor interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in download processor", e);
                }
            }
        });
        
        processorThread.setName("DownloadProcessor");
        processorThread.setDaemon(true);
        processorThread.start();
    }
    
    /**
     * Processes a download task asynchronously.
     * 
     * @param task The download task to process
     */
    @Async
    public void processDownloadAsync(DownloadTask task) {
        try {
            log.info("Starting download for task {}: {}", task.getId(), task.getUrl());
            
            // Update task status
            task.setStatus(DownloadTask.Status.DOWNLOADING);
            
            // Perform the actual download
            downloadService.downloadFile(task);
            
            // Mark task as completed
            task.setStatus(DownloadTask.Status.COMPLETED);
            log.info("Download completed for task {}: {}", task.getId(), task.getUrl());
            
        } catch (Exception e) {
            log.error("Download failed for task {}: {}", task.getId(), task.getUrl(), e);
            task.setStatus(DownloadTask.Status.FAILED);
            task.setErrorMessage(e.getMessage());
        }
    }
    
    /**
     * Gets the current queue size.
     * 
     * @return Number of tasks in the queue
     */
    public int getQueueSize() {
        return downloadQueue.size();
    }
    
    /**
     * Gets the maximum number of concurrent downloads.
     * 
     * @return Max concurrent downloads
     */
    public int getMaxConcurrentDownloads() {
        return maxConcurrentDownloads;
    }
} 