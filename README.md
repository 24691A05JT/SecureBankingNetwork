# Secure and Reliable Banking Network Using Routing and Encryption

A professional Java-based software system demonstrating a secure, reliable end-to-end banking transaction communication pipeline over simulated network topologies with custom routing algorithms, AES-256-GCM encryption, CRC-16 error detection, Sliding Window & Go-Back-N ARQ reliability protocols, real TCP socket transport, SQLite persistence, and an interactive JavaFX monitoring dashboard.

---

## 1. Project Overview

The **Secure Banking Network** integrates fundamental principles from **Computer Networking**, **Applied Cryptography**, **Software Reliability**, **Database Systems**, and **GUI Design** into a unified enterprise desktop platform.

It demonstrates how banking transactions (e.g., transfers between accounts) safely travel across IP-subnetted bank branches, inter-router backbones, and database servers through a 12-stage secure pipeline.

---

## 2. Problem Statement

Financial networks operating over public or multi-hop enterprise networks face critical security and reliability challenges:
- **Eavesdropping & Data Tampering**: Wiretapping plaintext transactions allows attackers to read or alter account numbers and transfer amounts.
- **Transmission Bit Errors**: Noise on physical/optical links can corrupt transmitted bytes.
- **Packet Loss & Out-of-Order Delivery**: Unreliable network links can lose or reorder frames.
- **Database Inconsistency**: Unhandled network failures can cause partial account balance updates or double-spending.

This project resolves these vulnerabilities by layering AES-256-GCM encryption, CRC-16-CCITT error detection, Sliding Window / Go-Back-N ARQ flow control, length-prefixed TCP socket framing, and SQLite ACID transaction guarantees.

---

## 3. Objectives

- **Custom Link-State & Distance Vector Routing**: Implement Dijkstra's Shortest Path and Bellman-Ford Distance Vector algorithms without third-party graph libraries.
- **Authenticated Cryptographic Security**: Implement AES-256-GCM encryption with 12-byte random IVs and 128-bit authentication tags.
- **Error Detection & Reliable Flow Control**: Implement CRC-16-CCITT polynomial checksums alongside Sliding Window and Go-Back-N ARQ protocols.
- **Real Socket Transport**: Provide 4-byte length-prefixed UTF-8 TCP framing using Java `Socket` and `ServerSocket`.
- **Atomic Database Persistence**: Ensure thread-safe ACID transaction handling using SQLite JDBC.
- **Real-Time Visual Monitoring**: Build a responsive JavaFX dashboard for topology visualization, shortest path highlighting, and pipeline verification.

---

## 4. Key Features

- **Link-State Routing**: Dijkstra's algorithm computes optimal paths across network nodes.
- **Distance Vector Routing**: Iterative Bellman-Ford convergence table generation.
- **AES-256-GCM Cryptography**: Authenticated encryption preventing ciphertext tampering and eavesdropping.
- **CRC-16-CCITT Error Checking**: Fast 16-bit polynomial error detection (`0x1021`, initial `0xFFFF`).
- **Sliding Window Protocol**: Sender/receiver window management with cumulative ACKs.
- **Go-Back-N ARQ**: In-order frame enforcement, out-of-order rejection, and automatic retransmission.
- **Real TCP Communication**: Client-server length-prefixed framing operating over localhost sockets.
- **SQLite Database Persistence**: Repositories managing atomic account deposits, withdrawals, and transfers.
- **End-to-End Pipeline (`SecureBankingPipeline`)**: 12-stage transaction execution engine.
- **JavaFX Dashboard**: Visual network topology, live route highlighting, security cards, and activity log feed.

---

## 5. System Architecture & Topology

