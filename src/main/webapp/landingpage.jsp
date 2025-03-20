<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Smart Healthcare System</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <style>
    :root {
      --primary-color: #2c3e50;
      --secondary-color: #3498db;
      --success-color: #27ae60;
      --danger-color: #e74c3c;
      --light-gray: #f9f9f9;
    }
    body {
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      margin: 0;
      background-color: var(--light-gray);
      color: #333;
    }
    .navbar {
      background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
      padding: 1rem 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: white;
      box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    }
    .navbar h1 {
      margin: 0;
      font-size: 1.5rem;
    }
    .nav-links a {
      color: white;
      text-decoration: none;
      margin-left: 1.5rem;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: background-color 0.3s ease;
    }
    .nav-links a:hover {
      background-color: var(--secondary-color);
    }
    .container {
      max-width: 1200px;
      margin: 2rem auto;
      padding: 0 1rem;
    }
    .card {
      background: white;
      border-radius: 8px;
      padding: 1.5rem;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      margin-bottom: 2rem;
      transition: transform 0.2s ease;
    }
    .card:hover {
      transform: translateY(-3px);
    }
    .dashboard-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 2rem;
    }
    .form-group {
      margin-bottom: 1rem;
    }
    label {
      display: block;
      margin-bottom: 0.5rem;
      color: var(--primary-color);
      font-weight: 500;
    }
    input, select, textarea {
      width: 100%;
      padding: 0.8rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      box-sizing: border-box;
    }
    button {
      background-color: var(--secondary-color);
      color: white;
      border: none;
      padding: 0.8rem 1.5rem;
      border-radius: 4px;
      cursor: pointer;
      transition: opacity 0.3s ease, transform 0.2s ease;
    }
    button:hover {
      opacity: 0.9;
      transform: translateY(-1px);
    }
    .data-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 1rem;
    }
    .data-table th, .data-table td {
      padding: 1rem;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }
    .data-table tbody tr:nth-child(odd) {
      background-color: #f4f4f4;
    }
    .data-table tbody tr:hover {
      background-color: #eaeaea;
    }
    .error-message {
      color: var(--danger-color);
      margin-bottom: 1rem;
      text-align: center;
    }
  </style>
