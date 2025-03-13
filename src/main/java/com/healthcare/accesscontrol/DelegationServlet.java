package com.healthcare.accesscontrol;

import com.healthcare.accessdelegation.RHSOAlgorithm;
import com.healthcare.accessdelegation.DelegatorNode;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/delegation-status")
public class DelegationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RHSOAlgorithm rhsAlgorithm = new RHSOAlgorithm();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Retrieve the optimal nodes using RHSO
        List<DelegatorNode> optimalNodes = rhsAlgorithm.selectDelegatorNodes();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < optimalNodes.size(); i++) {
            DelegatorNode node = optimalNodes.get(i);
            json.append("{\"nodeName\":\"").append(node.getNodeName())
                .append("\", \"url\":\"").append(node.getUrl())
                .append("\", \"fitness\":").append(node.getFitness()).append("}");
            if (i < optimalNodes.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        out.write(json.toString());
    }
}
