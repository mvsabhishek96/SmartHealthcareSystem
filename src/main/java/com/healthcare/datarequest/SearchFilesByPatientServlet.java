package com.healthcare.datarequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/SearchFilesByPatientServlet")
public class SearchFilesByPatientServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        String queryParam = null;
        boolean searchByUserId = false;
        
        // If a user is logged in, use their userId to search clouddata (via userId column)
        if (session != null && session.getAttribute("userId") != null) {
            queryParam = String.valueOf(session.getAttribute("userId"));
            searchByUserId = true;
        } else {
            // Otherwise, fall back to the provided patientUsername parameter
            queryParam = request.getParameter("patientUsername");
            if (queryParam == null || queryParam.trim().isEmpty()) {
                out.println("<p style='color:red;'>Error: Patient identifier is required.</p>");
                return;
            }
        }
        
        // Build SQL query: if logged in, search by userId; otherwise, search by patientUsername.
        String sql;
        if (searchByUserId) {
            sql = "SELECT id, fileName, timestamp, submissionType FROM clouddata WHERE userId = ?";
        } else {
            sql = "SELECT id, fileName, timestamp, submissionType FROM clouddata WHERE patientUsername = ?";
        }
        
        out.println("<html><head><title>Patient Files</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #f4f4f4; }");
        out.println("table { width: 80%; margin: 20px auto; border-collapse: collapse; }");
        out.println("th, td { padding: 10px; border: 1px solid #ccc; text-align: center; }");
        out.println("th { background-color: #3498db; color: #fff; }");
        out.println("</style>");
        out.println("</head><body>");
        
        if (searchByUserId) {
            out.println("<h2 style='text-align:center;'>Files for Patient (User ID): " + queryParam + "</h2>");
        } else {
            out.println("<h2 style='text-align:center;'>Files for Patient: " + queryParam + "</h2>");
        }
        
        out.println("<table>");
        out.println("<tr><th>ID</th><th>File Name</th><th>Timestamp</th><th>Submission Type</th></tr>");
        
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, queryParam);
            ResultSet rs = ps.executeQuery();
            
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int id = rs.getInt("id");
                String fileName = rs.getString("fileName");
                String timestamp = rs.getString("timestamp");
                String submissionType = rs.getString("submissionType");
                
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + fileName + "</td>");
                out.println("<td>" + timestamp + "</td>");
                out.println("<td>" + submissionType + "</td>");
                out.println("</tr>");
            }
            if (!hasResults) {
                out.println("<tr><td colspan='4'>No files found for this patient.</td></tr>");
            }
        } catch (Exception e) {
            e.printStackTrace(new java.io.PrintWriter(out));
        }
        
        out.println("</table>");
        out.println("</body></html>");
    }
}
