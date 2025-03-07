package com.healthcare.accesscontrol;

import java.util.List;
import com.healthcare.accessdelegation.DelegatorNode;

public class PBFTConsensus {
    /**
     * Executes the PBFT consensus algorithm.
     * @param delegatorNodes List of delegator nodes participating in consensus.
     * @param data The data (e.g., patient data) under consideration.
     * @param accessRequest The access request (e.g., user ID).
     * @return true if consensus grants access; false otherwise.
     */
    public boolean executeConsensus(List<DelegatorNode> delegatorNodes, String data, String accessRequest) {
        int totalNodes = delegatorNodes.size();
        int faultTolerance = (totalNodes - 1) / 3;  // Allow up to (totalNodes-1)/3 faulty nodes.
        int requiredConsensus = totalNodes - faultTolerance;
        
        int agreeCount = 0;
        for (DelegatorNode node : delegatorNodes) {
            if (node.decideAccess(accessRequest)) {
                agreeCount++;
            }
        }
        return agreeCount >= requiredConsensus;
    }
}
