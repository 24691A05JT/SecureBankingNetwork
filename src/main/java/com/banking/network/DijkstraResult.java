package com.banking.network;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Encapsulates the output of a Dijkstra shortest-path computation.
 */
public final class DijkstraResult {

    private final NetworkNode source;
    private final NetworkNode destination;
    private final double totalCost;
    private final List<NetworkNode> path;
    private final boolean reachable;

    public DijkstraResult(NetworkNode source, NetworkNode destination, double totalCost, List<NetworkNode> path, boolean reachable) {
        this.source = Objects.requireNonNull(source, "Source node cannot be null");
        this.destination = Objects.requireNonNull(destination, "Destination node cannot be null");
        this.totalCost = totalCost;
        this.path = path != null ? Collections.unmodifiableList(path) : Collections.emptyList();
        this.reachable = reachable;
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

    public List<NetworkNode> getPath() {
        return path;
    }

    public boolean isReachable() {
        return reachable;
    }

    /**
     * Identifies the immediate next hop node along the calculated shortest path.
     *
     * @return next hop NetworkNode, or null if source == destination or unreachable
     */
    public NetworkNode getNextHop() {
        if (!reachable || path.size() < 2) {
            return null;
        }
        return path.get(1);
    }

    /**
     * Returns a formatted arrow string representation of the node path.
     *
     * @return formatted path string (e.g. "Branch 1 -> Router-B1 -> Router-HQ -> Database Server")
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
            return String.format("DijkstraResult[Source: %s, Destination: %s -> UNREACHABLE]",
                    source.getNodeName(), destination.getNodeName());
        }
        return String.format("DijkstraResult[Source: %s, Destination: %s, Cost: %.1f, NextHop: %s, Path: %s]",
                source.getNodeName(),
                destination.getNodeName(),
                totalCost,
                getNextHop() != null ? getNextHop().getNodeName() : "DIRECT",
                getFormattedPath());
    }
}
