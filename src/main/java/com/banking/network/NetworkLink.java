package com.banking.network;

import java.util.Objects;

/**
 * Represents a directional or logical link connecting two network nodes with a specified routing cost.
 */
public class NetworkLink {

    private final NetworkNode source;
    private final NetworkNode destination;
    private final double cost;
    private final double bandwidthMbps;
    private final double latencyMs;

    /**
     * Constructs a NetworkLink with source, destination, and routing cost.
     *
     * @param source destination source node
     * @param destination target destination node
     * @param cost routing metric/weight (must be > 0)
     * @throws IllegalArgumentException if nodes are null or cost <= 0
     */
    public NetworkLink(NetworkNode source, NetworkNode destination, double cost) {
        this(source, destination, cost, 1000.0, 1.0); // Default 1Gbps, 1ms latency
    }

    /**
     * Full constructor including bandwidth and latency metrics.
     *
     * @param source source node
     * @param destination destination node
     * @param cost routing cost (must be > 0)
     * @param bandwidthMbps bandwidth in Mbps
     * @param latencyMs latency in milliseconds
     */
    public NetworkLink(NetworkNode source, NetworkNode destination, double cost, double bandwidthMbps, double latencyMs) {
        this.source = Objects.requireNonNull(source, "Source node cannot be null");
        this.destination = Objects.requireNonNull(destination, "Destination node cannot be null");

        if (cost <= 0) {
            throw new IllegalArgumentException("Routing link cost must be positive (> 0), got: " + cost);
        }
        if (bandwidthMbps < 0) {
            throw new IllegalArgumentException("Bandwidth cannot be negative, got: " + bandwidthMbps);
        }
        if (latencyMs < 0) {
            throw new IllegalArgumentException("Latency cannot be negative, got: " + latencyMs);
        }

        this.cost = cost;
        this.bandwidthMbps = bandwidthMbps;
        this.latencyMs = latencyMs;
    }

    public NetworkNode getSource() {
        return source;
    }

    public NetworkNode getDestination() {
        return destination;
    }

    public double getCost() {
        return cost;
    }

    public double getBandwidthMbps() {
        return bandwidthMbps;
    }

    public double getLatencyMs() {
        return latencyMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkLink that = (NetworkLink) o;
        return Double.compare(that.cost, cost) == 0 &&
                Objects.equals(source, that.source) &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, cost);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (cost: %.1f)", source.getNodeName(), destination.getNodeName(), cost);
    }
}
