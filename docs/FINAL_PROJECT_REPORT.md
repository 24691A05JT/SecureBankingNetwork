# SECURE AND RELIABLE BANKING NETWORK USING ROUTING AND ENCRYPTION
## Comprehensive Final Engineering Project Report

**Department:** Computer Science and Engineering  
**Institution:** Madanapalle Institute of Technology & Science  
**Academic Year:** 2026  

---

## 1. Title
**Secure and Reliable Banking Network Using Routing and Encryption**

---

## 2. Abstract
Modern banking networks connect branch offices, ATMs, customer terminals, management workstations, core routers, and database servers across distributed IP topologies. Ensuring data confidentiality, transmission reliability, path optimization, and atomic persistence requires an integrated approach. This project presents a Java-based software platform that integrates Link-State Dijkstra and Distance Vector routing, AES-256-GCM authenticated encryption, CRC-16-CCITT error detection, Sliding Window and Go-Back-N ARQ flow control, real TCP socket communication, SQLite persistence, and an interactive JavaFX monitoring dashboard. All 12 pipeline stages operate seamlessly, verified by 156 automated JUnit 5 tests with a 100% pass rate.

---

## 3. Introduction
Financial communication systems process critical transactions involving monetary values and private user records. Transmitting these transactions over multi-hop enterprise backbones exposes data to packet loss, bit corruption, unauthorized wiretapping, and database inconsistencies. This project presents an integrated solution demonstrating how networking, security, reliability, database management, and GUI visualization operate together in an enterprise financial application.

---

## 4. Problem Statement
Traditional network communication models treat routing, encryption, flow control, transport, and database management as isolated layers. In financial software engineering:
1. **Unencrypted Transport**: Transmitting plaintext transaction payloads allows eavesdroppers to capture account numbers and balances.
2. **Transmission Corruption**: Physical link noise corrupts bytes during transit.
3. **Unreliable Packet Delivery**: Frame drops or out-of-order delivery interrupt multi-step financial requests.
4. **Data Inconsistency**: Network drops midway through a transfer can result in partial account updates or lost funds.

---

## 5. Objectives
1. Design a 10-node simulated enterprise banking network topology with redundant backbones.
2. Implement IP addressing and Classless Inter-Domain Routing (CIDR) subnetting.
3. Implement Link-State Dijkstra Shortest Path routing.
4. Implement Bellman-Ford Distance Vector routing.
5. Secure transactions using AES-256-GCM authenticated encryption.
6. Detect transmission errors using CRC-16-CCITT polynomial checksums.
7. Manage flow control using Sliding Window protocol.
8. Enforce reliable delivery using Go-Back-N ARQ protocol.
9. Implement real TCP socket transport with length-prefixed UTF-8 framing.
10. Persist transactions atomically using SQLite JDBC with ACID guarantees.
11. Build a desktop monitoring dashboard using JavaFX.
12. Validate TCP transport and payload confidentiality using Wireshark.

---

## 6. Existing System
Existing academic software systems often implement individual networking concepts in isolation:
- Routing simulators evaluate shortest paths without encrypting or sending real data.
- Security assignments test AES encryption on static text strings without transport framing or database persistence.
- Database applications handle SQL queries without simulating network loss or socket communication.

---

## 7. Proposed System
The proposed system unifies all these layers into a single application. A banking transfer request is created, routed over a network graph, encrypted with AES-256-GCM, protected with a CRC-16 checksum, framed using Sliding Window and Go-Back-N ARQ, transmitted over a real TCP socket, verified, decrypted, executed atomically, and persisted in an SQLite database, all visually monitored via a JavaFX GUI.

---

## 8. System Architecture
The platform follows a modular, layered architecture:
- **Presentation Layer**: JavaFX Dashboard (`DashboardApplication`).
- **Integration Layer**: Pipeline Orchestrator (`SecureBankingPipeline`).
- **Application & Service Layer**: Core business operations (`BankingService`).
- **Security & Reliability Layer**: `EncryptionService`, `CRCService`, `SlidingWindow`, `GoBackN`.
- **Network & Transport Layer**: `DijkstraRouter`, `DistanceVectorRouter`, `BankingClient`, `BankingServer`.
- **Persistence Layer**: SQLite JDBC (`DatabaseManager`, `AccountRepository`, `TransactionRepository`).

---

## 9. Network Design
The system models a 10-node topology graph featuring redundant inter-router connections:
- Core Head Office & Database Server
- HQ Core Router
- Branch Routers (Branch 1, Branch 2)
- Terminal Gateways (Branch 1, Branch 2, ATM, Customer Service, Management)

---

## 10. IP Addressing and Subnetting
Subnetting divides the IP address space logically into /24 subnets:
- Head Office: `10.0.0.0/24`
- Branch 1: `10.0.1.0/24`
- Branch 2: `10.0.2.0/24`
- ATM: `10.0.10.0/24`
- Customer Service: `10.0.20.0/24`
- Management: `10.0.30.0/24`
- Database: `10.0.40.0/24`

---

## 11. Dijkstra Routing
`DijkstraRouter` computes shortest paths across weighted graph links using non-negative link costs. For a path from `Branch 1 Gateway` (`NODE-B1`) to `Database Server` (`NODE-DB`), Dijkstra computes:
- **Path**: `Branch 1 Gateway` → `Branch 1 Router` → `HQ Core Router` → `Database Server`
- **Total Cost**: `8.0`
- **Next Hop**: `Branch 1 Router`

---

## 12. Distance Vector Routing
`DistanceVectorRouter` implements the Bellman-Ford algorithm. Nodes iteratively exchange routing vectors with neighbors until tables converge. In the sample topology, Distance Vector converges in 3 iterations and produces an identical route cost of `8.0`.

