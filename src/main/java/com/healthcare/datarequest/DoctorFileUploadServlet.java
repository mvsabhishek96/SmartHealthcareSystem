package com.healthcare.datarequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/DoctorFileUploadServlet")
@MultipartConfig
public class DoctorFileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // URL of the PBFTAccessControlAndStorageServlet
    private static final String PBFT_URL = "http://localhost:8080/SmartHealthcareSystemnew/PBFTAccessControlAndStorageServlet";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check if doctor is logged in via session attribute "doctorEmail"
        String doctorEmail = (String) request.getSession().getAttribute("doctorEmail");
        if (doctorEmail == null) {
            response.getWriter().write("Not logged in. Please login first.");
            return;
        }
        
        // Retrieve the file name (already existing field)
        String fileName = request.getParameter("fileName");
        if (fileName == null || fileName.trim().isEmpty()) {
            response.getWriter().write("File name is required.");
            return;
        }
        
        // Retrieve the patient username for association
        String patientUsername = request.getParameter("patientUsername");
        if (patientUsername == null || patientUsername.trim().isEmpty()) {
            response.getWriter().write("Patient username is required.");
            return;
        }
        
        // Retrieve the file part from the request
        Part filePart = request.getPart("file");
        if (filePart == null) {
            response.getWriter().write("File part is missing.");
            return;
        }
        
        // Read file content
        InputStream fileContent = filePart.getInputStream();
        byte[] fileData = fileContent.readAllBytes();
        fileContent.close();
        
        // Process the file based on its MIME type (support PDF and text)
        String patientData = "";
        String contentType = filePart.getContentType();
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            // For PDF files, encode the binary data into a Base64 string
            patientData = Base64.getEncoder().encodeToString(fileData);
        } else {
            // For text files, convert the data to a UTF-8 string
            patientData = new String(fileData, "UTF-8");
        }
        
        // Prepare parameters to forward to PBFTAccessControlAndStorageServlet
        String urlParameters = "userId=" + URLEncoder.encode(doctorEmail, "UTF-8") +
                               "&patientData=" + URLEncoder.encode(patientData, "UTF-8") +
                               "&fileName=" + URLEncoder.encode(fileName, "UTF-8") +
                               "&patientUsername=" + URLEncoder.encode(patientUsername, "UTF-8") +
                               "&fromDoctor=true";
        
        // Forward the processed data via HTTP POST to PBFTAccessControlAndStorageServlet
        HttpURLConnection conn = null;
        try {
            URI uri = URI.create(PBFT_URL);
            URL url = uri.toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                out.writeBytes(urlParameters);
                out.flush();
            }
            
            int responseCode = conn.getResponseCode();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
            }
            
            // Return the response from PBFTAccessControlAndStorageServlet
            response.getWriter().write("Response from PBFTAccessControlAndStorageServlet: " + sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Error forwarding data: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
