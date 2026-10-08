package com.banking.network;

import java.util.*;

/**
 * Graph-based representation of the banking network topology.
 * Manages nodes, directional/bidirectional links, and neighbor relationships.
 */
public class NetworkTopology {

    private final Map<String, NetworkNode> nodes = new LinkedHashMap<>();
    private final Map<String, List<NetworkLink>> outgoingLinks = new LinkedHashMap<>();

    /**
     * Adds a network node to the topology.
     *
     * @param node the NetworkNode to add
     * @throws IllegalArgumentException if node is null or if a node with the same ID already exists
     */
    public void addNode(NetworkNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Network node cannot be null.");
        }
        if (nodes.containsKey(node.getNodeId())) {
            throw new IllegalArgumentException("Node with ID '" + node.getNodeId() + "' already exists in topology.");
        }
        nodes.put(node.getNodeId(), node);
        outgoingLinks.putIfAbsent(node.getNodeId(), new ArrayList<>());
    }

    /**
     * Removes a node and all associated links from the topology.
     *
     * @param nodeId ID of the node to remove
     * @return true if removed, false if node did not exist
     */
    public boolean removeNode(String nodeId) {
        if (nodeId == null || !nodes.containsKey(nodeId)) {
            return false;
        }
        nodes.remove(nodeId);
        outgoingLinks.remove(nodeId);

        // Remove links pointing to this node from other nodes
        for (List<NetworkLink> links : outgoingLinks.values()) {
            links.removeIf(link -> link.getDestination().getNodeId().equals(nodeId));
        }
        return true;
    }

    /**
     * Adds a directional link to the topology.
     *
     * @param link the NetworkLink to add
     * @throws IllegalArgumentException if link is null or if source/destination nodes do not exist in topology
     */
    public void addLink(NetworkLink link) {
        if (link == null) {
            throw new IllegalArgumentException("Network link cannot be null.");
        }
        String srcId = link.getSource().getNodeId();
        String destId = link.getDestination().getNodeId();

        if (!nodes.containsKey(srcId)) {
            throw new IllegalArgumentException("Cannot add link: Source node '" + srcId + "' does not exist in topology.");
        }
        if (!nodes.containsKey(destId)) {
            throw new IllegalArgumentException("Cannot add link: Destination node '" + destId + "' does not exist in topology.");
        }

        outgoingLinks.get(srcId).add(link);
    }

    /**
     * Helper method to add a bidirectional link between two nodes with a given cost.
     *
     * @param node1 first node
     * @param node2 second node
     * @param cost routing cost (> 0)
     */
    public void addBidirectionalLink(NetworkNode node1, NetworkNode node2, double cost) {
        if (node1 == null || node2 == null) {
            throw new IllegalArgumentException("Nodes cannot be null when adding bidirectional link.");
        }
        if (!containsNode(node1.getNodeId())) {
            addNode(node1);
        }
        if (!containsNode(node2.getNodeId())) {
            addNode(node2);
        }
        addLink(new NetworkLink(node1, node2, cost));
        addLink(new NetworkLink(node2, node1, cost));
    }

    /**
     * Removes a specific link from the topology.
     *
     * @param link the link to remove
     * @return true if removed, false otherwise
     */
    public boolean removeLink(NetworkLink link) {
        if (link == null || !nodes.containsKey(link.getSource().getNodeId())) {
            return false;
        }
        return outgoingLinks.get(link.getSource().getNodeId()).remove(link);
    }

    /**
     * Gets a node by its unique ID.
     *
     * @param nodeId node ID
     * @return NetworkNode or null if not found
     */
    public NetworkNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Checks if a node exists in the topology.
     *
     * @param nodeId node ID
     * @return true if present
     */
    public boolean containsNode(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    /**
     * Returns an unmodifiable collection of all nodes in the topology.
     *
     * @return collection of NetworkNode objects
     */
    public List<NetworkNode> getNodes() {
        return Collections.unmodifiableList(new ArrayList<>(nodes.values()));
    }

    /**
     * Returns outgoing links from a specific node.
     *
     * @param node source node
     * @return list of outgoing NetworkLink objects
     */
    public List<NetworkLink> getOutgoingLinks(NetworkNode node) {
        if (node == null || !nodes.containsKey(node.getNodeId())) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(outgoingLinks.getOrDefault(node.getNodeId(), Collections.emptyList()));
    }

    /**
     * Returns outgoing links from a node by ID.
     *
     * @param nodeId source node ID
     * @return list of outgoing NetworkLink objects
     */
    public List<NetworkLink> getOutgoingLinks(String nodeId) {
        if (nodeId == null || !nodes.containsKey(nodeId)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(outgoingLinks.getOrDefault(nodeId, Collections.emptyList()));
    }

    /**
     * Returns a list of immediate neighboring nodes reachable from the specified node.
     *
     * @param node source node
     * @return list of neighboring NetworkNode objects
     */
    public List<NetworkNode> getNeighbours(NetworkNode node) {
        if (node == null) return Collections.emptyList();
        return getNeighbours(node.getNodeId());
    }

    /**
     * Returns a list of immediate neighboring nodes reachable from the specified node ID.
     *
     * @param nodeId source node ID
     * @return list of neighboring NetworkNode objects
     */
    public List<NetworkNode> getNeighbours(String nodeId) {
        List<NetworkLink> links = getOutgoingLinks(nodeId);
        List<NetworkNode> neighbours = new ArrayList<>();
        for (NetworkLink link : links) {
            neighbours.add(link.getDestination());
        }
        return neighbours;
    }

    /**
     * Returns all links in the topology across all nodes.
     *
     * @return list of all NetworkLink objects
     */
    public List<NetworkLink> getLinks() {
        List<NetworkLink> allLinks = new ArrayList<>();
        for (List<NetworkLink> list : outgoingLinks.values()) {
            allLinks.addAll(list);
        }
        return Collections.unmodifiableList(allLinks);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getLinkCount() {
        int count = 0;
        for (List<NetworkLink> list : outgoingLinks.values()) {
            count += list.size();
        }
        return count;
    }

    public void clear() {
        nodes.clear();
        outgoingLinks.clear();
    }
}
