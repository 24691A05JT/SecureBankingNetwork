package com.banking.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistanceVectorRouterTest {

    private DistanceVectorRouter router;

    @BeforeEach
    void setUp() {
        router = new DistanceVectorRouter();
    }

    @Test
    @DisplayName("Test 1 — Basic network: A -> B -> C (cost 5)")
    void testBasicNetwork() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeC = new NetworkNode("NODE-C", "Node C", "10.0.0.3", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(nodeA);
        topology.addNode(nodeB);
        topology.addNode(nodeC);

        topology.addBidirectionalLink(nodeA, nodeB, 2.0);
        topology.addBidirectionalLink(nodeB, nodeC, 3.0);

        DistanceVectorResult result = router.computeRouting(topology, nodeA, nodeC);

        assertTrue(result.isConverged());
        assertTrue(result.isReachable());
        assertEquals(5.0, result.getTotalCost());
        assertEquals(nodeB, result.getNextHop());
        assertEquals(List.of(nodeA, nodeB, nodeC), result.getPath());
    }

    @Test
    @DisplayName("Test 2 — Multiple paths: selects lower-cost path")
    void testMultiplePaths() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode src = new NetworkNode("SRC", "Source", "10.0.0.1", "10.0.0.0/24", NodeType.BRANCH);
        NetworkNode b = new NetworkNode("B", "Router B", "10.0.0.2", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode d = new NetworkNode("D", "Router D", "10.0.0.3", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode dest = new NetworkNode("DEST", "Destination", "10.0.0.4", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(src);
        topology.addNode(b);
        topology.addNode(d);
        topology.addNode(dest);

        // Path 1 via B: SRC -> B (10) -> DEST (5) = 15
        topology.addBidirectionalLink(src, b, 10.0);
        topology.addBidirectionalLink(b, dest, 5.0);

        // Path 2 via D: SRC -> D (3) -> DEST (4) = 7
        topology.addBidirectionalLink(src, d, 3.0);
        topology.addBidirectionalLink(d, dest, 4.0);

        DistanceVectorResult result = router.computeRouting(topology, src, dest);

        assertTrue(result.isConverged());
        assertTrue(result.isReachable());
        assertEquals(7.0, result.getTotalCost());
        assertEquals(d, result.getNextHop());
        assertEquals(List.of(src, d, dest), result.getPath());
    }

    @Test
    @DisplayName("Test 3 — Direct route")
    void testDirectRoute() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(nodeA);
        topology.addNode(nodeB);
        topology.addBidirectionalLink(nodeA, nodeB, 4.0);

        DistanceVectorResult result = router.computeRouting(topology, nodeA, nodeB);

        assertTrue(result.isConverged());
        assertTrue(result.isReachable());
        assertEquals(4.0, result.getTotalCost());
        assertEquals(nodeB, result.getNextHop());
        assertEquals(List.of(nodeA, nodeB), result.getPath());
    }

    @Test
    @DisplayName("Test 4 — Same node: source = destination")
    void testSameNode() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode node = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.HEAD_OFFICE);
        topology.addNode(node);

        DistanceVectorResult result = router.computeRouting(topology, node, node);

        assertTrue(result.isConverged());
        assertTrue(result.isReachable());
        assertEquals(0.0, result.getTotalCost());
        assertNull(result.getNextHop());
        assertEquals(List.of(node), result.getPath());
    }

    @Test
    @DisplayName("Test 5 — Unreachable node handling")
    void testUnreachableNode() {
        NetworkTopology topology = new NetworkTopology();
        NetworkNode nodeA = new NetworkNode("NODE-A", "Node A", "10.0.0.1", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode nodeB = new NetworkNode("NODE-B", "Node B", "10.0.0.2", "10.0.0.0/24", NodeType.SERVER);

        topology.addNode(nodeA);
        topology.addNode(nodeB);

        DistanceVectorResult result = router.computeRouting(topology, nodeA, nodeB);

        assertTrue(result.isConverged());
        assertFalse(result.isReachable());
        assertEquals(Double.POSITIVE_INFINITY, result.getTotalCost());
        assertNull(result.getNextHop());
        assertTrue(result.getPath().isEmpty());
    }

    @Test
    @DisplayName("Test 6 — Convergence tracking")
    void testConvergenceTracking() {
        NetworkTopology topology = BankingTopologyBuilder.createSampleTopology();
        NetworkNode branch1 = topology.getNode("NODE-B1");
        NetworkNode database = topology.getNode("NODE-DB");

        DistanceVectorResult result = router.computeRouting(topology, branch1, database);

        assertTrue(result.isConverged());
        assertTrue(result.getIterations() > 0);
        assertTrue(result.getIterations() <= 10);
    }

    @Test
    @DisplayName("Test 7 — Correct next-hop selection")
    void testNextHopSelection() {
        NetworkTopology topology = BankingTopologyBuilder.createSampleTopology();
        NetworkNode branch1 = topology.getNode("NODE-B1");
        NetworkNode routerB1 = topology.getNode("ROUTER-B1");
        NetworkNode database = topology.getNode("NODE-DB");

        DistanceVectorResult result = router.computeRouting(topology, branch1, database);

        assertEquals(routerB1, result.getNextHop());
    }

    @Test
    @DisplayName("Test 8 — Banking topology reachability across important nodes")
    void testBankingTopologyReachability() {
        NetworkTopology topology = BankingTopologyBuilder.createSampleTopology();

        NetworkNode branch1 = topology.getNode("NODE-B1");
        NetworkNode branch2 = topology.getNode("NODE-B2");
        NetworkNode atm = topology.getNode("NODE-ATM");
        NetworkNode hq = topology.getNode("NODE-HQ");
        NetworkNode database = topology.getNode("NODE-DB");

        DistanceVectorResult res1 = router.computeRouting(topology, branch1, hq);
        assertTrue(res1.isReachable());

        DistanceVectorResult res2 = router.computeRouting(topology, branch1, database);
        assertTrue(res2.isReachable());

        DistanceVectorResult res3 = router.computeRouting(topology, branch2, database);
        assertTrue(res3.isReachable());

        DistanceVectorResult res4 = router.computeRouting(topology, atm, hq);
        assertTrue(res4.isReachable());
    }

    @Test
    @DisplayName("Test 9 — Dijkstra vs Distance Vector routing equality")
    void testDijkstraVsDistanceVectorEquality() {
        NetworkTopology topology = BankingTopologyBuilder.createSampleTopology();
        DijkstraRouter dijkstraRouter = new DijkstraRouter();

        NetworkNode branch1 = topology.getNode("NODE-B1");
        NetworkNode database = topology.getNode("NODE-DB");

        DijkstraResult dijkstraResult = dijkstraRouter.findShortestPath(topology, branch1, database);
        DistanceVectorResult dvResult = router.computeRouting(topology, branch1, database);

        assertEquals(dijkstraResult.getTotalCost(), dvResult.getTotalCost(), 1e-6);
        assertEquals(dijkstraResult.getNextHop(), dvResult.getNextHop());
        assertEquals(dijkstraResult.getFormattedPath(), dvResult.getFormattedPath());
    }
}
