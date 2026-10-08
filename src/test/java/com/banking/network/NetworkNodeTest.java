package com.banking.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkNodeTest {

    @Test
    @DisplayName("Test creating a valid NetworkNode")
    void testValidNetworkNode() {
        IPAddress ip = new IPAddress("10.0.1.10");
        Subnet subnet = BankingNetworkConfig.BRANCH_1;
        NetworkNode node = new NetworkNode("NODE-B1-01", "Branch 1 Gateway", ip, subnet, NodeType.BRANCH);

        assertEquals("NODE-B1-01", node.getNodeId());
        assertEquals("Branch 1 Gateway", node.getNodeName());
        assertEquals(ip, node.getIpAddress());
        assertEquals(subnet, node.getSubnet());
        assertEquals(NodeType.BRANCH, node.getNodeType());
    }

    @Test
    @DisplayName("Test NetworkNode validation when IP does not belong to subnet")
    void testNodeIpMismatch() {
        IPAddress ip = new IPAddress("10.0.2.10"); // Belongs to Branch 2
        Subnet subnet = BankingNetworkConfig.BRANCH_1; // Branch 1 subnet

        assertThrows(IllegalArgumentException.class, () ->
                new NetworkNode("NODE-MISMATCH", "Invalid Node", ip, subnet, NodeType.BRANCH)
        );
    }
}
