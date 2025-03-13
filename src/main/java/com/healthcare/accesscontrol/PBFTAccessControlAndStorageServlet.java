package com.healthcare.accesscontrol;

import com.healthcare.accessdelegation.DelegatorNode;
import com.healthcare.accessdelegation.NodeManager;
import com.healthcare.accessdelegation.RHSOAlgorithm;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;


@WebServlet("/PBFTAccessControlAndStorageServlet")
public class PBFTAccessControlAndStorageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        try (PrintWriter out = response.getWriter()) {
            System.out.println("Starting PBFT Access Control and Storage process...");
            
            // Fetch data from EdgeNodeServlet (assumed to be valid)
            String edgeNodeData = fetchDataFromEdgeNode();
            System.out.println("Fetched Edge Node Data: " + edgeNodeData);
            if (edgeNodeData == null || edgeNodeData.trim().isEmpty()) {
                out.println("<p>Error: No data received from EdgeNodeServlet.</p>");
                return;
            }
            
            // Retrieve parameters from the request
            String userId = request.getParameter("userId");
            String patientData = request.getParameter("patientData");
            if (userId == null || patientData == null || userId.trim().isEmpty() || patientData.trim().isEmpty()) {
                out.println("<p>Error: Missing or invalid parameters.</p>");
                return;
            }
            System.out.println("User ID: " + userId);
            System.out.println("Patient Data: " + patientData);
            
            // Execute PBFT consensus using available delegator nodes
           /* NodeManager nm = new NodeManager();
            List<DelegatorNode> availableNodes = nm.getAvailableNodes();
            RHSOAlgorithm rhso = new RHSOAlgorithm();
            List<DelegatorNode> selectedNodes = rhso.selectDelegatorNodes(availableNodes);*/
            PBFTConsensus pbft = new PBFTConsensus();
           // boolean accessGranted = pbft.executeConsensus(selectedNodes, patientData, userId);
            boolean accessGranted = pbft.executeConsensus(patientData, userId);

            out.println("<html><head><title>Access Control & Storage Result</title></head><body>");
            if (accessGranted) {
                out.println("<p>Access granted to user: <strong>" + userId + "</strong></p>");
                System.out.println("Access granted for user: " + userId);
                
                // Store patient data in CloudData table
                storeDataInCloudDB(userId, patientData);
                System.out.println("CloudData insertion executed for user: " + userId);
                // Compute the SHA-256 hash of the patient data
                String dataHash = computeSHA256(patientData);
                System.out.println("Patient Data Hash: " + dataHash);
                
                // Log the transaction in the BlockchainLog table
                logTransactionToBlockchainDB(userId, dataHash);
                
                // Notify the BlockchainServlet that a new hash is generated.
                notifyBlockchainServlet(userId, dataHash);
                
                out.println("<p>Patient data stored and new hash notification sent to BlockchainServlet.</p>");
                out.println("<h3>Fetched Data from EdgeNodeServlet:</h3>");
                out.println("<p>" + edgeNodeData + "</p>");
            } else {
                out.println("<p>Access control consensus did not grant access; data was not stored.</p>");
            }
            out.println("</body></html>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Sample method to fetch data from EdgeNodeServlet.
   private String fetchDataFromEdgeNode() {
        HttpURLConnection conn = null;
        try {
        	URI uri = URI.create("http://localhost:8080/SmartHealthcareSystemnew/EdgeNodeServletdatarequest");
        	URL url = uri.toURL();

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/plain");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int responseCode = conn.getResponseCode();
            System.out.println("EdgeNodeServlet Response Code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Failed to fetch data from EdgeNodeServlet. HTTP Code: " + responseCode);
                return null;
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) { 
                conn.disconnect();
            }
        }
    }
    
    // Compute SHA-256 hash of given data.
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
    
    // Store patient data in CloudData table.
    private void storeDataInCloudDB(String userId, String patientData) {
        String sql = "INSERT INTO CloudData (userId, patientData) VALUES (?, ?)";
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
        	System.out.println("Attempting to insert into CloudData for user: " + userId + " with patientData: " + patientData);
            ps.setString(1, userId);
            ps.setString(2, patientData);
            //ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            int rowsAffected = ps.executeUpdate();
            System.out.println("CloudData table updated. Rows inserted: " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Log the transaction in the BlockchainLog table.
    private void logTransactionToBlockchainDB(String userId, String dataHash) {
        String sql = "INSERT INTO BlockchainLog (userId, dataHash) VALUES (?, ?)";
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, dataHash);
            int rowsAffected = ps.executeUpdate();
            System.out.println("BlockchainLog table updated. Rows inserted: " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Notify BlockchainServlet via HTTP POST.
    private void notifyBlockchainServlet(String userId, String dataHash) {
    	String blockchainServletURL = "http://localhost:8080/SmartHealthcareSystemnew/BlockchainServlet";

        try {
        	URI uri = URI.create(blockchainServletURL);
        	URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String urlParameters = "userId=" + URLEncoder.encode(userId, "UTF-8") +
                                   "&dataHash=" + URLEncoder.encode(dataHash, "UTF-8") +
                                   "&timestamp=" + System.currentTimeMillis();
            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                out.writeBytes(urlParameters);
                out.flush();
            }
            int responseCode = conn.getResponseCode();
            System.out.println("Notified BlockchainServlet; response code: " + responseCode);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
