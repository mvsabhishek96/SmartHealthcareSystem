package com.healthcare.datarequest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@WebServlet("/EdgeNodeServletdatarequest")
public class EdgeNodeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Instance variables for doPost functionality
    private String deviceId;
    private String heartRateData;
    private String signature;

    // Database configuration for doGet functionality
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get data from the HTML form
        deviceId = request.getParameter("deviceId");
        heartRateData = request.getParameter("heartRateData");
        signature = request.getParameter("signature");

        if (deviceId == null || deviceId.trim().isEmpty() ||
                heartRateData == null || heartRateData.trim().isEmpty() ||
                signature == null || signature.trim().isEmpty()) {
            response.getWriter().write("Error: Missing one or more required fields.");
            return;
        }

        System.out.println("Received data from device " + deviceId + ": " + heartRateData + " | Signature: " + signature);

        // Forward data to PBFTAccessControlAndStorageServlet using URI
        try {
            URI uri = new URI("http", null, "localhost", 8080, "/SmartHealthcareSystemnew/PBFTAccessControlAndStorageServlet", null, null);
            URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String postData = "userId=" + deviceId + "&patientData=" + heartRateData;

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(postData);
                writer.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println("PBFTAccessControlAndStorageServlet Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Data successfully forwarded to PBFTAccessControlAndStorageServlet");
            } else {
                System.err.println("Failed to forward data. HTTP Code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error forwarding data to PBFTAccessControlAndStorageServlet: " + e.getMessage());
            e.printStackTrace();
        }

        // Confirmation message for the device
        response.getWriter().write("Data from device " + deviceId + " successfully processed and forwarded.");
    }
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = con.prepareStatement(
                     "SELECT userId, patientData, timestamp FROM CloudData ORDER BY timestamp DESC LIMIT 10")) {

            ResultSet rs = stmt.executeQuery();
            JSONArray jsonArray = new JSONArray();

            while (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("deviceId", rs.getString("userId"));
                json.put("heartRateData", rs.getString("patientData"));
                json.put("timestamp", rs.getString("timestamp"));
                jsonArray.add(json);
            }

            out.print(jsonArray.toJSONString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error\"}");
        }
    }
}
