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
import org.json.simple.JSONObject;

@WebServlet("/BlockchainStatus")
public class BlockchainStatus extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iot_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Get latest blockchain hash
            try (PreparedStatement stmt = con.prepareStatement(
                    "SELECT dataHash FROM BlockchainLog ORDER BY timestamp DESC LIMIT 1")) {
                ResultSet rs = stmt.executeQuery();
                json.put("latestHash", rs.next() ? rs.getString("dataHash") : "N/A");
            }
            // Get total block count
            try (PreparedStatement stmt = con.prepareStatement(
                    "SELECT COUNT(*) AS blockCount FROM BlockchainLog")) {
                ResultSet rs = stmt.executeQuery();
                json.put("blockCount", rs.next() ? rs.getInt("blockCount") : 0);
            }
            out.print(json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error\"}");
        }
    }
}
