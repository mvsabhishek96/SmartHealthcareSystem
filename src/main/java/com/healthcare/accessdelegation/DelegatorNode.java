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

    /**
     * Access decision method.
     * Currently always approves access.
     * The 80% approval logic is commented out for future use.
     */
    public boolean decideAccess(String accessRequest) {
        // Always approve access
        return true;

        // 80% approval, 20% denial (commented out)
        // return Math.random() > 0.2;
    }
}
