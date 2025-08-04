package com.downloadmanager.service;

import com.downloadmanager.model.DownloadTask;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Service that handles the actual file downloads.
 */
@Service
@Slf4j
public class DownloadService {
    
    private final OkHttpClient httpClient;
    private final String downloadDirectory;
    
    public DownloadService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        
        this.downloadDirectory = "downloads";
        createDownloadDirectory();
    }
    
    /**
     * Downloads a file from the given task.
     * 
     * @param task The download task
     * @throws IOException if download fails
     */
    public void downloadFile(DownloadTask task) throws IOException {
        log.info("Starting download for task {}: {}", task.getId(), task.getUrl());
        
        // Mark task as started
        task.markStarted();
        
        // Get filename from URL
        String filename = getFilenameFromUrl(task.getUrl());
        task.setFilename(filename);
        
        // Create download path
        Path downloadPath = Paths.get(downloadDirectory, filename);
        
        // Ensure unique filename
        downloadPath = ensureUniqueFilename(downloadPath);
        task.setFilename(downloadPath.getFileName().toString());
        
        // Create HTTP request
        Request request = new Request.Builder()
                .url(task.getUrl())
                .addHeader("User-Agent", "DownloadManager/1.0")
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " + response.message());
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("No response body");
            }
            
            long contentLength = body.contentLength();
            task.updateProgress(0, contentLength);
            
            log.info("Downloading {} ({} bytes) to {}", filename, contentLength, downloadPath);
            
            // Download with progress tracking
            try (InputStream inputStream = body.byteStream();
                 FileOutputStream outputStream = new FileOutputStream(downloadPath.toFile())) {
                
                byte[] buffer = new byte[8192];
                long downloadedBytes = 0;
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    downloadedBytes += bytesRead;
                    
                    // Update progress
                    task.updateProgress(downloadedBytes, contentLength);
                    
                    // Log progress every 1MB
                    if (downloadedBytes % (1024 * 1024) == 0) {
                        log.info("Task {}: {} / {} ({}%)", 
                                task.getId(), 
                                task.getFormattedFileSize(), 
                                task.getFormattedSpeed(),
                                task.getProgressString());
                    }
                }
                
                outputStream.flush();
            }
            
            // Mark as completed
            task.markCompleted();
            log.info("Download completed for task {}: {} -> {}", 
                    task.getId(), task.getUrl(), downloadPath);
            
        } catch (Exception e) {
            log.error("Download failed for task {}: {}", task.getId(), task.getUrl(), e);
            task.markFailed(e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extracts filename from URL.
     * 
     * @param url The URL
     * @return The filename
     */
    private String getFilenameFromUrl(String url) {
        try {
            String path = new URL(url).getPath();
            String filename = path.substring(path.lastIndexOf('/') + 1);
            
            if (filename.isEmpty()) {
                filename = "download_" + System.currentTimeMillis();
            }
            
            return filename;
        } catch (Exception e) {
            return "download_" + System.currentTimeMillis();
        }
    }
    
    /**
     * Ensures the filename is unique by adding a number if necessary.
     * 
     * @param path The original path
     * @return The unique path
     */
    private Path ensureUniqueFilename(Path path) {
        if (!Files.exists(path)) {
            return path;
        }
        
        String baseName = path.getFileName().toString();
        String extension = "";
        String nameWithoutExtension = baseName;
        
        int lastDot = baseName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = baseName.substring(lastDot);
            nameWithoutExtension = baseName.substring(0, lastDot);
        }
        
        int counter = 1;
        Path uniquePath = path;
        
        while (Files.exists(uniquePath)) {
            String newName = nameWithoutExtension + " (" + counter + ")" + extension;
            uniquePath = path.getParent().resolve(newName);
            counter++;
        }
        
        return uniquePath;
    }
    
    /**
     * Creates the download directory if it doesn't exist.
     */
    private void createDownloadDirectory() {
        try {
            Path dir = Paths.get(downloadDirectory);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("Created download directory: {}", dir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create download directory", e);
        }
    }
    
    /**
     * Gets the download directory path.
     * 
     * @return The download directory path
     */
    public String getDownloadDirectory() {
        return downloadDirectory;
    }
} 