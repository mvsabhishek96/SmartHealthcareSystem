<%@ page import="java.sql.*" %>
<%
    String userEmail = request.getParameter("userEmail");
    String password = request.getParameter("password");
    if(userEmail == null || password == null){
        out.println("Error: Missing login details.");
        return;
    }
    try {
    	Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/iot_db", "root", "");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM doctor WHERE userEmail = ? AND password = ?");
        ps.setString(1, userEmail);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            session.setAttribute("doctorEmail", userEmail);
            response.sendRedirect("doctor_upload.html");
        } else {
            out.println("Invalid login credentials. <a href='doctor_login.html'>Try again</a>");
        }
        con.close();
    } catch(Exception e){
        out.println("Error: " + e.getMessage());
    }
%>