---

## 13. AES-256-GCM Encryption
`EncryptionService` uses Java Cryptography Architecture (JCA) `AES/GCM/NoPadding` with 256-bit secret keys and 12-byte random IVs per operation. AES-GCM provides both data confidentiality and cryptographic authenticity via a 128-bit authentication tag.

---

## 14. CRC-16-CCITT Error Detection
`CRCService` implements the CRC-16-CCITT polynomial (`0x1021`, initial `0xFFFF`). It generates a 16-bit checksum appended to transmitted frame payloads. Verification checks ensure bit errors are detected before decryption.

---

## 15. Sliding Window Protocol
`SlidingWindow` manages sender and receiver window boundaries (configured size = 2). It limits outstanding unacknowledged frames and slides the base forward as cumulative acknowledgements are received.

---

## 16. Go-Back-N ARQ Protocol
`GoBackN` enforces in-order frame acceptance at the receiver. Out-of-order frames are rejected, and timeout events trigger window retransmission starting from the unacknowledged frame base.

---

## 17. TCP Socket Communication
`BankingClient` and `BankingServer` manage TCP socket communication over localhost (`127.0.0.1`). Payload bytes are framed using a 4-byte big-endian integer length prefix followed by UTF-8 bytes, bounded to a maximum message size of 1 MB.

---

## 18. Banking Transaction Processing
`BankingService` manages deposit, withdrawal, and transfer operations. Account balance updates are executed inside database transactions. Transfers check account statuses and balance thresholds before modifying funds.

---

## 19. SQLite Database
`DatabaseManager` provides SQLite JDBC database connection management, directory management, schema creation, and seeding. Repositories (`AccountRepository`, `TransactionRepository`, `UserRepository`) execute SQL queries using parameterized `PreparedStatement` objects.

---

## 20. JavaFX Dashboard
`DashboardApplication` provides a JavaFX desktop user interface:
- Visual topology canvas with node icons and link lines.
- Live path highlighting for shortest routes.
- Pipeline stage verification checklist.
- Account balance summary cards.
- Streaming real-time activity log feed.

---

## 21. Secure Banking Pipeline
`SecureBankingPipeline` orchestrates the 12 execution stages:
1. Transaction Creation
2. Path Routing
3. AES-256-GCM Encryption
4. CRC Checksum Generation
5. Reliable ARQ Framing
6. TCP Socket Transmission
7. Server Receipt
8. CRC Checksum Verification
9. AES-256-GCM Decryption
10. Banking Service Transfer
11. SQLite Database Persistence
12. Integrated Pipeline Response

---

## 22. Wireshark Network Analysis
Wireshark packet analysis over localhost loopback (`127.0.0.1`) confirms:
- TCP Three-Way Handshake (`SYN`, `SYN-ACK`, `ACK`).
- Application Data Segments containing 4-byte length prefixes + Base64 ciphertext.
- Confidentiality: Plaintext account numbers and transfer values are unreadable on the wire.
- TCP Four-Way Teardown (`FIN-ACK`).

---

## 23. Testing
Automated testing is built on JUnit 5. The test suite includes 19 test classes covering IP addressing, subnetting, Dijkstra, Distance Vector, database CRUD, banking transactions, AES encryption, CRC checksums, Sliding Window, Go-Back-N ARQ, TCP sockets, and integrated pipeline operations.

---

## 24. Results
- **Automated Tests**: **156 / 156 PASS** (100% pass rate)
- **Maven Build**: **`BUILD SUCCESS`**
- **Pipeline Execution**: Successful ₹5,000.00 transfer (`ACC10001` ₹50,000 → ₹45,000; `ACC10002` ₹25,000 → ₹30,000).

---

## 25. Advantages
1. Comprehensive integration of networking, security, ARQ, socket, database, and GUI layers.
2. Authenticated encryption preventing eavesdropping and tampering.
3. Atomic transaction safety guaranteeing balance integrity.
4. Real-time visual monitoring dashboard.

---

## 26. Limitations
1. Sockets operate over localhost (`127.0.0.1`) for local demonstration.
2. Network topology size is set to 10 nodes.
3. Persistence uses SQLite rather than a distributed cloud database.

---

## 27. Future Scope
1. Upgrade raw TCP sockets to TLS 1.3 transport encryption.
2. Deploy server nodes across physically separate distributed hosts.
3. Incorporate real-time intrusion detection and payload anomaly analysis.

---

## 28. Conclusion
The **Secure Banking Network** successfully demonstrates that financial transactions can be efficiently routed, securely encrypted, reliably framed, transmitted over TCP sockets, verified, and persisted in SQLite databases, providing a complete platform for secure network application engineering.

---

## 29. References
1. Kurose, J. F., & Ross, K. W. (2021). *Computer Networking: A Top-Down Approach* (8th ed.). Pearson.
2. Stallings, W. (2017). *Cryptography and Network Security: Principles and Practice* (7th ed.). Pearson.
3. Tanenbaum, A. S., & Wetherall, D. J. (2011). *Computer Networks* (5th ed.). Prentice Hall.
4. Oracle Corporation. (2026). *Java Cryptography Architecture (JCA) Reference Guide*.
5. OpenJFX Project. (2024). *JavaFX Documentation*.

## Wireshark Network Evidence

An actual Wireshark capture was performed using the Npcap loopback adapter. TCP communication over the local loopback address 127.0.0.1 was captured and saved as wireshark/tcp.pcapng.

This capture provides packet-level evidence for the TCP communication layer of the banking network application. Other project features such as routing, AES-GCM encryption, CRC verification, reliability mechanisms, and database operations are validated separately through application tests and execution logs.

