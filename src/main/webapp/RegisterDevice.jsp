<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.security.KeyPair, java.security.KeyPairGenerator, java.security.NoSuchAlgorithmException" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.sql.*" %>

<%
    String userName = request.getParameter("userName");
    String userEmail = request.getParameter("userEmail");
    String deviceId = request.getParameter("deviceId");
    String deviceType = request.getParameter("deviceType");

    if (userName == null || userEmail == null || deviceId == null || deviceType == null ||
        userName.trim().isEmpty() || userEmail.trim().isEmpty() ||
        deviceId.trim().isEmpty() || deviceType.trim().isEmpty()) {
        out.println("Error: All fields are required.");
        return;
    }

    class HECCKeyPairGenerator {
        public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(256);
            return keyGen.generateKeyPair();
        }
    }

    HECCKeyPairGenerator keyPairGen = new HECCKeyPairGenerator();
    KeyPair deviceKeyPair = keyPairGen.generateKeyPair();

    String devicePublicKey = Base64.getEncoder().encodeToString(deviceKeyPair.getPublic().getEncoded());
    String devicePrivateKey = Base64.getEncoder().encodeToString(deviceKeyPair.getPrivate().getEncoded());

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/iot_db", "root", "");
         PreparedStatement stmt = con.prepareStatement(
            "INSERT INTO Devices (deviceId, deviceType, devicePublicKey, devicePrivateKey) VALUES (?, ?, ?, ?)")) {

        stmt.setString(1, deviceId);
        stmt.setString(2, deviceType);
        stmt.setString(3, devicePublicKey);
        stmt.setString(4, devicePrivateKey);
        stmt.executeUpdate();
    } catch (Exception ex) {
        out.println("Database error: " + ex.getMessage());
        return;
    }

    out.println("<h2>Device Registered Successfully!</h2>");
    out.println("<p>Device Public Key (Base64): " + devicePublicKey + "</p>");
%>
