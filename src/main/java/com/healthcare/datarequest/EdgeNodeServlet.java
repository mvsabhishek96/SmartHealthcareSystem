package com.healthcare.datarequest;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/EdgeNodeServletdatarequest")
public class EdgeNodeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String deviceId = request.getParameter("deviceId");
        String heartRateData = request.getParameter("heartRateData");

        processHeartRateData(deviceId, heartRateData);

        response.getWriter().write("Data successfully processed by edge node.");
    }

    private void processHeartRateData(String deviceId, String heartRateData) {
        System.out.println("Processing data from device " + deviceId + ": " + heartRateData);
    }
}
