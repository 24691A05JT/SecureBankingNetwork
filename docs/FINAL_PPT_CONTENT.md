# SECURE AND RELIABLE BANKING NETWORK USING ROUTING AND ENCRYPTION
## Final Presentation Slide Deck (18 Slides)

**Department:** Computer Science and Engineering  
**Institution:** Madanapalle Institute of Technology & Science  
**Academic Year:** 2026  

---

## SLIDE 1: TITLE SLIDE
### SECURE AND RELIABLE BANKING NETWORK USING ROUTING AND ENCRYPTION

- **Presenter:** Final Year Project Demonstration
- **Department:** Computer Science and Engineering
- **Institution:** Madanapalle Institute of Technology & Science
- **Academic Year:** 2026
- **Domain:** Computer Networks, Cryptography, Distributed Systems & Database Security

---

## SLIDE 2: PROBLEM STATEMENT
### Challenges in Modern Financial Communication Infrastructure

- **Data Confidentiality & Eavesdropping**: Plaintext banking data sent over multi-hop networks allows unauthorized interception of account numbers and monetary values.
- **Transmission Bit Errors**: Optical and physical communication channels suffer from random bit flips and transmission noise.
- **Packet Loss & Out-of-Order Delivery**: Unreliable network links drop frames or deliver packets out of sequence.
- **Data Inconsistency**: Network drops during financial transfers can lead to partial account balance updates or double-spending.
- **Need for Integrated Verification**: Isolated algorithms do not demonstrate how routing, encryption, ARQ flow control, TCP transport, and databases work together.

---

## SLIDE 3: PROJECT OBJECTIVES
### Engineering Goals Achieved in the System

1. **Simulate Banking Network Topology**: Construct a realistic 10-node enterprise network.
2. **Implement IP Addressing & CIDR Subnetting**: Subnet headquarters, branches, ATMs, terminals, and database servers.
3. **Implement Link-State Dijkstra Routing**: Calculate shortest paths across weighted graph links.
4. **Implement Distance Vector Routing**: Compute Bellman-Ford routing vectors iteratively.
5. **Enforce AES-256-GCM Cryptography**: Secure transaction confidentiality and cryptographic authenticity.
6. **Detect Transmission Errors via CRC-16**: Calculate polynomial checksums (`CRC-16-CCITT`).
7. **Manage Flow Control via Sliding Window**: Implement sender and receiver window boundaries.
8. **Enforce Reliable Delivery via Go-Back-N ARQ**: Implement cumulative ACKs and retransmissions.
9. **Real TCP Socket Transport**: Provide 4-byte length-prefixed UTF-8 socket framing.
10. **Atomic Financial Persistence**: Persist transactions in SQLite using ACID compliance.
11. **JavaFX Visual Monitoring Dashboard**: Visualize topology, path highlighting, and real-time logs.
12. **Network Packet Analysis**: Validate TCP framing and payload confidentiality via Wireshark.

---

## SLIDE 4: SYSTEM ARCHITECTURE
### End-to-End Multilayered Architecture

```text
       [ ATM / Branch Gateway ]
                  ↓
       [ IP Subnetting Layer ]
                  ↓
  [ Routing Engine (Dijkstra & DV) ]
                  ↓
  [ AES-256-GCM Encryption Layer ]
                  ↓
   [ CRC-16-CCITT Checksum Layer ]
                  ↓
 [ Sliding Window & Go-Back-N ARQ ]
                  ↓
   [ Real TCP Client/Server Socket ]
                  ↓
      [ CRC Verification Layer ]
                  ↓
   [ AES-256-GCM Decryption Layer ]
                  ↓
   [ Banking Business Logic Service ]
                  ↓
      [ SQLite ACID Database ]
```

---

## SLIDE 5: NETWORK TOPOLOGY
### 10-Node Enterprise Banking Topology

- **Head Office Core (`NODE-HQ`)**: Central core mainframe (`10.0.0.1`)
- **HQ Core Router (`ROUTER-HQ`)**: Central backbone router (`10.0.0.254`)
- **Branch 1 Router (`ROUTER-B1`)**: Primary branch router (`10.0.1.254`)
- **Branch 2 Router (`ROUTER-B2`)**: Secondary branch router (`10.0.2.254`)
- **Branch 1 Gateway (`NODE-B1`)**: Branch 1 terminal gateway (`10.0.1.1`)
- **Branch 2 Gateway (`NODE-B2`)**: Branch 2 terminal gateway (`10.0.2.1`)
- **ATM Gateway (`NODE-ATM`)**: Remote ATM terminal gateway (`10.0.10.1`)
- **Customer Service Terminal (`NODE-CS`)**: Customer support node (`10.0.20.1`)
- **Management Workstation (`NODE-MGMT`)**: Manager terminal node (`10.0.30.1`)
- **Database Server (`NODE-DB`)**: Central transaction database server (`10.0.40.1`)

