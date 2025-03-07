package com.healthcare.accesscontrol;

import com.healthcare.accessdelegation.DelegatorNode;
import com.healthcare.accessdelegation.NodeManager;
import com.healthcare.accessdelegation.RHSOAlgorithm;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
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
    private static final String DB_USER = "dbuser";
    private static final String DB_PASSWORD = "dbpassword";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Retrieve parameters
        String userId = request.getParameter("userId");
        String patientData = request.getParameter("patientData");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        if (userId == null || patientData == null || userId.trim().isEmpty() || patientData.trim().isEmpty()) {
            out.println("Error: Missing or invalid parameters.");
            return;
        }
        
        // Get available delegator nodes via NodeManager
        NodeManager nm = new NodeManager();
        List<DelegatorNode> availableNodes = nm.getAvailableNodes();
        
        // Use RHSOAlgorithm to select the best nodes
        RHSOAlgorithm rhso = new RHSOAlgorithm();
        List<DelegatorNode> selectedNodes = rhso.selectDelegatorNodes(availableNodes);
        
        // Execute PBFT consensus using the selected nodes
        PBFTConsensus pbft = new PBFTConsensus();
        boolean accessGranted = pbft.executeConsensus(selectedNodes, patientData, userId);
        
        out.println("<html><head><title>Access Control & Storage Result</title></head><body>");
        out.println("<h2>PBFT Access Control Decision</h2>");
        
        if (accessGranted) {
            out.println("<p>Access granted to user: <strong>" + userId + "</strong></p>");
            // Store the patient data in the CloudData table.
            storeDataInCloudDB(userId, patientData);
            // Compute SHA-256 hash of the patient data.
            String dataHash = computeSHA256Hash(patientData);
            // Log the hash in the BlockchainLog table.
            logTransactionToBlockchainDB(userId, dataHash);
            // Also log via the mock blockchain service.
            MockBlockchainService mbs = new MockBlockchainService();
            mbs.logTransaction(userId, dataHash);
            
            out.println("<p>Patient data has been stored in the cloud and its hash logged on the blockchain.</p>");
        } else {
            out.println("<p>Access control consensus did not grant access; data was not stored.</p>");
        }
        out.println("</body></html>");
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
            e.printStackTrace();
            return null;
        }
    }
    
    private void storeDataInCloudDB(String userId, String patientData) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO CloudData (userId, patientData) VALUES (?, ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setString(2, patientData);
            ps.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(ps != null) try { ps.close(); } catch(Exception e){}
            if(con != null) try { con.close(); } catch(Exception e){}
        }
    }
    
    private void logTransactionToBlockchainDB(String userId, String dataHash) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO BlockchainLog (userId, dataHash) VALUES (?, ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setString(2, dataHash);
            ps.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(ps != null) try { ps.close(); } catch(Exception e){}
            if(con != null) try { con.close(); } catch(Exception e){}
        }
    }
}
