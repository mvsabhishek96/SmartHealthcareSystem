package com.healthcare.accessdelegation;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;

public class RHSOAlgorithm {
    public List<DelegatorNode> selectDelegatorNodes(List<DelegatorNode> availableNodes) {
        for (DelegatorNode node : availableNodes) {
            double normalizedEnergy = node.getEnergy() / 100.0;
            double normalizedLoad = (100 - node.getLoad()) / 100.0; // lower load is better
            double normalizedResource = node.getResourceAvailability() / 100.0;
            double fitness = 0.4 * node.getTrust() + 0.3 * normalizedEnergy
                    + 0.2 * normalizedLoad + 0.1 * normalizedResource;
            node.setFitness(fitness);
        }

        // Sort nodes by fitness in descending order
        availableNodes.sort(Comparator.comparingDouble(DelegatorNode::getFitness).reversed());

        // Select top 2 nodes
        return new ArrayList<>(availableNodes.subList(0, Math.min(2, availableNodes.size())));
    }
}
