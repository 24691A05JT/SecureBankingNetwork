package com.banking.network;

import java.util.*;

/**
 * Link-State Routing Engine implementing Dijkstra's Shortest Path algorithm.
 * Manually calculates optimal routes across graph topologies without third-party graph libraries.
 */
public class DijkstraRouter {

    /**
     * Helper pair for PriorityQueue ordering in Dijkstra's algorithm.
     */
    private static final class NodeDistance implements Comparable<NodeDistance> {
        private final NetworkNode node;
        private final double distance;

        public NodeDistance(NetworkNode node, double distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    /**
     * Computes the shortest path from source to destination using Dijkstra's algorithm.
     *
     * @param topology the network topology
     * @param source starting node
     * @param destination target node
     * @return DijkstraResult containing shortest path, total cost, and next hop
     * @throws IllegalArgumentException if topology, source, or destination is null or not present in topology
     */
    public DijkstraResult findShortestPath(NetworkTopology topology, NetworkNode source, NetworkNode destination) {
        if (topology == null) {
            throw new IllegalArgumentException("Network topology cannot be null.");
        }
        if (source == null) {
            throw new IllegalArgumentException("Source node cannot be null.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination node cannot be null.");
        }
        if (!topology.containsNode(source.getNodeId())) {
            throw new IllegalArgumentException("Source node '" + source.getNodeId() + "' does not exist in topology.");
        }
        if (!topology.containsNode(destination.getNodeId())) {
            throw new IllegalArgumentException("Destination node '" + destination.getNodeId() + "' does not exist in topology.");
        }

        // Edge case 1: Source and Destination are the same node
        if (source.equals(destination)) {
            List<NetworkNode> singleNodePath = List.of(source);
            return new DijkstraResult(source, destination, 0.0, singleNodePath, true);
        }

        Map<String, Double> distMap = new HashMap<>();
        Map<String, NetworkNode> prevMap = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>();

        // 1. Initialize distances
        for (NetworkNode node : topology.getNodes()) {
            distMap.put(node.getNodeId(), Double.POSITIVE_INFINITY);
        }
        distMap.put(source.getNodeId(), 0.0);
        pq.add(new NodeDistance(source, 0.0));

        // 2. Process nodes
        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            NetworkNode u = current.node;
            String uId = u.getNodeId();

            if (visited.contains(uId)) {
                continue;
            }
            visited.add(uId);

            // Optimization: If we reached the destination, we can break early
            if (uId.equals(destination.getNodeId())) {
                break;
            }

            double distU = distMap.get(uId);

            // Relax outgoing links
            for (NetworkLink link : topology.getOutgoingLinks(u)) {
                if (link.getCost() < 0) {
                    throw new IllegalArgumentException("Dijkstra algorithm does not support negative link costs: link "
                            + link + " has cost " + link.getCost());
                }

                NetworkNode v = link.getDestination();
                String vId = v.getNodeId();

                if (visited.contains(vId)) {
                    continue;
                }

                double newDist = distU + link.getCost();
                if (newDist < distMap.get(vId)) {
                    distMap.put(vId, newDist);
                    prevMap.put(vId, u);
                    pq.add(new NodeDistance(v, newDist));
                }
            }
        }

        // 3. Reconstruct Path
        double finalCost = distMap.get(destination.getNodeId());
        if (Double.isInfinite(finalCost) || !prevMap.containsKey(destination.getNodeId())) {
            // Unreachable destination
            return new DijkstraResult(source, destination, Double.POSITIVE_INFINITY, Collections.emptyList(), false);
        }

        LinkedList<NetworkNode> path = new LinkedList<>();
        NetworkNode step = destination;
        path.addFirst(step);

        while (prevMap.containsKey(step.getNodeId())) {
            step = prevMap.get(step.getNodeId());
            path.addFirst(step);
        }

        return new DijkstraResult(source, destination, finalCost, path, true);
    }

    /**
     * Helper overload to find shortest path using node IDs.
     *
     * @param topology network topology
     * @param sourceId source node ID
     * @param destId destination node ID
     * @return DijkstraResult
     */
    public DijkstraResult findShortestPath(NetworkTopology topology, String sourceId, String destId) {
        if (topology == null) throw new IllegalArgumentException("Topology cannot be null.");
        NetworkNode src = topology.getNode(sourceId);
        NetworkNode dest = topology.getNode(destId);
        return findShortestPath(topology, src, dest);
    }

    /**
     * Builds the complete routing table for a given source node to all reachable nodes in the topology.
     *
     * @param topology network topology
     * @param source source node
     * @return list of RoutingTableEntry objects
     */
    public List<RoutingTableEntry> buildRoutingTable(NetworkTopology topology, NetworkNode source) {
        if (topology == null || source == null) {
            throw new IllegalArgumentException("Topology and source node cannot be null.");
        }
        List<RoutingTableEntry> table = new ArrayList<>();
        for (NetworkNode destination : topology.getNodes()) {
            DijkstraResult result = findShortestPath(topology, source, destination);
            table.add(new RoutingTableEntry(result));
        }
        table.sort(Comparator.comparing(entry -> entry.getDestinationNode().getNodeId()));
        return table;
    }

    /**
     * Builds routing tables for all nodes in the topology.
     *
     * @param topology network topology
     * @return map of source node ID to list of RoutingTableEntry objects
     */
    public Map<String, List<RoutingTableEntry>> buildAllRoutingTables(NetworkTopology topology) {
        if (topology == null) throw new IllegalArgumentException("Topology cannot be null.");
        Map<String, List<RoutingTableEntry>> allTables = new LinkedHashMap<>();
        for (NetworkNode source : topology.getNodes()) {
            allTables.put(source.getNodeId(), buildRoutingTable(topology, source));
        }
        return allTables;
    }
}
