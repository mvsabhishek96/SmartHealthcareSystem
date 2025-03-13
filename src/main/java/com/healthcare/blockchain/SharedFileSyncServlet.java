package com.healthcare.blockchain;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebServlet(value = "/SharedFileSyncServlet", loadOnStartup = 1)
public class SharedFileSyncServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    // Path to the shared file on the network-attached storage.
    private static final String SHARED_FILE_PATH = "\\\\10.242.34.96\\SmartHealthcareSystemSparse\\blockchainlog.txt";
    // Local file path where the file will be downloaded.
    private static final String LOCAL_FILE_PATH = "C:\\\\TeamData\\\\newblockchainlog.txt";
    
    // Store the last modified timestamp of the shared file.
    private volatile long lastModifiedTime = 0;
    private ScheduledExecutorService scheduler;

    @Override
    public void init() throws ServletException {
        // Create a single-threaded scheduler that polls every second.
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                File sharedFile = new File(SHARED_FILE_PATH);
                if (sharedFile.exists()) {
                    long currentModifiedTime = sharedFile.lastModified();
                    // If the shared file has been updated since the last sync, copy it locally.
                    if (currentModifiedTime > lastModifiedTime) {
                        File localFile = new File(LOCAL_FILE_PATH);
                        Files.copy(sharedFile.toPath(), localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        lastModifiedTime = currentModifiedTime;
                        System.out.println("[" + new Date() + "] Local file updated from shared file.");
                    }
                } else {
                    System.out.println("Shared file does not exist: " + SHARED_FILE_PATH);
                }
            } catch (IOException e) {
                System.err.println("Error syncing shared file: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
        
        System.out.println("SharedFileSyncServlet started and polling every second.");
    }

    @Override
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        super.destroy();
    }
}
