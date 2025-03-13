package com.healthcare.accessdelegation;

public class DelegatorNode {
    private String nodeName;
    private String url; // Full endpoint URL for the node
    private double trust;
    private double energy;
    private double load;
    private double resourceAvailability;
    private double fitness;
    
    public DelegatorNode(String nodeName, String url, double trust, double energy, double load, double resourceAvailability) {
        this.nodeName = nodeName;
        this.url = url;
        this.trust = trust;
        this.energy = energy;
        this.load = load;
        this.resourceAvailability = resourceAvailability;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public String getUrl() {
        return url;
    }
    
    public double getTrust() {
        return trust;
    }
    
    public double getEnergy() {
        return energy;
    }
    
    public double getLoad() {
        return load;
    }
    
    public double getResourceAvailability() {
        return resourceAvailability;
    }
    
    public double getFitness() {
        return fitness;
    }
    
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