```
SecureBankingNetwork/
├── pom.xml
├── README.md
├── docs/
│   ├── WIRESHARK_ANALYSIS.md
│   ├── WIRESHARK_EVIDENCE.md
│   ├── DAY13_TEST_REPORT.md
│   └── NETWORK_VALIDATION.md
├── src/
│   ├── main/java/com/banking/
│   │   ├── Main.java
│   │   ├── communication/   (BankingClient, BankingServer)
│   │   ├── dashboard/       (DashboardApplication)
│   │   ├── database/        (DatabaseManager, AccountRepo, UserRepo, TxnRepo)
│   │   ├── integration/     (SecureBankingPipeline, Request, Response, Result, Options)
│   │   ├── model/           (Account, User, Transaction, NetworkNode)
│   │   ├── network/         (IPAddress, Subnet, Topology, DijkstraRouter, DistanceVectorRouter)
│   │   ├── reliability/     (CRCService, CRCResult, Frame, SlidingWindow, GoBackN)
│   │   ├── security/        (EncryptionService, EncryptedMessage)
│   │   ├── service/         (BankingService, BankingTransactionResult)
│   │   └── util/            (LoggerUtil)
│   └── test/java/com/banking/ (156 Automated JUnit 5 Unit & Integration Tests)
```

### Simulated Banking Topology Nodes (10 Nodes)

1. **`NODE-HQ`**: Head Office Core (`10.0.0.1/24`)
2. **`ROUTER-HQ`**: HQ Core Router (`10.0.0.254/24`)
3. **`ROUTER-B1`**: Branch 1 Router (`10.0.1.254/24`)
4. **`ROUTER-B2`**: Branch 2 Router (`10.0.2.254/24`)
5. **`NODE-B1`**: Branch 1 Gateway (`10.0.1.1/24`)
6. **`NODE-B2`**: Branch 2 Gateway (`10.0.2.1/24`)
7. **`NODE-ATM`**: ATM Gateway (`10.0.10.1/24`)
8. **`NODE-CS`**: Customer Service Terminal (`10.0.20.1/24`)
9. **`NODE-MGMT`**: Management Workstation (`10.0.30.1/24`)
10. **`NODE-DB`**: Database Server (`10.0.40.1/24`)

---

## 6. Development Roadmap (Day 1 – Day 14)

- **Day 1**: Project initialization, Maven structure, dependency configuration.
- **Day 2**: IP Addressing & CIDR Subnetting (`IPAddress`, `Subnet`, `BankingNetworkConfig`).
- **Day 3**: Link-State Routing (`NetworkTopology`, `NetworkLink`, `DijkstraRouter`).
- **Day 4**: Distance Vector Routing (`DistanceVectorRouter`, Bellman-Ford convergence).
- **Day 5**: Banking Service & SQLite Persistence (`DatabaseManager`, `BankingService`).
- **Day 6**: AES-256-GCM Encryption & Decryption (`EncryptionService`, `EncryptedMessage`).
- **Day 7**: CRC-16-CCITT Error Detection Checksum (`CRCService`, `CRCResult`).
- **Day 8**: Sliding Window Protocol (`SlidingWindow`, `Frame`, `Acknowledgement`).
- **Day 9**: Go-Back-N ARQ Reliability Protocol (`GoBackN`).
- **Day 10**: Real TCP Client/Server Socket Communication (`BankingClient`, `BankingServer`).
- **Day 11**: Full Secure Banking Pipeline Integration (`SecureBankingPipeline`).
- **Day 12**: Professional JavaFX Desktop Dashboard & Visual Topology Map (`DashboardApplication`).
- **Day 13**: Wireshark Packet Analysis Guides & Comprehensive Testing (`docs/`).
- **Day 14**: Final Engineering Review, Code Quality Audit, and System Optimization.

---

## 7. Integrated Pipeline Flow (12 Stages)

When a transaction request is executed:

1. **Stage 1 — Transaction Creation**: Creates and validates `Transaction` (`ACC10001` -> `ACC10002`, ₹5,000.00).
2. **Stage 2 — Routing**: Computes shortest route (`Branch 1 Gateway` -> `Branch 1 Router` -> `HQ Core Router` -> `Database Server`, Cost: 8.0) using Dijkstra and Distance Vector.
3. **Stage 3 — Encryption**: Encrypts transaction payload with AES-256-GCM using a unique 12-byte IV.
4. **Stage 4 — CRC Generation**: Generates CRC-16-CCITT checksum for data payload integrity.
5. **Stage 5 — Reliable Frame Transmission**: Models frame transmission, sequence numbers, and ACKs using Sliding Window and Go-Back-N.
6. **Stage 6 — TCP Transmission**: Transmits 4-byte length-prefixed payload over TCP socket (`127.0.0.1`).
7. **Stage 7 — Server Receipt**: Server receives and acknowledges TCP message payload.
8. **Stage 8 — CRC Verification**: Verifies CRC-16 checksum; aborts if transmission line corruption is detected.
9. **Stage 9 — Decryption**: Decrypts payload using AES-256-GCM; aborts if tag mismatch or tampered.
10. **Stage 10 — Banking Transaction**: Executes atomic transfer in `BankingService`.
11. **Stage 11 — Database Persistence**: Persists transaction and updates account balances in SQLite.
12. **Stage 12 — Response**: Returns `PipelineResult` with complete metrics and updated account balances.