</head>
<body>
  <!-- Navigation -->
  <nav class="navbar">
    <h1>Smart Healthcare System</h1>
    <div class="nav-links">
      <a href="#dashboard">Cloud Node</a>
      <a href="#register">Register Device</a>
      <a href="devicerequesting.html">Submit Data</a>
      <a href="#blockchain">Blockchain</a>
      <a href="#nodes">Gateway</a>
      <a href="#patient-portal">IoT user</a>
    </div>
  </nav>
  
  <div class="container">
    <!-- Dashboard Section -->
    <section id="dashboard" class="card">
      <h2>System Overview</h2>
      <div class="dashboard-cards">
        <!-- Recent Device Data Card -->
        <div class="card">
          <h3>Recent Device Data</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th>Device ID</th>
                <th>File Name</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              <%
                String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
                String DB_USER = "root";
                String DB_PASSWORD = "";
                Connection conn = null;
                Statement stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT userId, fileName, timestamp FROM clouddata ORDER BY timestamp DESC LIMIT 10");
                    while(rs.next()){
              %>
              <tr>
                <td><%= rs.getString("userId") %></td>
                <td><%= rs.getString("fileName") %></td>
                <td><%= rs.getTimestamp("timestamp") %></td>
              </tr>
              <%
                    }
                    rs.close();
                } catch(Exception e) {
                    out.println("<tr><td colspan='3'>Error: " + e.getMessage() + "</td></tr>");
                } finally {
                    if(stmt != null) stmt.close();
                    if(conn != null) conn.close();
                }
              %>
            </tbody>
          </table>
        </div>
        <!-- Blockchain Status Card -->
        <div class="card">
          <h3>Blockchain Status</h3>
          <div>
            <!-- For demonstration, static values are used. -->
            <p>Latest Hash: <strong>ABC123DEF456</strong></p>
            <p>Total Blocks: <strong>58</strong></p>
          </div>
        </div>
      </div>
      <!-- Additional Dashboard Options -->
      <div class="dashboard-cards" style="margin-top: 2rem;">
        <!-- IoT Users Card -->
        <div class="card">
          <h3>IoT Users</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th>User ID</th>
                <th>Username</th>
                <th>Email</th>
              </tr>
            </thead>
            <tbody>
              <%
                conn = null;
                stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT userId, userName, userEmail FROM users");
                    while(rs.next()){
              %>
              <tr>
                <td><%= rs.getInt("userId") %></td>
                <td><%= rs.getString("userName") %></td>
                <td><%= rs.getString("userEmail") %></td>
              </tr>
              <%
                    }
                    rs.close();
                } catch(Exception e) {
                    out.println("<tr><td colspan='3'>Error: " + e.getMessage() + "</td></tr>");
                } finally {
                    if(stmt != null) stmt.close();
                    if(conn != null) conn.close();
                }
              %>
            </tbody>
          </table>
        </div>
        <!-- IoT Devices Card -->
        <div class="card">
          <h3>IoT Devices</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th>Device ID</th>
                <th>Device Type</th>
              </tr>
            </thead>
            <tbody>
              <%
                conn = null;
                stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT deviceId, deviceType FROM devices");
                    while(rs.next()){
              %>
              <tr>
                <td><%= rs.getString("deviceId") %></td>
                <td><%= rs.getString("deviceType") %></td>
              </tr>
              <%
                    }
                    rs.close();
                } catch(Exception e) {
                    out.println("<tr><td colspan='2'>Error: " + e.getMessage() + "</td></tr>");
                } finally {
                    if(stmt != null) stmt.close();
                    if(conn != null) conn.close();
                }
              %>
            </tbody>
          </table>
        </div>
        <!-- File Names Card -->
        <div class="card">
          <h3>File Names</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th>File Name</th>
                <th>Submission Type</th>
              </tr>
            </thead>
            <tbody>
              <%
                conn = null;
                stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT fileName, submissionType FROM clouddata");
                    while(rs.next()){
              %>
              <tr>
                <td><%= rs.getString("fileName") %></td>
                <td><%= rs.getString("submissionType") %></td>
              </tr>
              <%
                    }
                    rs.close();
                } catch(Exception e) {
                    out.println("<tr><td colspan='2'>Error: " + e.getMessage() + "</td></tr>");
                } finally {
                    if(stmt != null) stmt.close();
                    if(conn != null) conn.close();
                }
              %>
            </tbody>
          </table>
        </div>
        <!-- Doctor Names Card -->
        <div class="card">
          <h3>Doctor Names</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th>Doctor ID</th>
                <th>Name</th>
                <th>Email</th>
              </tr>
            </thead>
            <tbody>
              <%
                conn = null;
                stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, userName, userEmail FROM doctor");
                    while(rs.next()){
              %>
              <tr>
                <td><%= rs.getInt("id") %></td>
                <td><%= rs.getString("userName") %></td>
                <td><%= rs.getString("userEmail") %></td>
              </tr>
              <%
                    }
                    rs.close();
                } catch(Exception e) {
                    out.println("<tr><td colspan='3'>Error: " + e.getMessage() + "</td></tr>");
                } finally {
                    if(stmt != null) stmt.close();
                    if(conn != null) conn.close();
                }
              %>
            </tbody>
          </table>
        </div>
      </div>
    </section>
    
    <!-- Device Registration Section -->
    <section id="register" class="card" style="display: none;">
      <h2>Device Registration</h2>
      <form action="devicerequesting.html" method="post">
        <div class="form-group">
          <label for="userName">User Name</label>
          <input type="text" id="userName" name="userName" required>
        </div>
        <div class="form-group">
          <label for="userEmail">User Email</label>
          <input type="email" id="userEmail" name="userEmail" required>
        </div>
        <div class="form-group">
          <label for="deviceId">Device ID</label>
          <input type="text" id="deviceId" name="deviceId" required>
        </div>
        <div class="form-group">
          <label for="deviceType">Device Type</label>
          <select id="deviceType" name="deviceType" required>
            <option value="">Select Device Type</option>
            <option value="Sensor">Sensor</option>
            <option value="Actuator">Actuator</option>
            <option value="Gateway">Gateway</option>
          </select>
        </div>
        <button type="submit">Register Device</button>
      </form>
    </section>
    
    <!-- Blockchain Section -->
    <section id="blockchain" class="card" style="display: none;">
      <h2>Blockchain Records</h2>
      <table class="data-table">
        <thead>
          <tr>
            <th>Timestamp</th>
            <th>User ID</th>
            <th>Hash</th>
          </tr>
        </thead>
        <tbody>
          <%
            conn = null;
            stmt = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT timestamp, userId, dataHash FROM blockchainlog ORDER BY timestamp DESC LIMIT 10");
                while(rs.next()){
          %>
          <tr>
            <td><%= rs.getTimestamp("timestamp") %></td>
            <td><%= rs.getString("userId") %></td>
            <td><%= rs.getString("dataHash") %></td>
          </tr>
          <%
                }
                rs.close();
            } catch(Exception e) {
                out.println("<tr><td colspan='3'>Error: " + e.getMessage() + "</td></tr>");
            } finally {
                if(stmt != null) stmt.close();
                if(conn != null) conn.close();
            }
          %>
        </tbody>
      </table>
    </section>
    
    <!-- Node Status Section -->
    <section id="nodes" class="card" style="display: none;">
      <h2>RHSO Aggregator - Node Metrics</h2>
      <table class="data-table" id="nodeTable">
        <thead>
          <tr>
            <th>Node Name</th>
            <th>URL</th>
            <th>Trust</th>
            <th>Energy</th>
            <th>Load</th>
            <th>Resource Availability</th>
            <th>Fitness</th>
          </tr>
        </thead>
        <tbody id="nodeStatus">
          <!-- Placeholder text (AJAX functionality not implemented) -->
          <tr><td colspan="7">Node status functionality not implemented.</td></tr>
        </tbody>
      </table>
      <button id="refreshButton" class="btn btn-secondary mt-3">Refresh Status</button>
    </section>
    
    <!-- Patient Portal Section -->
    <section id="patient-portal" class="card" style="display: none;">
      <h2>Patient Portal</h2>
      <div id="patient-registration-section">
        <h5>Patient Registration</h5>
        <form id="patient-reg-form" action="RegisterPatient.jsp" method="post">
          <div class="form-group mb-3">
            <input type="text" class="form-control" name="userName" placeholder="Full Name" required>
          </div>
          <div class="form-group mb-3">
            <input type="email" class="form-control" name="userEmail" placeholder="Email" required>
          </div>
          <div class="form-group mb-3">
            <input type="password" class="form-control" name="password" placeholder="Password" required>
          </div>
          <button type="submit" class="btn btn-primary w-100">Register</button>
        </form>
        <p class="text-center mt-3">
          Already registered? <a href="#" onclick="togglePatientForms()">Login Here</a>
        </p>
      </div>
      <div id="patient-login-section" style="display: none;">
        <h5>Patient Login</h5>
        <form id="patient-login-form" action="GatewayServlet" method="post">
          <div class="form-group mb-3">
            <input type="text" class="form-control" name="username" placeholder="Username" required>
          </div>
          <div class="form-group mb-3">
            <input type="password" class="form-control" name="password" placeholder="Password" required>
          </div>
          <button type="submit" class="btn btn-primary w-100">Login</button>
        </form>
        <p class="text-center mt-3">
          Not registered? <a href="#" onclick="togglePatientForms()">Register Here</a>
        </p>
      </div>
    </section>
    
  </div>
  
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
  <script>
    // Navigation handling: Toggle internal sections; external links (e.g. devicerequesting.html) load externally.
    document.querySelectorAll('.nav-links a').forEach(link => {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const sectionId = link.getAttribute('href');
        if(sectionId.startsWith("#")) {
          document.querySelectorAll('section').forEach(section => section.style.display = 'none');
          document.querySelector(sectionId).style.display = 'block';
          if(sectionId === "#nodes") { /* Node status could be refreshed via AJAX if implemented */ }
          if(sectionId === "#blockchain") { /* Blockchain data could be refreshed if implemented */ }
          if(sectionId === "#dashboard") { /* Dashboard data is loaded by JSP queries above */ }
        } else {
          window.location.href = link.getAttribute('href');
        }
      });
    });
    // Show dashboard by default
    document.querySelector('#dashboard').style.display = 'block';
    
    function togglePatientForms() {
      const regSection = document.getElementById("patient-registration-section");
      const loginSection = document.getElementById("patient-login-section");
      if (regSection.style.display === "none") {
        regSection.style.display = "block";
        loginSection.style.display = "none";
      } else {
        regSection.style.display = "none";
        loginSection.style.display = "block";
      }
    }
    
    document.getElementById('refreshButton').addEventListener('click', function(){
      // Placeholder for refresh functionality; you could implement AJAX here.
      alert("Refresh functionality not implemented.");
    });
  </script>
</body>
</html>
