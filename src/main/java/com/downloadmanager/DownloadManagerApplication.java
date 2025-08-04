package com.downloadmanager;

import com.downloadmanager.service.DownloadQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Download Manager.
 * Accepts URLs as command line arguments and adds them to an async download queue.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@Slf4j
public class DownloadManagerApplication implements CommandLineRunner {

    private final DownloadQueueService downloadQueueService;
    
    public DownloadManagerApplication(DownloadQueueService downloadQueueService) {
        this.downloadQueueService = downloadQueueService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DownloadManagerApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            log.error("At least one URL is required as a command line argument");
            log.info("Usage: java -jar download-manager.jar <url1> [url2] [url3] ...");
            log.info("Example: java -jar download-manager.jar https://example.com/file1.zip https://example.com/file2.pdf");
            System.exit(1);
        }
        
        log.info("Starting Download Manager with {} URLs", args.length);
        
        try {
            // Add each URL to the download queue
            for (String url : args) {
                log.info("Adding URL to download queue: {}", url);
                downloadQueueService.addToQueue(url);
            }
            
            log.info("All URLs have been added to the download queue");
            log.info("Downloads will be processed asynchronously in the background");
            log.info("Use Ctrl+C to stop the application (downloads will continue until completion)");
            
            // Keep the application running to allow downloads to complete
            // In a real application, you might want to add a shutdown hook or monitoring
            Thread.currentThread().join();
            
        } catch (InterruptedException e) {
            log.info("Application interrupted, shutting down gracefully");
        } catch (Exception e) {
            log.error("Failed to process URLs", e);
            System.exit(1);
        }
    }
} 