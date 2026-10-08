package com.banking.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkLinkTest {

    @Test
    @DisplayName("Test creating a valid NetworkLink")
    void testValidLink() {
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);

        NetworkLink link = new NetworkLink(nodeA, nodeB, 5.0);

        assertEquals(nodeA, link.getSource());
        assertEquals(nodeB, link.getDestination());
        assertEquals(5.0, link.getCost());
    }

    @Test
    @DisplayName("Test rejection of negative or zero routing costs")
    void testInvalidCost() {
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);

        assertThrows(IllegalArgumentException.class, () -> new NetworkLink(nodeA, nodeB, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new NetworkLink(nodeA, nodeB, -5.0));
    }

    @Test
    @DisplayName("Test null source or destination node rejection")
    void testNullNodes() {
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);

        assertThrows(NullPointerException.class, () -> new NetworkLink(null, nodeA, 5.0));
        assertThrows(NullPointerException.class, () -> new NetworkLink(nodeA, null, 5.0));
    }
}
