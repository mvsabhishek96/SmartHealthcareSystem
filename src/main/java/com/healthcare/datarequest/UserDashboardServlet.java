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
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Ensure the patient is logged in.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.html");
            return;
        }
        
        // Use the logged-in username to query the clouddata table by patientUsername.
        String patientUsername = (String) session.getAttribute("username");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h2>Welcome, " + patientUsername + "!</h2>");
        out.println("<h3>Your Uploaded Patient Data</h3>");
        
        // Query clouddata where patientUsername matches the logged-in username
        String sql = "SELECT id, fileName, timestamp, submissionType " +
                     "FROM clouddata WHERE patientUsername = ? AND submissionType = 'doctor' " +
                     "ORDER BY timestamp DESC LIMIT 5";
        
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, patientUsername);
            ResultSet rs = ps.executeQuery();
            
            out.println("<table border='1' cellpadding='5'>");
            out.println("<tr><th>ID</th><th>File Name</th><th>Timestamp</th><th>Status</th><th>Action</th></tr>");
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int id = rs.getInt("id");
                String fileName = rs.getString("fileName");
                String timestamp = rs.getString("timestamp");
                String subType = rs.getString("submissionType");
                String status = subType.equals("doctor") ? "Available" : "Access Used";
                
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + fileName + "</td>");
                out.println("<td>" + timestamp + "</td>");
                out.println("<td>" + status + "</td>");
                if (subType.equals("doctor")) {
                    out.println("<td><form action='FileAccessRequestServlet' method='get'>");
                    out.println("<input type='hidden' name='fileId' value='" + id + "' />");
                    out.println("<input type='hidden' name='userId' value='" + session.getAttribute("userId") + "' />");
                    out.println("<input type='submit' value='Request Access' />");
                    out.println("</form></td>");
                } else {
                    out.println("<td>--</td>");
                }
                out.println("</tr>");
            }
            if (!hasResults) {
                out.println("<tr><td colspan='5'>No files found for this patient.</td></tr>");
            }
            out.println("</table>");
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Database error: " + e.getMessage());
        }
    }
}