*Redundant inter-router backbone paths allow routing algorithms to select optimal low-cost routes.*

---

## SLIDE 6: IP ADDRESSING & SUBNETTING
### Classless Inter-Domain Routing (CIDR) Configuration

| Subnet Domain | Network Address | Subnet Mask | Usable Host Range | Total Usable Hosts |
| :--- | :--- | :--- | :--- | :---: |
| **Head Office** | `10.0.0.0/24` | `255.255.255.0` | `10.0.0.1 – 10.0.0.254` | 254 |
| **Branch 1** | `10.0.1.0/24` | `255.255.255.0` | `10.0.1.1 – 10.0.1.254` | 254 |
| **Branch 2** | `10.0.2.0/24` | `255.255.255.0` | `10.0.2.1 – 10.0.2.254` | 254 |
| **ATM Network** | `10.0.10.0/24` | `255.255.255.0` | `10.0.10.1 – 10.0.10.254` | 254 |
| **Customer Service**| `10.0.20.0/24` | `255.255.255.0` | `10.0.20.1 – 10.0.20.254` | 254 |
| **Management** | `10.0.30.0/24` | `255.255.255.0` | `10.0.30.1 – 10.0.30.254` | 254 |
| **Database Server** | `10.0.40.0/24` | `255.255.255.0` | `10.0.40.1 – 10.0.40.254` | 254 |

---

## SLIDE 7: ROUTING ALGORITHMS
### Link-State Dijkstra vs. Distance Vector Comparison

- **Dijkstra's Algorithm**:
  - Global link-state algorithm using full graph topology knowledge.
  - Computes minimum-cost path from source to all nodes using a `PriorityQueue`.
- **Distance Vector Algorithm**:
  - Decentralized Bellman-Ford algorithm using iterative neighbor vector exchanges.
  - Converges to optimal distance tables in finite iterations.
- **Computed Path Result**:
  - **Path**: `Branch 1 Gateway` → `Branch 1 Router` → `HQ Core Router` → `Database Server`
  - **Total Cost**: `8.0`
  - **Next Hop**: `Branch 1 Router`
  - **Result**: Dijkstra and Distance Vector algorithms produce matching optimal routes (`ROUTES MATCH`).

---

## SLIDE 8: CRYPTOGRAPHIC ENCRYPTION
### AES-256-GCM Security Model

- **Algorithm**: Advanced Encryption Standard (AES) in Galois/Counter Mode (GCM).
- **Key Length**: 256-bit secret key generated via `SecureRandom`.
- **Initialization Vector (IV)**: 12-byte random IV generated per operation (prevents IV reuse).
- **Authentication Tag**: 128-bit authentication tag enforcing cryptographic integrity.
- **Security Guarantees**:
  - **Confidentiality**: Prevents unauthorized reading of payload fields.
  - **Authenticity & Integrity**: Tampered ciphertext causes GCM tag mismatch, rejecting malicious payload before execution.

---

## SLIDE 9: CRC ERROR DETECTION
### CRC-16-CCITT Checksum Mechanics

- **Polynomial**: `0x1021` ($x^{16} + x^{12} + x^5 + 1$)
- **Initial Value**: `0xFFFF`
- **Standard Known Test Vector**: `"123456789"` → `0x29B1`
- **Operational Role**:
  - Detects physical and transport-layer transmission bit flips.
  - Attached to transmitted frames (`CRC=0x....`).
- **Key Engineering Distinction**:
  - **CRC-16**: Fast error detection for transmission noise.
  - **AES-GCM**: Cryptographic protection against adversarial tampering.

---

## SLIDE 10: RELIABLE TRANSMISSION PROTOCOLS
### Sliding Window & Go-Back-N ARQ Flow Control

- **Sliding Window Protocol**:
  - Limits unacknowledged outstanding frames to configured window size (e.g., `2`).
  - Advances window base dynamically as ACKs are received.
- **Go-Back-N ARQ Protocol**:
  - **In-Order Enforcement**: Receiver accepts frame only if sequence matches `expectedSeqNum`.
  - **Cumulative ACKs**: Receiver sends cumulative acknowledgements for received frames.
  - **Retransmission**: Timeout triggers Go-Back-N window retransmission starting from missing frame base.

---

## SLIDE 11: REAL TCP SOCKET TRANSPORT
### Client-Server Socket Communication

- **Implementation**: Java `Socket` and `ServerSocket` over localhost (`127.0.0.1`).
- **Framing Protocol**: 4-byte big-endian integer length prefix + UTF-8 payload.
- **Payload Capacity**: Max message size bound to 1 MB (`1,048,576 bytes`).
- **Socket Lifecycle**:
  - Server auto-binds to available port (`TCP_CAPTURE_TARGET=127.0.0.1:<PORT>`).
  - Client connects, sends length prefix and payload, reads response, closes socket cleanly.

