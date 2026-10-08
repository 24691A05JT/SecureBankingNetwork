package com.banking.network;

import java.util.*;

/**
 * Distance Vector Routing Engine implementing the Bellman-Ford algorithm.
 * Independently calculates shortest path routing vectors across graph topologies via iterative table convergence.
 */
public class DistanceVectorRouter {

    private static final int DEFAULT_MAX_ITERATIONS = 100;

    /**
     * Executes Distance Vector routing over the provided topology and calculates the shortest path
     * between source and destination nodes.
     *
     * @param topology network topology graph
     * @param source source node
     * @param destination destination node
     * @return DistanceVectorResult containing route metrics, iterations, and routing tables
     */
    public DistanceVectorResult computeRouting(NetworkTopology topology, NetworkNode source, NetworkNode destination) {
        return computeRouting(topology, source, destination, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Computes Distance Vector routing with a configurable maximum safety iteration limit.
     *
     * @param topology network topology graph
     * @param source source node
     * @param destination destination node
     * @param maxIterations safety upper bound for convergence loop
     * @return DistanceVectorResult
     */
    public DistanceVectorResult computeRouting(NetworkTopology topology, NetworkNode source, NetworkNode destination, int maxIterations) {
        if (topology == null) throw new IllegalArgumentException("Network topology cannot be null.");
        if (source == null) throw new IllegalArgumentException("Source node cannot be null.");
        if (destination == null) throw new IllegalArgumentException("Destination node cannot be null.");
        if (!topology.containsNode(source.getNodeId())) {
            throw new IllegalArgumentException("Source node '" + source.getNodeId() + "' does not exist in topology.");
        }
        if (!topology.containsNode(destination.getNodeId())) {
            throw new IllegalArgumentException("Destination node '" + destination.getNodeId() + "' does not exist in topology.");
        }

        Map<String, DistanceVectorTable> tables = initializeTables(topology);

        // Bellman-Ford Iterative Convergence Loop
        int iterations = 0;
        boolean converged = false;

        for (int i = 1; i <= maxIterations; i++) {
            iterations = i;
            boolean changedInIteration = false;

            // Compute candidate updates for all nodes
            for (NetworkNode u : topology.getNodes()) {
                DistanceVectorTable uTable = tables.get(u.getNodeId());
                List<NetworkLink> outgoingLinks = topology.getOutgoingLinks(u);

                for (NetworkNode d : topology.getNodes()) {
                    if (u.equals(d)) {
                        continue; // Self distance remains 0.0
                    }

                    double currentCost = Double.POSITIVE_INFINITY;
                    DistanceVectorEntry existingEntry = uTable.getEntry(d);
                    if (existingEntry != null && existingEntry.isReachable()) {
                        currentCost = existingEntry.getCost();
                    }

                    double bestCost = currentCost;
                    NetworkNode bestNextHop = existingEntry != null ? existingEntry.getNextHop() : null;
                    boolean isReachable = existingEntry != null && existingEntry.isReachable();

                    for (NetworkLink link : outgoingLinks) {
                        if (link.getCost() < 0) {
                            throw new IllegalArgumentException("Distance Vector routing does not support negative link costs: link "
                                    + link + " has cost " + link.getCost());
                        }

                        NetworkNode v = link.getDestination();
                        double linkCost = link.getCost();

                        DistanceVectorTable vTable = tables.get(v.getNodeId());
                        DistanceVectorEntry vToDEntry = vTable.getEntry(d);

                        if (vToDEntry != null && vToDEntry.isReachable()) {
                            double candidateCost = linkCost + vToDEntry.getCost();
                            if (candidateCost < bestCost - 1e-9) {
                                bestCost = candidateCost;
                                bestNextHop = v;
                                isReachable = true;
                            }
                        }
                    }

                    if (isReachable && uTable.updateEntry(d, bestNextHop, bestCost, true)) {
                        changedInIteration = true;
                    }
                }
            }

            if (!changedInIteration) {
                converged = true;
                break;
            }
        }

        // Handle path extraction for requested source & destination
        if (source.equals(destination)) {
            List<NetworkNode> singleNodePath = List.of(source);
            return new DistanceVectorResult(source, destination, 0.0, null, singleNodePath, iterations, converged, true, tables);
        }

        DistanceVectorTable sourceTable = tables.get(source.getNodeId());
        DistanceVectorEntry targetEntry = sourceTable.getEntry(destination);

        if (targetEntry == null || !targetEntry.isReachable()) {
            return new DistanceVectorResult(source, destination, Double.POSITIVE_INFINITY, null, Collections.emptyList(), iterations, converged, false, tables);
        }

        // Trace path through next hops
        List<NetworkNode> path = reconstructPath(tables, source, destination);
        NetworkNode nextHop = targetEntry.getNextHop();

        return new DistanceVectorResult(source, destination, targetEntry.getCost(), nextHop, path, iterations, converged, true, tables);
    }

    /**
     * Initializes routing tables for all nodes in the topology.
     */
    private Map<String, DistanceVectorTable> initializeTables(NetworkTopology topology) {
        Map<String, DistanceVectorTable> tables = new LinkedHashMap<>();

        for (NetworkNode u : topology.getNodes()) {
            DistanceVectorTable table = new DistanceVectorTable(u);
            // Distance to self = 0
            table.updateEntry(u, u, 0.0, true);

            // Directly connected neighbors
            for (NetworkLink link : topology.getOutgoingLinks(u)) {
                NetworkNode v = link.getDestination();
                double cost = link.getCost();
                DistanceVectorEntry existing = table.getEntry(v);
                if (existing == null || cost < existing.getCost()) {
                    table.updateEntry(v, v, cost, true);
                }
            }
            tables.put(u.getNodeId(), table);
        }
        return tables;
    }

    /**
     * Reconstructs the complete path from source to destination by following next hops.
     */
    private List<NetworkNode> reconstructPath(Map<String, DistanceVectorTable> tables, NetworkNode source, NetworkNode destination) {
        List<NetworkNode> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        NetworkNode current = source;

        path.add(current);
        visited.add(current.getNodeId());

        while (!current.equals(destination)) {
            DistanceVectorTable curTable = tables.get(current.getNodeId());
            if (curTable == null) break;

            DistanceVectorEntry entry = curTable.getEntry(destination);
            if (entry == null || !entry.isReachable() || entry.getNextHop() == null) {
                break;
            }

            NetworkNode next = entry.getNextHop();
            if (visited.contains(next.getNodeId())) {
                // Prevent infinite loop if loop detected
                break;
            }

            path.add(next);
            visited.add(next.getNodeId());
            current = next;
        }

        return path;
    }
}
