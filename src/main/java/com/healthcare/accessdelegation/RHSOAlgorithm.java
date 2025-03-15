package com.healthcare.accessdelegation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RHSOAlgorithm {
    // Base URLs for nodes (without any endpoint appended)
    private static final String[] NODE_BASE_URLS = {
        "http://10.242.34.96:8080/SmartHealthcareSystemnew",
        "http://10.242.38.191:8080/SmartHealthcareSystemnew",
        "http://10.242.71.209:8080/SmartHealthcareSystemnew",
        "http://10.242.34.96:8080/SmartHealthcareSystemnew"
    };

    // Static cache for optimal nodes
    private static volatile List<DelegatorNode> cachedOptimalNodes = new ArrayList<>();

    // Static initializer: compute optimal nodes on startup and update periodically.
    static {
        updateCachedNodes();
        // Refresh cache every 5 minutes.
        Thread cacheUpdater = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(300000); // 5 minutes in milliseconds
                    updateCachedNodes();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cacheUpdater.setDaemon(true);
        cacheUpdater.start();
    }

    // Update the cached optimal nodes.
    public static void updateCachedNodes() {
        RHSOAlgorithm algorithm = new RHSOAlgorithm();
        List<DelegatorNode> nodes = algorithm.getAllNodes();
        algorithm.calculateFitness(nodes);
        nodes.sort(Comparator.comparingDouble(DelegatorNode::getFitness).reversed());
        cachedOptimalNodes = nodes.subList(0, Math.min(2, nodes.size()));
    }

    // Return cached optimal nodes.
    public List<DelegatorNode> selectDelegatorNodes() {
        return cachedOptimalNodes;
    }
    
    // Returns all nodes with metrics.
    public List<DelegatorNode> getAllNodes() {
        List<DelegatorNode> nodes = new ArrayList<>();
        for (String baseUrl : NODE_BASE_URLS) {
            try {
                // Append /node-status to get node metrics.
                JSONObject data = fetchNodeStatus(baseUrl + "/node-status");
                String nodeName = data.get("nodeName").toString();
                double trust = Double.parseDouble(data.get("trust").toString());
                double energy = Double.parseDouble(data.get("energy").toString());
                double load = Double.parseDouble(data.get("load").toString());
                double resourceAvailability = Double.parseDouble(data.get("resourceAvailability").toString());
                // Save the base URL in the DelegatorNode so we can append /access-decision later.
                nodes.add(new DelegatorNode(nodeName, baseUrl, trust, energy, load, resourceAvailability));
            } catch (Exception e) {
                System.err.println("Error fetching data from " + baseUrl + "/node-status: " + e.getMessage());
            }
        }
        return nodes;
    }
    
    private JSONObject fetchNodeStatus(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JSONParser parser = new JSONParser();
        JSONObject data = (JSONObject) parser.parse(in);
        in.close();
        return data;
    }
    
    // Compute fitness for each node.
    public void calculateFitness(List<DelegatorNode> nodes) {
        for (DelegatorNode node : nodes) {
            double fitness = 0.4 * node.getTrust() +
                             0.3 * node.getEnergy() +
                             0.2 * (1 - node.getLoad()) +
                             0.1 * node.getResourceAvailability();
            node.setFitness(fitness);
        }
    }
}


















/**package com.healthcare.accessdelegation;

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
    // List of node-status endpoints
    private static final String[] NODE_URLS = {
        "http://10.242.34.96:8080/SmartHealthcareSystemnew/",
        "http://10.242.38.191:8080/SmartHealthcareSystemnew/",
        "http://10.242.71.209:8080/SmartHealthcareSystemnew/",
        "http://10.242.34.96:8080/SmartHealthcareSystemnew/"
    };

    // Static cache for optimal nodes
    private static volatile List<DelegatorNode> cachedOptimalNodes = new ArrayList<>();

    // Static initializer: compute optimal nodes at startup and update periodically.
    static {
        updateCachedNodes();
        // Refresh cache every 5 minutes.
        Thread cacheUpdater = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(300000); // 300,000 ms = 5 minutes
                    updateCachedNodes();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cacheUpdater.setDaemon(true);
        cacheUpdater.start();
    }

    // Update the cached optimal nodes.
    public static void updateCachedNodes() {
        RHSOAlgorithm algorithm = new RHSOAlgorithm();
        List<DelegatorNode> nodes = algorithm.getAllNodes();
        algorithm.calculateFitness(nodes);
        nodes.sort(Comparator.comparingDouble(DelegatorNode::getFitness).reversed());
        cachedOptimalNodes = nodes.subList(0, Math.min(2, nodes.size()));
    }

    // Return cached optimal nodes.
    public List<DelegatorNode> selectDelegatorNodes() {
        return cachedOptimalNodes;
    }
    
    // Returns all nodes with metrics.
    public List<DelegatorNode> getAllNodes() {
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
    
    // Compute fitness for each node.
    public void calculateFitness(List<DelegatorNode> nodes) {
        for (DelegatorNode node : nodes) {
            double fitness = 0.4 * node.getTrust() +
                             0.3 * node.getEnergy() +
                             0.2 * (1 - node.getLoad()) +
                             0.1 * node.getResourceAvailability();
            node.setFitness(fitness);
        }
    }
}**/