---

## SLIDE 12: BANKING SERVICE & DATABASE PERSISTENCE
### ACID Transaction Processing via SQLite

- **Database Engine**: SQLite JDBC (`data/banking.db` or in-memory).
- **Atomic Money Transfer**:
  - **Source Account (`ACC10001`)**: Initial = ₹50,000.00 → Final = ₹45,000.00
  - **Destination Account (`ACC10002`)**: Initial = ₹25,000.00 → Final = ₹30,000.00
  - **Transfer Amount**: ₹5,000.00
- **Transaction Safety**:
  - Executed inside `Connection.setAutoCommit(false)`.
  - Insufficient funds, invalid amounts, or database errors trigger `rollback()`, leaving balances unchanged.

---

## SLIDE 13: END-TO-END SECURE PIPELINE
### 12-Stage Integrated Transaction Pipeline (`SecureBankingPipeline`)

```text
[1] Transaction Creation → [2] Route Selection → [3] AES-256-GCM Encryption
                                                       ↓
[6] TCP Socket Transport ← [5] Reliable ARQ ← [4] CRC Checksum Generation
         ↓
[7] Server Receipt → [8] CRC Verification → [9] AES Decryption
                                                 ↓
[12] Pipeline Response ← [11] Database Audit ← [10] Atomic Banking Transfer
```

---

## SLIDE 14: JAVAFX DESKTOP DASHBOARD
### Real-Time Monitoring Interface (`DashboardApplication`)

- **Interactive Topology Canvas**: Renders 10-node banking network with node types and IP addresses.
- **Live Shortest Path Highlighting**: Visualizes Dijkstra routes in glowing emerald/cyan.
- **Pipeline Stage Checklist**: 9-stage verification checklist updating live after transaction processing.
- **Account Summary Card**: Displays live account balances fetched from SQLite.
- **Real-Time Activity Feed**: Displays timestamped log events.

---

## SLIDE 15: WIRESHARK PACKET ANALYSIS
### Network Layer Traffic Verification

- **Capture Interface**: Npcap Loopback Adapter / Loopback (`127.0.0.1`).
- **Display Filter**: `tcp.port == <PORT>`
- **Observed Protocol Events**:
  - **TCP Handshake**: `SYN` → `SYN-ACK` → `ACK`.
  - **Application Payload**: 4-byte length prefix followed by Base64 ciphertext.
  - **Confidentiality Proof**: Account numbers and amounts do **NOT** appear in plaintext on the wire.
  - **Teardown**: `FIN-ACK` 4-way release.

---

## SLIDE 16: AUTOMATED TESTING & METRICS
### 100% Pass Rate Across 156 JUnit Tests

- **Total Test Cases Executed**: **156 / 156 PASS**
- **Failures / Errors / Skipped**: **0 / 0 / 0**
- **Maven Build Result**: **`BUILD SUCCESS`**
- **Test Coverage Areas**:
  - IP Addressing & Subnetting (35 tests)
  - Network Topology & Routing (48 tests)
  - Security & CRC Checksums (24 tests)
  - Sliding Window & Go-Back-N ARQ (26 tests)
  - Real TCP Socket Communication (15 tests)
  - Integrated Banking Pipeline (15 tests)
  - SQLite Persistence & Banking Logic (17 tests)

---

## SLIDE 17: SYSTEM ADVANTAGES & LIMITATIONS

### Key Advantages
1. **Complete Multilayer Integration**: Combines networking, cryptography, ARQ, sockets, and databases.
2. **Authenticated Security**: AES-256-GCM prevents eavesdropping and tampering.
3. **Atomic Safety**: ACID transfers guarantee zero partial balance corruption.
4. **Visual Verification**: JavaFX dashboard provides immediate feedback.

### System Limitations
1. **Localhost Socket Scope**: Operates over `127.0.0.1` for local demonstration.
2. **Simulated Topology Scale**: Topology fixed at 10 enterprise nodes.
3. **Embedded Database**: Uses SQLite rather than distributed cloud databases.

---

## SLIDE 18: FUTURE SCOPE & CONCLUSION

### Future Scope
- **TLS 1.3 Transport Security**: Upgrade raw TCP sockets to TLS-encrypted channels.
- **Multi-Branch Distributed Deployment**: Deploy server nodes across remote physical hosts.
- **Intrusion Detection System (IDS)**: Incorporate automated packet payload anomaly detection.

### Conclusion
The **Secure Banking Network** successfully demonstrates that high-value financial transactions can be reliably routed, encrypted, framed, transmitted, verified, and persisted across complex enterprise network topologies.
