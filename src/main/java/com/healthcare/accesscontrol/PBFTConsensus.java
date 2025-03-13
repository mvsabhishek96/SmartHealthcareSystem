package com.healthcare.accesscontrol;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

public class PBFTConsensus {
    private static final String[] TEAM_NODES = {
        "http://10.242.34.96:8080/SmartHealthcareSystemnew",
        "http://10.242.34.96:8080/SmartHealthcareSystemnew",
        "http://10.242.34.96:8080/SmartHealthcareSystemnew",
        "http://10.242.34.96:8080/SmartHealthcareSystemnew"
    };

    // Modified method signature to include patientData and userId
    public boolean executeConsensus(String patientData, String userId) {
        System.out.println("Executing consensus for user: " + userId + " with patient data: " + patientData);
        int totalNodes = TEAM_NODES.length;
        int faultTolerance = (totalNodes - 1) / 3;
        int requiredConsensus = totalNodes - faultTolerance;

        AtomicInteger agreeCount = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();

        // Create and start threads to check each node
        for (String nodeUrl : TEAM_NODES) {
            Thread t = new Thread(() -> {
                if (isNodeOnline(nodeUrl)) {
                    agreeCount.incrementAndGet();
                }
            });
            threads.add(t);
            t.start();
        }

        // Wait for each thread to complete, up to 10 seconds per thread.
        for (Thread t : threads) {
            try {
                t.join(10000); // 10-second timeout for thread synchronization
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted while waiting: " + e.getMessage());
            }
        }

        return agreeCount.get() >= requiredConsensus;
        //return true;
    }

    private boolean isNodeOnline(String nodeUrl) {
        try {
            URI uri = URI.create(nodeUrl + "/health");
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Failed to reach node: " + nodeUrl + " Response: " + responseCode);
            }
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            System.err.println("IOException while reaching node: " + nodeUrl + " Message: " + e.getMessage());
            return false;
        }
    }
}


































/*package com.healthcare.accesscontrol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PBFTConsensus {
    // List of team node endpoints (replace with actual VPN IPs/hostnames)
    private static final String[] TEAM_NODES = {
    		"http://10.242.34.96:8080/SmartHealthcareSystemnew",
    	    "http://10.242.20.107:8080/SmartHealthcareSystemnew",
    	    "http://10.242.38.191:8080/SmartHealthcareSystemnew",
    	    "http://10.242.34.96:8080/SmartHealthcareSystemnew"
    };

    // Shared folder path (using the ZeroTier IP provided shared folder)
    private static final String SHARED_FOLDER = "\\\\10.242.34.96\\SmartHealthcareSystemSparse\\blockchainlog.txt";


    // Scheduled executor for periodic tasks
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Variable to store the last modified timestamp of the file for polling
    private long lastModifiedTime = 0;

    // Constructor: Starts node availability checks and file polling upon creation.
    public PBFTConsensus() {
        //startNodeAvailabilityChecks();
        startFilePolling();
    }

    /**
     * Executes the PBFT consensus algorithm by communicating with real nodes.
     *
     * @param data The data (e.g., patient data) under consideration.
     * @param accessRequest The access request (e.g., user ID).
     * @return true if consensus is achieved; false otherwise.
     */

    /*public boolean executeConsensus(String data, String accessRequest) {
        int totalNodes = TEAM_NODES.length;
        int faultTolerance = (totalNodes - 1) / 3; // Allow up to (totalNodes-1)/3 faulty nodes.
        int requiredConsensus = totalNodes - faultTolerance;

        AtomicInteger agreeCount = new AtomicInteger(0);

        // Send access request to all nodes
        for (String nodeUrl : TEAM_NODES) {
            if (isNodeOnline(nodeUrl)) {
                new Thread(() -> {
                    try {
                        // Create URI then convert to URL for the /access-decision endpoint
                        URI uri = URI.create(nodeUrl + "/access-decision");
                        URL url = uri.toURL();
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setConnectTimeout(3000);
                        conn.setReadTimeout(3000);

                        // Send data and accessRequest as part of the request body
                        String requestBody = "data=" + data + "&accessRequest=" + accessRequest;
                        conn.getOutputStream().write(requestBody.getBytes());

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            // Read the response (assuming 'true' or 'false')
                            boolean decision = Boolean.parseBoolean(new String(conn.getInputStream().readAllBytes()));
                            if (decision) {
                                agreeCount.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to get decision from node " + nodeUrl + ": " + e.getMessage());
                    }
                }).start();
            }
        }

        // Wait for responses (this is a simple implementation; consider using more robust synchronization)
        try {
            Thread.sleep(5000); // Wait for 5 seconds for all nodes to respond
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //return agreeCount.get() >= requiredConsensus;
        return true;
    }

    // Starts periodic checks of node availability every second.
   /* private void startNodeAvailabilityChecks() {
        final Runnable checker = () -> {
            int availableNodes = checkAvailableNodes();
            int totalNodes = TEAM_NODES.length;
            System.out.println("Available nodes: " + availableNodes + "/" + totalNodes);
        };
        scheduler.scheduleAtFixedRate(checker, 0, 1, TimeUnit.SECONDS);
    }

    // Checks the number of available nodes by pinging their /health endpoint.
    private int checkAvailableNodes() {
        int availableCount = 0;
        for (String nodeUrl : TEAM_NODES) {
            if (isNodeOnline(nodeUrl)) {
                availableCount++;
            }
        }
        return availableCount;
    }*/

    // Checks if a node is online by sending an HTTP GET to its /health endpoint.
    /*private boolean isNodeOnline(String nodeUrl) {
        try {
            URI uri = URI.create(nodeUrl + "/health");
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }

    // Distributes the blockchain log to all available nodes.
    private void distributeBlockchainLog() {
        File sourceFile = new File(SHARED_FOLDER);
        for (String nodeUrl : TEAM_NODES) {
            if (isNodeOnline(nodeUrl)) {
                new Thread(() -> {
                    try {
                        URI uri = URI.create(nodeUrl + "/update-blockchain");
                        URL url = uri.toURL();
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        // Read and send file bytes
                        byte[] fileBytes = Files.readAllBytes(sourceFile.toPath());
                        conn.getOutputStream().write(fileBytes);
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            System.out.println("Updated blockchain log on node: " + nodeUrl);
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to update node " + nodeUrl + ": " + e.getMessage());
                    }
                }).start();
            }
        }
    }

    // Polls the shared file for changes by checking its last modified timestamp.
    private void startFilePolling() {
        Runnable poller = () -> {
            File file = new File(SHARED_FOLDER);
            long currentModifiedTime = file.lastModified();
            // If the file's timestamp has increased, assume a change and trigger distribution
            if (currentModifiedTime > lastModifiedTime) {
                lastModifiedTime = currentModifiedTime;
                System.out.println("blockchainlog.txt has been modified (detected via polling).");
                distributeBlockchainLog();
            }
        };
        // Schedule the polling task to run every second
        scheduler.scheduleAtFixedRate(poller, 0, 1, TimeUnit.SECONDS);
    }
}*/
