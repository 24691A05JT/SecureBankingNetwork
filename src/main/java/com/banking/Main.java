package com.banking;

import com.banking.database.AccountRepository;
import com.banking.database.DatabaseManager;
import com.banking.model.*;
import com.banking.network.*;
import com.banking.service.BankingService;
import com.banking.service.BankingTransactionResult;
import com.banking.util.LoggerUtil;

import com.banking.reliability.CRCResult;
import com.banking.reliability.CRCService;
import com.banking.reliability.Frame;
import com.banking.reliability.GoBackN;
import com.banking.reliability.SlidingWindow;
import com.banking.security.EncryptedMessage;
import com.banking.security.EncryptionService;
import javax.crypto.SecretKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main application entry point demonstrating Day 2 IP Subnetting, Day 3 Dijkstra Routing,
 * Day 4 Distance Vector Routing, Day 5 Banking Transactions & SQLite Persistence,
 * Day 6 AES-GCM Encryption & Decryption, Day 7 CRC Error Detection, Day 8 Sliding Window Protocol,
 * and Day 9 Go-Back-N ARQ Protocol.
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

        System.out.println("\n==========================================");
        System.out.println("DAY 7 - CRC ERROR DETECTION");
        System.out.println("==========================================");

        LoggerUtil.info("Calculating CRC-16-CCITT for Banking Transaction Message...");
        String originalTxnPayload = EncryptionService.serializeTransaction(txn);

        System.out.println("\nOriginal Banking Message:");
        System.out.println("-------------------------");
        System.out.println(originalTxnPayload);

        CRCResult crcResult = CRCService.process(originalTxnPayload);

        System.out.println("\nCRC-16-CCITT Checksum:");
        System.out.println("----------------------");
        System.out.println(crcResult.getCrcFormatted());

        boolean origVerify = CRCService.verifyCRC(originalTxnPayload, crcResult.getCrc());

        System.out.println("\nOriginal Verification:");
        System.out.println("----------------------");
        System.out.println(origVerify ? "PASSED" : "FAILED");

        System.out.println("\nSimulating Transmission Line Corruption...");
        String corruptedTxnPayload = originalTxnPayload.replace("AMOUNT=5000.00", "AMOUNT=9000.00");

        System.out.println("\nCorrupted Banking Message:");
        System.out.println("--------------------------");
        System.out.println(corruptedTxnPayload);

        boolean corruptVerify = CRCService.verifyCRC(corruptedTxnPayload, crcResult.getCrc());

        System.out.println("\nCorrupted Verification:");
        System.out.println("-----------------------");
        System.out.println(corruptVerify ? "PASSED" : "FAILED");

        System.out.println("\nTransmission Error:");
        System.out.println("-------------------");
        System.out.println(!corruptVerify ? "DETECTED" : "UNDETECTED");

        System.out.println("\n==========================================");
        LoggerUtil.info("Day 7 CRC Error Detection demonstration completed successfully.");

        System.out.println("\n==========================================");
        System.out.println("DAY 8 - SLIDING WINDOW PROTOCOL");
        System.out.println("==========================================");

        LoggerUtil.info("Initializing Sliding Window Protocol (Total Frames: 6, Window Size: 3)...");
        SlidingWindow slidingWindow = new SlidingWindow(6, 3);

        String[] payloads = {
            "HEADER: TXN-SEC-8821",
            "SOURCE=ACC10001",
            "DESTINATION=ACC10002",
            "AMOUNT=5000.00",
            "TYPE=TRANSFER",
            "CHECKSUM=0xA953"
        };

        System.out.println("\nInitial Sliding Window State:");
        System.out.println("-----------------------------");
        System.out.println(slidingWindow.getFormattedWindowState());

        System.out.println("\nTransmitting Initial Window Frames (0..2):");
        System.out.println("----------------------------------------");
        for (int i = 0; i < 3; i++) {
            slidingWindow.sendNextFrame(payloads[i]);
        }
        System.out.println(slidingWindow.getFormattedWindowState());

        System.out.println("\nProcessing ACK for Frame #0:");
        System.out.println("---------------------------");
        slidingWindow.receiveAcknowledgement(0);
        System.out.println(slidingWindow.getFormattedWindowState());

        System.out.println("\nSending Frame #3 into Available Window Slot:");
        System.out.println("------------------------------------------");
        slidingWindow.sendNextFrame(payloads[3]);
        System.out.println(slidingWindow.getFormattedWindowState());

        System.out.println("\nSimulating Loss of Frame #1:");
        System.out.println("----------------------------");
        slidingWindow.simulateFrameLoss(1);
        System.out.println(slidingWindow.getFormattedWindowState());

        System.out.println("\nProcessing ACK for Frame #2 (Out-of-order ACK before Frame #1):");
        System.out.println("---------------------------------------------------------------");
        slidingWindow.receiveAcknowledgement(2);
        System.out.println(slidingWindow.getFormattedWindowState());
        System.out.println("Base position (Stuck at #1 due to lost frame): " + slidingWindow.getBase());

        System.out.println("\nRetransmitting and ACKing Lost Frame #1 & Remaining Frames:");
        System.out.println("---------------------------------------------------------");
        slidingWindow.receiveAcknowledgement(1); // ACK Frame #1 -> advances base past 2 to 3!
        System.out.println(slidingWindow.getFormattedWindowState());

        while (slidingWindow.canSend()) {
            int seq = slidingWindow.getNextSequenceNumber();
            slidingWindow.sendNextFrame(payloads[seq]);
        }
        System.out.println(slidingWindow.getFormattedWindowState());

        for (int i = 3; i < 6; i++) {
            slidingWindow.receiveAcknowledgement(i);
        }

        System.out.println("\nFinal Sliding Window State:");
        System.out.println("---------------------------");
        System.out.println(slidingWindow.getFormattedWindowState());
        System.out.println("Transmission Complete : " + slidingWindow.isComplete());

        System.out.println("\n==========================================");
        LoggerUtil.info("Day 8 Sliding Window Protocol demonstration completed successfully.");

        System.out.println("\n==========================================");
        System.out.println("DAY 9 - GO-BACK-N ARQ PROTOCOL");
        System.out.println("==========================================");

        LoggerUtil.info("Initializing Go-Back-N ARQ Protocol (Total Frames: 6, Window Size: 3)...");
        GoBackN gbn = new GoBackN(6, 3);

        String[] gbnPayloads = {
            "HEADER: TXN-GBN-9942",
            "SRC=ACC10001",
            "DEST=ACC10002",
            "AMOUNT=5000.00",
            "TYPE=TRANSFER",
            "CRC=0x29B1"
        };

        System.out.println("\nInitial Go-Back-N State:");
        System.out.println("------------------------");
        System.out.println(gbn.getFormattedState());

        System.out.println("\nTransmitting Frame #0 (Delivered & Accepted):");
        System.out.println("-------------------------------------------");
        gbn.sendNextFrame(gbnPayloads[0]);
        System.out.println(gbn.getFormattedState());

        System.out.println("\nSimulating Network Loss of Frame #1...");
        gbn.simulateFrameLoss(1);

        System.out.println("\nTransmitting Frame #1 (Lost) and Frame #2 (Discarded out-of-order):");
        System.out.println("----------------------------------------------------------------");
        gbn.sendNextFrame(gbnPayloads[1]); // Lost
        gbn.sendNextFrame(gbnPayloads[2]); // Sent, but discarded because receiver expects #1!

        System.out.println("\nState After Frame #1 Loss & Frame #2 Discard:");
        System.out.println("--------------------------------------------");
        System.out.println(gbn.getFormattedState());

        System.out.println("\nTimeout Detected! Triggering Go-Back-N Window Retransmission...");
        System.out.println("---------------------------------------------------------------");
        gbn.handleTimeout();
        System.out.println(gbn.getFormattedState());

        System.out.println("\nTransmitting Remaining Frames (3..5):");
        System.out.println("-------------------------------------");
        while (gbn.canSend()) {
            int seq = gbn.getNextSequenceNumber();
            gbn.sendNextFrame(gbnPayloads[seq]);
        }

        System.out.println("\nFinal Go-Back-N Protocol State:");
        System.out.println("-------------------------------");
        System.out.println(gbn.getFormattedState());
        System.out.println("Transmission Complete : " + gbn.isComplete());

        System.out.println("\n==========================================");
        LoggerUtil.info("Day 9 Go-Back-N ARQ demonstration completed successfully.");
    }
}




