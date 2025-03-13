package com.healthcare.blockchain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/BlockchainServlet")
public class BlockchainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Local file path where records are first appended.
    private static final String LOCAL_FILE_PATH = "C:\\TeamData\\newblockchainlog.txt";
    // Network-attached shared folder file path.
    private static final String SHARED_FILE_PATH = "\\\\10.242.34.96\\SmartHealthcareSystemSparse\\blockchainlog.txt";
    
    // Genesis hash for the blockchain
    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().println("BlockchainServlet is up!");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Retrieve parameters sent by the caller
        String userId = request.getParameter("userId");
        String newDataHash = request.getParameter("dataHash");
        String timestampStr = request.getParameter("timestamp");
        
        if (userId == null || newDataHash == null || timestampStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters.");
            return;
        }
        
        // Convert timestamp from milliseconds to human-readable format
        long timestamp = Long.parseLong(timestampStr);
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(date);
        
        // Fetch the previous combined hash from the local file
        String previousHash = fetchLatestLocalHash();
        if (previousHash == null || previousHash.isEmpty()) {
            previousHash = GENESIS_HASH;
        }
        
        // Combine the previous hash with the new data hash
        String combinedData = previousHash + newDataHash;
        String newCombinedHash = computeSHA256(combinedData);
        
        // Prepare metadata and the new record
        String metadata = "User: " + userId + ", Timestamp: " + formattedDate;
        String record = "Combined Hash: " + newCombinedHash + "\n" +
                        metadata + "\n" +
                        "------------------------------\n";
        
        // Append the new record to the local file
        appendRecordToLocalFile(record);
        // After updating the local file, copy it to the shared folder.
        updateSharedFolder();
        
        // Note: Insertion into the BlockchainLog database has been removed.
        
        response.getWriter().println("Blockchain updated with new hash record.");
    }
    
    // Computes SHA-256 hash for the given string.
    private String computeSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Reads the local file to fetch the latest "Combined Hash".
    private String fetchLatestLocalHash() {
        File file = new File(LOCAL_FILE_PATH);
        String lastHash = "";
        if (!file.exists()) {
            return lastHash;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Combined Hash:")) {
                    lastHash = line.substring("Combined Hash:".length()).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastHash;
    }
    
    // Appends a new record to the local file.
    private void appendRecordToLocalFile(String record) {
        File file = new File(LOCAL_FILE_PATH);
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(record);
            System.out.println("Appended new record to local file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Copies the local file to the network-attached shared folder.
    private void updateSharedFolder() {
        File localFile = new File(LOCAL_FILE_PATH);
        File sharedFile = new File(SHARED_FILE_PATH);
        try {
            Files.copy(localFile.toPath(), sharedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Updated shared folder file with the latest local file content.");
        } catch (IOException e) {
            System.err.println("Failed to update shared folder file: " + e.getMessage());
        }
    }
}




































/*package com.healthcare.blockchain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;

@WebServlet("/BlockchainServlet")
public class BlockchainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // GitHub repository and local clone configuration
    private static final String LOCAL_REPO_PATH = "C:/Users/ABHISHEK/SmartHealthcareSystemSparse"; // Local sparse-checkout folder
    private static final String GITHUB_FILE_PATH = "blockchainlog.txt"; // File where blockchain records are stored
    private static final String GITHUB_USERNAME = "mvsabhishek96"; // <-- Replace with your GitHub username
    private static final String GITHUB_TOKEN = "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"; // <-- Replace with your GitHub personal access token
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db"; 
    private static final String DB_USER = "root"; 
    private static final String DB_PASSWORD = ""; 
    
    // Genesis hash
    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().println("BlockchainServlet is up!");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Retrieve parameters sent by PBFTAccessControlAndStorageServlet
        String userId = request.getParameter("userId");
        String newDataHash = request.getParameter("dataHash");
        String timestampStr = request.getParameter("timestamp");
        
        if (userId == null || newDataHash == null || timestampStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters.");
            return;
        }

        // Convert timestamp from milliseconds to human-readable format
        long timestamp = Long.parseLong(timestampStr);
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(date);

        // Fetch the previous combined hash from the GitHub-backed log file
        String previousHash = fetchLatestGithubHash();
        if (previousHash == null || previousHash.isEmpty()) {
            previousHash = GENESIS_HASH;
        }

        // Combine the previous hash with the new patient data hash
        String combinedData = previousHash + newDataHash;
        String newCombinedHash = computeSHA256(combinedData);

        // Prepare metadata with formatted timestamp
        String metadata = "User: " + userId + ", Timestamp: " + formattedDate;
        String record = "Combined Hash: " + newCombinedHash + "\n" +
                        metadata + "\n" +
                        "------------------------------\n";

        // Update GitHub repository with the new blockchain record
        updateGithubWithNewHash(record);

        // Log the new combined hash into the BlockchainLog table
        insertNewBlockchainRecord(userId, newCombinedHash, formattedDate);

        response.getWriter().println("Blockchain updated with new hash record.");
    }

    // Compute SHA-256 hash for a given string
    private String computeSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fetch the latest "Combined Hash" from the GitHub log file
    private String fetchLatestGithubHash() {
        String filePath = LOCAL_REPO_PATH + "/" + GITHUB_FILE_PATH;
        System.out.println("Reading blockchain log file from: " + new File(filePath).getAbsolutePath());
        String lastHash = "";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Combined Hash:")) {
                    lastHash = line.substring("Combined Hash:".length()).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastHash;
    }

    // Update the GitHub repository by appending the new blockchain record
    private void updateGithubWithNewHash(String record) {
        try {
            Git git = Git.open(new File(LOCAL_REPO_PATH));
            File file = new File(LOCAL_REPO_PATH + "/" + GITHUB_FILE_PATH);
            try (FileWriter fw = new FileWriter(file, true)) {
                fw.write(record);
            }
            git.add().addFilepattern(GITHUB_FILE_PATH).call();
            git.commit().setMessage("Updated blockchain log with new record").call();
            CredentialsProvider cp = new UsernamePasswordCredentialsProvider(GITHUB_USERNAME, GITHUB_TOKEN);
            git.push().setCredentialsProvider(cp).call();
            System.out.println("GitHub repository updated with new blockchain record.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Insert the new blockchain record into the BlockchainLog table
    private void insertNewBlockchainRecord(String userId, String newCombinedHash, String timestamp) {
        String sql = "INSERT INTO BlockchainLog (userId, dataHash, timestamp) VALUES (?, ?, ?)";
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, newCombinedHash);
            ps.setString(3, timestamp);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
*/
