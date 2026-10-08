package com.banking.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraRouterTest {

    private DijkstraRouter router;

    @BeforeEach
    void setUp() {
        router = new DijkstraRouter();
    }

    @Test
    @DisplayName("Test basic shortest path: A -> B -> C (cost 5) vs A -> C (cost 10)")
    void testBasicShortestPath() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeC = new NetworkNode("NODE-C", "Node C", "10.0.0.3", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(nodeA);
        topology.addNode(nodeB);
        topology.addNode(nodeC);

        topology.addLink(new NetworkLink(nodeA, nodeB, 2.0));
        topology.addLink(new NetworkLink(nodeB, nodeC, 3.0));
        topology.addLink(new NetworkLink(nodeA, nodeC, 10.0));

        DijkstraResult result = router.findShortestPath(topology, nodeA, nodeC);

        assertTrue(result.isReachable());
        assertEquals(5.0, result.getTotalCost());
        assertEquals(3, result.getPath().size());
        assertEquals(List.of(nodeA, nodeB, nodeC), result.getPath());
        assertEquals(nodeB, result.getNextHop());
    }

    @Test
    @DisplayName("Test direct route: A -> B (cost 4)")
    void testDirectRoute() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(nodeA);
        topology.addNode(nodeB);
        topology.addLink(new NetworkLink(nodeA, nodeB, 4.0));

        DijkstraResult result = router.findShortestPath(topology, nodeA, nodeB);

        assertTrue(result.isReachable());
        assertEquals(4.0, result.getTotalCost());
        assertEquals(List.of(nodeA, nodeB), result.getPath());
        assertEquals(nodeB, result.getNextHop());
    }

    @Test
    @DisplayName("Test multiple routes and optimal selection")
    void testMultipleRoutesSelection() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode src = new NetworkNode("SRC", "Source", "10.0.0.1", "10.0.0.0/24", NodeType.BRANCH);
        NetworkNode r1 = new NetworkNode("R1", "Router 1", "10.0.0.2", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode r2 = new NetworkNode("R2", "Router 2", "10.0.0.3", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode dest = new NetworkNode("DEST", "Destination", "10.0.0.4", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(src);
        topology.addNode(r1);
        topology.addNode(r2);
        topology.addNode(dest);

        // Path 1 via R1: SRC -> R1 (3) -> DEST (4) = Total 7
        topology.addLink(new NetworkLink(src, r1, 3.0));
        topology.addLink(new NetworkLink(r1, dest, 4.0));

        // Path 2 via R2: SRC -> R2 (2) -> DEST (10) = Total 12
        topology.addLink(new NetworkLink(src, r2, 2.0));
        topology.addLink(new NetworkLink(r2, dest, 10.0));

        DijkstraResult result = router.findShortestPath(topology, src, dest);

        assertTrue(result.isReachable());
        assertEquals(7.0, result.getTotalCost());
        assertEquals(r1, result.getNextHop());
        assertEquals(List.of(src, r1, dest), result.getPath());
    }

    @Test
    @DisplayName("Test same source and destination")
    void testSameSourceAndDestination() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode node = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.HEAD_OFFICE);
        topology.addNode(node);

        DijkstraResult result = router.findShortestPath(topology, node, node);

        assertTrue(result.isReachable());
        assertEquals(0.0, result.getTotalCost());
        assertEquals(List.of(node), result.getPath());
        assertNull(result.getNextHop());
    }

    @Test
    @DisplayName("Test unreachable destination detection")
    void testUnreachableDestination() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(nodeA);
        topology.addNode(nodeB);
        // No links added

        DijkstraResult result = router.findShortestPath(topology, nodeA, nodeB);

        assertFalse(result.isReachable());
        assertEquals(Double.POSITIVE_INFINITY, result.getTotalCost());
        assertTrue(result.getPath().isEmpty());
        assertNull(result.getNextHop());
        assertEquals("UNREACHABLE", result.getFormattedPath());
    }

    @Test
    @DisplayName("Test banking network topology Dijkstra routing")
    void testBankingTopologyRouting() {
        NetworkTopology topology = BankingTopologyBuilder.createSampleTopology();

        NetworkNode branch1 = topology.getNode("NODE-B1");
        NetworkNode database = topology.getNode("NODE-DB");
        NetworkNode routerB1 = topology.getNode("ROUTER-B1");

        DijkstraResult result = router.findShortestPath(topology, branch1, database);

        assertTrue(result.isReachable());
        assertEquals(8.0, result.getTotalCost());
        assertEquals(routerB1, result.getNextHop());
        assertEquals("Branch 1 Gateway -> Branch 1 Router -> HQ Core Router -> Database Server", result.getFormattedPath());
    }

    @Test
    @DisplayName("Test routing table generation for a node")
    void testBuildRoutingTable() {
        NetworkTopology topology = BankingTopologyBuilder.createSampleTopology();
        NetworkNode branch1 = topology.getNode("NODE-B1");

        List<RoutingTableEntry> routingTable = router.buildRoutingTable(topology, branch1);

        assertEquals(topology.getNodeCount(), routingTable.size());
        assertTrue(routingTable.stream().allMatch(RoutingTableEntry::isReachable));
    }
}
