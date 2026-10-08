package com.banking.network;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Encapsulates the output of Distance Vector routing execution,
 * including convergence status, iteration count, shortest path, total cost, and all vector tables.
 */
public final class DistanceVectorResult {

    private final NetworkNode source;
    private final NetworkNode destination;
    private final double totalCost;
    private final NetworkNode nextHop;
    private final List<NetworkNode> path;
    private final int iterations;
    private final boolean converged;
    private final boolean reachable;
    private final Map<String, DistanceVectorTable> allTables;

    public DistanceVectorResult(NetworkNode source,
                                NetworkNode destination,
                                double totalCost,
                                NetworkNode nextHop,
                                List<NetworkNode> path,
                                int iterations,
                                boolean converged,
                                boolean reachable,
                                Map<String, DistanceVectorTable> allTables) {
        this.source = Objects.requireNonNull(source, "Source node cannot be null");
        this.destination = Objects.requireNonNull(destination, "Destination node cannot be null");
        this.totalCost = totalCost;
        this.nextHop = nextHop;
        this.path = path != null ? Collections.unmodifiableList(path) : Collections.emptyList();
        this.iterations = iterations;
        this.converged = converged;
        this.reachable = reachable;
        this.allTables = allTables != null ? Collections.unmodifiableMap(allTables) : Collections.emptyMap();
    }

    public NetworkNode getSource() {
        return source;
    }

    public NetworkNode getDestination() {
        return destination;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public NetworkNode getNextHop() {
        return nextHop;
    }

    public List<NetworkNode> getPath() {
        return path;
    }

    public int getIterations() {
        return iterations;
    }

    public boolean isConverged() {
        return converged;
    }

    public boolean isReachable() {
        return reachable;
    }

    public Map<String, DistanceVectorTable> getAllTables() {
        return allTables;
    }

    /**
     * Returns a formatted arrow string representation of the node path.
     *
     * @return formatted path string (e.g. "Branch 1 Gateway -> Branch 1 Router -> HQ Core Router -> Database Server")
     */
    public String getFormattedPath() {
        if (!reachable) {
            return "UNREACHABLE";
        }
        return path.stream()
                .map(NetworkNode::getNodeName)
                .collect(Collectors.joining(" -> "));
    }

    @Override
    public String toString() {
        if (!reachable) {
            return String.format("DistanceVectorResult[Source: %s, Destination: %s -> UNREACHABLE, Iterations: %d, Converged: %b]",
                    source.getNodeName(), destination.getNodeName(), iterations, converged);
        }
        return String.format("DistanceVectorResult[Source: %s, Destination: %s, Cost: %.1f, NextHop: %s, Iterations: %d, Path: %s]",
                source.getNodeName(),
                destination.getNodeName(),
                totalCost,
                nextHop != null ? nextHop.getNodeName() : "LOCAL",
                iterations,
                getFormattedPath());
    }
}
