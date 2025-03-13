/*package com.healthcare.accessdelegation;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/EdgeServerServlet")
public class EdgeServerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String deviceId = request.getParameter("deviceId");
        String heartRateData = request.getParameter("heartRateData");

        if (deviceId == null || heartRateData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing deviceId or heartRateData");
            return;
        }

        // Select the delegator nodes
        List<DelegatorNode> delegatorNodes = performAccessDelegation();

        if (delegatorNodes.isEmpty()) {
            response.getWriter().write("No delegator nodes available.");
            return;
        }

        StringBuilder selectedNodes = new StringBuilder();

        // Send data to each delegator node and log their names
        for (DelegatorNode node : delegatorNodes) {
            sendToEdgeNode(node, deviceId, heartRateData);
            selectedNodes.append(node.getNodeName()).append(", ");
        }

        // Remove trailing comma and space
        if (selectedNodes.length() > 0) {
            selectedNodes.setLength(selectedNodes.length() - 2);
        }

        // Log and respond with the selected nodes
        System.out.println("Selected delegator nodes: " + selectedNodes);
        response.getWriter().write("Data processed by selected delegator nodes: " + selectedNodes);
    }

    private List<DelegatorNode> performAccessDelegation() {
        NodeManager nodeManager = new NodeManager();
        List<DelegatorNode> availableNodes = nodeManager.getAvailableNodes();
        RHSOAlgorithm rhso = new RHSOAlgorithm();
        return rhso.selectDelegatorNodes(availableNodes);
    }

    private void sendToEdgeNode(DelegatorNode node, String deviceId, String heartRateData) {
        try {
            URI uri = URI.create("http://localhost:8080/healthcare/EdgeNodeServlet");
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write("deviceId=" + deviceId + "&heartRateData=" + heartRateData);
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Sent data to node " + node.getNodeName() + ". Response code: " + responseCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
*/