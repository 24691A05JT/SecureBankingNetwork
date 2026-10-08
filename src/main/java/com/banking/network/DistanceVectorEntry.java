package com.banking.network;

import java.util.Objects;

/**
 * Represents a single routing entry in a node's Distance Vector table.
 * Stores destination, next hop, total path cost, and reachability status.
 */
public final class DistanceVectorEntry {

    private final NetworkNode sourceNode;
    private final NetworkNode destinationNode;
    private final NetworkNode nextHop;
    private final double cost;
    private final boolean reachable;

    public DistanceVectorEntry(NetworkNode sourceNode, NetworkNode destinationNode, NetworkNode nextHop, double cost, boolean reachable) {
        this.sourceNode = Objects.requireNonNull(sourceNode, "Source node cannot be null");
        this.destinationNode = Objects.requireNonNull(destinationNode, "Destination node cannot be null");
        this.nextHop = nextHop;
        this.cost = cost;
        this.reachable = reachable;
    }

    public NetworkNode getSourceNode() {
        return sourceNode;
    }

    public NetworkNode getDestinationNode() {
        return destinationNode;
    }

    public NetworkNode getNextHop() {
        return nextHop;
    }

    public double getCost() {
        return cost;
    }

    public boolean isReachable() {
        return reachable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DistanceVectorEntry entry = (DistanceVectorEntry) o;
        return Double.compare(entry.cost, cost) == 0 &&
                reachable == entry.reachable &&
                Objects.equals(sourceNode, entry.sourceNode) &&
                Objects.equals(destinationNode, entry.destinationNode) &&
                Objects.equals(nextHop, entry.nextHop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceNode, destinationNode, nextHop, cost, reachable);
    }

    @Override
    public String toString() {
        String nextHopStr = (nextHop != null) ? nextHop.getNodeName() : (sourceNode.equals(destinationNode) ? "LOCAL" : "NONE");
        String costStr = reachable ? String.format("%.1f", cost) : "INF";
        return String.format("%-25s | %-20s | %-6s",
                destinationNode.getNodeName(), nextHopStr, costStr);
    }
}
