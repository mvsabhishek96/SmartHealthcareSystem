package com.healthcare.datarequest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.PublicKey;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/GatewayServlet")
public class GatewayServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");

        String deviceId = request.getParameter("deviceId");
        String heartRateData = request.getParameter("heartRateData");
        String signature = request.getParameter("signature");

        if (deviceId == null || heartRateData == null || signature == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: Missing parameters.");
            return;
        }

        try {
            HttpSession session = request.getSession();
            PublicKey devicePublicKey = (PublicKey) session.getAttribute(deviceId + "_publicKey");

            if (devicePublicKey == null) {
                devicePublicKey = fetchPublicKeyFromDB(deviceId);
                if (devicePublicKey != null) {
                    session.setAttribute(deviceId + "_publicKey", devicePublicKey);
                }
            }

            if (devicePublicKey == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Error: Device not found.");
                return;
            }

            // Placeholder for actual signature validation — assuming valid for now
            boolean isValid = true;

            if (isValid) {
                boolean forwarded = forwardDataToEdgeNode(deviceId, heartRateData, signature);
                if (forwarded) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Data forwarded successfully.");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Failed to forward data to Edge Node.");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid signature.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Server error.");
        }
    }

    private PublicKey fetchPublicKeyFromDB(String deviceId) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/iot_db", "root", "");
             PreparedStatement stmt = con.prepareStatement("SELECT devicePublicKey FROM Devices WHERE deviceId = ?")) {

            stmt.setString(1, deviceId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String publicKeyBase64 = rs.getString("devicePublicKey");
                return getPublicKeyFromBase64(publicKeyBase64);
            }
        }
        return null;
    }

    private PublicKey getPublicKeyFromBase64(String base64PublicKey) throws Exception {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(base64PublicKey);
        java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(keyBytes);
        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(keySpec);
    }

    private boolean forwardDataToEdgeNode(String deviceId, String heartRateData, String signature) {
        HttpURLConnection connection = null;
        try {
        	URI uri = new URI("http", null, "localhost", 8080, "/SmartHealthcareSystemnew/EdgeNodeServletdatarequest", null, null);
        	URL url = uri.toURL();


            System.out.println("Connecting to: " + url);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "text/plain");

            String formData = "deviceId=" + deviceId + "&heartRateData=" + heartRateData + "&signature=" + signature;
            System.out.println("Sending data: " + formData);

            try (Writer writer = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write(formData);
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            System.out.println("Response code: " + responseCode);
            System.out.println("Response message: " + responseMessage);

            // Read the full response from the Edge Node servlet for more insight
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Edge Node response: " + line);
                }
            }

            return responseCode == HttpServletResponse.SC_OK;

        } catch (IOException e) {
            System.out.println("IOException while sending data to Edge Node: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("Exception while building URL or sending request: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
