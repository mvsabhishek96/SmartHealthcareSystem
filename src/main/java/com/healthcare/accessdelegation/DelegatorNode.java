package com.healthcare.accessdelegation;

public class DelegatorNode {
    private String nodeName;
    private double trust;
    private int energy;
    private int load;
    private int resourceAvailability;
    private double fitness;

    public DelegatorNode(String nodeName, double trust, int energy, int load, int resourceAvailability) {
        this.nodeName = nodeName;
        this.trust = trust;
        this.energy = energy;
        this.load = load;
        this.resourceAvailability = resourceAvailability;
    }

    public String getNodeName() {
        return nodeName;
    }

    public double getTrust() {
        return trust;
    }

    public int getEnergy() {
        return energy;
    }

    public int getLoad() {
        return load;
    }

    public int getResourceAvailability() {
        return resourceAvailability;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
    
    // Added method: simulate an access decision with 80% probability of approval.
    public boolean decideAccess(String accessRequest) {
        return Math.random() > 0.2;
    }
}
