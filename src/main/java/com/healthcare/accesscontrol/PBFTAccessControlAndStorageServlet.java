package com.healthcare.accesscontrol;

import com.healthcare.accessdelegation.DelegatorNode;
import com.healthcare.accessdelegation.NodeManager;
import com.healthcare.accessdelegation.RHSOAlgorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

            // Fetch data from EdgeNodeServlet
            String edgeNodeData = fetchDataFromEdgeNode();
            System.out.println("Fetched Edge Node Data: " + edgeNodeData); // Debug log

            if (edgeNodeData == null || edgeNodeData.trim().isEmpty()) {
                out.println("<p>Error: No data received from EdgeNodeServlet.</p>");
                System.err.println("Error: No data received from EdgeNodeServlet.");
                return;
            }

            // Retrieve parameters from the request
            String userId = request.getParameter("userId");
            String patientData = request.getParameter("patientData");

            if (userId == null || patientData == null || userId.trim().isEmpty() || patientData.trim().isEmpty()) {
                out.println("Error: Missing or invalid parameters.");
                System.err.println("Error: Missing or invalid parameters (userId or patientData).");
                return;
            }

            System.out.println("User ID: " + userId);
            System.out.println("Patient Data: " + patientData);

            // Get available delegator nodes via NodeManager
            NodeManager nm = new NodeManager();
            List<DelegatorNode> availableNodes = nm.getAvailableNodes();
            System.out.println("Available Delegator Nodes: " + availableNodes.size());

            // Use RHSOAlgorithm to select the best nodes
            RHSOAlgorithm rhso = new RHSOAlgorithm();
            List<DelegatorNode> selectedNodes = rhso.selectDelegatorNodes(availableNodes);
            System.out.println("Selected Delegator Nodes: " + selectedNodes.size());

            // Execute PBFT consensus using the selected nodes
            PBFTConsensus pbft = new PBFTConsensus();
            boolean accessGranted = pbft.executeConsensus(selectedNodes, patientData, userId);

            out.println("<html><head><title>Access Control & Storage Result</title></head><body>");
            out.println("<h2>PBFT Access Control Decision</h2>");

            if (accessGranted) {
                out.println("<p>Access granted to user: <strong>" + userId + "</strong></p>");
                System.out.println("Access granted for user: " + userId);

                // Store patient data in CloudData table
                storeDataInCloudDB(userId, patientData);

                // Compute the SHA-256 hash of the patient data
                String dataHash = computeSHA256Hash(patientData);

                // Log the hash in the BlockchainLog table
                logTransactionToBlockchainDB(userId, dataHash);

                // Also log via the mock blockchain service
                MockBlockchainService mbs = new MockBlockchainService();
                mbs.logTransaction(userId, dataHash);

                out.println("<p>Data has been stored in the cloud and its hash logged on the blockchain.</p>");
                out.println("<h3>Fetched Data from EdgeNodeServlet:</h3>");
                out.println("<p>" + edgeNodeData + "</p>");
            } else {
                out.println("<p>Access control consensus did not grant access; data was not stored.</p>");
                System.err.println("Access denied by PBFT consensus.");
            }
            out.println("</body></html>");
        }
    }

    // Fetch data from EdgeNodeServlet
    private String fetchDataFromEdgeNode() {
        HttpURLConnection conn = null;
        try {
            URL url = new URI("http://localhost:8080/SmartHealthcareSystemnew/EdgeNodeServletdatarequest").toURL();

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
                System.out.println("Content Length: " + conn.getContentLength());
                return response.toString();
            }
        } catch (Exception e) {
            System.err.println("Error fetching data from EdgeNodeServlet: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String computeSHA256Hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            System.err.println("Error computing SHA-256 hash: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void storeDataInCloudDB(String userId, String patientData) {
        String sql = "INSERT INTO CloudData (userId, patientData) VALUES (?, ?)";
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, patientData);
            int rowsAffected = ps.executeUpdate();
            System.out.println("CloudData table updated. Rows inserted: " + rowsAffected);
        } catch (Exception e) {
            System.err.println("Error storing data in CloudData table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logTransactionToBlockchainDB(String userId, String dataHash) {
        String sql = "INSERT INTO BlockchainLog (userId, dataHash) VALUES (?, ?)";
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, dataHash);
            int rowsAffected = ps.executeUpdate();
            System.out.println("BlockchainLog table updated. Rows inserted: " + rowsAffected);
        } catch (Exception e) {
            System.err.println("Error logging transaction to BlockchainLog table: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
