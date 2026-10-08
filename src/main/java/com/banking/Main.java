package com.banking;

import com.banking.database.AccountRepository;
import com.banking.database.DatabaseManager;
import com.banking.model.*;
import com.banking.network.*;
import com.banking.service.BankingService;
import com.banking.service.BankingTransactionResult;
import com.banking.util.LoggerUtil;

import com.banking.security.EncryptedMessage;
import com.banking.security.EncryptionService;
import javax.crypto.SecretKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main application entry point demonstrating Day 2 IP Subnetting, Day 3 Dijkstra Routing,
 * Day 4 Distance Vector Routing, Day 5 Banking Transactions & SQLite Persistence,
 * and Day 6 AES-GCM Encryption & Decryption.
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

        DijkstraRouter dijkstraRouter = new DijkstraRouter();

        NetworkNode srcNode = topology.getNode("NODE-B1");
        NetworkNode destNode = topology.getNode("NODE-DB");

        System.out.println("\nRouting Request");
        System.out.println("---------------");
        System.out.println("Source      : " + srcNode.getNodeName() + " (" + srcNode.getNodeId() + ")");
        System.out.println("Destination : " + destNode.getNodeName() + " (" + destNode.getNodeId() + ")");

        DijkstraResult dijkstraResult = dijkstraRouter.findShortestPath(topology, srcNode, destNode);

        System.out.println("\nShortest Path Calculation Result:");
        System.out.println("---------------------------------");
        System.out.println("Path        : " + dijkstraResult.getFormattedPath());
        System.out.println("Total Cost  : " + dijkstraResult.getTotalCost());
        System.out.println("Next Hop    : " + (dijkstraResult.getNextHop() != null ? dijkstraResult.getNextHop().getNodeName() : "None"));

        System.out.println("\nRouting Table for " + srcNode.getNodeName() + " (" + srcNode.getNodeId() + "):");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("%-18s | %-24s | %-18s | %-6s | %s%n", "Source Node", "Destination Node", "Next Hop", "Cost", "Shortest Path");
        System.out.println("----------------------------------------------------------------------------------");

        List<RoutingTableEntry> routingTable = dijkstraRouter.buildRoutingTable(topology, srcNode);
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
        System.out.println("DAY 4 - DISTANCE VECTOR ROUTING");
        System.out.println("==========================================");

        LoggerUtil.info("Executing Distance Vector Bellman-Ford Iterative Routing Engine...");
        DistanceVectorRouter dvRouter = new DistanceVectorRouter();
        DistanceVectorResult dvResult = dvRouter.computeRouting(topology, srcNode, destNode);

        System.out.println("\nDistance Vector Routing");
        System.out.println("-----------------------");
        System.out.println("Source      : " + srcNode.getNodeName() + " (" + srcNode.getNodeId() + ")");
        System.out.println("Destination : " + destNode.getNodeName() + " (" + destNode.getNodeId() + ")");
        System.out.println("Convergence : " + (dvResult.isConverged() ? "SUCCESS" : "FAILED"));
        System.out.println("Iterations  : " + dvResult.getIterations());

        System.out.println("\nShortest Path Calculation Result:");
        System.out.println("---------------------------------");
        System.out.println("Path        : " + dvResult.getFormattedPath());
        System.out.println("Total Cost  : " + dvResult.getTotalCost());
        System.out.println("Next Hop    : " + (dvResult.getNextHop() != null ? dvResult.getNextHop().getNodeName() : "None"));

        System.out.println("\n" + dvResult.getAllTables().get(srcNode.getNodeId()).getFormattedTable());

        System.out.println("\n" + RouteComparison.formatComparison(dijkstraResult, dvResult));

        System.out.println("\n==========================================");
        System.out.println("DAY 5 - BANKING TRANSACTION SYSTEM");
        System.out.println("==========================================");

        LoggerUtil.info("Connecting to SQLite Database & Initializing Schema...");
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeSchema();
        dbManager.seedSampleData();

        System.out.println("\nDatabase:");
        System.out.println("SQLite (" + dbManager.getDbUrl() + ")");
        System.out.println("\nDatabase Status:");
        System.out.println("CONNECTED");

        AccountRepository accountRepo = new AccountRepository(dbManager);
        BankingService bankingService = new BankingService(dbManager);

        Optional<Account> acc1BeforeOpt = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> acc2BeforeOpt = accountRepo.findByAccountNumber("ACC10002");

        System.out.println("\nDemo Accounts (Before Transfer):");
        System.out.println("-------------------------------");
        acc1BeforeOpt.ifPresent(acc -> System.out.printf("%s : ₹%.2f%n", acc.getAccountNumber(), acc.getBalance()));
        acc2BeforeOpt.ifPresent(acc -> System.out.printf("%s : ₹%.2f%n", acc.getAccountNumber(), acc.getBalance()));

        String srcAcc = "ACC10001";
        String destAcc = "ACC10002";
        double transferAmount = 5000.0;

        System.out.println("\nTransaction Request");
        System.out.println("-------------------");
        System.out.println("Source      : " + srcAcc);
        System.out.println("Destination : " + destAcc);
        System.out.printf("Amount      : ₹%.2f%n", transferAmount);

        BankingTransactionResult txResult = bankingService.transfer(srcAcc, destAcc, transferAmount);

        System.out.println("\nTransaction Result");
        System.out.println("------------------");
        System.out.println("Transaction ID : " + txResult.getTransactionId());
        System.out.println("Status         : " + (txResult.isSuccess() ? "SUCCESS" : "FAILED"));
        System.out.println("Message        : " + txResult.getMessage());

        Optional<Account> acc1AfterOpt = accountRepo.findByAccountNumber("ACC10001");
        Optional<Account> acc2AfterOpt = accountRepo.findByAccountNumber("ACC10002");

        System.out.println("\nUpdated Balances (After Transfer)");
        System.out.println("---------------------------------");
        acc1AfterOpt.ifPresent(acc -> System.out.printf("%s : ₹%.2f%n", acc.getAccountNumber(), acc.getBalance()));
        acc2AfterOpt.ifPresent(acc -> System.out.printf("%s : ₹%.2f%n", acc.getAccountNumber(), acc.getBalance()));

        System.out.println("\nTransaction stored successfully in SQLite database.");

        System.out.println("\n==========================================");
        System.out.println("DAY 6 - ENCRYPTION & DECRYPTION");
        System.out.println("==========================================");

        LoggerUtil.info("Generating 256-bit AES Cryptographic Key...");
        SecretKey secretKey = EncryptionService.generateKey();

        Transaction txn = new Transaction("ACC10001", "ACC10002", 5000.00, TransactionType.TRANSFER, "Day 6 Secure Wire Transfer");
        txn.setStatus(TransactionStatus.SUCCESS);

        String originalMessage = EncryptionService.serializeTransaction(txn);

        System.out.println("\nOriginal Transaction Message:");
        System.out.println("-----------------------------");
        System.out.println(originalMessage);

        EncryptedMessage encryptedMessage = EncryptionService.encryptTransaction(txn, secretKey);

        System.out.println("\nEncrypted Transaction:");
        System.out.println("----------------------");
        System.out.println(encryptedMessage.getEncryptedData());

        System.out.println("\nInitialization Vector (IV):");
        System.out.println("---------------------------");
        System.out.println(encryptedMessage.getInitializationVector());

        Transaction decryptedTxn = EncryptionService.decryptTransaction(encryptedMessage, secretKey);
        String decryptedMessage = EncryptionService.serializeTransaction(decryptedTxn);

        System.out.println("\nDecrypted Transaction Message:");
        System.out.println("------------------------------");
        System.out.println(decryptedMessage);

        boolean verified = originalMessage.equals(decryptedMessage);
        if (verified) {
            LoggerUtil.info("Encryption round-trip verification passed.");
        }

        System.out.println("\nVerification:");
        System.out.println("-------------");
        System.out.println("Original == Decrypted : " + verified);

        System.out.println("\n==========================================");
        LoggerUtil.info("Day 6 Encryption & Decryption demonstration completed successfully.");
    }
}

