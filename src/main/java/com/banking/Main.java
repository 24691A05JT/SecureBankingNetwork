package com.banking;

import com.banking.network.*;
import com.banking.util.LoggerUtil;

import java.util.List;
import java.util.Map;

/**
 * Main application entry point demonstrating Day 2 IP Addressing and Day 3 Dijkstra Routing.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("       SECURE BANKING NETWORK             ");
        System.out.println("==========================================");
        System.out.println();
        System.out.println("DAY 2 - IP ADDRESSING & SUBNETTING");
        System.out.println();

        LoggerUtil.info("Initializing Banking Logical Subnet Topology...");
        System.out.println("\nBanking Network:");
        System.out.println("----------------");

        for (Map.Entry<String, Subnet> entry : BankingNetworkConfig.getAllSubnets().entrySet()) {
            String name = entry.getKey();
            Subnet subnet = entry.getValue();

            System.out.println("\n" + name);
            System.out.println("  CIDR:         " + subnet.getCidrNotation());
            System.out.println("  Mask:         " + subnet.getSubnetMask());
            System.out.println("  Network:      " + subnet.getNetworkAddress());
            System.out.println("  Broadcast:    " + subnet.getBroadcastAddress());
            System.out.println("  First Host:   " + subnet.getFirstUsableHost());
            System.out.println("  Last Host:    " + subnet.getLastUsableHost());
            System.out.println("  Total Addrs:  " + subnet.getTotalAddresses());
            System.out.println("  Usable Hosts: " + subnet.getUsableHostCount());
        }

        System.out.println("\n------------------------------------------");
        System.out.println("Subnet Verification:");
        System.out.println("------------------------------------------");

        IPAddress ip1 = new IPAddress("10.0.1.25");
        IPAddress ip2 = new IPAddress("10.0.1.50");
        IPAddress ip3 = new IPAddress("10.0.2.25");

        Subnet branch1Subnet = BankingNetworkConfig.BRANCH_1;

        System.out.println("\nChecking IPs in " + branch1Subnet.getCidrNotation() + " (Branch 1):");
        System.out.println("IP 1: " + ip1);
        System.out.println("IP 2: " + ip2);
        boolean sameSubnet1 = branch1Subnet.isSameSubnet(ip1, ip2);
        System.out.println("Same Subnet: " + sameSubnet1);

        System.out.println("\nChecking IPs across Branch 1 and Branch 2:");
        System.out.println("IP 1: " + ip1);
        System.out.println("IP 3: " + ip3);
        boolean sameSubnet2 = branch1Subnet.isSameSubnet(ip1, ip3);
        System.out.println("Same Subnet: " + sameSubnet2);

        System.out.println("\n==========================================");
        System.out.println("DAY 3 - DIJKSTRA SHORTEST PATH ROUTING");
        System.out.println("==========================================");

        LoggerUtil.info("Constructing Simulated Banking Network Topology Graph...");
        NetworkTopology topology = BankingTopologyBuilder.createSampleTopology();

        System.out.println("\nNetwork Topology");
        System.out.println("----------------");
        System.out.println("Total Nodes : " + topology.getNodeCount());
        System.out.println("Total Links : " + topology.getLinkCount());

        DijkstraRouter router = new DijkstraRouter();

        NetworkNode srcNode = topology.getNode("NODE-B1");
        NetworkNode destNode = topology.getNode("NODE-DB");

        System.out.println("\nRouting Request");
        System.out.println("---------------");
        System.out.println("Source      : " + srcNode.getNodeName() + " (" + srcNode.getNodeId() + ")");
        System.out.println("Destination : " + destNode.getNodeName() + " (" + destNode.getNodeId() + ")");

        DijkstraResult result = router.findShortestPath(topology, srcNode, destNode);

        System.out.println("\nShortest Path Calculation Result:");
        System.out.println("---------------------------------");
        System.out.println("Path        : " + result.getFormattedPath());
        System.out.println("Total Cost  : " + result.getTotalCost());
        System.out.println("Next Hop    : " + (result.getNextHop() != null ? result.getNextHop().getNodeName() : "None"));

        System.out.println("\nRouting Table for " + srcNode.getNodeName() + " (" + srcNode.getNodeId() + "):");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("%-18s | %-24s | %-18s | %-6s | %s%n", "Source Node", "Destination Node", "Next Hop", "Cost", "Shortest Path");
        System.out.println("----------------------------------------------------------------------------------");

        List<RoutingTableEntry> routingTable = router.buildRoutingTable(topology, srcNode);
        for (RoutingTableEntry entry : routingTable) {
            String nextHopStr = entry.getNextHop() != null ? entry.getNextHop().getNodeName() : (entry.getSourceNode().equals(entry.getDestinationNode()) ? "LOCAL" : "NONE");
            System.out.printf("%-18s | %-24s | %-18s | %-6.1f | %s%n",
                    entry.getSourceNode().getNodeName(),
                    entry.getDestinationNode().getNodeName(),
                    nextHopStr,
                    entry.getTotalCost(),
                    entry.getFormattedPath());
        }

        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("\n==========================================");
        LoggerUtil.info("Day 3 Dijkstra Shortest Path Routing demonstration completed successfully.");
    }
}
