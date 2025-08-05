package com.downloadmanager.service;

import com.downloadmanager.model.FetchDataCount;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * Service that handles the actual downloading of data list.
 */
@Service
@Slf4j
public class DataCountFetchService {
    private final OkHttpClient httpClient;
    private final TokenService tokenService;

    @Autowired
    public DataCountFetchService(OkHttpClient client, TokenService tokenService) {
        this.httpClient = client;
        this.tokenService = tokenService;
    }
    
    /**
     * Downloads a count of items from the given job.
     * 
     * @param job The download job
     */
    public void fetch(FetchDataCount job) throws IOException {
        log.info("Starting download for task {}: {}", job.getId(), job.getUrl());
        
        // Mark job as started
        job.markStarted();

        // Create HTTP request
        Request request = new Request.Builder()
                .url(job.getUrl())
                .addHeader("User-Agent", "DownloadManager/1.0")
                .addHeader("Authorization", "Bearer " + tokenService.getToken(job.getOrg()))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " + response.message());
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("No response body");
            }

            // read the response header and extract the LINK header
            

            // Mark as completed
            job.markCompleted();
            log.info("Download completed for job {}: {}",
                    job.getId(), job.getUrl());
            
        } catch (Exception e) {
            log.error("Download failed for task {}: {}", job.getId(), job.getUrl(), e);
            job.markFailed(e.getMessage());
            throw e;
        }
    }
} 