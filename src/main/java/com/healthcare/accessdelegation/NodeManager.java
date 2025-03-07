package com.healthcare.accessdelegation;

import com.healthcare.accessdelegation.DelegatorNode;
import java.util.ArrayList;
import java.util.List;

public class NodeManager {
    public List<DelegatorNode> getAvailableNodes() {
        List<DelegatorNode> nodes = new ArrayList<>();
        nodes.add(new DelegatorNode("Node1", 0.9, 80, 50, 70));
        nodes.add(new DelegatorNode("Node2", 0.8, 70, 60, 60));
        nodes.add(new DelegatorNode("Node3", 0.85, 75, 55, 65));
        nodes.add(new DelegatorNode("Node4", 0.95, 90, 45, 80));
        return nodes;
    }
}
