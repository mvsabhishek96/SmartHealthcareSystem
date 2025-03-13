package com.healthcare.accessdelegation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RHSOAlgorithm {
    // List of node-status endpoints for available nodes
    private static final String[] NODE_URLS = {
        "http://10.242.34.96:8080/SmartHealthcareSystemnew/node-status",
        "http://10.242.38.191:8080/SmartHealthcareSystemnew/node-status",
        "http://10.242.71.209:8080/SmartHealthcareSystemnew/node-status",
        "http://10.242.34.96:8080/SmartHealthcareSystemnew/node-status"
    };

    // Returns top two nodes (if needed elsewhere)
    public List<DelegatorNode> selectDelegatorNodes() {
        List<DelegatorNode> nodes = fetchRealTimeNodeData();
        calculateFitness(nodes);
        nodes.sort(Comparator.comparingDouble(DelegatorNode::getFitness).reversed());
        return nodes.subList(0, Math.min(2, nodes.size()));
    }
    
    // New method to return all nodes with metrics
    public List<DelegatorNode> getAllNodes() {
        List<DelegatorNode> nodes = fetchRealTimeNodeData();
        calculateFitness(nodes);
        return nodes;
    }
    
    private List<DelegatorNode> fetchRealTimeNodeData() {
        List<DelegatorNode> nodes = new ArrayList<>();
        for (String url : NODE_URLS) {
            try {
                JSONObject data = fetchNodeStatus(url);
                String nodeName = data.get("nodeName").toString();
                double trust = Double.parseDouble(data.get("trust").toString());
                double energy = Double.parseDouble(data.get("energy").toString());
                double load = Double.parseDouble(data.get("load").toString());
                double resourceAvailability = Double.parseDouble(data.get("resourceAvailability").toString());
                nodes.add(new DelegatorNode(nodeName, url, trust, energy, load, resourceAvailability));
            } catch (Exception e) {
                System.err.println("Error fetching data from " + url + ": " + e.getMessage());
            }
        }
        return nodes;
    }
    
    private JSONObject fetchNodeStatus(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JSONParser parser = new JSONParser();
        JSONObject data = (JSONObject) parser.parse(in);
        in.close();
        return data;
    }
    
    private void calculateFitness(List<DelegatorNode> nodes) {
        for (DelegatorNode node : nodes) {
            double fitness = 0.4 * node.getTrust() +
                             0.3 * node.getEnergy() +
                             0.2 * (1 - node.getLoad()) +  // Lower load is better
                             0.1 * node.getResourceAvailability();
            node.setFitness(fitness);
        }
    }
}
