package com.banking;

import com.banking.network.BankingNetworkConfig;
import com.banking.network.IPAddress;
import com.banking.network.Subnet;
import com.banking.util.LoggerUtil;

import java.util.Map;

/**
 * Main application entry point demonstrating Day 2 IP Addressing and Subnetting functionality.
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
        LoggerUtil.info("Day 2 IP Addressing & Subnetting demonstration finished successfully.");
    }
}
