package com.healthcare.datarequest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/EdgeNodeServletdatarequest")
public class EdgeNodeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String deviceId;
    private String heartRateData;
    private String signature;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Serve the last submitted data
        if (deviceId == null || heartRateData == null || signature == null) {
            response.getWriter().write("No data available. Please submit data through the form.");
            return;
        }

        response.setContentType("application/json");
        response.getWriter().write("{\"deviceId\":\"" + deviceId + "\",\"heartRateData\":\"" + heartRateData + "\",\"signature\":\"" + signature + "\"}");
    }
}
