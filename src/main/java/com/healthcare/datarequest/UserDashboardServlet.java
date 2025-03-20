package com.healthcare.datarequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/UserDashboardServlet")
public class UserDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // One hour in milliseconds
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.html");
            return;
        }
        
        String patientUsername = (String) session.getAttribute("username");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        // Basic HTML header
        out.println("<html><head><title>User Dashboard</title>");
        out.println("<style>");
        out.println("table { width: 100%; border-collapse: collapse; }");
        out.println("th, td { padding: 8px; border: 1px solid #ddd; text-align: center; }");
        out.println("button { padding: 5px 10px; }");
        out.println("</style>");
        out.println("<script>");
        out.println("function requestAccess(fileId, btn) {");
        out.println("  btn.disabled = true;");
        out.println("  fetch('FileAccessRequestServlet?fileId=' + fileId + '&ajax=true')"); 
        out.println("    .then(response => response.json())");
        out.println("    .then(data => {");
        out.println("      if (data.status === 'approved') {");
        out.println("         // Update the row: change status to Approved and replace the button with a Download link");
        out.println("         var row = btn.parentNode.parentNode;");
        out.println("         row.cells[3].innerHTML = 'Approved';");
        out.println("         row.cells[4].innerHTML = \"<a href='DownloadServlet?fileId=\" + fileId + \"'>Download</a>\";");
        out.println("      } else {");
        out.println("         alert('Access request failed: ' + data.status);");
        out.println("         btn.disabled = false;");
        out.println("      }");
        out.println("    })");
        out.println("    .catch(error => {");
        out.println("       console.error('Error:', error);");
        out.println("       alert('An error occurred');");
        out.println("       btn.disabled = false;");
        out.println("    });");
        out.println("}");
        out.println("</script>");
        out.println("</head><body>");
        
        out.println("<h2>Welcome, " + patientUsername + "!</h2>");
        out.println("<h3>Your Uploaded Patient Data</h3>");
        out.println("<table>");
        out.println("<tr><th>ID</th><th>File Name</th><th>Timestamp</th><th>Status</th><th>Action</th></tr>");
        
        String sql = "SELECT id, fileName, timestamp, submissionType, lastDownloaded FROM clouddata " +
                     "WHERE patientUsername = ? ORDER BY timestamp DESC";
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patientUsername);
            ResultSet rs = ps.executeQuery();
            boolean hasRecord = false;
            while (rs.next()) {
                int id = rs.getInt("id");
                String fileName = rs.getString("fileName");
                String timestamp = rs.getString("timestamp");
                String submissionType = rs.getString("submissionType");
                Timestamp lastDownloaded = rs.getTimestamp("lastDownloaded");
                
                // If record is marked downloaded and within one hour, skip it.
                if ("downloaded".equals(submissionType) && lastDownloaded != null &&
                    (System.currentTimeMillis() - lastDownloaded.getTime()) < ONE_HOUR_MS) {
                    continue;
                }
                // If record is downloaded but more than one hour has passed, reset it.
                if ("downloaded".equals(submissionType) && lastDownloaded != null &&
                    (System.currentTimeMillis() - lastDownloaded.getTime()) >= ONE_HOUR_MS) {
                    try (PreparedStatement psReset = con.prepareStatement(
                        "UPDATE clouddata SET submissionType = 'doctor', lastDownloaded = NULL WHERE id = ?")) {
                        psReset.setInt(1, id);
                        psReset.executeUpdate();
                        submissionType = "doctor";
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                
                hasRecord = true;
                String status = "Pending Approval";
                String actionHtml = "";
                if ("doctor".equals(submissionType)) {
                    // Render a button that calls AJAX function on click.
                    actionHtml = "<button onclick='requestAccess(" + id + ", this)'>Request Access</button>";
                } else if ("downloaded".equals(submissionType)) {
                    status = "Approved";
                    actionHtml = "<a href='DownloadServlet?fileId=" + id + "'>Download</a>";
                }
                
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + fileName + "</td>");
                out.println("<td>" + timestamp + "</td>");
                out.println("<td>" + status + "</td>");
                out.println("<td>" + actionHtml + "</td>");
                out.println("</tr>");
            }
            if (!hasRecord) {
                out.println("<tr><td colspan='5'>No files available for access.</td></tr>");
            }
            out.println("</table>");
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Database error: " + e.getMessage());
        }
        out.println("</body></html>");
    }
}
