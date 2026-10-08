package com.banking.network;

import java.util.*;

/**
 * Represents the complete Distance Vector routing table maintained by a single node.
 * Maps destination nodes to DistanceVectorEntry records.
 */
public class DistanceVectorTable {

    private final NetworkNode sourceNode;
    private final Map<String, DistanceVectorEntry> entries = new LinkedHashMap<>();

    public DistanceVectorTable(NetworkNode sourceNode) {
        this.sourceNode = Objects.requireNonNull(sourceNode, "Source node cannot be null");
    }

    public NetworkNode getSourceNode() {
        return sourceNode;
    }

    /**
     * Gets the entry for a given destination node.
     *
     * @param destination destination node
     * @return DistanceVectorEntry or null
     */
    public DistanceVectorEntry getEntry(NetworkNode destination) {
        if (destination == null) return null;
        return entries.get(destination.getNodeId());
    }

    /**
     * Gets the entry for a given destination node ID.
     *
     * @param destNodeId destination node ID
     * @return DistanceVectorEntry or null
     */
    public DistanceVectorEntry getEntry(String destNodeId) {
        if (destNodeId == null) return null;
        return entries.get(destNodeId);
    }

    /**
     * Updates or inserts a distance vector entry for a destination node.
     *
     * @param destination destination node
     * @param nextHop next hop node
     * @param cost total route cost
     * @param reachable true if destination is reachable
     * @return true if the entry was updated or newly added, false if no change occurred
     */
    public boolean updateEntry(NetworkNode destination, NetworkNode nextHop, double cost, boolean reachable) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination node cannot be null.");
        }
        DistanceVectorEntry newEntry = new DistanceVectorEntry(sourceNode, destination, nextHop, cost, reachable);
        DistanceVectorEntry oldEntry = entries.get(destination.getNodeId());

        if (oldEntry == null || !oldEntry.equals(newEntry)) {
            entries.put(destination.getNodeId(), newEntry);
            return true;
        }
        return false;
    }

    /**
     * Returns an unmodifiable collection of all entries in this routing table.
     *
     * @return collection of DistanceVectorEntry objects
     */
    public Collection<DistanceVectorEntry> getEntries() {
        return Collections.unmodifiableCollection(entries.values());
    }

    /**
     * Formats the routing table into a readable string representation.
     *
     * @return formatted table string
     */
    public String getFormattedTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("------------------------------------------------------------%n"));
        sb.append(String.format("DISTANCE VECTOR TABLE - %s (%s)%n", sourceNode.getNodeName().toUpperCase(), sourceNode.getNodeId()));
        sb.append(String.format("------------------------------------------------------------%n"));
        sb.append(String.format("%-25s | %-20s | %-6s%n", "Destination", "Next Hop", "Cost"));
        sb.append(String.format("------------------------------------------------------------%n"));

        List<DistanceVectorEntry> sortedList = new ArrayList<>(entries.values());
        sortedList.sort(Comparator.comparing(entry -> entry.getDestinationNode().getNodeId()));

        for (DistanceVectorEntry entry : sortedList) {
            sb.append(entry.toString()).append(String.format("%n"));
        }
        sb.append(String.format("------------------------------------------------------------"));
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFormattedTable();
    }
}
