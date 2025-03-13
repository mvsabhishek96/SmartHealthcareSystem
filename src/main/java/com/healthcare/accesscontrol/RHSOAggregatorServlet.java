package com.healthcare.accesscontrol;

import com.healthcare.accessdelegation.RHSOAlgorithm;
import com.healthcare.accessdelegation.DelegatorNode;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/rhso-status")
public class RHSOAggregatorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RHSOAlgorithm rhsAlgorithm = new RHSOAlgorithm();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<DelegatorNode> nodes = rhsAlgorithm.getAllNodes();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < nodes.size(); i++) {
            DelegatorNode node = nodes.get(i);
            json.append("{");
            json.append("\"nodeName\":\"").append(node.getNodeName()).append("\",");
            json.append("\"url\":\"").append(node.getUrl()).append("\",");
            json.append("\"trust\":").append(node.getTrust()).append(",");
            json.append("\"energy\":").append(node.getEnergy()).append(",");
            json.append("\"load\":").append(node.getLoad()).append(",");
            json.append("\"resourceAvailability\":").append(node.getResourceAvailability()).append(",");
            json.append("\"fitness\":").append(node.getFitness());
            json.append("}");
            if (i < nodes.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        out.write(json.toString());
    }
}
