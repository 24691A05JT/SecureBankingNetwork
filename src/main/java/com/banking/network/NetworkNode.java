package com.banking.network;

import java.util.Objects;

/**
 * Represents a node in the banking network topology (e.g. Router, ATM, Branch Gateway, Server).
 * Prepared for routing algorithms in subsequent phases.
 */
public class NetworkNode {

    private final String nodeId;
    private final String nodeName;
    private final IPAddress ipAddress;
    private final Subnet subnet;
    private final NodeType nodeType;

    public NetworkNode(String nodeId, String nodeName, IPAddress ipAddress, Subnet subnet, NodeType nodeType) {
        this.nodeId = Objects.requireNonNull(nodeId, "Node ID cannot be null");
        this.nodeName = Objects.requireNonNull(nodeName, "Node name cannot be null");
        this.ipAddress = Objects.requireNonNull(ipAddress, "IP address cannot be null");
        this.subnet = Objects.requireNonNull(subnet, "Subnet cannot be null");
        this.nodeType = Objects.requireNonNull(nodeType, "Node type cannot be null");

        if (!subnet.contains(ipAddress)) {
            throw new IllegalArgumentException("IP address " + ipAddress + " does not belong to subnet " + subnet.getCidrNotation());
        }
    }

    public NetworkNode(String nodeId, String nodeName, String ipStr, String cidrStr, NodeType nodeType) {
        this(nodeId, nodeName, new IPAddress(ipStr), new Subnet(cidrStr), nodeType);
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public Subnet getSubnet() {
        return subnet;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkNode node = (NetworkNode) o;
        return Objects.equals(nodeId, node.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return String.format("NetworkNode[id='%s', name='%s', ip=%s, subnet=%s, type=%s]",
                nodeId, nodeName, ipAddress.getDottedDecimal(), subnet.getCidrNotation(), nodeType);
    }
}
