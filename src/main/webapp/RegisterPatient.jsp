<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.security.KeyPair, java.security.KeyPairGenerator, java.security.NoSuchAlgorithmException"%>
<%@ page import="java.util.Base64"%>
<%@ page import="java.sql.*" %>
<html>
<head>
  <title>Patient Registration</title>
  <style>
    body { font-family: Arial, sans-serif; background-color: #f5f6fa; }
    .container { max-width: 600px; margin: 2rem auto; padding: 1rem; background-color: #fff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    h2 { text-align: center; }
    .form-group { margin-bottom: 1rem; }
    label { display: block; margin-bottom: 0.5rem; }
    input { width: 100%; padding: 0.8rem; border: 1px solid #ddd; border-radius: 4px; }
    button { background-color: #3498db; color: #fff; border: none; padding: 0.8rem 1.5rem; border-radius: 4px; cursor: pointer; display: block; margin: 1rem auto; }
    button:hover { opacity: 0.9; }
    .message { text-align: center; font-weight: bold; }
  </style>
</head>
<body>
  <div class="container">
    <h2>Patient Registration</h2>
    <%
      // Retrieve form parameters
      String userName = request.getParameter("userName");
      String userEmail = request.getParameter("userEmail");
      String password = request.getParameter("password");

      if(userName == null || userEmail == null || password == null ||
         userName.trim().isEmpty() || userEmail.trim().isEmpty() || password.trim().isEmpty()){
          out.println("<p class='message' style='color:red;'>Error: All fields are required.</p>");
          return;
      }

      // Generate user key pair using EC algorithm
      KeyPair userKeyPair = null;
      try {
          KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
          keyGen.initialize(256);
          userKeyPair = keyGen.generateKeyPair();
      } catch(NoSuchAlgorithmException e) {
          out.println("<p class='message' style='color:red;'>Error generating key pair: " + e.getMessage() + "</p>");
          return;
      }
      
      String userPublicKey = Base64.getEncoder().encodeToString(userKeyPair.getPublic().getEncoded());
      String userPrivateKey = Base64.getEncoder().encodeToString(userKeyPair.getPrivate().getEncoded());
      
      // Database connection parameters
      String dbURL = "jdbc:mysql://localhost:3306/iot_db";
      String dbUser = "root";
      String dbPass = "";
      
      Connection con = null;
      try {
          // Load the MySQL driver
          Class.forName("com.mysql.cj.jdbc.Driver");
          con = DriverManager.getConnection(dbURL, dbUser, dbPass);
          
          // Insert into users table
          String sql = "INSERT INTO users (userName, userEmail, password, userPublicKey, userPrivateKey) VALUES (?, ?, ?, ?, ?)";
          PreparedStatement ps = con.prepareStatement(sql);
          ps.setString(1, userName);
          ps.setString(2, userEmail);
          ps.setString(3, password);  // For production, store a hashed version.
          ps.setString(4, userPublicKey);
          ps.setString(5, userPrivateKey);
          int result = ps.executeUpdate();
          
          if(result > 0){
              out.println("<p class='message' style='color:green;'>Registration successful! Please proceed to login.</p>");
          } else {
              out.println("<p class='message' style='color:red;'>Registration failed. Please try again.</p>");
          }
      } catch(Exception e){
          out.println("<p class='message' style='color:red;'>Database error: " + e.getMessage() + "</p>");
          e.printStackTrace(new java.io.PrintWriter(out));
      } finally {
          if(con != null){
              try { con.close(); } catch(Exception ex){ }
          }
      }
    %>
  </div>
</body>
</html>
