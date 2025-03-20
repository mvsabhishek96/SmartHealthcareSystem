package com.healthcare.fileaccess;

import com.healthcare.accessdelegation.RHSOAlgorithm;
import com.healthcare.accessdelegation.DelegatorNode;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.net.HttpURLConnection;

@WebServlet("/FileAccessRequestServlet")
public class FileAccessRequestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // One hour in milliseconds
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileIdStr = request.getParameter("fileId");
        // Check if AJAX parameter is provided
        String ajaxParam = request.getParameter("ajax");
        boolean isAjax = "true".equalsIgnoreCase(ajaxParam);
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.getWriter().println("Error: User not logged in.");
            return;
        }
        String patientUsername = (String) session.getAttribute("username");
        
        if (fileIdStr == null) {
            response.getWriter().println("Error: Missing fileId parameter.");
            return;
        }
        
        int fileId = Integer.parseInt(fileIdStr);
        String subType = null;
        Timestamp lastDownloaded = null;
        
        // Query using patientUsername
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(
               "SELECT submissionType, lastDownloaded FROM clouddata WHERE id = ? AND patientUsername = ?")) {
            
            ps.setInt(1, fileId);
            ps.setString(2, patientUsername);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                subType = rs.getString("submissionType");
                lastDownloaded = rs.getTimestamp("lastDownloaded");
            } else {
                response.getWriter().println("Error: Record not found.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
            return;
        }
        
        boolean allowAccess = false;
        if ("doctor".equals(subType)) {
            allowAccess = true;
        } else if ("downloaded".equals(subType) && lastDownloaded != null) {
            long elapsed = System.currentTimeMillis() - lastDownloaded.getTime();
            if (elapsed >= ONE_HOUR_MS) {
                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement psReset = con.prepareStatement(
                       "UPDATE clouddata SET submissionType = 'doctor', lastDownloaded = NULL WHERE id = ?")) {
                    psReset.setInt(1, fileId);
                    psReset.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                allowAccess = true;
            }
        }
        
        if (!allowAccess) {
            response.getWriter().println("Access for this record is currently restricted (downloaded within the last hour).");
            return;
        }
        
        // Use RHSOAlgorithm to get top two nodes and send an access-decision request.
        RHSOAlgorithm rhs = new RHSOAlgorithm();
        List<DelegatorNode> topNodes = rhs.selectDelegatorNodes();
        boolean consensusApproved = false;
        for (DelegatorNode node : topNodes) {
            try {
                java.net.URI uri = java.net.URI.create(node.getUrl() + "/access-decision");
                java.net.URL url = uri.toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    consensusApproved = true;
                    break;
                }
            } catch (Exception e) {
                System.err.println("Error contacting node " + node.getUrl() + ": " + e.getMessage());
            }
        }
        
        if (!consensusApproved) {
            response.getWriter().println("Access request denied by consensus.");
            return;
        }
        
        // Update record to mark as downloaded (approved) and set lastDownloaded timestamp
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement psUpdate = con.prepareStatement(
                 "UPDATE clouddata SET submissionType = 'downloaded', lastDownloaded = CURRENT_TIMESTAMP WHERE id = ?")) {
            psUpdate.setInt(1, fileId);
            psUpdate.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If AJAX request, return JSON instead of redirecting.
        if (isAjax) {
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"approved\"}");
        } else {
            response.sendRedirect("UserDashboardServlet");
        }
    }
}
