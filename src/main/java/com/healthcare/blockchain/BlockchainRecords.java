package com.healthcare.blockchain;

import java.io.IOException;
import java.io.PrintWriter;
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

@WebServlet("/BlockchainRecords")
public class BlockchainRecords extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Database configuration – update these values as needed
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONArray jsonArray = new JSONArray();
        
        // Query the BlockchainLog table for the required fields
        String sql = "SELECT timestamp, userId, dataHash FROM BlockchainLog ORDER BY timestamp DESC";
        
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("timestamp", rs.getString("timestamp"));
                json.put("userId", rs.getString("userId"));
                json.put("dataHash", rs.getString("dataHash"));
                jsonArray.add(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "Database error");
            out.print(errorObj.toJSONString());
            return;
        }
        
        out.print(jsonArray.toJSONString());
    }
}
