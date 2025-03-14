<%@ page import="java.sql.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Device Registration</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }
        .container {
            background: #fff;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 400px;
        }
        h2 {
            text-align: center;
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
        }
        input[type="text"],
        input[type="email"],
        input[type="password"],
        select,
        button {
            width: 100%;
            padding: 10px;
            margin-bottom: 15px;
            border: 1px solid #ccc;
            border-radius: 8px;
            box-sizing: border-box;
        }
        button {
            background-color: #5cb85c;
            color: #fff;
            font-size: 16px;
            border: none;
            cursor: pointer;
        }
        button:hover {
            background-color: #4cae4c;
        }
        .error {
            color: red;
            text-align: center;
        }
        p {
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Device Registration</h2>
        <form action="RegisterDevice.jsp" method="post">
            <label for="userName">User Name:</label>
            <input type="text" id="userName" name="userName" required>

            <label for="userEmail">User Email:</label>
            <input type="email" id="userEmail" name="userEmail" required>

            <label for="deviceId">Device ID:</label>
            <input type="text" id="deviceId" name="deviceId" required>

            <label for="deviceType">Device Type:</label>
            <select id="deviceType" name="deviceType" required>
                <option value="">Select Device Type</option>
                <option value="Sensor">Sensor</option>
                <option value="Actuator">Actuator</option>
                <option value="Gateway">Gateway</option>
            </select>
            
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>
            
            <button type="submit">Register Device</button>
        </form>
        <p>Already registered? <a href="doctor_login.html">Login Here</a></p>
        <hr>
        <p id="message">
        <% 
            if(request.getMethod().equalsIgnoreCase("POST")){
                String userName = request.getParameter("userName");
                String userEmail = request.getParameter("userEmail");
                String deviceId = request.getParameter("deviceId");
                String deviceType = request.getParameter("deviceType");
                String password = request.getParameter("password");
                
                if(userName == null || userEmail == null || deviceId == null || deviceType == null || password == null){
                    out.println("Error: Missing required fields.");
                } else {
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/iot_db", "root", "");
                        PreparedStatement ps = con.prepareStatement("INSERT INTO doctor (userName, userEmail, deviceId, deviceType, password) VALUES (?, ?, ?, ?, ?)");
                        ps.setString(1, userName);
                        ps.setString(2, userEmail);
                        ps.setString(3, deviceId);
                        ps.setString(4, deviceType);
                        ps.setString(5, password);
                        int i = ps.executeUpdate();
                        if(i > 0){
                            out.println("Registration successful. <a href='doctor_login.html'>Click here to login</a>");
                        } else {
                            out.println("Registration failed. Please try again.");
                        }
                        con.close();
                    } catch(Exception e){
                        out.println("Error: " + e.getMessage());
                    }
                }
            }
        %>
        </p>
    </div>
</body>
</html>
