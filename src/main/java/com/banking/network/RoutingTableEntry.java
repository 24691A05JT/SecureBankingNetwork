package com.banking.network;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents an entry in a node's routing table generated via Dijkstra shortest path calculations.
 */
public final class RoutingTableEntry {

    private final NetworkNode sourceNode;
    private final NetworkNode destinationNode;
    private final NetworkNode nextHop;
    private final double totalCost;
    private final List<NetworkNode> path;
    private final boolean reachable;

    public RoutingTableEntry(DijkstraResult result) {
        Objects.requireNonNull(result, "DijkstraResult cannot be null");
        this.sourceNode = result.getSource();
        this.destinationNode = result.getDestination();
        this.nextHop = result.getNextHop();
        this.totalCost = result.getTotalCost();
        this.path = result.getPath();
        this.reachable = result.isReachable();
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

    public double getTotalCost() {
        return totalCost;
    }

    public List<NetworkNode> getPath() {
        return path;
    }

    public boolean isReachable() {
        return reachable;
    }

    public String getFormattedPath() {
        if (!reachable) return "UNREACHABLE";
        return path.stream()
                .map(NetworkNode::getNodeName)
                .collect(Collectors.joining(" -> "));
    }

    @Override
    public String toString() {
        String nextHopStr = (nextHop != null) ? nextHop.getNodeName() : (sourceNode.equals(destinationNode) ? "LOCAL" : "NONE");
        String costStr = reachable ? String.format("%.1f", totalCost) : "INF";
        return String.format("%-18s | %-18s | %-18s | %-6s | %s",
                sourceNode.getNodeName(), destinationNode.getNodeName(), nextHopStr, costStr, getFormattedPath());
    }
}
