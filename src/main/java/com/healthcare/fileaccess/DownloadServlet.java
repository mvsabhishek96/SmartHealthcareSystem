package com.healthcare.fileaccess;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileIdStr = request.getParameter("fileId");
        if (fileIdStr == null) {
            response.getWriter().println("Error: Missing fileId parameter.");
            return;
        }
        int fileId = Integer.parseInt(fileIdStr);
        String patientData = null;
        String fileName = null;
        
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(
                "SELECT patientData, fileName FROM clouddata WHERE id = ? AND submissionType = 'downloaded'")) {
            ps.setInt(1, fileId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                patientData = rs.getString("patientData");
                fileName = rs.getString("fileName");
            } else {
                response.getWriter().println("Error: File not found or not approved.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
            return;
        }
        
        byte[] fileBytes;
        try {
            fileBytes = Base64.getDecoder().decode(patientData.replaceAll("\\s+", ""));
        } catch (IllegalArgumentException e) {
            response.getWriter().println("Error decoding file data: " + e.getMessage());
            return;
        }
        
        // Determine content type based on file extension
        String contentType = "application/octet-stream";
        if (fileName != null) {
            if (fileName.toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (fileName.toLowerCase().endsWith(".txt")) {
                contentType = "text/plain";
            }
        }
        
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLength(fileBytes.length);
        try (OutputStream outStream = response.getOutputStream()) {
            outStream.write(fileBytes);
        }
    }
}
