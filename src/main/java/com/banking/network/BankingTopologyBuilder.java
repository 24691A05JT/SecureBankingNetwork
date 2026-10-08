package com.banking.network;

/**
 * Utility builder to construct a realistic simulated banking network topology.
 * Provides multiple path redundancies to evaluate Dijkstra's shortest path routing.
 */
public final class BankingTopologyBuilder {

    private BankingTopologyBuilder() {
        // Utility class
    }

    /**
     * Builds and returns a fully populated NetworkTopology instance with simulated banking nodes and links.
     *
     * @return initialized NetworkTopology
     */
    public static NetworkTopology createSampleTopology() {
        NetworkTopology topology = new NetworkTopology();

        // 1. Define Nodes
        NetworkNode headOfficeNode = new NetworkNode("NODE-HQ", "Head Office Core", "10.0.0.1", "10.0.0.0/24", NodeType.HEAD_OFFICE);
        NetworkNode routerHq = new NetworkNode("ROUTER-HQ", "HQ Core Router", "10.0.0.254", "10.0.0.0/24", NodeType.ROUTER);
        NetworkNode routerB1 = new NetworkNode("ROUTER-B1", "Branch 1 Router", "10.0.1.254", "10.0.1.0/24", NodeType.ROUTER);
        NetworkNode routerB2 = new NetworkNode("ROUTER-B2", "Branch 2 Router", "10.0.2.254", "10.0.2.0/24", NodeType.ROUTER);
        NetworkNode branch1Node = new NetworkNode("NODE-B1", "Branch 1 Gateway", "10.0.1.1", "10.0.1.0/24", NodeType.BRANCH);
        NetworkNode branch2Node = new NetworkNode("NODE-B2", "Branch 2 Gateway", "10.0.2.1", "10.0.2.0/24", NodeType.BRANCH);
        NetworkNode atmNode = new NetworkNode("NODE-ATM", "ATM Gateway", "10.0.10.1", "10.0.10.0/24", NodeType.ATM);
        NetworkNode customerServiceNode = new NetworkNode("NODE-CS", "Customer Service Terminal", "10.0.20.1", "10.0.20.0/24", NodeType.CUSTOMER_SERVICE);
        NetworkNode managementNode = new NetworkNode("NODE-MGMT", "Management Workstation", "10.0.30.1", "10.0.30.0/24", NodeType.MANAGER);
        NetworkNode databaseNode = new NetworkNode("NODE-DB", "Database Server", "10.0.40.1", "10.0.40.0/24", NodeType.SERVER);

        // 2. Add Nodes to Topology
        topology.addNode(headOfficeNode);
        topology.addNode(routerHq);
        topology.addNode(routerB1);
        topology.addNode(routerB2);
        topology.addNode(branch1Node);
        topology.addNode(branch2Node);
        topology.addNode(atmNode);
        topology.addNode(customerServiceNode);
        topology.addNode(managementNode);
        topology.addNode(databaseNode);

        // 3. Connect Terminal Nodes to their Gateway Routers
        topology.addBidirectionalLink(branch1Node, routerB1, 2.0);
        topology.addBidirectionalLink(branch2Node, routerB2, 2.0);
        topology.addBidirectionalLink(atmNode, routerB1, 3.0);
        topology.addBidirectionalLink(customerServiceNode, routerB2, 4.0);
        topology.addBidirectionalLink(managementNode, routerHq, 1.0);
        topology.addBidirectionalLink(databaseNode, routerHq, 2.0);
        topology.addBidirectionalLink(headOfficeNode, routerHq, 1.0);

        // 4. Inter-Router Backbone Connections (with multiple path costs)
        // Path A: Branch 1 -> Router-B1 -> Router-HQ -> Database (Cost: 2 + 4 + 2 = 8)
        topology.addBidirectionalLink(routerB1, routerHq, 4.0);

        // Path B: Branch 1 -> Router-B1 -> Router-B2 -> Router-HQ -> Database (Cost: 2 + 10 + 4 + 2 = 18)
        topology.addBidirectionalLink(routerB1, routerB2, 10.0);

        // Branch 2 -> Router-HQ Link
        topology.addBidirectionalLink(routerB2, routerHq, 4.0);

        return topology;
    }
}