---

## 8. Wireshark Network Packet Analysis

The application outputs diagnostic target information on startup:
```text
[INFO] TCP_CAPTURE_TARGET=127.0.0.1:<PORT>
```

### Display Filter
```text
tcp.port == <PORT>
```

### Verified Observations
- **TCP Three-Way Handshake**: `SYN` -> `SYN-ACK` -> `ACK`.
- **Application Data**: 4-byte big-endian integer length prefix + UTF-8 payload.
- **Confidentiality**: Hex bytes contain Base64 ciphertext (`MgdsFMCWiS...`). Account numbers and amounts do **NOT** appear as plain human-readable text on the wire.
- **TCP Connection Teardown**: Graceful 4-way release (`FIN-ACK`).

---

## 9. How to Build & Run

### Prerequisites
- Java JDK 26 (or JDK 21+)
- Apache Maven 3.9+

### Build & Run Unit Tests
```powershell
mvn clean compile test package
```

### Run Main Command-Line Demonstration
```powershell
mvn exec:java "-Dexec.mainClass=com.banking.Main"
```

### Launch JavaFX Desktop Dashboard
```powershell
mvn javafx:run
```

---

## 10. Automated Test Suite Metrics

```text
Results:

Tests run: 156, Failures: 0, Errors: 0, Skipped: 0
Build Status: BUILD SUCCESS (100.0% Pass Rate)
```

| Module / Package | Test Class Name | Test Count | Status |
| :--- | :--- | :---: | :---: |
| Network | `IPAddressTest` | 11 | PASSED |
| Network | `SubnetTest` | 14 | PASSED |
| Network | `BankingNetworkConfigTest` | 10 | PASSED |
| Network | `NetworkNodeTest` | 8 | PASSED |
| Network | `NetworkLinkTest` | 8 | PASSED |
| Network | `NetworkTopologyTest` | 10 | PASSED |
| Network Routing | `DijkstraRouterTest` | 11 | PASSED |
| Network Routing | `DistanceVectorRouterTest` | 11 | PASSED |
| Database | `DatabaseManagerTest` | 2 | PASSED |
| Database | `UserRepositoryTest` | 4 | PASSED |
| Database | `AccountRepositoryTest` | 5 | PASSED |
| Database | `TransactionRepositoryTest` | 4 | PASSED |
| Banking Service | `BankingServiceTest` | 8 | PASSED |
| Security | `EncryptionServiceTest` | 14 | PASSED |
| Reliability | `CRCServiceTest` | 10 | PASSED |
| Flow Control | `SlidingWindowTest` | 15 | PASSED |
| ARQ Protocol | `GoBackNTest` | 11 | PASSED |
| TCP Sockets | `BankingClientServerTest` | 15 | PASSED |
| Pipeline Integration | `SecureBankingPipelineTest` | 15 | PASSED |
| **TOTAL** | **19 Test Suites** | **156** | **100% PASS** |

---

## 11. Security & Verification Assertions

- **AES-256-GCM Integrity**: Tampered ciphertext or invalid keys immediately trigger authentication tag mismatch failures, stopping transaction execution.
- **CRC-16 Error Safeguard**: Corrupted data bytes fail CRC verification before decryption or database execution.
- **Atomic Balance Safety**: Transfer operations utilize database transactions (`commit()` / `rollback()`). Invalid amounts or insufficient funds leave account balances completely untouched.
- **Zero Key Leakage**: AES secret keys are never printed, stored in logs, or exposed over network sockets.
