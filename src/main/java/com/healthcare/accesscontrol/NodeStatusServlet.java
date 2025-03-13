package com.healthcare.accesscontrol;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@WebServlet("/node-status")
public class NodeStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONObject status = new JSONObject();
        // Create a dynamic node identifier using server name and port
        String nodeName = request.getServerName() + ":" + request.getServerPort();
        status.put("nodeName", nodeName);
        status.put("trust", calculateTrust());
        status.put("energy", getEnergyLevel());
        status.put("load", getCurrentLoad());
        status.put("resourceAvailability", getResourceAvailability());
        
        response.setContentType("application/json");
        response.getWriter().write(status.toJSONString());
    }
    
    // Compute trust as an average of energy, resource availability, and (1 - load fraction)
    private double calculateTrust() {
        double energy = getEnergyLevel();
        double loadPercentage = getCurrentLoad(); // load as a percentage (0 to 100)
        double resource = getResourceAvailability();
        // Convert load percentage to a fraction: (1 - load/100)
        return (energy + resource + (1 - loadPercentage / 100.0)) / 3;
    }
    
    // Compute energy level based on free memory ratio and CPU idle fraction
    private double getEnergyLevel() {
        double memoryRatio = (double) Runtime.getRuntime().freeMemory() / Runtime.getRuntime().totalMemory();
        // Convert load percentage to fraction and subtract from 1 for idle percentage
        double cpuIdle = 1.0 - (getCurrentLoad() / 100.0);
        return (memoryRatio + cpuIdle) / 2;
    }
    
    // Compute current load using getSystemLoadAverage() divided by the number of processors,
    // then return the normalized value as a percentage (0 to 100).
    private double getCurrentLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double loadAvg = osBean.getSystemLoadAverage();
        if (loadAvg < 0) { 
            return 0.0; // fallback if load average is not available
        }
        int processors = osBean.getAvailableProcessors();
        double normalizedLoad = loadAvg / processors;
        if (normalizedLoad > 1.0) {
            normalizedLoad = 1.0;
        }
        return normalizedLoad * 100; // Return load as a percentage
    }
    
    // Compute resource availability based on disk free space and a simulated network metric
    private double getResourceAvailability() {
        File root = new File("/");
        double diskRatio = (double) root.getFreeSpace() / root.getTotalSpace();
        double networkAvailability = 0.95; // simulated network availability metric
        return (diskRatio + networkAvailability) / 2;
    }
}
