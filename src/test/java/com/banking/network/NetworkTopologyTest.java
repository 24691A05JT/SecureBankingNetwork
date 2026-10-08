package com.banking.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkTopologyTest {

    @Test
    @DisplayName("Test adding nodes and preventing duplicate node IDs")
    void testAddNodeAndDuplicates() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeADup = new NetworkNode("NODE-A", "Duplicate A", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(nodeA);
        assertEquals(1, topology.getNodeCount());
        assertTrue(topology.containsNode("NODE-A"));

        assertThrows(IllegalArgumentException.class, () -> topology.addNode(nodeADup));
    }

    @Test
    @DisplayName("Test adding valid links and rejecting invalid node references")
    void testAddLinkValidation() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);
        NetworkNode nodeExt = new NetworkNode("NODE-EXT", "External", "10.0.0.3", "10.0.0.0/24", NodeType.ATM);

        topology.addNode(nodeA);
        topology.addNode(nodeB);

        NetworkLink validLink = new NetworkLink(nodeA, nodeB, 4.0);
        topology.addLink(validLink);
        assertEquals(1, topology.getLinkCount());

        NetworkLink invalidLink = new NetworkLink(nodeA, nodeExt, 5.0);
        assertThrows(IllegalArgumentException.class, () -> topology.addLink(invalidLink));
    }

    @Test
    @DisplayName("Test neighbor retrieval")
    void testGetNeighbours() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);
        NetworkNode nodeC = new NetworkNode("NODE-C", "Node C", "10.0.0.3", "10.0.0.0/24", NodeType.ATM);

        topology.addNode(nodeA);
        topology.addNode(nodeB);
        topology.addNode(nodeC);

        topology.addLink(new NetworkLink(nodeA, nodeB, 2.0));
        topology.addLink(new NetworkLink(nodeA, nodeC, 3.0));

        List<NetworkNode> neighboursA = topology.getNeighbours(nodeA);
        assertEquals(2, neighboursA.size());
        assertTrue(neighboursA.contains(nodeB));
        assertTrue(neighboursA.contains(nodeC));
    }

    @Test
    @DisplayName("Test node removal and link cleanup")
    void testRemoveNode() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(nodeA);
        topology.addNode(nodeB);
        topology.addBidirectionalLink(nodeA, nodeB, 5.0);

        assertEquals(2, topology.getLinkCount());
        assertTrue(topology.removeNode("NODE-B"));
        assertEquals(1, topology.getNodeCount());
        assertEquals(0, topology.getLinkCount());
    }
}
